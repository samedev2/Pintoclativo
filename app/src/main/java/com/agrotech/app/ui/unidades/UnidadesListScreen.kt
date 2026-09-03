package com.agrotech.app.ui.unidades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agrotech.app.data.local.entities.UnidadeEntity
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.components.EntityListCard
import com.agrotech.app.ui.components.IconChip
import com.agrotech.app.ui.components.SectionLabel
import com.agrotech.app.ui.theme.CanvasLight
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.InkLight
import com.agrotech.app.ui.theme.PillShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnidadesListScreen(
    aoAbrirUnidade: (UnidadeEntity) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: UnidadesViewModel = viewModel(
        factory = viewModelFactory {
            initializer { UnidadesViewModel(container.unidadeRepository) }
        }
    )
    val unidades by viewModel.unidades.collectAsStateWithLifecycle()
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CanvasLight,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconChip(
                            icon = Icons.Filled.Agriculture,
                            size = 32.dp,
                            background = GreenPrimary,
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                        Text(
                            "AgroTech",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkLight,
                            modifier = Modifier.padding(start = 10.dp)
                        )
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = GreenPrimary,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nova unidade")
            }
        }
    ) { padding ->
        if (unidades.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nenhuma unidade cadastrada ainda.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SectionLabel("Unidades", modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
                }
                items(unidades, key = { it.id }) { unidade ->
                    EntityListCard(
                        icon = Icons.Filled.Home,
                        title = unidade.nome,
                        subtitle = "Controle técnico de frango de corte",
                        onClick = { aoAbrirUnidade(unidade) }
                    )
                }
            }
        }
    }

    if (mostrarDialogo) {
        var nome by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Nova unidade") },
            text = {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da unidade") },
                    shape = PillShape,
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.criarUnidade(nome)
                    mostrarDialogo = false
                }) { Text("Salvar", color = GreenPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}
