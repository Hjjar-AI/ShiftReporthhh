package com.hos.rushdpatients.ui.ward

import com.hos.rushdpatients.ui.components.AppAlertDialog
import com.hos.rushdpatients.R
import com.hos.rushdpatients.ui.components.rememberUiStrings

import com.hos.rushdpatients.ui.components.AppActionLabel
import com.hos.rushdpatients.ui.components.UiAction
import com.hos.rushdpatients.ui.theme.UiSpacing
import com.hos.rushdpatients.ui.components.AppTextButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hos.rushdpatients.data.model.Patient

@Composable
internal fun PatientRecycleBinDialog(
    patients: List<Patient>,
    readOnly: Boolean,
    onRestore: (Patient) -> Unit,
    onDismiss: () -> Unit
) {
    val uiText = rememberUiStrings()
    AppAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(uiText(R.string.ui_ward_review_dialogs_009)) },
        text = {
            if (patients.isEmpty()) {
                Text(uiText(R.string.ui_ward_review_dialogs_010))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(UiSpacing.small)) {
                    items(patients, key = { it.id }) { patient ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(patient.displayName, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    patient.admittanceNumber,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            if (!readOnly) {
                                AppTextButton(onClick = { onRestore(patient) }) {
                                    AppActionLabel(UiAction.RESTORE)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            AppTextButton(onClick = onDismiss) { Text(uiText(R.string.ui_close)) }
        }
    )
}
