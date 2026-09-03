package com.agrotech.app.ui.lote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.agrotech.app.ui.theme.HairlineLight
import com.agrotech.app.ui.theme.InkLight
import com.agrotech.app.ui.theme.MutedTextLight
import com.agrotech.app.ui.theme.SurfaceLight

// A aba "Mortalidade" do mockup original foi renomeada para "Controle" no
// redesign — o controle semanal de mortalidade e descarte é o coração da
// ficha técnica de frango de corte, então a aba usa esse nome mais curto.
// O "Dashboard" da Home vira só "Dash" aqui dentro, pra caber no tab bar
// sem truncar e não competir com a topbar.
private val ABAS = listOf("Dash", "Controle", "Ração", "Peso")

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
                title = {
                    Column {
                        Text(
                            lote?.let { "Lote ${it.numeroLote}" } ?: "Lote",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkLight
                        )
                        lote?.let {
                            Text(
                                "${it.linhagem} · ${it.qtdAves} aves",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedTextLight
                            )
                        }
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
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = abaSelecionada,
                containerColor = SurfaceLight,
                contentColor = GreenPrimary,
                indicator = { positions ->
                    if (abaSelecionada < positions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(positions[abaSelecionada]),
                            height = 3.dp,
                            color = GreenPrimary
                        )
                    }
                },
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HairlineLight)
                    )
                }
            ) {
                ABAS.forEachIndexed { indice, titulo ->
                    Tab(
                        selected = abaSelecionada == indice,
                        onClick = { abaSelecionada = indice },
                        text = {
                            // Texto menor (labelLarge em vez de titleSmall) para
                            // não competir visualmente com o título da topbar.
                            Text(
                                titulo,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (abaSelecionada == indice) FontWeight.SemiBold else FontWeight.Medium
                            )
                        },
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
