package com.agrotech.app.ui.lote.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agrotech.app.ui.lote.LoteDetalheViewModel
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

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

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CartaoResumo(
                    titulo = "Saldo atual",
                    valor = "$saldoAtual aves",
                    modifier = Modifier.weight(1f)
                )
                CartaoResumo(
                    titulo = "% acumulado",
                    valor = "${String.format("%.2f", percAcumulado * 100)}%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CartaoResumo(
                    titulo = "Ração recebida",
                    valor = "${(totalRacaoKg * 100).roundToInt() / 100.0} kg",
                    modifier = Modifier.weight(1f)
                )
                CartaoResumo(
                    titulo = "Dias de alojamento",
                    valor = "$diasDesdeAlojamento dias",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Text("Pesagens registradas", style = MaterialTheme.typography.titleMedium)
        }
        if (pesagens.isEmpty()) {
            item { Text("Nenhuma pesagem registrada ainda.") }
        } else {
            items(pesagens.sortedBy { it.checkpoint.dias }, key = { it.checkpoint }) { pesagem ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(pesagem.checkpoint.label)
                        Text("${pesagem.pesoKg} kg")
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaoResumo(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodySmall)
            Text(valor, style = MaterialTheme.typography.titleLarge)
        }
    }
}
