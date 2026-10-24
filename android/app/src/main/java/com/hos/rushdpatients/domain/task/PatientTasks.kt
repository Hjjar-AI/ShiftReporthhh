package com.hos.rushdpatients.domain.task

import com.hos.rushdpatients.data.model.Patient
import com.hos.rushdpatients.data.model.PatientTask

/** Legacy counter slots remain zero for source compatibility; tasks are a plain checklist. */
data class TaskCounts(val pending: Int, val overdue: Int, val unassigned: Int)

data class WardTaskEntry(val patient: Patient, val task: PatientTask)

object PatientTasks {
    // Legacy public arguments remain source-compatible; checklist behavior ignores time/assignment/priority.
    @Suppress("UNUSED_PARAMETER")
    fun counts(tasks: List<PatientTask>, nowEpochMillis: Long): TaskCounts {
        val pending = tasks.filterNot { it.done }
        return TaskCounts(pending.size, 0, 0)
    }

    fun carryForward(tasks: List<PatientTask>, clearOwners: Boolean): List<PatientTask> =
        tasks.filterNot { it.done }.map { task ->
            if (clearOwners) task.copy(ownerDoctorId = null, ownerName = null) else task
        }

    @Suppress("UNUSED_PARAMETER")
    fun overdue(task: PatientTask, nowEpochMillis: Long): Boolean =
        false // Imported legacy deadlines do not affect checklist behavior.

    /** Display only: never reorder the stored task list or change completion/deadline data. */
    @Suppress("UNUSED_PARAMETER")
    fun orderedForDisplay(tasks: List<PatientTask>, nowEpochMillis: Long): List<PatientTask> =
        tasks.sortedWith(displayComparator())

    @Suppress("UNUSED_PARAMETER")
    fun wardPending(patients: List<Patient>, supervisorId: String?, residentId: String?, nowEpochMillis: Long): List<WardTaskEntry> {
        return patients.filter { (supervisorId == null || it.responsibleSpecialistId == supervisorId) &&
            (residentId == null || it.responsibleResidentId == residentId) }
            .flatMap { patient -> patient.tasks.filterNot { it.done }.map { WardTaskEntry(patient, it) } }
    }

    private fun displayComparator(): Comparator<PatientTask> = compareBy<PatientTask> { it.done }

    @Suppress("UNUSED_PARAMETER")
    fun statusLabel(task: PatientTask, nowEpochMillis: Long): String = when {
        task.done -> "مكتملة"
        else -> "معلقة"
    }

    @Suppress("UNUSED_PARAMETER")
    fun requireValid(tasks: List<PatientTask>, requireCompletion: Boolean = true) {
        require(tasks.map { it.id }.distinct().size == tasks.size) { "توجد معرّفات مهام مكررة" }
        tasks.forEach { task ->
            require(task.id.isNotBlank() && task.description.isNotBlank()) { "وصف المهمة مطلوب" }

        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun summary(
        tasks: List<PatientTask>,
        names: Map<String, String> = emptyMap(),
        includeUnassigned: Boolean = true,
        includeRoutinePriority: Boolean = true,
        nowEpochMillis: Long = System.currentTimeMillis()
    ): String = orderedForDisplay(tasks, nowEpochMillis).joinToString("\n") { task ->
        (if (task.done) "✓ " else "☐ ") + statusLabel(task, nowEpochMillis) + ": " + task.description +
            metadataSuffix(task)
    }

    /** Legacy metadata is accepted on import but never displayed. */
    @Suppress("UNUSED_PARAMETER")
    fun metadata(
        task: PatientTask,
        names: Map<String, String> = emptyMap(),
        includeUnassigned: Boolean = true,
        includeRoutinePriority: Boolean = true
    ): String = metadataSuffix(task).removePrefix(" · ")

    @Suppress("UNUSED_PARAMETER")
    private fun metadataSuffix(task: PatientTask): String = ""
}
