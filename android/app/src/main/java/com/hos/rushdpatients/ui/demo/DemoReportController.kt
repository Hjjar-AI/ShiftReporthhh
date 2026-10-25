package com.hos.rushdpatients.ui.demo

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.hos.rushdpatients.R
import com.hos.rushdpatients.domain.report.*
import com.hos.rushdpatients.network.telegram.Markdown
import com.hos.rushdpatients.pdf.*
import com.hos.rushdpatients.sync.KnownShiftSnapshot
import com.hos.rushdpatients.ui.components.UiStrings
import com.hos.rushdpatients.ui.components.readinessWarnings
import com.hos.rushdpatients.ui.report.*
import com.hos.rushdpatients.util.ShiftDate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.UUID

/** Fake publication/delivery; the report UI, readiness rules and PDF renderer are production code. */
internal class DemoReportController(
    private val context: Context,
    private val uiText: UiStrings,
    private val scope: CoroutineScope,
    private val repository: InMemoryDemoWardRepository,
    private val exporter: PdfReportExporter,
    private val saver: MediaStoreSaver,
    private val textBuilder: TextReportBuilder,
    private val selectDoctors: (List<String>) -> Unit
) : ReportController {
    private val _state = MutableStateFlow(ReportUiState())
    override val state = _state.asStateFlow()
    override val simulation = true
    private var reviewed: DemoWardState? = null
    private val files = mutableMapOf<String, File>()
    private var fileJob: Job? = null
    private var generation = 0L
    val busy get() = _state.value.isBusy

    override fun load(reviewNotice: String?) {
        if (_state.value.isBusy && !_state.value.loading) return
        clearReviews()
        val snapshot = repository.state.value
        reviewed = snapshot
        val shift = snapshot.shift
        val changes = repository.changes(snapshot)
        val briefing = buildList {
            changes.added.forEach { add(uiText(R.string.report_patient_added, it.displayName)) }
            changes.changed.forEach { add(uiText(R.string.report_patient_changed, it.displayName)) }
            changes.removed.forEach { add(uiText(R.string.report_patient_removed, it.displayName)) }
            if (changes.settingsChanged) add(uiText(R.string.report_shift_settings_changed))
        }
        val summary = snapshot.summary.copy(shiftDate = ShiftDate.current()).takeIf { snapshot.shiftDoctors.isNotEmpty() }
        val text = summary?.let {
            textBuilder.build(shift.copy(date = it.shiftDate), snapshot.ordered, snapshot.shiftDoctors, it, snapshot.names, snapshot.names)
                .joinToString("\n\n", transform = Markdown::toPlainText)
        }.orEmpty()
        _state.update { it.copy(loading = false, shift = shift, patients = snapshot.ordered,
            doctors = snapshot.shiftDoctors, availableDoctors = snapshot.doctors,
            summary = summary, previewMarkdown = uiText(R.string.demo_document_label) + "\n\n" + text,
            supervisorTargets = snapshot.doctors.filter { doctor -> doctor.clinicalRole.canBeSupervisor() }
                .mapNotNull { doctor -> snapshot.patients.count { it.responsibleSpecialistId == doctor.id }
                    .takeIf { it > 0 }?.let { SupervisorReportTarget(doctor.id, doctor.fullName, -1L, it) } },
            readinessWarnings = readinessWarnings(uiText, snapshot.patients),
            readinessIssues = ReportReadiness.issues(snapshot.patients), changeBriefing = briefing,
            publicationOutcome = null, deliveryOutcome = null, error = null, reviewNotice = reviewNotice,
            deliveryRecords = repository.receipts.value, retryAction = null) }
    }

    override fun setShiftDoctors(ids: List<String>, onSuccess: () -> Unit) {
        if (busy) return
        selectDoctors(ids)
        onSuccess()
        load()
    }
    override fun setReportAsPdf(value: Boolean) { if (!busy) _state.update { it.copy(reportAsPdf = value) } }
    override fun setPdfAppearance(style: PdfStyle, requested: PdfExportOptions) {
        if (busy) return
        clearReviews()
        _state.update { it.copy(pdfStyle = style, pdfOptions = requested.copy(orientation = PdfOrientation.PORTRAIT,
            darkMode = false, ruleWeight = PdfRuleWeight.MEDIUM, ruleContrast = PdfRuleContrast.MEDIUM)) }
    }
    override fun fetchDoctorsForReport() { if (!busy) load(uiText(R.string.demo_fetch_complete)) }
    override fun dismissSnackbar() { _state.update { it.copy(snackbar = null) } }
    override fun previewPdf() = preview(null)
    override fun previewSupervisorPdf(supervisorId: String) = preview(supervisorId)

    private fun clearReviews() {
        generation++
        val obsolete = files.values.toList()
        files.clear()
        ClinicalPdfCache.delete(obsolete)
        _state.update { it.copy(pdfPreviewUri = null, pdfShareUri = null, pdfReviewUris = emptyMap(),
            openedPdfUris = emptySet(), pdfReviewGeneration = generation) }
    }
    fun reset() {
        fileJob?.cancel()
        clearReviews()
        reviewed = null
        _state.value = ReportUiState()
    }
    private fun matches(snapshot: DemoWardState): Boolean {
        val current = repository.state.value
        return snapshot.shiftId == current.shiftId && snapshot.actorId == current.actorId &&
            !KnownShiftSnapshot.changes(current.ordered, snapshot.ordered,
                current.shiftDoctorIds != snapshot.shiftDoctorIds || current.sort != snapshot.sort).pending &&
            current.doctors == snapshot.doctors
    }
    private fun requireSnapshot(): DemoWardState {
        val snapshot = checkNotNull(reviewed)
        check(matches(snapshot)) { uiText(R.string.demo_review_changed) }
        return snapshot
    }
    private fun key(supervisor: String?) = supervisor?.let { "supervisor:$it" } ?: "general"
    private fun preview(supervisor: String?) {
        if (busy) return
        val snapshot = reviewed ?: return
        val current = _state.value
        val token = generation
        val route = key(supervisor)
        _state.update { it.copy(previewingPdf = true, error = null) }
        fileJob = scope.launch {
            var temporary: File? = null
            try {
                requireSnapshot()
                val file = withContext(Dispatchers.IO) {
                    val directory = File(context.cacheDir, "pdf_previews").apply { mkdirs() }
                    val output = File.createTempFile("DEMO_", ".pdf", directory)
                    temporary = output
                    val patients = snapshot.ordered.filter { supervisor == null || it.responsibleSpecialistId == supervisor }
                    val doctors = if (supervisor == null) snapshot.shiftDoctors else snapshot.doctors.filter { it.id == supervisor }
                    require(patients.isNotEmpty() && doctors.isNotEmpty()) { uiText(R.string.ui_demo_ward_view_model_006) }
                    val summary = requireNotNull(current.summary).copy(patientCount = patients.size,
                        psychoCount = patients.count { it.diagnosisType == com.hos.rushdpatients.data.model.DiagnosisType.PSYCHIATRIC ||
                            it.diagnosisType == com.hos.rushdpatients.data.model.DiagnosisType.DUAL },
                        escortCount = patients.count { it.hasCompanion }, doctors = doctors)
                    exporter.export(patients, doctors, summary, snapshot.names, snapshot.names,
                        current.pdfOptions.copy(hospitalName = uiText(R.string.ui_demo_navigation_001)),
                        elegant = current.pdfStyle == PdfStyle.CARDS, outputFile = output,
                        documentLabel = uiText(R.string.demo_document_label))
                    check(output.length() > 0)
                    output
                }
                if (token != generation) return@launch
                requireSnapshot()
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                files.put(route, file)?.let { previous -> ClinicalPdfCache.delete(listOf(previous)) }
                temporary = null
                _state.update { it.copy(pdfPreviewUri = uri, pdfReviewUris = it.pdfReviewUris + (route to uri.toString())) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.update { it.copy(error = e.message) } }
            finally {
                withContext(NonCancellable + Dispatchers.IO) { temporary?.delete() }
                _state.update { it.copy(previewingPdf = false) }
            }
        }
    }
    override fun onPdfPreviewOpened(uri: Uri) {
        _state.update { if (it.pdfPreviewUri == uri && uri.toString() in it.pdfReviewUris.values)
            it.copy(pdfPreviewUri = null, openedPdfUris = it.openedPdfUris + uri.toString()) else it }
    }
    override fun onPdfViewerUnavailable() {
        _state.update { it.copy(pdfPreviewUri = null, error = uiText(R.string.ui_report_local_actions_002)) }
    }
    private fun checkedFile(route: String): File {
        requireSnapshot()
        val current = _state.value
        check(current.pdfReviewUris[route] in current.openedPdfUris) { uiText(R.string.ui_report_pdf_workspace_003) }
        return checkNotNull(files[route])
    }
    override fun sharePdf() {
        if (busy) return
        try {
            val file = checkedFile("general")
            _state.update { it.copy(pdfShareUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)) }
        } catch (e: Exception) { _state.update { it.copy(error = e.message) } }
    }
    override fun consumePdfShare() { _state.update { it.copy(pdfShareUri = null) } }
    override fun exportLocally() = saveReviewed(listOf("general"))
    override fun exportSupervisorPdfs() = saveReviewed(_state.value.supervisorTargets.map { key(it.doctorId) })
    private fun saveReviewed(routes: List<String>) {
        if (busy) return
        _state.update { it.copy(exportingLocalPdf = true, error = null) }
        fileJob = scope.launch {
            try {
                val copies = routes.map { it to checkedFile(it) }
                check(copies.isNotEmpty())
                for ((route, file) in copies) {
                    checkNotNull(saver.savePdf(context, file,
                        "ShiftReport_DEMO_${_state.value.summary?.shiftDate}_${route.replace(':', '_')}.pdf", verifyContents = true)) {
                        uiText(R.string.report_pdf_save_failed)
                    }
                }
                _state.update { it.copy(snackbar = uiText(R.string.ui_demo_ward_view_model_011)) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.update { it.copy(error = e.message) } }
            finally { _state.update { it.copy(exportingLocalPdf = false) } }
        }
    }
    // Assignment rendering currently requires production admin identity; never resolve that dependency in demo.
    override fun exportAssignments() { _state.update { it.copy(snackbar = uiText(R.string.ui_demo_local_only)) } }
    override fun sendSelectedReports(includeGeneral: Boolean, supervisorIds: Set<String>) {
        if (busy) return
        try {
            val snapshot = requireSnapshot()
            val current = _state.value
            val targets = current.supervisorTargets.filter { it.doctorId in supervisorIds }
            check(targets.size == supervisorIds.size && (includeGeneral || targets.isNotEmpty()))
            if (includeGeneral && current.reportAsPdf) checkedFile("general")
            targets.forEach { checkedFile(key(it.doctorId)) }
            val destinations = buildList {
                if (includeGeneral) add("general" to uiText(R.string.ui_report_preview_screen_097))
                targets.forEach { add(key(it.doctorId) to it.doctorName) }
            }
            val receipts = destinations.map { (route, label) -> ReportDeliveryRecord(
                UUID.randomUUID().toString(), snapshot.shiftId, route, "demo-${snapshot.revision}",
                "DEMO · $label", if (route == "general" && !current.reportAsPdf) "TEXT" else "PDF",
                snapshot.actorId, System.currentTimeMillis(), DeliveryStatus.SENT) }
            repository.receipts.update { it + receipts }
            repository.publish(snapshot)
            _state.update { it.copy(publicationOutcome = ReportStepOutcome.CONFIRMED,
                deliveryOutcome = ReportStepOutcome.CONFIRMED, deliveryRecords = repository.receipts.value,
                snackbar = uiText(R.string.demo_send_complete)) }
        } catch (e: Exception) { _state.update { it.copy(error = e.message) } }
    }
    override fun resolveDelivery(record: ReportDeliveryRecord, resolution: DeliveryResolution) {
        repository.receipts.update { rows -> rows.map { if (it.id != record.id) it else it.copy(status = when (resolution) {
            DeliveryResolution.RECEIVED -> DeliveryStatus.SENT
            DeliveryResolution.NOT_RECEIVED -> DeliveryStatus.NOT_SENT
            DeliveryResolution.ALLOW_COPY -> DeliveryStatus.UNKNOWN_COPY_ALLOWED
        }) } }
        _state.update { it.copy(deliveryRecords = repository.receipts.value) }
    }
    override fun retryLastAction(onReviewSend: () -> Unit) { onReviewSend() }
}
