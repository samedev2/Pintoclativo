package com.agrotech.app.ui.lote

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.lote.dashboard.DashboardTab
import com.agrotech.app.ui.lote.mortalidade.MortalidadeTab
import com.agrotech.app.ui.lote.peso.PesoTab
import com.agrotech.app.ui.lote.racao.RacaoTab
import com.agrotech.app.ui.theme.CanvasLight
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.InkLight
import com.agrotech.app.ui.theme.MutedTextLight
import com.agrotech.app.ui.theme.SurfaceLight

private val ABAS = listOf("Dashboard", "Mortalidade", "Ração", "Peso")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoteDetalheScreen(
    loteId: Long,
    navController: NavController,
    aoVoltar: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: LoteDetalheViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                LoteDetalheViewModel(
                    loteId = loteId,
                    loteRepository = container.loteRepository,
                    mortalidadeRepository = container.mortalidadeRepository,
                    racaoRepository = container.racaoRepository,
                    pesagemRepository = container.pesagemRepository
                )
            }
        }
    )

    val lote by viewModel.lote.collectAsStateWithLifecycle()
    var abaSelecionada by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = CanvasLight,
        topBar = {
            TopAppBar(
                title = { Text(lote?.let { "Lote ${it.numeroLote}" } ?: "Lote") },
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
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = abaSelecionada,
                    containerColor = SurfaceLight,
                    contentColor = GreenPrimary
                ) {
                    ABAS.forEachIndexed { indice, titulo ->
                        Tab(
                            selected = abaSelecionada == indice,
                            onClick = { abaSelecionada = indice },
                            text = { Text(titulo) },
                            selectedContentColor = GreenPrimary,
                            unselectedContentColor = MutedTextLight
                        )
                    }
                }

                when (abaSelecionada) {
                    0 -> DashboardTab(viewModel)
                    1 -> MortalidadeTab(viewModel)
                    2 -> RacaoTab(loteId = loteId, viewModel = viewModel, navController = navController)
                    3 -> PesoTab(viewModel)
                }
            }
        }
    }
}
