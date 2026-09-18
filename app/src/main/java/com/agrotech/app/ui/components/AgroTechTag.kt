package com.agrotech.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agrotech.app.ui.theme.FullPillShape
import com.agrotech.app.ui.theme.GreenChipBg
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.GreenPrimaryDark
import com.agrotech.app.ui.theme.Hairline
import com.agrotech.app.ui.theme.Ink
import com.agrotech.app.ui.theme.InkSoft
import com.agrotech.app.ui.theme.Muted
import com.agrotech.app.ui.theme.OnInk
import com.agrotech.app.ui.theme.PillShape
import com.agrotech.app.ui.theme.Spacing

/**
 * Tag/Badge do design system (Ui/shadcn). Pílula full-rounded com
 * 3 variantes:
 *
 *  - `Solid` (default): bg `Ink`, text `OnInk`. Pra "etiquetas" de
 *    destaque (ex.: "Semana 4", "Em curso").
 *  - `Outline`: bg `surface`, border `Hairline`, text `onSurface`.
 *    Pra tags neutras/secundárias.
 *  - `Subtle`: bg `GreenChipBg`, text `GreenPrimaryDark`. Pra tags
 *    de domínio agro (ex.: "Macho", "Engorda 2").
 *
 * Especificação:
 *  - raio: 999dp (FullPillShape)
 *  - padding: 2px vertical / 8px horizontal
 *  - font: 12px Medium
 */
enum class TagVariant { Solid, Outline, Subtle }

@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    variant: TagVariant = TagVariant.Solid
) {
    val (bg, fg, border) = when (variant) {
        TagVariant.Solid   -> Triple(InkSoft, OnInk, null)
        TagVariant.Outline -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            androidx.compose.foundation.BorderStroke(1.dp, Hairline)
        )
        TagVariant.Subtle  -> Triple(GreenChipBg, GreenPrimaryDark, null)
    }
    val finalModifier = if (border != null) modifier.border(border, FullPillShape) else modifier
    Box(
        modifier = finalModifier
            .background(bg, FullPillShape)
            .padding(horizontal = Spacing.sm, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
