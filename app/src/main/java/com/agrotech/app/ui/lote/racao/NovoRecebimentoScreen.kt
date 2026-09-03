package com.agrotech.app.ui.lote.racao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.agrotech.app.data.local.entities.TipoRacao
import com.agrotech.app.navigation.Rotas
import com.agrotech.app.ui.lote.LoteDetalheViewModel
import com.agrotech.app.ui.theme.CanvasLight
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.HairlineLight
import com.agrotech.app.ui.theme.InkLight
import com.agrotech.app.ui.theme.MutedTextLight
import com.agrotech.app.ui.theme.PillShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoRecebimentoScreen(
    backStackEntry: NavBackStackEntry,
    navController: NavController,
    viewModel: LoteDetalheViewModel,
    aoVoltar: () -> Unit
) {
    var dataMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var mostrarSeletorData by remember { mutableStateOf(false) }
    var numeroNota by remember { mutableStateOf("") }
    var tipoRacao by remember { mutableStateOf(TipoRacao.PRE_INICIAL) }
    var tipoMenuAberto by remember { mutableStateOf(false) }
    var quantidadeKg by remember { mutableStateOf("") }

    val formatoData = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    val linhasOcr by backStackEntry.savedStateHandle
        .getStateFlow("ocr_lines", emptyList<String>())
        .collectAsStateWithLifecycle()

    // Paleta explícita pros campos: tudo mais escuro que o default do
    // Material 3 (que vinha com label/placeholder em cinza-claro demais).
    val coresCampos = OutlinedTextFieldDefaults.colors(
        focusedTextColor = InkLight,
        unfocusedTextColor = InkLight,
        focusedLabelColor = GreenPrimary,
        unfocusedLabelColor = InkLight,
        focusedPlaceholderColor = MutedTextLight,
        unfocusedPlaceholderColor = MutedTextLight,
        focusedBorderColor = GreenPrimary,
        unfocusedBorderColor = HairlineLight,
        cursorColor = GreenPrimary,
        focusedTrailingIconColor = GreenPrimary,
        unfocusedTrailingIconColor = InkLight
    )

    Scaffold(
        containerColor = CanvasLight,
        topBar = {
            TopAppBar(
                title = { Text("Recebimento de ração") },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = CanvasLight,
                    titleContentColor = InkLight,
                    navigationIconContentColor = InkLight,
                    actionIconContentColor = InkLight
                ),
                windowInsets = WindowInsets.statusBars
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
            OutlinedButton(
                onClick = { navController.navigate(Rotas.OCR_CAMERA) },
                shape = PillShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = GreenPrimary)
                Text("  Ler NF pela câmera", color = GreenPrimary)
            }

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

            OutlinedTextField(
                value = formatoData.format(Date(dataMillis)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Data") },
                shape = PillShape,
                colors = coresCampos,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { mostrarSeletorData = true }) { Text("Alterar", color = GreenPrimary) }
                }
            )

            OutlinedTextField(
                value = numeroNota,
                onValueChange = { numeroNota = it },
                label = { Text("N° da nota") },
                shape = PillShape,
                colors = coresCampos,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = tipoMenuAberto,
                onExpandedChange = { tipoMenuAberto = it }
            ) {
                OutlinedTextField(
                    value = tipoRacao.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de ração") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoMenuAberto) },
                    shape = PillShape,
                    colors = coresCampos,
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

            OutlinedTextField(
                value = quantidadeKg,
                onValueChange = { quantidadeKg = it },
                label = { Text("Quantidade (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = PillShape,
                colors = coresCampos,
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
                    aoVoltar()
                },
                enabled = numeroNota.isNotBlank() && quantidadeKg.replace(",", ".").toDoubleOrNull() != null,
                shape = PillShape,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    disabledContainerColor = GreenPrimary.copy(alpha = 0.35f),
                    disabledContentColor = androidx.compose.ui.graphics.Color.White
                ),
                modifier = Modifier.fillMaxWidth()
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
