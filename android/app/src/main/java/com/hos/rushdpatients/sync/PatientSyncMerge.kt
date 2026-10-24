package com.hos.rushdpatients.sync

import com.hos.rushdpatients.data.model.Patient
import com.hos.rushdpatients.data.model.withoutTrackingMetadata

internal fun samePatientContent(left: Patient?, right: Patient?): Boolean {
    if (left == null || right == null) return left == right
    return left.withoutTrackingMetadata().copy(revision = 0, sortOrder = 0) ==
        right.withoutTrackingMetadata().copy(revision = 0, sortOrder = 0)
}

