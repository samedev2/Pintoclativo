package com.agrotech.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrotech.app.ui.theme.CardShape
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.HairlineLight
import com.agrotech.app.ui.theme.IconChipBackground
import com.agrotech.app.ui.theme.InkLight
import com.agrotech.app.ui.theme.MutedTextLight
import com.agrotech.app.ui.theme.PillShape
import com.agrotech.app.ui.theme.SurfaceLight

/** Rótulo uppercase pequeno e cinza, usado acima de listas e campos de formulário. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MutedTextLight,
        letterSpacing = 0.6.sp,
        modifier = modifier
    )
}

/** Quadrado arredondado com fundo verde clarinho, usado em stat cards e itens de lista. */
@Composable
fun IconChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = GreenPrimary,
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

/** Cartão base do design system: 24dp de raio, fundo branco, borda hairline. */
@Composable
fun AgroTechCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, HairlineLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

/** Item de lista com ícone-chip, título, subtítulo, badge opcional e chevron. */
@Composable
fun EntityListCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    AgroTechCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconChip(icon)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = InkLight)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MutedTextLight)
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .background(IconChipBackground, PillShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(badge, style = MaterialTheme.typography.labelSmall, color = GreenPrimary)
                    }
                }
            }
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = MutedTextLight)
        }
    }
}

/** Cartão de estatística usado no grid 2x2 do Dashboard. */
@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    AgroTechCard(modifier = modifier.fillMaxWidth()) {
        IconChip(icon)
        Column(modifier = Modifier.padding(top = 12.dp)) {
            SectionLabel(label)
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = InkLight,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** Controle segmentado (pill) de duas ou mais opções, ex.: Gênero Macho/Fêmea. */
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
            .background(IconChipBackground.copy(alpha = 0.4f), PillShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, opcao ->
            val selecionado = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(index) }
                    .background(if (selecionado) GreenPrimary else Color.Transparent, PillShape)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    opcao,
                    color = if (selecionado) Color.White else InkLight,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

/** Seletor de semanas em chips roláveis com setas nas pontas. */
@Composable
fun WeekSelector(
    weeks: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            val indiceAtual = weeks.indexOf(selected)
            if (indiceAtual > 0) onSelect(weeks[indiceAtual - 1])
        }) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Semana anterior", tint = MutedTextLight)
        }
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(weeks) { semana ->
                val selecionado = semana == selected
                Box(
                    modifier = Modifier
                        .background(
                            if (selecionado) GreenPrimary else SurfaceLight,
                            PillShape
                        )
                        .border(1.dp, if (selecionado) GreenPrimary else HairlineLight, PillShape)
                        .clickable { onSelect(semana) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Sem. $semana",
                        color = if (selecionado) Color.White else InkLight,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        IconButton(onClick = {
            val indiceAtual = weeks.indexOf(selected)
            if (indiceAtual < weeks.lastIndex) onSelect(weeks[indiceAtual + 1])
        }) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Próxima semana", tint = MutedTextLight)
        }
    }
}

/** Gráfico de barras simples (sem dependência externa) para totais por semana. */
@Composable
fun SimpleBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 120.dp
) {
    if (data.isEmpty()) return
    val maxValor = data.maxOf { it.second }.coerceAtLeast(1f)
    Row(
        modifier = modifier.fillMaxWidth().height(barHeight + 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                            if (destaque) GreenPrimary else IconChipBackground,
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )
                Text(
                    rotulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedTextLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
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
