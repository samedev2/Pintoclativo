package com.agrotech.app.ui.lote.peso

import androidx.compose.foundation.layout.Arrangement
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
import com.agrotech.app.data.local.entities.CheckpointPeso
import com.agrotech.app.data.local.entities.PesagemEntity
import com.agrotech.app.ui.lote.LoteDetalheViewModel

@Composable
fun PesoTab(viewModel: LoteDetalheViewModel) {
    val pesagens by viewModel.pesagens.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            checkpoint.label,
            modifier = Modifier.weight(1f).padding(top = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        OutlinedTextField(
            value = pesoTexto,
            onValueChange = { pesoTexto = it },
            label = { Text("Peso (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        IconButton(onClick = {
            pesoTexto.replace(",", ".").toDoubleOrNull()?.let { aoSalvar(it) }
        }) {
            Icon(Icons.Filled.Check, contentDescription = "Salvar peso")
        }
    }
}
