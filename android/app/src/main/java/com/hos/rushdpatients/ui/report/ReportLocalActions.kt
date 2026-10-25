package com.hos.rushdpatients.ui.report

import com.hos.rushdpatients.ui.components.UiStrings

import com.hos.rushdpatients.R

import android.content.Context
import androidx.core.content.FileProvider
import com.hos.rushdpatients.config.AppConstants
import com.hos.rushdpatients.data.repository.SettingsRepository
import com.hos.rushdpatients.domain.auth.AdminAuthorizer
import com.hos.rushdpatients.domain.report.ReportBuilder
import com.hos.rushdpatients.domain.report.ReportReview
import com.hos.rushdpatients.pdf.MediaStoreSaver
import com.hos.rushdpatients.pdf.PdfAssignmentExporter
import com.hos.rushdpatients.pdf.PdfStyle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Local file actions share the ViewModel state/scope and its single reviewed-artifact store. */
internal class ReportLocalActions(
    private val uiText: UiStrings,
    private val _state: MutableStateFlow<ReportUiState>,
    private val viewModelScope: kotlinx.coroutines.CoroutineScope,
    private val shiftId: String, private val context: Context,
    private val reportBuilder: ReportBuilder, private val settingsRepository: SettingsRepository,
    private val assignmentPdfExporter: PdfAssignmentExporter, private val mediaStoreSaver: MediaStoreSaver,
    private val adminAuthorizer: AdminAuthorizer, private val pdfWorkspace: ReportPdfWorkspace,
    private val reviewedSnapshot: () -> com.hos.rushdpatients.domain.report.BuiltReport?
) {

    fun previewPdf() = previewPdfFor(null)

    fun previewSupervisorPdf(supervisorId: String) = previewPdfFor(supervisorId)

    private fun previewPdfFor(supervisorId: String?) {
        val current = _state.value
        if (current.isBusy || current.shift == null) return
        val reviewGeneration = current.pdfReviewGeneration
        _state.update { it.copy(previewingPdf = true, error = null, doctorRegistryAction = null, retryAction = null,
            openedPdfUris = it.openedPdfUris - listOfNotNull(it.pdfReviewUris[pdfWorkspace.pdfKey(supervisorId)]).toSet(),
            pdfReviewUris = it.pdfReviewUris - pdfWorkspace.pdfKey(supervisorId)) }
        viewModelScope.launch {
            var temporary: File? = null
            try {
                val built = requireNotNull(reviewedSnapshot()) { "افتح معاينة التقرير أولاً" }
                val prepared = withContext(Dispatchers.IO) {
                    val selected = if (supervisorId == null) built else pdfWorkspace.supervisorPdfReport(built, supervisorId)
                    val options = pdfWorkspace.loadPdfOptions()
                    val style = PdfStyle.fromSetting(settingsRepository.get(AppConstants.SETTING_PDF_STYLE))
                    val dir = File(context.cacheDir, "pdf_previews").apply { mkdirs() }
                    val file = File.createTempFile("reviewed_report_", ".pdf", dir)
                    temporary = file
                    try {
                        pdfWorkspace.renderPdf(selected.patients, selected.doctors, selected.summary,
                            selected.doctorNames, selected.doctorNames, options, file, style)
                        ReviewedPdf(file, pdfWorkspace.pdfDigest(file), ReportReview.fingerprint(built), options, style, built)
                    } catch (e: Exception) { file.delete(); throw e }
                }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", prepared.file)
                check(_state.value.pdfReviewGeneration == reviewGeneration) {
                    uiText(R.string.ui_report_local_actions_001)
                }
                pdfWorkspace.replaceReview(pdfWorkspace.pdfKey(supervisorId), prepared)
                temporary = null
                _state.update { it.copy(previewingPdf = false, pdfPreviewUri = uri,
                    pdfReviewUris = it.pdfReviewUris + (pdfWorkspace.pdfKey(supervisorId) to uri.toString())) }
            } catch (e: CancellationException) {
                pdfWorkspace.removeReview(pdfWorkspace.pdfKey(supervisorId))
                _state.update { it.copy(previewingPdf = false) }; throw e
            } catch (e: Exception) {
                pdfWorkspace.removeReview(pdfWorkspace.pdfKey(supervisorId))
                _state.update { it.copy(previewingPdf = false, error = e.message ?: uiText(R.string.ui_report_local_actions_002),
                    retryAction = if (supervisorId == null) ReportRetryAction.PREVIEW else null) }
            } finally {
                temporary?.let { com.hos.rushdpatients.pdf.ClinicalPdfCache.delete(listOf(it)) }
            }
        }
    }

    // Retain successful viewer launches with the artifact owner across privacy/recreation.
    fun onPdfPreviewOpened(uri: android.net.Uri) = _state.update { current ->
        if (current.pdfPreviewUri == uri && uri.toString() in current.pdfReviewUris.values) {
            current.copy(pdfPreviewUri = null, openedPdfUris = current.openedPdfUris + uri.toString())
        } else current
    }

    fun sharePdf() {
        val current = _state.value
        if (current.isBusy || current.shift == null) return
        _state.update { it.copy(sharingPdf = true, error = null, doctorRegistryAction = null, retryAction = null) }
        viewModelScope.launch {
            try {
                val built = reviewedSnapshot() ?: throw PdfReviewRequiredException(uiText)
                val uri = withContext(Dispatchers.IO) {
                    val reviewedFile = pdfWorkspace.checkedPdf(built)
                    val shareDir = File(context.cacheDir, "shared_reports").apply { mkdirs() }
                    shareDir.listFiles()?.forEach { it.delete() }
                    val file = File(shareDir, pdfWorkspace.reportFileName(built.summary.shiftDate))
                    try {
                        reviewedFile.copyTo(file, overwrite = true)
                        check(pdfWorkspace.pdfDigest(file) == pdfWorkspace.pdfDigest(reviewedFile)) {
                            uiText(R.string.ui_report_local_actions_004)
                        }
                        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    } catch (e: Exception) {
                        file.delete()
                        throw e
                    }
                }
                _state.update {
                    it.copy(
                        sharingPdf = false,
                        pdfShareUri = uri,
                        lastOperation = uiText(R.string.ui_report_local_actions_003),
                        retryAction = null
                    )
                }
            } catch (e: CancellationException) {
                _state.update { it.copy(sharingPdf = false) }
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        sharingPdf = false,
                        error = e.message ?: uiText(R.string.ui_report_local_actions_004),
                        retryAction = ReportRetryAction.SHARE
                    )
                }
            }
        }
    }

    fun consumePdfShare() = _state.update { it.copy(pdfShareUri = null) }

    fun exportAssignments() {
        val current = _state.value
        if (current.isBusy || current.shift == null) return
        _state.update { it.copy(exportingLocalPdf = true, error = null, doctorRegistryAction = null, retryAction = null) }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val actorId = adminAuthorizer.requireAdmin().id
                    val built = reportBuilder.buildAssignments(shiftId)
                    val tmp = File.createTempFile("rushd_assignments_", ".pdf", context.cacheDir)
                    try {
                        assignmentPdfExporter.export(built.patients, built.doctorNames, built.registryDoctors, built.summary,
                            pdfWorkspace.loadPdfOptions(), tmp)
                        require(adminAuthorizer.requireAdmin().id == actorId) { uiText(R.string.ui_report_local_actions_005) }
                        checkNotNull(mediaStoreSaver.savePdf(context, tmp, pdfWorkspace.reportFileName(built.summary.shiftDate, "Names"),
                            verifyContents = true)) {
                            uiText(R.string.ui_report_local_actions_006)
                        }
                    } finally {
                        tmp.delete()
                    }
                }
                _state.update { it.copy(exportingLocalPdf = false, snackbar = uiText(R.string.ui_report_local_actions_007),
                    lastOperation = uiText(R.string.ui_report_local_actions_008), retryAction = null) }
            } catch (e: CancellationException) {
                _state.update { it.copy(exportingLocalPdf = false) }
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(exportingLocalPdf = false, error = e.message ?: uiText(R.string.ui_report_local_actions_006),
                    retryAction = ReportRetryAction.ASSIGNMENTS) }
            }
        }
    }

    fun exportLocally() = exportLocally(separate = false)

    fun exportSupervisorPdfs() = exportLocally(separate = true)

    private fun exportLocally(separate: Boolean) {
        val current = _state.value
        if (current.isBusy || current.shift == null) return
        _state.update { it.copy(exportingLocalPdf = true, error = null, doctorRegistryAction = null, retryAction = null) }
        viewModelScope.launch {
            try {
                val built = reviewedSnapshot() ?: throw PdfReviewRequiredException(uiText)
                val count = if (separate) {
                    withContext(Dispatchers.IO) {
                        pdfWorkspace.saveSupervisorPdfs(built, pdfWorkspace.loadPdfOptions())
                    }
                } else {
                    withContext(Dispatchers.IO) {
                        val file = pdfWorkspace.checkedPdf(built)
                        checkNotNull(mediaStoreSaver.savePdf(context, file,
                            pdfWorkspace.reportFileName(built.summary.shiftDate), verifyContents = true)) {
                            uiText(R.string.ui_report_local_actions_011)
                        }
                    }
                    1
                }
                _state.update {
                    it.copy(
                        exportingLocalPdf = false,
                        snackbar = uiText(R.string.ui_report_local_actions_009, count),
                        lastOperation = uiText(R.string.ui_report_local_actions_010),
                        retryAction = null
                    )
                }
            } catch (e: CancellationException) {
                _state.update { it.copy(exportingLocalPdf = false) }
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        exportingLocalPdf = false,
                        error = e.message ?: uiText(R.string.ui_report_local_actions_011),
                        retryAction = if (separate) ReportRetryAction.SAVE_SUPERVISORS else ReportRetryAction.SAVE
                    )
                }
            }
        }
    }

    fun onPdfViewerUnavailable() {
        pdfWorkspace.clearReviews()
        _state.update { it.copy(pdfPreviewUri = null, pdfReviewUris = emptyMap(), openedPdfUris = emptySet(),
            pdfReviewGeneration = it.pdfReviewGeneration + 1,
            error = uiText(R.string.ui_report_local_actions_012)) }
    }
}
