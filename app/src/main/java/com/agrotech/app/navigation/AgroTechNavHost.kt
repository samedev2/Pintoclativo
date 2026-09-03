package com.agrotech.app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.lote.LoteDetalheScreen
import com.agrotech.app.ui.lote.LoteDetalheViewModel
import com.agrotech.app.ui.lote.racao.NovoRecebimentoScreen
import com.agrotech.app.ui.lotes.LotesListScreen
import com.agrotech.app.ui.lotes.NovoLoteScreen
import com.agrotech.app.ui.ocr.OcrCameraScreen
import com.agrotech.app.ui.splash.SplashScreen
import com.agrotech.app.ui.unidades.UnidadesListScreen

@Composable
fun AgroTechNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Rotas.SPLASH) {

        composable(Rotas.SPLASH) {
            SplashScreen(
                aoTerminar = {
                    navController.navigate(Rotas.UNIDADES) {
                        // Remove a splash do backstack — o usuário não deve
                        // conseguir voltar pra ela com o botão "voltar".
                        popUpTo(Rotas.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Rotas.UNIDADES) {
            UnidadesListScreen(
                aoAbrirUnidade = { unidade ->
                    navController.navigate(Rotas.lotes(unidade.id, unidade.nome))
                }
            )
        }

        composable(
            route = Rotas.LOTES,
            arguments = listOf(
                navArgument("unidadeId") { type = NavType.LongType },
                navArgument("unidadeNome") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val unidadeId = backStackEntry.arguments?.getLong("unidadeId") ?: 0L
            val unidadeNome = Uri.decode(backStackEntry.arguments?.getString("unidadeNome") ?: "")
            LotesListScreen(
                unidadeId = unidadeId,
                unidadeNome = unidadeNome,
                aoVoltar = { navController.popBackStack() },
                aoAbrirNovoLote = { navController.navigate(Rotas.novoLote(unidadeId)) },
                aoAbrirLote = { lote -> navController.navigate(Rotas.loteDetalhe(lote.id)) }
            )
        }

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
                        popUpTo(Rotas.LOTES) { inclusive = false }
                    }
                }
            )
        }

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

        composable(
            route = Rotas.NOVO_RECEBIMENTO,
            arguments = listOf(navArgument("loteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val loteId = backStackEntry.arguments?.getLong("loteId") ?: 0L
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
            NovoRecebimentoScreen(
                backStackEntry = backStackEntry,
                navController = navController,
                viewModel = viewModel,
                aoVoltar = { navController.popBackStack() }
            )
        }

        composable(Rotas.OCR_CAMERA) {
            OcrCameraScreen(navController = navController)
        }
    }
}
