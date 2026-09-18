package com.agrotech.app.ui.lote.peso

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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agrotech.app.data.local.entities.CheckpointPeso
import com.agrotech.app.data.local.entities.PesagemEntity
import com.agrotech.app.ui.components.AgroTechCard
import com.agrotech.app.ui.components.AgroTechTextField
import com.agrotech.app.ui.components.IconChip
import com.agrotech.app.ui.lote.LoteDetalheViewModel
import com.agrotech.app.ui.theme.GreenPrimary

@Composable
fun PesoTab(viewModel: LoteDetalheViewModel) {
    val pesagens by viewModel.pesagens.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(CheckpointPeso.entries, key = { it.name }) { checkpoint ->
            val registro = pesagens.find { it.checkpoint == checkpoint }
            LinhaPeso(
                checkpoint = checkpoint,
                registro = registro,
                aoSalvar = { pesoKg -> viewModel.salvarPeso(checkpoint, pesoKg) }
            )
        }
    }
}

@Composable
private fun LinhaPeso(
    checkpoint: CheckpointPeso,
    registro: PesagemEntity?,
    aoSalvar: (Double) -> Unit
) {
    var pesoTexto by remember(registro) { mutableStateOf(registro?.pesoKg?.toString() ?: "") }
    val lancado = registro != null
    val corConteudo = if (lancado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant

    AgroTechCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconChip(
                icon = Icons.Filled.Schedule,
                tint = if (lancado) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                background = if (lancado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                // labelSmall em vez de titleSmall para não competir com a aba.
                Text(
                    checkpoint.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = corConteudo,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                Text(
                    if (lancado) "Peso registrado" else "Aguardando pesagem",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            AgroTechTextField(
                value = pesoTexto,
                onValueChange = { pesoTexto = it },
                label = { Text("Peso (kg)") },
                placeholder = {
                    Text(
                        "— kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                pesoTexto.replace(",", ".").toDoubleOrNull()?.let { aoSalvar(it) }
            }) {
                Icon(Icons.Filled.Check, contentDescription = "Salvar peso", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
