package com.agrotech.app.ui.lote

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.ScrollableTabRow
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
import com.agrotech.app.ui.theme.GreenPrimary

// A aba "Mortalidade" do mockup original foi renomeada para "Controle" no
// redesign — o controle semanal de mortalidade e descarte é o coração da
// ficha técnica de frango de corte, então a aba usa esse nome mais curto.
// O "Dashboard" da Home vira só "Dash" aqui dentro, pra caber no tab bar
// sem truncar e não competir com a topbar.
private val ABAS = listOf("Dash", "Controle", "Ração", "Peso")

/**
 * Sub-tela: detalhe de um lote (com abas Dash/Controle/Ração/Peso).
 *
 * **Não tem `Scaffold` próprio** — é conteúdo dentro do
 * [com.agrotech.app.ui.main.MainScaffold], que já provê bottom bar
 * e `containerColor`. O `TopAppBar` é só visual (sem bottom bar).
 */
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
    var abaSelecionada by rememberSaveable { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        lote?.let { "Lote ${it.numeroLote}" } ?: "Lote",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    lote?.let {
                        Text(
                            "${it.linhagem} · ${it.qtdAves} aves",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

        ScrollableTabRow(
            edgePadding = 12.dp,
            selectedTabIndex = abaSelecionada,
            containerColor = MaterialTheme.colorScheme.surface,
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
                        .background(MaterialTheme.colorScheme.outlineVariant)
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
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
