package com.ropekanbaru.booking.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropekanbaru.booking.data.remote.BookingDto

/** Palet Tailwind yang dipakai versi web, agar tampilan Android serasi. */
object Tw {
    val Slate50 = Color(0xFFF8FAFC)
    val Slate100 = Color(0xFFF1F5F9)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate300 = Color(0xFFCBD5E1)
    val Slate400 = Color(0xFF94A3B8)
    val Slate500 = Color(0xFF64748B)
    val Slate600 = Color(0xFF475569)
    val Slate700 = Color(0xFF334155)
    val Slate900 = Color(0xFF0F172A)
    val Indigo50 = Color(0xFFEEF2FF)
    val Indigo100 = Color(0xFFE0E7FF)
    val Indigo500 = Color(0xFF6366F1)
    val Indigo600 = Color(0xFF4F46E5)
    val Indigo700 = Color(0xFF4338CA)
    val Emerald50 = Color(0xFFECFDF5)
    val Emerald100 = Color(0xFFD1FAE5)
    val Emerald200 = Color(0xFFA7F3D0)
    val Emerald600 = Color(0xFF059669)
    val Emerald700 = Color(0xFF047857)
    val Red50 = Color(0xFFFEF2F2)
    val Red100 = Color(0xFFFEE2E2)
    val Red200 = Color(0xFFFECACA)
    val Red500 = Color(0xFFEF4444)
    val Red600 = Color(0xFFDC2626)
    val Red700 = Color(0xFFB91C1C)
    val Amber50 = Color(0xFFFFFBEB)
    val Amber600 = Color(0xFFD97706)
    val Sky50 = Color(0xFFF0F9FF)
    val Sky600 = Color(0xFF0284C7)
}

private val CardShape = RoundedCornerShape(12.dp)
private val ControlShape = RoundedCornerShape(8.dp)

/** Kartu putih berbingkai tipis seperti kelas `.card` di versi web. */
@Composable
fun WebCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = CardShape,
        color = Color.White,
        border = BorderStroke(1.dp, Tw.Slate200),
        shadowElevation = 1.dp,
        modifier = modifier.then(if (onClick != null) Modifier.clip(CardShape).clickable(onClick = onClick) else Modifier),
    ) { Column(content = content) }
}

/** Judul kartu dengan garis bawah dan tautan aksi di kanan ("Lihat jadwal →"). */
@Composable
fun CardHeader(title: String, action: String? = null, onAction: () -> Unit = {}, trailing: (@Composable RowScope.() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 6.dp, bottom = 6.dp).heightIn(min = 44.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Tw.Slate900, modifier = Modifier.weight(1f))
        trailing?.invoke(this)
        if (action != null) TextButton(onClick = onAction) { Text("$action →", fontWeight = FontWeight.Medium) }
    }
    HorizontalDivider(color = Tw.Slate200)
}

/** Kotak ikon berwarna (kartu statistik, judul bagian Pengaturan). */
@Composable
fun IconBox(icon: ImageVector, background: Color, tint: Color, size: Int = 40) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size.dp).clip(ControlShape).background(background)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size((size / 2).dp))
    }
}

/** Badge kecil membulat seperti kelas `.badge`. */
@Composable
fun Pill(text: String, background: Color, content: Color, modifier: Modifier = Modifier, dot: Color? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clip(RoundedCornerShape(50)).background(background).padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        if (dot != null) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, color = content, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
    }
}

/** Status booking seperti StatusBadge di web: Terjadwal / Berlangsung / Selesai / Dibatalkan. */
@Composable
fun StatusBadge(booking: BookingDto, modifier: Modifier = Modifier) = when {
    booking.isCancelled -> Pill("Dibatalkan", Tw.Red100, Tw.Red700, modifier)
    booking.isOngoing -> Pill("Berlangsung", Tw.Emerald100, Tw.Emerald700, modifier, dot = Tw.Emerald600)
    booking.hasEnded -> Pill("Selesai", Tw.Slate100, Tw.Slate600, modifier)
    else -> Pill("Terjadwal", Tw.Indigo50, Tw.Indigo700, modifier)
}

/** Baris booking dalam daftar (Booking Saya, Dashboard, Semua Booking), sama dengan versi web. */
@Composable
fun BookingListRow(
    booking: BookingDto,
    onClick: () -> Unit,
    showUser: Boolean = false,
    mine: Boolean = false,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
            .alpha(if (booking.isCancelled) 0.8f else 1f)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Box(
            Modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(if (booking.isCancelled) Tw.Slate300 else roomColor(booking.room?.color, Tw.Indigo600)),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    (if (booking.isRecurring) "↻ " else "") + booking.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Tw.Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                StatusBadge(booking)
            }
            Text(
                "${friendlyDate(booking.date)}, ${booking.startTime}–${booking.endTime}",
                style = MaterialTheme.typography.bodySmall,
                color = Tw.Slate500,
            )
            val detail = listOfNotNull(
                booking.room?.let { "${it.name} · Lt. ${it.floor}" },
                booking.user?.name?.takeIf { showUser }?.let { if (mine) "$it (Anda)" else it },
            ).joinToString(" · ")
            if (detail.isNotEmpty()) {
                Text(detail, style = MaterialTheme.typography.bodySmall, color = Tw.Slate500, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (booking.isCancelled && !booking.cancelReason.isNullOrBlank()) {
                Text("Alasan: ${booking.cancelReason}", style = MaterialTheme.typography.bodySmall, color = Tw.Red600)
            }
        }
    }
}

/** Kotak kosong dengan ikon, judul, dan keterangan (EmptyState di web). */
@Composable
fun EmptyState(icon: ImageVector, title: String, description: String? = null, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 32.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp).clip(CircleShape).background(Tw.Slate100)) {
            Icon(icon, null, tint = Tw.Slate400)
        }
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = Tw.Slate700)
        description?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Tw.Slate500) }
    }
}

// ---------------------------------------------------------------- Tombol (sudut 8dp seperti web)

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    danger: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ControlShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (danger) Tw.Red600 else Tw.Indigo600,
            disabledContainerColor = (if (danger) Tw.Red600 else Tw.Indigo600).copy(alpha = 0.5f),
            disabledContentColor = Color.White,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier,
    ) {
        icon?.let {
            Icon(it, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    danger: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = ControlShape,
        border = BorderStroke(1.dp, Tw.Slate300),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = if (danger) Tw.Red600 else Tw.Slate700),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier,
    ) {
        icon?.let {
            Icon(it, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun GhostButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, danger: Boolean = false) {
    TextButton(
        onClick = onClick,
        shape = ControlShape,
        colors = ButtonDefaults.textButtonColors(contentColor = if (danger) Tw.Red600 else Tw.Slate600),
        modifier = modifier,
    ) {
        icon?.let {
            Icon(it, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, fontWeight = FontWeight.Medium)
    }
}

// ---------------------------------------------------------------- Isian dengan label di atas

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Tw.Slate700, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}

/** Tinggi seragam semua kolom isian & pilihan, agar baris form selalu sejajar. */
val FieldHeight = 48.dp

/** Kolom isian bergaya `.input` di web: label di atas, tinggi seragam, sudut 8dp, bingkai slate. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    hint: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
    labelTrailing: (@Composable () -> Unit)? = null,
    textFieldModifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val colors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Tw.Slate300,
        focusedBorderColor = Tw.Indigo500,
        unfocusedContainerColor = Color.White,
        focusedContainerColor = Color.White,
        disabledContainerColor = Tw.Slate100,
        disabledBorderColor = Tw.Slate200,
        disabledTextColor = Tw.Slate500,
    )
    Column(modifier) {
        FieldLabel(label, trailing = labelTrailing)
        // BasicTextField + DecorationBox: OutlinedTextField bawaan selalu setinggi 56dp,
        // sedangkan kolom di web lebih ringkas dan harus sejajar dengan SelectField.
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            interactionSource = interaction,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = if (enabled) Tw.Slate900 else Tw.Slate500),
            cursorBrush = SolidColor(Tw.Indigo600),
            modifier = textFieldModifier.fillMaxWidth().heightIn(min = FieldHeight),
            decorationBox = { inner ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = inner,
                    enabled = enabled,
                    singleLine = singleLine,
                    visualTransformation = visualTransformation,
                    interactionSource = interaction,
                    placeholder = placeholder?.let { { Text(it, color = Tw.Slate400, style = MaterialTheme.typography.bodyLarge) } },
                    trailingIcon = trailingIcon,
                    colors = colors,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = enabled,
                            isError = false,
                            interactionSource = interaction,
                            colors = colors,
                            shape = ControlShape,
                            focusedBorderThickness = 2.dp,
                            unfocusedBorderThickness = 1.dp,
                        )
                    },
                )
            },
        )
        hint?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Tw.Slate500, modifier = Modifier.padding(top = 4.dp)) }
    }
}

/**
 * Baris form yang responsif: di HP kolom-kolomnya bertumpuk (satu kolom), di tablet berdampingan
 * dengan lebar sama dan rata atas. Pakai untuk pasangan isian seperti "Level | Department".
 */
@Composable
fun FormRow(vararg fields: @Composable (Modifier) -> Unit) {
    if (isWideLayout()) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
            fields.forEach { it(Modifier.weight(1f)) }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            fields.forEach { it(Modifier.fillMaxWidth()) }
        }
    }
}

/** Kotak centang dengan teks (dan keterangan opsional), seluruh baris bisa diketuk. */
@Composable
fun CheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, description: String? = null, enabled: Boolean = true) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(ControlShape)
            .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(vertical = 4.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = if (enabled) Tw.Slate700 else Tw.Slate400)
            description?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Tw.Slate500) }
        }
    }
}

/** Tombol bergaya kolom pilihan (`select` di web) yang membuka daftar pilihan. */
@Composable
fun <T> SelectField(
    value: String,
    options: List<T>,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(
            shape = ControlShape,
            color = Color.White,
            border = BorderStroke(1.dp, Tw.Slate300),
            modifier = Modifier.fillMaxWidth().heightIn(min = FieldHeight).clip(ControlShape).clickable(enabled = options.isNotEmpty()) { open = true },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = FieldHeight).padding(horizontal = 12.dp)) {
                leadingIcon?.let {
                    Icon(it, null, tint = Tw.Slate500, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(value, color = Tw.Slate900, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text("▾", color = Tw.Slate500)
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(label(option)) }, onClick = {
                    onSelect(option)
                    open = false
                })
            }
        }
    }
}

/** Jumlah kecil di samping judul bagian ("Sedang Berlangsung  0"). */
@Composable
fun CountPill(count: Int) = Pill("$count", Tw.Slate100, Tw.Slate600)

/** Pilihan bersegmen dengan latar indigo pada yang aktif (Hari / Minggu / Bulan, Internal / Eksternal). */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, selectedColor: Color = Tw.Indigo600) {
    Surface(shape = ControlShape, color = Color.White, border = BorderStroke(1.dp, Tw.Slate300), modifier = modifier.height(FieldHeight)) {
        Row(Modifier.padding(3.dp)) {
            options.forEachIndexed { i, label ->
                val active = i == selected
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (active) selectedColor else Color.Transparent)
                        .clickable { onSelect(i) }
                        .semantics {
                            role = Role.Tab
                            this.selected = active
                        }
                        .padding(horizontal = 14.dp),
                ) {
                    Text(label, color = if (active) Color.White else Tw.Slate600, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                }
            }
        }
    }
}

/** Grup tombol ‹ Hari ini › seperti toolbar jadwal di web. */
@Composable
fun PrevTodayNext(onPrev: () -> Unit, onToday: () -> Unit, onNext: () -> Unit, prevLabel: String, nextLabel: String) {
    Surface(shape = ControlShape, color = Color.White, border = BorderStroke(1.dp, Tw.Slate300)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable(onClick = onPrev).semantics { contentDescription = prevLabel; role = Role.Button }.padding(10.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Tw.Slate700, modifier = Modifier.size(20.dp))
            }
            Box(Modifier.width(1.dp).fillMaxHeight().background(Tw.Slate300))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable(onClick = onToday).padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text("Hari ini", color = Tw.Slate700, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            }
            Box(Modifier.width(1.dp).fillMaxHeight().background(Tw.Slate300))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable(onClick = onNext).semantics { contentDescription = nextLabel; role = Role.Button }.padding(10.dp)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Tw.Slate700, modifier = Modifier.size(20.dp))
            }
        }
    }
}
