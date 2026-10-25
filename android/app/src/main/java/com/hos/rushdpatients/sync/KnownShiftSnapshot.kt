package com.hos.rushdpatients.sync

import com.hos.rushdpatients.data.model.Patient
import com.hos.rushdpatients.data.model.Shift
import com.hos.rushdpatients.data.model.withoutTrackingMetadata
import com.hos.rushdpatients.domain.sort.SortSpecCodec

enum class KnownShiftState { UNKNOWN, MATCHED, CHANGED }

data class ShiftChanges(
    val state: KnownShiftState,
    val added: List<Patient>,
    val changed: List<Patient>,
    val removed: List<Patient>,
    val settingsChanged: Boolean
) {
    val count: Int get() = added.size + changed.size + removed.size + if (settingsChanged) 1 else 0
    val pending: Boolean get() = count > 0
}

/** One clinical comparison for fetch guards, publication status, counts and briefings. */
object KnownShiftSnapshot {
    fun patientContent(patient: Patient): Patient = patient.withoutTrackingMetadata().copy(revision = 0, sortOrder = 0,
        tasks = patient.tasks.map { com.hos.rushdpatients.data.model.PatientTask(it.id, it.description, done = it.done) })

    fun orderedContent(patients: List<Patient>): List<Patient> = patients.filterNot { it.isDeleted }
        .sortedWith(compareBy<Patient> { it.sortOrder }.thenBy { it.id }).map(::patientContent)

    fun compare(shift: Shift?, patients: List<Patient>, csv: String?): KnownShiftState = changes(shift, patients, csv).state

    fun changes(shift: Shift?, patients: List<Patient>, csv: String?): ShiftChanges {
        return againstBaseline(shift, patients, csv?.let { runCatching { CsvCodec.decode(it) }.getOrNull() })
    }

    fun againstBaseline(shift: Shift?, patients: List<Patient>, parsed: ParsedShift?): ShiftChanges {
        val base = parsed?.takeIf { it.shiftId == shift?.id && it.shiftDate == shift?.date }
        val result = changes(patients, base?.patients.orEmpty(),
            shift != null && if (base == null) shift.doctorIds.isNotEmpty() || SortSpecCodec.decode(shift.sortSpecJson).levels.isNotEmpty()
            else shift.doctorIds != base.doctorIds ||
                SortSpecCodec.decode(shift.sortSpecJson) != SortSpecCodec.decode(base.sortSpecJson))
        return result.copy(state = when {
            base == null -> KnownShiftState.UNKNOWN
            result.pending -> KnownShiftState.CHANGED
            else -> KnownShiftState.MATCHED
        })
    }

    fun changes(current: List<Patient>, baseline: List<Patient>, settingsChanged: Boolean = false): ShiftChanges {
        val after = current.filterNot { it.isDeleted }.sortedWith(compareBy<Patient> { it.sortOrder }.thenBy { it.id })
        val before = baseline.filterNot { it.isDeleted }.sortedWith(compareBy<Patient> { it.sortOrder }.thenBy { it.id })
        val previous = before.associateBy { it.id }
        val next = after.associateBy { it.id }
        val previousOrder = before.mapIndexed { index, row -> row.id to index }.toMap()
        val nextOrder = after.mapIndexed { index, row -> row.id to index }.toMap()
        val changed = after.filter { row -> previous[row.id]?.let { old ->
            patientContent(old) != patientContent(row) || previousOrder[row.id] != nextOrder[row.id]
        } == true }
        val added = after.filter { it.id !in previous }
        val removed = before.filter { it.id !in next }
        val pending = added.isNotEmpty() || changed.isNotEmpty() || removed.isNotEmpty() || settingsChanged
        return ShiftChanges(if (pending) KnownShiftState.CHANGED else KnownShiftState.MATCHED, added, changed, removed, settingsChanged)
    }
}
