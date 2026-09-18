package com.agrotech.app.ui.lotes

import com.agrotech.app.ui.components.AdaptivePair
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agrotech.app.data.local.entities.Genero
import com.agrotech.app.data.local.entities.LoteEntity
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.components.AgroTechTextField
import com.agrotech.app.ui.components.SectionLabel
import com.agrotech.app.ui.components.SegmentedControl
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.PillShape
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Sub-tela: criar um novo lote dentro de uma unidade.
 *
 * **Não tem `Scaffold` próprio** — é conteúdo dentro do
 * [com.agrotech.app.ui.main.MainScaffold], que já provê bottom bar
 * e `containerColor`. O botão "Salvar lote" fica num footer
 * fixo (Surface) que sempre aparece no rodapé, com o formulário
 * scrollando acima dele.
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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

    var numeroLote by rememberSaveable { mutableStateOf("") }
    var genero by rememberSaveable { mutableStateOf(Genero.MACHO) }
    var metragem by rememberSaveable { mutableStateOf("") }
    var qtdAves by rememberSaveable { mutableStateOf("") }
    var linhagem by rememberSaveable { mutableStateOf("") }
    var dataAlojamentoMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var mostrarSeletorData by rememberSaveable { mutableStateOf(false) }
    var densidade by rememberSaveable { mutableStateOf("") }
    var densidadeEditadaManualmente by rememberSaveable { mutableStateOf(false) }
    var pesoInicial by rememberSaveable { mutableStateOf("") }
    var percMortTransp by rememberSaveable { mutableStateOf("0") }
    var diasVazio by rememberSaveable { mutableStateOf("0") }
    var distribuicaoLote by rememberSaveable { mutableStateOf("") }

    val formatoData = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    // Densidade = qtd aves / metragem, recalculada automaticamente até o usuário editá-la na mão.
    val metragemValor = metragem.replace(",", ".").toDoubleOrNull()
    val qtdAvesValor = qtdAves.toIntOrNull()
    if (!densidadeEditadaManualmente && metragemValor != null && metragemValor > 0 && qtdAvesValor != null) {
        val calculada = qtdAvesValor / metragemValor
        val formatada = String.format(Locale("pt", "BR"), "%.2f", calculada)
        if (densidade != formatada) densidade = formatada
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Novo lote") },
            navigationIcon = {
                IconButton(onClick = aoVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface
            ),
            windowInsets = WindowInsets.statusBars
        )

        Column(modifier = Modifier.weight(1f)) {
            // Formulário scrollável
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CampoComLabel("N° do lote") {
                    AgroTechTextField(
                        value = numeroLote,
                        onValueChange = { numeroLote = it },
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

                AdaptivePair {
                    CampoComLabel("Metragem (m²)", modifier = Modifier.weight(1f)) {
                        AgroTechTextField(
                            value = metragem,
                            onValueChange = { metragem = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    CampoComLabel("Qtd. de aves", modifier = Modifier.weight(1f)) {
                        AgroTechTextField(
                            value = qtdAves,
                            onValueChange = { qtdAves = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                CampoComLabel("Linhagem") {
                    AgroTechTextField(
                        value = linhagem,
                        onValueChange = { linhagem = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                CampoComLabel("Data de alojamento") {
                    AgroTechTextField(
                        value = formatoData.format(java.util.Date(dataAlojamentoMillis)),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            TextButton(onClick = { mostrarSeletorData = true }) {
                                Text("Alterar", color = GreenPrimary)
                            }
                        }
                    )
                }

                AdaptivePair {
                    CampoComLabel("Densidade (aves/m²)", modifier = Modifier.weight(1f)) {
                        AgroTechTextField(
                            value = densidade,
                            onValueChange = {
                                densidade = it
                                densidadeEditadaManualmente = true
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    CampoComLabel("Peso inicial (kg)", modifier = Modifier.weight(1f)) {
                        AgroTechTextField(
                            value = pesoInicial,
                            onValueChange = { pesoInicial = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                AdaptivePair {
                    CampoComLabel("% mort. transporte", modifier = Modifier.weight(1f)) {
                        AgroTechTextField(
                            value = percMortTransp,
                            onValueChange = { percMortTransp = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    CampoComLabel("Vazio (dias)", modifier = Modifier.weight(1f)) {
                        AgroTechTextField(
                            value = diasVazio,
                            onValueChange = { diasVazio = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                CampoComLabel("Distribuição do lote") {
                    AgroTechTextField(
                        value = distribuicaoLote,
                        onValueChange = { distribuicaoLote = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Espaçador pro footer não cobrir o último campo

            }

            // Footer fixo com botão "Salvar lote"
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
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
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) {
                        Text("Salvar lote", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    }
                }
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
