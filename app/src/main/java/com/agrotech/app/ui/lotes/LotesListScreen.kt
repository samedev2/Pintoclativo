package com.agrotech.app.ui.lotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agrotech.app.data.local.entities.LoteEntity
import com.agrotech.app.domain.calculo.RacaoCalculator
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.components.EntityListCard
import com.agrotech.app.ui.theme.CanvasLight
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.InkLight
import com.agrotech.app.ui.theme.MutedTextLight
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
        containerColor = CanvasLight,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            unidadeNome,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkLight
                        )
                        Text(
                            "Lotes da unidade",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTextLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = InkLight
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
                onClick = aoAbrirNovoLote,
                containerColor = GreenPrimary,
                contentColor = Color.White
            ) {
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
                Text("Nenhum lote cadastrado nesta unidade.", color = MutedTextLight)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lotes, key = { it.id }) { lote ->
                    val semanaAtual = RacaoCalculator.semanaAtual(lote.dataAlojamento)
                    EntityListCard(
                        icon = Icons.Filled.Inventory2,
                        title = "Lote ${lote.numeroLote}",
                        subtitle = "${lote.linhagem} · ${lote.genero.label} · ${lote.qtdAves} aves\n" +
                            "Alojado em ${formatoData.format(java.util.Date(lote.dataAlojamento))}",
                        badge = "Semana $semanaAtual em curso",
                        onClick = { aoAbrirLote(lote) }
                    )
                }
            }
        }
    }
}
