package com.agrotech.app.ui.fotos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.agrotech.app.ui.components.CabecalhoVerde

/**
 * Câmera ao vivo (GranjaCam) em tela cheia normal, aberta pelo card "Acompanhar em tempo real" do
 * Início. É a mesma opção GranjaCam que antes vivia dentro da aba Fotos.
 */
@Composable
fun CameraAoVivoScreen(aoVoltar: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        CabecalhoVerde(titulo = "Acompanhar em tempo real", aoVoltar = aoVoltar)
        GranjaCamPane(Modifier.weight(1f).fillMaxSize())
    }
}
