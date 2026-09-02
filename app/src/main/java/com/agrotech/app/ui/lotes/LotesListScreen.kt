package com.agrotech.app.ui.lotes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agrotech.app.data.local.entities.LoteEntity
import com.agrotech.app.ui.common.rememberAppContainer
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotesListScreen(
    unidadeId: Long,
    unidadeNome: String,
    aoVoltar: () -> Unit,
    aoAbrirNovoLote: () -> Unit,
    aoAbrirLote: (LoteEntity) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: LotesViewModel = viewModel(
        factory = viewModelFactory {
            initializer { LotesViewModel(unidadeId, container.loteRepository) }
        }
    )
    val lotes by viewModel.lotes.collectAsStateWithLifecycle()
    val formatoData = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(unidadeNome) },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = aoAbrirNovoLote) {
                Icon(Icons.Filled.Add, contentDescription = "Novo lote")
            }
        }
    ) { padding ->
        if (lotes.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nenhum lote cadastrado nesta unidade.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 8.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lotes, key = { it.id }) { lote ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { aoAbrirLote(lote) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Icon(Icons.Filled.Inventory2, contentDescription = null)
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text("Lote ${lote.numeroLote}", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${lote.linhagem} · ${lote.genero.label} · ${lote.qtdAves} aves",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Alojado em ${formatoData.format(java.util.Date(lote.dataAlojamento))}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
