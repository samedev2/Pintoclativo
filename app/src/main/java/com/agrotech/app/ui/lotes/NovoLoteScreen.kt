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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agrotech.app.data.local.entities.Genero
import com.agrotech.app.data.local.entities.LoteEntity
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.components.SectionLabel
import com.agrotech.app.ui.components.SegmentedControl
import com.agrotech.app.ui.theme.CanvasLight
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.InkLight
import com.agrotech.app.ui.theme.PillShape
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
        containerColor = CanvasLight,
        topBar = {
            TopAppBar(
                title = { Text("Novo lote") },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CanvasLight,
                    titleContentColor = InkLight,
                    navigationIconContentColor = InkLight,
                    actionIconContentColor = InkLight
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
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
                shape = PillShape,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = GreenPrimary.copy(alpha = 0.35f),
                    disabledContentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Salvar lote", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CampoComLabel("N° do lote") {
                OutlinedTextField(
                    value = numeroLote,
                    onValueChange = { numeroLote = it },
                    shape = PillShape,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            CampoComLabel("Gênero") {
                SegmentedControl(
                    options = Genero.entries.map { it.label },
                    selectedIndex = Genero.entries.indexOf(genero),
                    onSelect = { genero = Genero.entries[it] }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoComLabel("Metragem (m²)", modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = metragem,
                        onValueChange = { metragem = it },
                        shape = PillShape,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                CampoComLabel("Qtd. de aves", modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = qtdAves,
                        onValueChange = { qtdAves = it },
                        shape = PillShape,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            CampoComLabel("Linhagem") {
                OutlinedTextField(
                    value = linhagem,
                    onValueChange = { linhagem = it },
                    shape = PillShape,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            CampoComLabel("Data de alojamento") {
                OutlinedTextField(
                    value = formatoData.format(java.util.Date(dataAlojamentoMillis)),
                    onValueChange = {},
                    readOnly = true,
                    shape = PillShape,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = { mostrarSeletorData = true }) {
                            Text("Alterar", color = GreenPrimary)
                        }
                    }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoComLabel("Densidade (aves/m²)", modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = densidade,
                        onValueChange = {
                            densidade = it
                            densidadeEditadaManualmente = true
                        },
                        shape = PillShape,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                CampoComLabel("Peso inicial (kg)", modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = pesoInicial,
                        onValueChange = { pesoInicial = it },
                        shape = PillShape,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CampoComLabel("% mort. transporte", modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = percMortTransp,
                        onValueChange = { percMortTransp = it },
                        shape = PillShape,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                CampoComLabel("Vazio (dias)", modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = diasVazio,
                        onValueChange = { diasVazio = it },
                        shape = PillShape,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            CampoComLabel("Distribuição do lote") {
                OutlinedTextField(
                    value = distribuicaoLote,
                    onValueChange = { distribuicaoLote = it },
                    shape = PillShape,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
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

@Composable
private fun CampoComLabel(
    label: String,
    modifier: Modifier = Modifier,
    campo: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        SectionLabel(label, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
        campo()
    }
}
