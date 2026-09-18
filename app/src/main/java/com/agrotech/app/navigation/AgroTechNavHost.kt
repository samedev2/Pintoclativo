package com.agrotech.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.login.LoginScreen
import com.agrotech.app.ui.main.MainScaffold
import com.agrotech.app.ui.splash.SplashScreen

/**
 * NavHost raiz do app. Fluxo simplificado (sem check-in com selfie):
 *
 *  SPLASH ─► (sem sessão) ─► LOGIN ─► MAIN (4 abas + FAB central desabilitado)
 *  SPLASH ─► (com sessão) ─► MAIN
 *
 * O `aoAbrirCheckin` foi removido — o check-in com selfie não é mais
 * parte do fluxo. O MainScaffold continua com o callback de sair
 * pra voltar pro LOGIN.
 */
@Composable
fun AgroTechNavHost() {
    val navController = rememberNavController()
    val container = rememberAppContainer()

    NavHost(navController = navController, startDestination = Rotas.SPLASH) {

        composable(Rotas.SPLASH) {
            SplashScreen(
                destino = { emailLogado ->
                    val proxima = if (emailLogado == null) {
                        Rotas.LOGIN
                    } else {
                        Rotas.MAIN
                    }
                    navController.navigate(proxima) {
                        popUpTo(Rotas.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Rotas.LOGIN) {
            LoginScreen(
                aoLoginSucesso = {
                    // Login OK → entra direto no Main (sem check-in).
                    navController.navigate(Rotas.MAIN) {
                        popUpTo(Rotas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Rotas.MAIN) {
            MainScaffold(
                aoAbrirCheckin = { /* desabilitado: check-in removido */ },
                aoSair = {
                    container.sessionManager.encerrarSessao()
                    navController.navigate(Rotas.LOGIN) {
                        popUpTo(Rotas.MAIN) { inclusive = true }
                    }
                }
            )
        }
    }
}
