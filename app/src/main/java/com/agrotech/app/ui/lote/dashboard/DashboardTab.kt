package com.agrotech.app.ui.lote.dashboard

import com.agrotech.app.ui.components.AdaptivePair
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agrotech.app.domain.calculo.RacaoCalculator
import com.agrotech.app.ui.components.AgroTechCard
import com.agrotech.app.ui.components.SimpleBarChart
import com.agrotech.app.ui.components.StatCard
import com.agrotech.app.ui.lote.LoteDetalheViewModel
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DashboardTab(viewModel: LoteDetalheViewModel) {
    val lote by viewModel.lote.collectAsStateWithLifecycle()
    val resumos by viewModel.resumoSemanas.collectAsStateWithLifecycle()
    val recebimentos by viewModel.recebimentos.collectAsStateWithLifecycle()
    val pesagens by viewModel.pesagens.collectAsStateWithLifecycle()

    val loteAtual = lote ?: return
    val ultimoResumo = resumos.lastOrNull()
    val saldoAtual = ultimoResumo?.saldo ?: loteAtual.qtdAves
    val percAcumulado = ultimoResumo?.percentualAcumulado ?: 0.0
    val totalRacaoKg = recebimentos.sumOf { it.quantidadeKg }
    val diasDesdeAlojamento = TimeUnit.MILLISECONDS.toDays(
        System.currentTimeMillis() - loteAtual.dataAlojamento
    ).coerceAtLeast(0)

    val racaoPorSemana = RacaoCalculator.agruparPorSemana(recebimentos, loteAtual.dataAlojamento)
        .takeLast(4)
        .map { (semana, total) -> "Sem $semana" to total.toFloat() }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AdaptivePair {
                StatCard(
                    icon = Icons.Filled.TrendingUp,
                    label = "Saldo atual",
                    value = "$saldoAtual",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.Percent,
                    label = "% acumulado",
                    value = "${String.format("%.2f", percAcumulado * 100)}%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            AdaptivePair {
                StatCard(
                    icon = Icons.Filled.Inventory2,
                    label = "Ração recebida",
                    value = "${(totalRacaoKg * 100).roundToInt() / 100.0} kg",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.CalendarMonth,
                    label = "Dias de alojamento",
                    value = "$diasDesdeAlojamento dias",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (racaoPorSemana.isNotEmpty()) {
            item {
                AgroTechCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Ração recebida por semana", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "Quilos, últimas ${racaoPorSemana.size} semanas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SimpleBarChart(
                        data = racaoPorSemana,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    )
                }
            }
        }
        item {
            Text("Pesagens registradas", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        if (pesagens.isEmpty()) {
            item { Text("Nenhuma pesagem registrada ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(pesagens.sortedBy { it.checkpoint.dias }, key = { it.checkpoint }) { pesagem ->
                AgroTechCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(pesagem.checkpoint.label, color = MaterialTheme.colorScheme.onSurface)
                        Text("${pesagem.pesoKg} kg", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
