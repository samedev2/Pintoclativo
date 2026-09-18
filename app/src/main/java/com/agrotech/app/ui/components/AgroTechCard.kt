package com.agrotech.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agrotech.app.ui.theme.CardShape
import com.agrotech.app.ui.theme.Spacing

/**
 * Cartão base do design system (Ui/shadcn-inspired).
 *
 * Especificação (do `https://styles.refero.design/style/0fd67ec5...`):
 *  - fundo: `MaterialTheme.colorScheme.surface` (#ffffff)
 *  - raio: 24dp
 *  - borda: 1px solid `outlineVariant` (#e5e5e5)
 *  - sombra: 0 0 0 1px rgba(23,23,23,0.05) + 0 1px 3px rgba(0,0,0,0.1)
 *  - padding interno: 20dp (default — pode ser customizado)
 */
@Composable
fun AgroTechCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = Spacing.xl,
    elevation: Dp = 3.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val finalModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    Card(
        modifier = finalModifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}
