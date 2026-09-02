package com.agrotech.app.ui.lote.mortalidade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agrotech.app.data.local.entities.DiaSemana
import com.agrotech.app.data.local.entities.MortalidadeDiariaEntity
import com.agrotech.app.ui.lote.LoteDetalheViewModel

private val SEMANAS = (1..8).toList()

@Composable
fun MortalidadeTab(viewModel: LoteDetalheViewModel) {
    val registros by viewModel.mortalidadeRegistros.collectAsStateWithLifecycle()
    val resumos by viewModel.resumoSemanas.collectAsStateWithLifecycle()
    var semanaSelecionada by remember { mutableStateOf(1) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SEMANAS.forEach { semana ->
                FilterChip(
                    selected = semana == semanaSelecionada,
                    onClick = { semanaSelecionada = semana },
                    label = { Text("Sem. $semana") }
                )
            }
        }

        Column(modifier = Modifier.padding(top = 16.dp)) {
            val resumoSemana = resumos.find { it.semana == semanaSelecionada }
            if (resumoSemana != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Total: ${resumoSemana.total}  ·  %Sem: ${String.format("%.2f", resumoSemana.percentualSemana * 100)}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "%Acum: ${String.format("%.2f", resumoSemana.percentualAcumulado * 100)}%  ·  Saldo: ${resumoSemana.saldo}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DiaSemana.entries, key = { it.name }) { dia ->
                    val registroExistente = registros.find { it.semana == semanaSelecionada && it.diaSemana == dia }
                    LinhaDia(
                        dia = dia,
                        registroExistente = registroExistente,
                        aoSalvar = { mortalidade, descarte ->
                            viewModel.salvarRegistroDiario(semanaSelecionada, dia, mortalidade, descarte)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LinhaDia(
    dia: DiaSemana,
    registroExistente: MortalidadeDiariaEntity?,
    aoSalvar: (Int, Int) -> Unit
) {
    var mortalidade by remember(registroExistente) {
        mutableStateOf(registroExistente?.mortalidade?.toString() ?: "")
    }
    var descarte by remember(registroExistente) {
        mutableStateOf(registroExistente?.descarte?.toString() ?: "")
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            dia.label,
            modifier = Modifier.weight(0.8f).padding(top = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = mortalidade,
            onValueChange = { mortalidade = it },
            label = { Text("Mort.") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = descarte,
            onValueChange = { descarte = it },
            label = { Text("Desc.") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        IconButton(onClick = {
            aoSalvar(mortalidade.toIntOrNull() ?: 0, descarte.toIntOrNull() ?: 0)
        }) {
            Icon(Icons.Filled.Check, contentDescription = "Salvar dia")
        }
    }
}
