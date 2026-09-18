package com.agrotech.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agrotech.app.ui.theme.PillShape
import com.agrotech.app.ui.theme.Spacing

/**
 * `OutlinedTextField` no estilo do Ui/shadcn. Especificação:
 *
 *  - background: `surfaceVariant` (canvas claro) — input "afundado"
 *  - raio: 18dp (PillShape)
 *  - border: NENHUMA em repouso; 1px solid `outline` no focus (ring
 *    com offset 0)
 *  - padding interno: 8px vertical / 12px horizontal
 *  - texto: 14px regular, `onSurface`
 *  - placeholder: `onSurfaceVariant` (Muted)
 *  - label flutuante: `onSurface` em repouso, `primary` no focus
 *
 * Cores do Material 3 (filled style):
 *  - focusedContainerColor: igual ao unfocado (canvas)
 *  - focusedIndicatorColor: outline (hairline)
 *  - unfocusedIndicatorColor: transparente
 *  - cursor: primary
 */
@Composable
fun AgroTechTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = PillShape,
    contentPadding: Dp = 0.dp
) {
    val cores = OutlinedTextFieldDefaults.colors(
        // Container (filled style) — sempre canvas, sem cor de focus diferente
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        // Texto
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        // Label flutuante
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        // Placeholder
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        // Borda (filled style: indicator)
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
        disabledBorderColor = androidx.compose.ui.graphics.Color.Transparent,
        // Cursor
        cursorColor = MaterialTheme.colorScheme.primary,
        // Ícones
        focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
        focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        shape = shape,
        colors = cores
    )
}

/**
 * Label uppercase pequeno e cinza, usado acima de listas e campos
 * de formulário. Inspirado no `.text-caption` do Ui/shadcn.
 */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
