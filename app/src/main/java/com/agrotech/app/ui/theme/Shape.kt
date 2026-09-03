package com.agrotech.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** 24dp em cartões; 18dp em botões/inputs/badges/chips (raios do mockup). */
val CardShape = RoundedCornerShape(24.dp)
val PillShape = RoundedCornerShape(18.dp)

val AgroTechShapes = Shapes(
    extraSmall = PillShape,
    small = PillShape,
    medium = CardShape,
    large = CardShape,
    extraLarge = CardShape
)
