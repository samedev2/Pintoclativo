package com.agrotech.app.ui.lotes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.agrotech.app.data.local.entities.UnidadeEntity
import com.agrotech.app.domain.calculo.RacaoCalculator
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.components.EntityListCard
import com.agrotech.app.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Conteúdo da aba "Lotes" (ou sub-tela de lotes de uma unidade).
 *
 * Dois modos de uso:
 * - `unidadeId == 0L` → raiz da aba Lotes: lista de **unidades**.
 * - `unidadeId > 0L` → sub-rota `LOTES`: lista de **lotes** dessa
 *   unidade.
 *
 * **Não tem `Scaffold` próprio** — é conteúdo dentro do
 * [com.agrotech.app.ui.main.MainScaffold], que já provê bottom bar
 * e `containerColor`. O FAB de "+" fica posicionado dentro do Box
 * (BottomEnd com padding) e só aparece na sub-rota de lotes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotesListScreen(
    unidadeId: Long,
    unidadeNome: String,
    aoVoltar: () -> Unit,
    aoAbrirNovoLote: () -> Unit,
    aoAbrirLote: (LoteEntity) -> Unit
) {
    if (unidadeId == 0L) {
        LotesAbaRoot()
    } else {
        LotesDaUnidade(
            unidadeId = unidadeId,
            unidadeNome = unidadeNome,
            aoVoltar = aoVoltar,
            aoAbrirNovoLote = aoAbrirNovoLote,
            aoAbrirLote = aoAbrirLote
        )
    }
}

/** Raiz da aba Lotes: lista as unidades cadastradas. Toca numa
 *  unidade → [aoAbrirUnidade] (callback que navega pra sub-rota). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotesAbaRoot(
    aoAbrirUnidade: (UnidadeEntity) -> Unit = {}
) {
    val container = rememberAppContainer()
    val unidades by container.unidadeRepository.observarUnidades()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        "Lotes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Selecione uma granja",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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

        if (unidades.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma unidade cadastrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(unidades, key = { it.id }) { unidade ->
                    EntityListCard(
                        icon = Icons.Filled.Inventory2,
                        title = unidade.nome,
                        subtitle = "Controle técnico de frango de corte",
                        onClick = { aoAbrirUnidade(unidade) }
                    )
                }
            }
        }
    }
}

/** Sub-rota de Lotes: lista os lotes de uma unidade específica. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotesDaUnidade(
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

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        unidadeNome,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Lotes da unidade",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = aoVoltar) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
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

        Box(modifier = Modifier.fillMaxSize()) {
            if (lotes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Nenhum lote cadastrado nesta unidade.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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

            FloatingActionButton(
                onClick = aoAbrirNovoLote,
                containerColor = GreenPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(56.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Novo lote")
            }
        }
    }
}
