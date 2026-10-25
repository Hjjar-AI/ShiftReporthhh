package com.hos.rushdpatients.sync

import com.hos.rushdpatients.data.model.Patient

internal fun samePatientContent(left: Patient?, right: Patient?): Boolean =
    if (left == null || right == null) left == right
    else KnownShiftSnapshot.patientContent(left) == KnownShiftSnapshot.patientContent(right)
