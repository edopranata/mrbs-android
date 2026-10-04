package com.ropekanbaru.booking.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Kotak pesan error merah seperti versi web, dengan tombol "Coba lagi" (opsional). */
@Composable
fun ErrorCard(message: String, onRetry: (() -> Unit)? = null) {
    Surface(
        color = Tw.Red50,
        border = BorderStroke(1.dp, Tw.Red200),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(message, color = Tw.Red700, style = MaterialTheme.typography.bodyMedium)
            onRetry?.let { SecondaryButton("Coba lagi", onClick = it) }
        }
    }
}

private fun LocalDate.toUtcMillis() = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.toLocalDate() = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

/** Dialog pilih tanggal dengan batas tanggal minimal/maksimal (opsional). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    selected: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = selected.toUtcMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = utcTimeMillis.toLocalDate()
                return (minDate == null || !date.isBefore(minDate)) && (maxDate == null || !date.isAfter(maxDate))
            }
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onConfirm(it.toLocalDate()) }
                onDismiss()
            }) { Text("Pilih") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    ) {
        DatePicker(state = state)
    }
}

/** Dialog konfirmasi seperti ConfirmDialog di web; [danger] mewarnai tombol konfirmasi merah. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    danger: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(title, color = Tw.Slate900) },
        text = { Text(message, color = Tw.Slate600) },
        confirmButton = {
            PrimaryButton(confirmText, danger = danger, onClick = {
                onDismiss()
                onConfirm()
            })
        },
        dismissButton = { SecondaryButton("Batal", onClick = onDismiss) },
    )
}
