package com.agrotech.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Tokens de raio do design system (Ui/shadcn). Cada raio tem um
 * propósito semântico — não usar valores arbitrários.
 *
 *  - `CardShape` (24dp) → cartões, sheets, dialogs
 *  - `PillShape` (18dp) → botões, inputs, chips, tags
 *  - `FullPillShape` (999dp) → chips circulares (gender, badges)
 *  - `SheetHandleShape` (top 12dp) → handle do bottom sheet
 */
val CardShape = RoundedCornerShape(24.dp)
val PillShape = RoundedCornerShape(18.dp)
val FullPillShape = RoundedCornerShape(999.dp)
val SheetHandleShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
val AvatarShape = RoundedCornerShape(999.dp)

val AgroTechShapes = Shapes(
    extraSmall = PillShape,
    small = PillShape,
    medium = CardShape,
    large = CardShape,
    extraLarge = CardShape
)

/** Tokens de espaçamento (4dp grid). Use SEMPRE em vez de `8.dp` cru. */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}
