package com.hos.rushdpatients.sync

import com.hos.rushdpatients.config.AppConstants
import com.hos.rushdpatients.data.repository.PatientRepository
import com.hos.rushdpatients.data.repository.SettingsRepository
import com.hos.rushdpatients.data.repository.ShiftRepository
import com.hos.rushdpatients.util.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Derived local publication state. Legacy stored pending flags are not authority. */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class PatientPublicationStatus @Inject constructor(
    private val shifts: ShiftRepository,
    private val patients: PatientRepository,
    private val settings: SettingsRepository,
    private val dispatchers: DispatcherProvider
) {
    suspend fun hasPending(): Boolean = withContext(dispatchers.io) {
        shifts.getRecent(12).any { shift ->
            KnownShiftSnapshot.changes(shift, patients.getForShift(shift.id),
                settings.get(AppConstants.SETTING_KNOWN_SHIFT_SNAPSHOT_PREFIX + shift.id)).pending
        }
    }

    fun observeShift(id: String): Flow<ShiftChanges> {
        // Decode only when the stored baseline changes, independent of patient emissions.
        val baseline = settings.observe(AppConstants.SETTING_KNOWN_SHIFT_SNAPSHOT_PREFIX + id)
            .distinctUntilChanged().map { csv -> csv?.let { runCatching { CsvCodec.decode(it) }.getOrNull() } }
        return combine(shifts.observeById(id), patients.observeForShift(id), baseline) { shift, rows, base ->
            KnownShiftSnapshot.againstBaseline(shift, rows, base)
        }.distinctUntilChanged().flowOn(dispatchers.io)
    }

    fun observePending(): Flow<Boolean> = shifts.observeAll().map { rows ->
        rows.sortedByDescending { it.date }.take(12).map { it.id }
    }.distinctUntilChanged().flatMapLatest { ids ->
        if (ids.isEmpty()) flowOf(false)
        else combine(ids.map(::observeShift)) { changes -> changes.any { it.pending } }
    }.distinctUntilChanged()
}
