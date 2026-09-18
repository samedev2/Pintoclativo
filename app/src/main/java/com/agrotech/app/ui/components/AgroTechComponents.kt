package com.agrotech.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agrotech.app.ui.theme.FullPillShape
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.Hairline
import com.agrotech.app.ui.theme.IconChipBackground
import com.agrotech.app.ui.theme.PillShape
import com.agrotech.app.ui.theme.Spacing

/**
 * Quadrado arredondado com fundo `IconChipBackground` (verde
 * agro claro), usado em stat cards e itens de lista. Default 40dp.
 */
@Composable
fun IconChip(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.tertiary,
    background: Color = IconChipBackground,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(background, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

/**
 * Item de lista (Ui/shadcn list item). Card com IconChip + título +
 * subtítulo + chevron. Badge opcional vira um [Tag] à direita do
 * título.
 */
@Composable
fun EntityListCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    AgroTechCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconChip(icon)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (badge != null) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(Spacing.xs))
                    Tag(text = badge, variant = TagVariant.Subtle)
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Stat card do Ui/shadcn. Label em `labelSmall` uppercase muted
 * (12px) + valor em `displaySmall` 24px SemiBold com tracking
 * tight (-0.025em). Default 24dp radius com borda + sombra.
 */
@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    AgroTechCard(modifier = modifier.fillMaxWidth()) {
        IconChip(icon)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(Spacing.md))
        SectionLabel(label)
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = Spacing.xs)
        )
    }
}

/**
 * Controle segmentado (pill) de duas ou mais opções, ex.: Gênero
 * Macho/Fêmea. Especificação Ui/shadcn: pill full-rounded, item
 * selecionado com bg `Ink` + text `OnInk`.
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, PillShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, opcao ->
            val selecionado = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .heightIn(min = 36.dp)
                    .clickable { onSelect(index) }
                    .background(
                        if (selecionado) MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        PillShape
                    )
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    opcao,
                    color = if (selecionado) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Seletor de semanas em chips roláveis com setas nas pontas.
 * Mantido do design antigo (não tem equivalente exato no Ui).
 */
@Composable
fun WeekSelector(
    weeks: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selected, weeks) {
        val index = weeks.indexOf(selected)
        if (index >= 0) listState.animateScrollToItem(index)
    }
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(enabled = selected != weeks.firstOrNull(), onClick = {
            val indiceAtual = weeks.indexOf(selected)
            if (indiceAtual > 0) onSelect(weeks[indiceAtual - 1])
        }) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Semana anterior",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyRow(
            state = listState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(weeks) { semana ->
                val selecionado = semana == selected
                Box(
                    modifier = Modifier
                        .background(
                            if (selecionado) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface,
                            FullPillShape
                        )
                        .border(
                            1.dp,
                            if (selecionado) MaterialTheme.colorScheme.primary
                            else Hairline,
                            FullPillShape
                        )
                        .heightIn(min = 36.dp)
                        .clickable { onSelect(semana) }
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sem. $semana",
                        color = if (selecionado) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        IconButton(onClick = {
            val indiceAtual = weeks.indexOf(selected)
            if (indiceAtual >= 0 && indiceAtual < weeks.lastIndex) onSelect(weeks[indiceAtual + 1])
        }) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Próxima semana",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Gráfico de barras simples (sem dependência externa) pra totais
 * por semana. Barras do item em destaque usam `primary`, demais
 * usam `surfaceVariant` (cinza claro Ui).
 */
@Composable
fun SimpleBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 120.dp
) {
    if (data.isEmpty()) return
    val maxValor = data.maxOf { it.second }.coerceAtLeast(1f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight + 24.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, (rotulo, valor) ->
            val destaque = index == data.lastIndex
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight * (valor / maxValor).coerceIn(0.04f, 1f))
                        .background(
                            if (destaque) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )
                Text(
                    rotulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}

/** Borda tracejada aplicada por cima do conteúdo, seguindo o `shape` informado. */
fun Modifier.dashedBorder(
    color: Color,
    shape: Shape = PillShape,
    strokeWidth: Dp = 1.5.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp
): Modifier = this.drawWithContent {
    drawContent()
    val outline = shape.createOutline(size, layoutDirection, this)
    val path = Path().apply { addOutline(outline) }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f)
        )
    )
}
