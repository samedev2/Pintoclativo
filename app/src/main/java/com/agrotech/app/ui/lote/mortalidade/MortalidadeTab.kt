package com.agrotech.app.ui.lote.mortalidade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.agrotech.app.ui.components.AgroTechCard
import com.agrotech.app.ui.components.AgroTechTextField
import com.agrotech.app.ui.components.SectionLabel
import com.agrotech.app.ui.components.WeekSelector
import com.agrotech.app.ui.lote.LoteDetalheViewModel
import com.agrotech.app.ui.theme.GreenPrimary

private val SEMANAS = (1..8).toList()

@Composable
fun MortalidadeTab(viewModel: LoteDetalheViewModel) {
    val registros by viewModel.mortalidadeRegistros.collectAsStateWithLifecycle()
    val resumos by viewModel.resumoSemanas.collectAsStateWithLifecycle()
    var semanaSelecionada by remember { mutableStateOf(1) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            WeekSelector(
                weeks = SEMANAS,
                selected = semanaSelecionada,
                onSelect = { semanaSelecionada = it }
            )
        }

        val resumoSemana = resumos.find { it.semana == semanaSelecionada }
        if (resumoSemana != null) {
            item {
                AgroTechCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth()) {
                        ResumoValor("Total", "${resumoSemana.total}", Modifier.weight(1f))
                        ResumoValor("% Sem", "${String.format("%.2f", resumoSemana.percentualSemana * 100)}%", Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        ResumoValor("% Acum", "${String.format("%.2f", resumoSemana.percentualAcumulado * 100)}%", Modifier.weight(1f))
                        ResumoValor("Saldo", "${resumoSemana.saldo}", Modifier.weight(1f), destaque = true)
                    }
                }
            }
        }

        items(DiaSemana.entries, key = { "${semanaSelecionada}_${it.name}" }) { dia ->
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

@Composable
private fun ResumoValor(label: String, valor: String, modifier: Modifier = Modifier, destaque: Boolean = false) {
    Column(modifier = modifier) {
        SectionLabel(label)
        Text(
            valor,
            style = MaterialTheme.typography.titleLarge,
            color = if (destaque) GreenPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 2.dp)
        )
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

    AgroTechCard(modifier = Modifier.fillMaxWidth()) {
        Text(dia.label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AgroTechTextField(
                value = mortalidade,
                onValueChange = { mortalidade = it },
                label = { Text("Mort.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            AgroTechTextField(
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
                Icon(Icons.Filled.Check, contentDescription = "Salvar dia", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
