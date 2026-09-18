package com.agrotech.app.ui.lote.racao

import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.agrotech.app.data.local.entities.TipoRacao
import com.agrotech.app.ui.components.AgroTechTextField
import com.agrotech.app.ui.lote.LoteDetalheViewModel
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.PillShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Sub-tela: criar um novo recebimento de ração para um lote.
 *
 * **Não tem `Scaffold` próprio** — é conteúdo dentro do
 * [com.agrotech.app.ui.main.MainScaffold], que já provê bottom bar
 * e `containerColor`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoRecebimentoScreen(
    backStackEntry: NavBackStackEntry,
    navController: NavController,
    viewModel: LoteDetalheViewModel,
    aoVoltar: () -> Unit
) {
    var dataMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var mostrarSeletorData by rememberSaveable { mutableStateOf(false) }
    var numeroNota by rememberSaveable { mutableStateOf("") }
    var tipoRacao by rememberSaveable { mutableStateOf(TipoRacao.PRE_INICIAL) }
    var tipoMenuAberto by rememberSaveable { mutableStateOf(false) }
    var quantidadeKg by rememberSaveable { mutableStateOf("") }

    val formatoData = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    // Os resultados do OCR são salvos pela OcrCameraScreen no savedStateHandle
    // do entry anterior da pilha. Quando o usuário clica em "Ler NF pela câmera"
    // direto da aba Ração, o entry anterior é o LoteDetalheScreen (que hospeda
    // a aba). Quando o user clica no "+", o entry anterior também é o
    // LoteDetalheScreen, então lemos do `previousBackStackEntry` para pegar
    // os resultados tirados antes. Fallback pro `backStackEntry` próprio caso
    // o caminho seja outro no futuro.
    val linhasOcr by remember(backStackEntry) {
        val handleOrigem = navController.previousBackStackEntry?.savedStateHandle
            ?: backStackEntry.savedStateHandle
        handleOrigem.getStateFlow("ocr_lines", emptyList<String>())
    }.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Recebimento de ração") },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // O botão "Ler NF pela câmera" não fica mais aqui dentro — ele está
            // no topo da aba Ração (RacaoTab). O OCR é disparado direto de lá
            // e os resultados voltam pelo savedStateHandle do LoteDetalheScreen,
            // ficando disponíveis para este formulário quando o user clica no "+".

            if (linhasOcr.isNotEmpty()) {
                Text("Toque para preencher com o texto reconhecido:", style = MaterialTheme.typography.bodySmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(linhasOcr) { linha ->
                        SuggestionChip(
                            onClick = {
                                if (linha.any { it.isDigit() }) {
                                    if (numeroNota.isBlank()) numeroNota = linha else quantidadeKg = linha
                                }
                            },
                            label = { Text(linha.take(24)) }
                        )
                    }
                }
            }

            AgroTechTextField(
                value = formatoData.format(Date(dataMillis)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Data") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { mostrarSeletorData = true }) { Text("Alterar", color = GreenPrimary) }
                }
            )

            AgroTechTextField(
                value = numeroNota,
                onValueChange = { numeroNota = it },
                label = { Text("N° da nota") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = tipoMenuAberto,
                onExpandedChange = { tipoMenuAberto = it }
            ) {
                // O dropdown usa OutlinedTextField direto (não AgroTechTextField)
                // porque precisa do `menuAnchor` — extension específica do
                // ExposedDropdownMenuBox que não combinaria com nosso wrapper.
                // Mantém o PillShape + as cores via colors= para ficar igual.
                OutlinedTextField(
                    value = tipoRacao.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de ração") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoMenuAberto) },
                    shape = PillShape,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = GreenPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = GreenPrimary,
                        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = tipoMenuAberto,
                    onDismissRequest = { tipoMenuAberto = false }
                ) {
                    TipoRacao.entries.forEach { opcao ->
                        DropdownMenuItem(
                            text = { Text(opcao.label) },
                            onClick = {
                                tipoRacao = opcao
                                tipoMenuAberto = false
                            }
                        )
                    }
                }
            }

            AgroTechTextField(
                value = quantidadeKg,
                onValueChange = { quantidadeKg = it },
                label = { Text("Quantidade (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    viewModel.salvarRecebimento(
                        data = dataMillis,
                        numeroNota = numeroNota,
                        tipo = tipoRacao,
                        quantidadeKg = quantidadeKg.replace(",", ".").toDoubleOrNull() ?: 0.0
                    )
                    // Limpa o cache de OCR no entry anterior pra não persistir
                    // dados de uma foto antiga na próxima vez que o user abrir
                    // o formulário manual.
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.remove<ArrayList<String>>("ocr_lines")
                    aoVoltar()
                },
                enabled = numeroNota.isNotBlank() && quantidadeKg.replace(",", ".").toDoubleOrNull() != null,
                shape = PillShape,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    disabledContainerColor = GreenPrimary.copy(alpha = 0.35f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                Text("Salvar recebimento", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            }
        }
    }

    if (mostrarSeletorData) {
        val estadoData = rememberDatePickerState(initialSelectedDateMillis = dataMillis)
        DatePickerDialog(
            onDismissRequest = { mostrarSeletorData = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoData.selectedDateMillis?.let { dataMillis = it }
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
