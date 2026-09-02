package com.agrotech.app.ui.lotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agrotech.app.data.local.entities.Genero
import com.agrotech.app.data.local.entities.LoteEntity
import com.agrotech.app.ui.common.rememberAppContainer
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoLoteScreen(
    unidadeId: Long,
    aoVoltar: () -> Unit,
    aoSalvar: (Long) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: LotesViewModel = viewModel(
        factory = viewModelFactory {
            initializer { LotesViewModel(unidadeId, container.loteRepository) }
        }
    )

    var numeroLote by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf(Genero.MACHO) }
    var generoMenuAberto by remember { mutableStateOf(false) }
    var metragem by remember { mutableStateOf("") }
    var qtdAves by remember { mutableStateOf("") }
    var linhagem by remember { mutableStateOf("") }
    var dataAlojamentoMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var mostrarSeletorData by remember { mutableStateOf(false) }
    var densidade by remember { mutableStateOf("") }
    var densidadeEditadaManualmente by remember { mutableStateOf(false) }
    var pesoInicial by remember { mutableStateOf("") }
    var percMortTransp by remember { mutableStateOf("0") }
    var diasVazio by remember { mutableStateOf("0") }
    var distribuicaoLote by remember { mutableStateOf("") }

    val formatoData = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    // Densidade = qtd aves / metragem, recalculada automaticamente até o usuário editá-la na mão.
    val metragemValor = metragem.replace(",", ".").toDoubleOrNull()
    val qtdAvesValor = qtdAves.toIntOrNull()
    if (!densidadeEditadaManualmente && metragemValor != null && metragemValor > 0 && qtdAvesValor != null) {
        val calculada = qtdAvesValor / metragemValor
        val formatada = String.format(Locale("pt", "BR"), "%.2f", calculada)
        if (densidade != formatada) densidade = formatada
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo lote") },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = numeroLote,
                onValueChange = { numeroLote = it },
                label = { Text("N° do lote") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = generoMenuAberto,
                onExpandedChange = { generoMenuAberto = it }
            ) {
                OutlinedTextField(
                    value = genero.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gênero") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = generoMenuAberto) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = generoMenuAberto,
                    onDismissRequest = { generoMenuAberto = false }
                ) {
                    Genero.entries.forEach { opcao ->
                        DropdownMenuItem(
                            text = { Text(opcao.label) },
                            onClick = {
                                genero = opcao
                                generoMenuAberto = false
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = metragem,
                    onValueChange = { metragem = it },
                    label = { Text("Metragem (m²)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = qtdAves,
                    onValueChange = { qtdAves = it },
                    label = { Text("Qtd. de aves") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = linhagem,
                onValueChange = { linhagem = it },
                label = { Text("Linhagem") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = formatoData.format(java.util.Date(dataAlojamentoMillis)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Data de alojamento") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { mostrarSeletorData = true }) { Text("Alterar") }
                }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = densidade,
                    onValueChange = {
                        densidade = it
                        densidadeEditadaManualmente = true
                    },
                    label = { Text("Densidade (aves/m²)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = pesoInicial,
                    onValueChange = { pesoInicial = it },
                    label = { Text("Peso inicial (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = percMortTransp,
                    onValueChange = { percMortTransp = it },
                    label = { Text("% mort. transporte") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = diasVazio,
                    onValueChange = { diasVazio = it },
                    label = { Text("Vazio (dias)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = distribuicaoLote,
                onValueChange = { distribuicaoLote = it },
                label = { Text("Distribuição do lote") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    val lote = LoteEntity(
                        unidadeId = unidadeId,
                        numeroLote = numeroLote,
                        genero = genero,
                        metragem = metragemValor ?: 0.0,
                        qtdAves = qtdAvesValor ?: 0,
                        linhagem = linhagem,
                        dataAlojamento = dataAlojamentoMillis,
                        densidade = densidade.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        pesoInicial = pesoInicial.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        percMortTransp = percMortTransp.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        diasVazio = diasVazio.toIntOrNull() ?: 0,
                        distribuicaoLote = distribuicaoLote
                    )
                    viewModel.salvarNovoLote(lote, aoSalvar)
                },
                enabled = numeroLote.isNotBlank() && qtdAvesValor != null && qtdAvesValor > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar lote")
            }
        }
    }

    if (mostrarSeletorData) {
        val estadoData = rememberDatePickerState(initialSelectedDateMillis = dataAlojamentoMillis)
        DatePickerDialog(
            onDismissRequest = { mostrarSeletorData = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoData.selectedDateMillis?.let { dataAlojamentoMillis = it }
                    mostrarSeletorData = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarSeletorData = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoData)
        }
    }
}
