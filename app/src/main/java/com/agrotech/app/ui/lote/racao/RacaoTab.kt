package com.agrotech.app.ui.lote.racao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.agrotech.app.navigation.Rotas
import com.agrotech.app.ui.lote.LoteDetalheViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RacaoTab(
    loteId: Long,
    viewModel: LoteDetalheViewModel,
    navController: NavController
) {
    val recebimentos by viewModel.recebimentos.collectAsStateWithLifecycle()
    val formatoData = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Rotas.novoRecebimento(loteId)) }) {
                Icon(Icons.Filled.Add, contentDescription = "Novo recebimento")
            }
        }
    ) { padding ->
        if (recebimentos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nenhum recebimento de ração registrado.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, padding.calculateTopPadding() + 8.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recebimentos, key = { it.id }) { recebimento ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${formatoData.format(Date(recebimento.data))} · Nota ${recebimento.numeroNota}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "${recebimento.tipoRacao.label} · ${recebimento.quantidadeKg} kg",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { viewModel.removerRecebimento(recebimento) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remover")
                            }
                        }
                    }
                }
            }
        }
    }
}
