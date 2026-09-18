package com.agrotech.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.agrotech.app.R

/**
 * Tipografia do design system (Ui/shadcn-inspired). Usa Inter como
 * substituto neutro de Geist (mesma métrica e personalidade) — Inter
 * já vem empacotado no app, então não precisa baixar fontes externas.
 *
 * Convenção:
 *  - Headings:  peso 600, letter-spacing levemente negativo (-0.025em)
 *    pra dar densidade de marca. Tamanhos: 36/30/24.
 *  - Titles:    peso 600, sem tracking. 22/16/14.
 *  - Body:      peso 400. 16/14/12.
 *  - Labels:    peso 500, UPPERCASE opcional. 14/12/11.
 */
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private val trackingTight = (-0.6).sp       // -0.025em em 24sp
private val trackingTighter = (-0.75).sp    // -0.025em em 30sp
private val trackingTightest = (-0.9).sp    // -0.025em em 36sp

val AgroTechTypography = Typography(
    // === Headlines (stat values, hero text) ===
    displayLarge = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = trackingTightest
    ),
    displayMedium = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = trackingTighter
    ),
    displaySmall = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = trackingTight
    ),

    // === Headlines (são aliases do Material 3 que não usamos muito) ===
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = trackingTighter
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = trackingTight
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 28.sp
    ),

    // === Titles (section headers, card titles) ===
    titleLarge = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp
    ),

    // === Body (texto geral) ===
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp
    ),

    // === Labels (botões, badges, section labels) ===
    labelLarge = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp
    )
)
