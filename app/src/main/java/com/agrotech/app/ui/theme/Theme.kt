package com.agrotech.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Tema do AgroTech. Paleta clara segue o Ui/shadcn (Canvas/Paper/Ink
 * com acento verde agro). O `primary` do Material 3 é o **Ink** (não o
 * verde) — o verde vira `tertiary`/acento de domínio.
 *
 * Use `MaterialTheme.colorScheme.primary` pra botões filled escuros.
 * Use `MaterialTheme.colorScheme.tertiary` pra destaques verdes.
 */
private val LightColors = lightColorScheme(
    primary = Ink,                          // botão filled dark
    onPrimary = OnInk,
    primaryContainer = PaperAlt,            // chip/badge bg claro
    onPrimaryContainer = Ink,

    secondary = InkSoft,
    onSecondary = OnInk,
    secondaryContainer = Hairline,
    onSecondaryContainer = Ink,

    tertiary = GreenPrimary,                // acento agro
    onTertiary = OnGreen,
    tertiaryContainer = GreenChipBg,
    onTertiaryContainer = GreenPrimaryDark,

    background = Paper,                     // fundo branco solicitado
    onBackground = Ink,
    surface = Paper,                        // cartões
    onSurface = Ink,
    surfaceVariant = PaperAlt,
    onSurfaceVariant = Muted,

    error = Destructive,
    onError = OnInk,
    errorContainer = DestructiveBg,
    onErrorContainer = Destructive,

    outline = Muted,                        // divisor
    outlineVariant = Hairline,
    surfaceTint = Color.Transparent
)

@Composable
fun AgroTechTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        // A operação de campo usa sempre a interface clara. Isso impede que o
        // modo escuro do aparelho transforme telas e formulários em preto.
        colorScheme = LightColors,
        shapes = AgroTechShapes,
        typography = AgroTechTypography,
        content = content
    )
}
