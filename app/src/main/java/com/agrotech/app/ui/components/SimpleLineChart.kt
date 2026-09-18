package com.agrotech.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agrotech.app.ui.theme.GreenPrimary

/**
 * Gráfico de linha minimalista (sem dependência externa), usado para
 * séries curtas como "produção de leite nos últimos 14 dias" ou
 * "evolução do saldo do lote". Recebe pares `(rótulo, valor)` e
 * desenha a curva + linhas tracejadas horizontais como referência.
 *
 * Abaixo da curva, mostra até 5 rótulos distribuídos uniformemente
 * (primeiro, último e 3 intermediários) para não poluir a base.
 */
@Composable
fun SimpleLineChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 120.dp,
    corLinha: Color = GreenPrimary,
    linhasReferencia: Int = 3
) {
    if (data.isEmpty()) return
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val maxValor = data.maxOf { it.second }.coerceAtLeast(1f)
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
                val w = size.width
                val h = size.height
                val n = data.size
                if (n < 2) return@Canvas

                // Linhas tracejadas horizontais como referência.
                for (i in 1..linhasReferencia) {
                    val y = h * (i.toFloat() / (linhasReferencia + 1))
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(w, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                }

                // Curva conectando os pontos.
                val path = Path()
                data.forEachIndexed { index, (_, valor) ->
                    val x = w * (index.toFloat() / (n - 1))
                    val y = h - (valor / maxValor) * h * 0.95f - h * 0.025f
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = corLinha,
                    style = Stroke(width = 3f)
                )
            }
        }
        // Rótulos sob a curva — distribuídos uniformemente em um Row.
        val pontosExibir = if (data.size <= 5) data.indices.toList()
        else listOf(0, data.size / 4, data.size / 2, (3 * data.size) / 4, data.lastIndex)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        ) {
            pontosExibir.forEach { idx ->
                Text(
                    text = data[idx].first,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
