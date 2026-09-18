package com.agrotech.app.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.agrotech.app.ui.common.rememberAppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Tela de abertura minimalista. Decide o destino **sem vídeo**:
 *  - Sem sessão → LOGIN
 *  - Com sessão → MAIN (entra direto, sem check-in)
 *
 * O `destinoChamado` (object anônimo) protege contra re-entrância
 * caso o `LaunchedEffect` re-execute por causa das chaves mudando.
 *
 * Pré-aquece o `userDao` e `SessionManager` em background pra
 * evitar ANR de 5s+ em devices com base legada (a primeira chamada
 * lazy do `prefs` no `SessionManager` faz I/O bloqueante).
 */
@Composable
fun SplashScreen(
    destino: (email: String?) -> Unit
) {
    val container = rememberAppContainer()
    val destinoChamado = remember { object { var feito = false } }

    LaunchedEffect(Unit) {
        val email = try {
            withContext(Dispatchers.IO) {
                container.userDao.hashCode()
                container.sessionManager.hashCode()
                container.authRepository.sessaoAtiva()
            }
        } catch (e: Throwable) {
            null
        }
        if (!destinoChamado.feito) {
            destinoChamado.feito = true
            destino(email)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    )
}
