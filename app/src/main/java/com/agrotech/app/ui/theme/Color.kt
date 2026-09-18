package com.agrotech.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design system do AgroTech — inspirado no **Ui (shadcn)** exposto em
 * https://styles.refero.design/style/0fd67ec5-7e9c-4ca9-b368-5d9c7388477a
 *
 * Paleta neutra (Canvas/Paper/Ink/Muted/Hairline) + acento verde pra
 * contexto agro. Todos os tokens abaixo são usados em [AgroTechTheme]
 * e nos componentes do `ui/components/`.
 *
 * Convenção de nomenclatura:
 *  - `Ink*`   → texto/fundo escuro
 *  - `Paper*` → superfície clara (cards, sheet)
 *  - `Canvas*`→ fundo da tela
 *  - `Muted*` → texto secundário
 *  - `Hair*`  → borda/divisor hairline
 *  - `Destructive*` → ações destrutivas (vermelho)
 *  - `Green*` → acento agro (primário de domínio)
 */

// === Ink (texto + ações dark filled) ===
val Ink = Color(0xFF0A0A0A)
val InkSoft = Color(0xFF171717)         // filled action dark (botão primário)
val OnInk = Color(0xFFFAFAFA)           // texto em cima de Ink/InkSoft
val InkDisabled = Color(0xFF404040)

// === Paper (cards, sheet) ===
val Paper = Color(0xFFFFFFFF)
val PaperAlt = Color(0xFFFAFAFA)

// === Canvas (fundo da tela) ===
val Canvas = Color(0xFFFFFFFF)

// === Muted (texto secundário) ===
val Muted = Color(0xFF737373)
val MutedSubtle = Color(0xFFA3A3A3)

// === Hairline (borda/divisor) ===
val Hairline = Color(0xFFE5E5E5)
val HairlineSoft = Color(0xFFF0F0F0)

// === Destructive ===
val Destructive = Color(0xFFE7000B)
val DestructiveBg = Color(0xFFFEE2E2)

// === Acento agro (verde) — usado como destaque de domínio (chips, badges) ===
val GreenPrimary = Color(0xFF2E7D32)
val GreenPrimaryDark = Color(0xFF1B5E20)
val GreenSecondary = Color(0xFF66BB6A)
val GreenChipBg = Color(0xFFE8F5E9)
val OnGreen = Color(0xFFFFFFFF)

// === Compat (aliases pros nomes antigos) — manter até migrar todos os call sites ===
@Deprecated("Use Ink")        val InkLight       = Ink
@Deprecated("Use Muted")      val MutedTextLight = Muted
@Deprecated("Use Hairline")   val HairlineLight  = Hairline
@Deprecated("Use Canvas")     val CanvasLight    = Canvas
@Deprecated("Use Paper")      val SurfaceLight   = Paper
@Deprecated("Use PaperAlt")   val SurfaceAltLight = PaperAlt
@Deprecated("Use GreenChipBg")val IconChipBackground = GreenChipBg
@Deprecated("Use OnGreen")    val OnPrimaryLight = OnGreen
