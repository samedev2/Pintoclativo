package com.agrotech.app.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.agrotech.app.ui.theme.DeepGreen

/**
 * Cabeçalho verde das telas internas do novo design (Novo lançamento, Foto da granja):
 * seta de voltar + título, sobre o verde profundo, já com o espaço da barra de status.
 * Deixa os ícones da barra de status claros enquanto estiver na tela.
 */
@Composable
fun CabecalhoVerde(
    titulo: String,
    aoVoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconesClarosNaBarraDeStatus()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepGreen)
            .statusBarsPadding()
            .padding(start = 8.dp, end = 18.dp, top = 6.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = aoVoltar),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
        }
        Text(titulo, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Ícones claros na barra de status (para fundos escuros); volta ao padrão ao sair da tela. */
@Composable
fun IconesClarosNaBarraDeStatus() {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(view) {
        val janela = (view.context as? Activity)?.window
        val controle = janela?.let { WindowCompat.getInsetsController(it, view) }
        controle?.isAppearanceLightStatusBars = false
        onDispose { controle?.isAppearanceLightStatusBars = true }
    }
}

/** Cartão do novo design: fundo branco, borda fina e raio de 14dp. */
@Composable
fun CartaoNovo(
    modifier: Modifier = Modifier,
    aoClicar: (() -> Unit)? = null,
    conteudo: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val forma = RoundedCornerShape(14.dp)
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = forma,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, com.agrotech.app.ui.theme.Hairline),
        shadowElevation = 1.dp,
        onClick = aoClicar ?: {},
        enabled = aoClicar != null
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(14.dp), content = conteudo)
    }
}

/** Ícone dentro de um quadrado arredondado, como nos cartões do novo design. */
@Composable
fun QuadroIcone(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    fundo: Color,
    cor: Color,
    modifier: Modifier = Modifier,
    tamanho: androidx.compose.ui.unit.Dp = 36.dp
) {
    Box(
        modifier = modifier
            .size(tamanho)
            .background(fundo, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icone, contentDescription = null, tint = cor, modifier = Modifier.size(tamanho * 0.65f))
    }
}
