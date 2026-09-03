package com.agrotech.app.ui.splash

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.agrotech.app.R

/**
 * Tela de abertura do app. Toca o vídeo `R.raw.agro_intro` em tela cheia
 * (sem controles) e, ao terminar, chama [aoTerminar] para o NavHost
 * navegar para a primeira tela do app.
 *
 * O `VideoView` é embrulhado em `AndroidView` porque ainda não existe um
 * equivalente Compose oficial. A composição é descartada assim que o
 * callback [aoTerminar] é disparado, então não precisamos nos preocupar
 * com vazamento de recursos — o `DisposableEffect` abaixo garante que o
 * vídeo seja parado se a tela sair de cena antes de terminar.
 */
@Composable
fun SplashScreen(aoTerminar: () -> Unit) {
    val context = LocalContext.current
    val videoViewRef = remember { mutableListOf<VideoView?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val uri = Uri.parse(
                        "android.resource://${ctx.packageName}/${R.raw.agro_intro}"
                    )
                    setVideoURI(uri)
                    setZOrderOnTop(false)
                    setOnPreparedListener { mp ->
                        mp.isLooping = false
                        mp.setVolume(0f, 0f)
                        start()
                    }
                    setOnCompletionListener { aoTerminar() }
                    setOnErrorListener { _, what, extra ->
                        // Se o vídeo falhar (codec ausente, arquivo corrompido),
                        // não trava o app — segue direto para a próxima tela.
                        aoTerminar()
                        true
                    }
                    videoViewRef.add(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            videoViewRef.firstOrNull()?.let { v ->
                if (v.isPlaying) v.stopPlayback()
            }
        }
    }
}
