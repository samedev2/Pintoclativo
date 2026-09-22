package com.agrotech.app.ui.main

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agrotech.app.navigation.Rotas
import com.agrotech.app.ui.common.AbaNav
import com.agrotech.app.ui.common.BottomNavBar
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.fotos.CameraAoVivoScreen
import com.agrotech.app.ui.fotos.FotosScreen
import com.agrotech.app.ui.inicio.InicioScreen
import com.agrotech.app.ui.lancamentos.FechamentoDiarioScreen
import com.agrotech.app.ui.lancamentos.LancamentoSalvoScreen
import com.agrotech.app.ui.lancamentos.LancamentosScreen
import com.agrotech.app.ui.lancamentos.RecebimentoRacaoScreen
import com.agrotech.app.ui.mais.MaisScreen
import com.agrotech.app.ui.lote.LoteDetalheScreen
import com.agrotech.app.ui.lote.LoteDetalheViewModel
import com.agrotech.app.ui.lote.racao.NovoRecebimentoScreen
import com.agrotech.app.ui.lotes.NovoLoteScreen
import com.agrotech.app.ui.ocr.OcrCameraScreen
import com.agrotech.app.ui.perfil.PerfilScreen
import com.agrotech.app.ui.relatorios.RelatoriosScreen

/**
 * Tela principal do app autenticado: modelo de **4 abas fixas**
 * (Início, Lotes, Lançamentos, Mais) com bottom bar sempre visível.
 *
 * Cada aba é um destino raiz do `NavHost` interno. As sub-telas
 * (ex.: `LoteDetalheScreen` dentro da aba Lotes) ficam na mesma
 * pilha, então o back do hardware volta pra raiz da aba antes de
 * tentar sair do app.
 *
 * A câmera ao vivo, a foto pelo celular, os relatórios, o scanner e o perfil não são abas — são
 * telas empilhadas por cima, abertas a partir do Início ou da aba Mais.
 *
 * O check-in com selfie foi removido do app. O `aoAbrirCheckin`
 * é mantido na assinatura pra não quebrar a chamada do NavHost,
 * mas é um no-op.
 *
 * @param aoAbrirCheckin [obsoleto] callback do check-in — não é
 *   mais invocado por nenhuma tela. Mantido só pra evitar mudar
 *   a assinatura do NavHost.
 * @param aoSair chamado quando o user pede logout (botão no Perfil).
 *   Deve encerrar a sessão e voltar pro LOGIN no NavController raiz.
 */
@Composable
fun MainScaffold(
    aoAbrirCheckin: (String) -> Unit,
    aoSair: () -> Unit
) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination?.route
    val abaAtual = AbaNav.daRota(rotaAtual)

    // Navega pra raiz da aba. `popUpTo(startDestination)` + `saveState` + `restoreState`
    // faz cada aba manter seu próprio back stack — trocar de aba não acumula histórico.
    val irParaAba: (AbaNav) -> Unit = { aba ->
        navController.navigate(aba.rota) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                abaAtual = abaAtual,
                aoSelecionar = irParaAba
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            NavHost(
                modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                navController = navController,
                startDestination = AbaNav.INICIO.rota
            ) {
                // === Aba Início ===
                composable(AbaNav.INICIO.rota) {
                    InicioScreen(
                        aoAbrirLotes = { navController.navigate(Rotas.LOTES_RAIZ) },
                        aoAbrirRelatorios = { navController.navigate(Rotas.RELATORIOS_RAIZ) },
                        aoAbrirPerfil = { navController.navigate(Rotas.PERFIL) },
                        aoAbrirCameraAoVivo = { navController.navigate(Rotas.CAMERA_AO_VIVO) }
                    )
                }

                // === Câmera ao vivo (GranjaCam), aberta pelo card do Início ===
                composable(Rotas.CAMERA_AO_VIVO) {
                    CameraAoVivoScreen(aoVoltar = { navController.popBackStack() })
                }

                // === Aba Lançamentos: hub com fechamento diário e recebimento de ração ===
                composable(AbaNav.LANCAMENTOS.rota) {
                    LancamentosScreen(
                        aoAbrirFechamento = { navController.navigate(Rotas.FECHAMENTO_DIARIO) },
                        aoAbrirRecebimento = { navController.navigate(Rotas.RECEBIMENTO_RACAO) }
                    )
                }
                composable(Rotas.FECHAMENTO_DIARIO) {
                    FechamentoDiarioScreen(
                        aoVoltar = { navController.popBackStack() },
                        aoSalvar = {
                            navController.navigate(Rotas.LANCAMENTO_SALVO) {
                                popUpTo(Rotas.FECHAMENTO_DIARIO) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Rotas.RECEBIMENTO_RACAO) {
                    RecebimentoRacaoScreen(
                        aoVoltar = { navController.popBackStack() },
                        aoSalvar = {
                            navController.navigate(Rotas.LANCAMENTO_SALVO) {
                                popUpTo(Rotas.RECEBIMENTO_RACAO) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Rotas.LANCAMENTO_SALVO) {
                    LancamentoSalvoScreen(
                        aoVoltarAoLote = {
                            navController.popBackStack(AbaNav.LANCAMENTOS.rota, inclusive = false)
                        }
                    )
                }

                // === Aba Mais (fotos, desempenho, relatórios, scanner, perfil) ===
                composable(AbaNav.MAIS.rota) {
                    MaisScreen(
                        aoAbrirFotos = { navController.navigate(Rotas.FOTO_CELULAR) },
                        aoAbrirDesempenho = { navController.navigate(Rotas.RELATORIOS_RAIZ) },
                        aoAbrirRelatorios = { navController.navigate(Rotas.RELATORIOS_RAIZ) },
                        aoAbrirScanner = { navController.navigate(Rotas.SCANNER) { launchSingleTop = true } },
                        aoAbrirPerfil = { navController.navigate(Rotas.PERFIL) }
                    )
                }

                // === Foto da granja pelo celular (câmera/galeria) ===
                composable(Rotas.FOTO_CELULAR) {
                    FotosScreen(aoVoltar = { navController.popBackStack() })
                }

                // === Aba Lotes — raiz: lista de unidades ===
                composable(Rotas.LOTES_RAIZ) {
                    com.agrotech.app.ui.lotes.LotesAbaRoot(
                        aoAbrirUnidade = { unidade ->
                            navController.navigate(Rotas.lotes(unidade.id, unidade.nome))
                        }
                    )
                }

                // === Aba Lotes — detalhe de uma unidade (com lista de lotes) ===
                composable(
                    route = Rotas.LOTES,
                    arguments = listOf(
                        navArgument("unidadeId") { type = NavType.LongType },
                        navArgument("unidadeNome") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val unidadeId = backStackEntry.arguments?.getLong("unidadeId") ?: 0L
                    val unidadeNome = java.net.URLDecoder.decode(
                        backStackEntry.arguments?.getString("unidadeNome") ?: "",
                        "UTF-8"
                    )
                    com.agrotech.app.ui.lotes.LotesDaUnidade(
                        unidadeId = unidadeId,
                        unidadeNome = unidadeNome,
                        aoVoltar = { navController.popBackStack() },
                        aoAbrirNovoLote = { navController.navigate(Rotas.novoLote(unidadeId)) },
                        aoAbrirLote = { lote -> navController.navigate(Rotas.loteDetalhe(lote.id)) }
                    )
                }

                // === Sub-tela: novo lote ===
                composable(
                    route = Rotas.NOVO_LOTE,
                    arguments = listOf(navArgument("unidadeId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val unidadeId = backStackEntry.arguments?.getLong("unidadeId") ?: 0L
                    NovoLoteScreen(
                        unidadeId = unidadeId,
                        aoVoltar = { navController.popBackStack() },
                        aoSalvar = { loteId ->
                            navController.navigate(Rotas.loteDetalhe(loteId)) {
                                popUpTo(Rotas.LOTES_RAIZ) { inclusive = false }
                            }
                        }
                    )
                }

                // === Sub-tela: detalhe do lote (com abas Dash/Controle/Ração/Peso) ===
                composable(
                    route = Rotas.LOTE_DETALHE,
                    arguments = listOf(navArgument("loteId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val loteId = backStackEntry.arguments?.getLong("loteId") ?: 0L
                    LoteDetalheScreen(
                        loteId = loteId,
                        navController = navController,
                        aoVoltar = { navController.popBackStack() }
                    )
                }

                // === Sub-tela: novo recebimento de ração (do detalhe do lote) ===
                composable(
                    route = Rotas.NOVO_RECEBIMENTO,
                    arguments = listOf(navArgument("loteId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val loteId = backStackEntry.arguments?.getLong("loteId") ?: 0L
                    val containerLocal = rememberAppContainer()
                    val viewModel: LoteDetalheViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer {
                                LoteDetalheViewModel(
                                    loteId = loteId,
                                    loteRepository = containerLocal.loteRepository,
                                    mortalidadeRepository = containerLocal.mortalidadeRepository,
                                    racaoRepository = containerLocal.racaoRepository,
                                    pesagemRepository = containerLocal.pesagemRepository
                                )
                            }
                        }
                    )
                    NovoRecebimentoScreen(
                        backStackEntry = backStackEntry,
                        navController = navController,
                        viewModel = viewModel,
                        aoVoltar = { navController.popBackStack() }
                    )
                }

                // === Sub-tela: câmera OCR (lê NF pra ração) ===
                composable(Rotas.OCR_CAMERA) {
                    OcrCameraScreen(navController = navController)
                }

                // Scanner central de demonstração: QR Code ou OCR de NF.
                composable(Rotas.SCANNER) {
                    OcrCameraScreen(navController = navController, modoDemonstracao = true)
                }

                // === Relatórios (acessados pela aba Mais) ===
                composable(Rotas.RELATORIOS_RAIZ) {
                    RelatoriosScreen(
                        aoAbrirRelatorio = { tipo ->
                            navController.navigate(Rotas.relatorioDetalhe(tipo.chave))
                        }
                    )
                }

                // === Sub-tela: detalhe de um relatório ===
                composable(
                    route = Rotas.RELATORIO_DETALHE,
                    arguments = listOf(navArgument("tipo") { type = NavType.StringType })
                ) { backStackEntry ->
                    val tipo = backStackEntry.arguments?.getString("tipo") ?: ""
                    com.agrotech.app.ui.relatorios.RelatorioDetalheScreen(
                        tipo = tipo,
                        aoVoltar = { navController.popBackStack() }
                    )
                }

                // === Perfil ===
                composable(Rotas.PERFIL) {
                    PerfilScreen(aoSair = aoSair)
                }
            }
        }
    }
}
