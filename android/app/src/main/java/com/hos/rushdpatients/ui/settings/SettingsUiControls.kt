package com.hos.rushdpatients.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.hos.rushdpatients.R
import com.hos.rushdpatients.ui.components.*
import com.hos.rushdpatients.ui.theme.*

/** A single spacing choice and an accessibility preference; other layout defaults are consistent. */
@Composable
internal fun SettingsUiControls(value: UiPreferences, onChange: (UiPreferences) -> Unit) {
    val text = rememberUiStrings()
    Column(verticalArrangement = Arrangement.spacedBy(UiSpacing.controlGap)) {
        AppToggleRow(text(R.string.ui_comfortable_spacing), value.density == UiDensity.RELAXED,
            { onChange(value.copy(density = if (it) UiDensity.RELAXED else UiDensity.NORMAL).simplified()) })
        AppToggleRow(text(R.string.ui_reduced_motion), value.reducedMotion,
            { onChange(value.copy(reducedMotion = it).simplified()) })
    }
}
