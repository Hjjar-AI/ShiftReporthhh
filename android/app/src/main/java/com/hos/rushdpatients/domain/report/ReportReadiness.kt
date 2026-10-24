package com.hos.rushdpatients.domain.report

import com.hos.rushdpatients.data.model.Patient

enum class ReadinessKind { INCOMPLETE }
data class ReadinessIssue(val kind: ReadinessKind, val patientIds: Set<String>)

/** One actionable note; clinical edit timestamps are not a readiness requirement. */
object ReportReadiness {
    @Suppress("UNUSED_PARAMETER")
    fun issues(patients: List<Patient>, shiftDate: java.time.LocalDate = com.hos.rushdpatients.util.ShiftDate.current()): List<ReadinessIssue> {
        val ids = patients.filter {
            it.responsibleSpecialistId.isNullOrBlank() || it.responsibleResidentId.isNullOrBlank() ||
                it.initialDiagnosis.isBlank() || it.treatmentPlan.isBlank() || it.followUp.isBlank()
        }.mapTo(linkedSetOf()) { it.id }
        return if (ids.isEmpty()) emptyList() else listOf(ReadinessIssue(ReadinessKind.INCOMPLETE, ids))
    }
}
