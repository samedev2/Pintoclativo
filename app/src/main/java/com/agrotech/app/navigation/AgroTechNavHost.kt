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
import com.agrotech.app.ui.checkin.InstrucoesCheckinScreen
import com.agrotech.app.ui.checkin.SelfieCheckinScreen
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.lote.LoteDetalheScreen
import com.agrotech.app.ui.lote.LoteDetalheViewModel
import com.agrotech.app.ui.lote.racao.NovoRecebimentoScreen
import com.agrotech.app.ui.login.LoginScreen
import com.agrotech.app.ui.lotes.LotesListScreen
import com.agrotech.app.ui.lotes.NovoLoteScreen
import com.agrotech.app.ui.ocr.OcrCameraScreen
import com.agrotech.app.ui.splash.SplashScreen
import com.agrotech.app.ui.unidades.UnidadesListScreen

@Composable
fun AgroTechNavHost() {
    val navController = rememberNavController()
    val container = rememberAppContainer()

    NavHost(navController = navController, startDestination = Rotas.SPLASH) {

        composable(Rotas.SPLASH) {
            SplashScreen(
                destino = { emailLogado ->
                    val proxima = when {
                        emailLogado == null -> Rotas.LOGIN
                        else -> Rotas.checkin(emailLogado)
                    }
                    navController.navigate(proxima) {
                        popUpTo(Rotas.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Rotas.LOGIN) {
            LoginScreen(
                aoLoginSucesso = { email ->
                    // Decide destino pós-login baseado no contador de
                    // logins: 5 primeiros veem a tela de instruções; do
                    // 6º em diante, vai direto pra câmera do check-in.
                    val destino = if (container.sessionManager.mostrarInstrucoesCheckin()) {
                        Rotas.instrucoesCheckin(email)
                    } else {
                        Rotas.checkin(email)
                    }
                    navController.navigate(destino) {
                        popUpTo(Rotas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Rotas.INSTRUCOES_CHECKIN,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
            InstrucoesCheckinScreen(
                email = email,
                aoIniciarCheckin = {
                    navController.navigate(Rotas.checkin(email)) {
                        popUpTo(Rotas.INSTRUCOES_CHECKIN) { inclusive = true }
                    }
                },
                aoVoltar = {
                    // Volta pro login (encerra sessão). O user pode
                    // ter chegado aqui e desistido.
                    container.sessionManager.encerrarSessao()
                    navController.navigate(Rotas.LOGIN) {
                        popUpTo(Rotas.INSTRUCOES_CHECKIN) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Rotas.CHECKIN,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = Uri.decode(backStackEntry.arguments?.getString("email") ?: "")
            SelfieCheckinScreen(
                email = email,
                aoAprovado = {
                    navController.navigate(Rotas.UNIDADES) {
                        popUpTo(Rotas.CHECKIN) { inclusive = true }
                    }
                },
                aoSair = {
                    container.sessionManager.encerrarSessao()
                    navController.navigate(Rotas.LOGIN) {
                        popUpTo(Rotas.CHECKIN) { inclusive = true }
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
