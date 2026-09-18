package com.agrotech.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agrotech.app.ui.theme.Destructive
import com.agrotech.app.ui.theme.DestructiveBg
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.Hairline
import com.agrotech.app.ui.theme.Ink
import com.agrotech.app.ui.theme.OnInk
import com.agrotech.app.ui.theme.OnGreen
import com.agrotech.app.ui.theme.PillShape
import com.agrotech.app.ui.theme.Spacing

/**
 * Variantes de botão do design system (Ui/shadcn).
 *
 *  - `PrimaryButton` (filled dark) → bg `Ink`, text `OnInk`. Pra ação
 *    principal da tela.
 *  - `SecondaryButton` (filled canvas) → bg `surfaceVariant`, text
 *    `onSurface`. Pra ação secundária.
 *  - `OutlineButton` (outlined) → bg `surface`, border `Hairline`,
 *    text `onSurface`.
 *  - `DestructiveButton` (filled red) → bg `Destructive`, text `OnInk`.
 *  - `AccentButton` (filled green) → bg `GreenPrimary`, text `OnGreen`.
 *    Usar com moderação (é o acento agro, não o primary).
 *
 * Especificação:
 *  - raio: 18dp (PillShape)
 *  - padding: 0px vertical / 12px horizontal
 *  - height: 36dp (default) ou 52dp (large) pra CTAs
 *  - font: 14px Medium
 */
enum class ButtonSize { Default, Large }

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Default,
    leadingIcon: ImageVector? = null
) {
    DsButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        size = size,
        leadingIcon = leadingIcon,
        background = Ink,
        contentColor = OnInk
    )
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Default,
    leadingIcon: ImageVector? = null
) {
    DsButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        size = size,
        leadingIcon = leadingIcon,
        background = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Default,
    leadingIcon: ImageVector? = null
) {
    DsButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        size = size,
        leadingIcon = leadingIcon,
        background = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, Hairline)
    )
}

@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Default,
    leadingIcon: ImageVector? = null
) {
    DsButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        size = size,
        leadingIcon = leadingIcon,
        background = DestructiveBg,
        contentColor = Destructive
    )
}

@Composable
fun AccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: ButtonSize = ButtonSize.Default,
    leadingIcon: ImageVector? = null
) {
    DsButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        size = size,
        leadingIcon = leadingIcon,
        background = GreenPrimary,
        contentColor = OnGreen
    )
}

@Composable
private fun DsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    size: ButtonSize,
    leadingIcon: ImageVector?,
    background: Color,
    contentColor: Color,
    border: BorderStroke? = null
) {
    val height: Dp = if (size == ButtonSize.Large) 52.dp else 36.dp
    val bg = if (enabled) background else contentColor.copy(alpha = 0.12f)
    val fg = if (enabled) contentColor else contentColor.copy(alpha = 0.4f)
    Row(
        modifier = modifier
            .heightIn(min = height)
            .background(bg, PillShape)
            .then(
                if (border != null) Modifier.border(border, PillShape) else Modifier
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(Spacing.xs))
        }
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
