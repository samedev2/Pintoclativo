package com.agrotech.app.ui.fotos

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.VideoView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.agrotech.app.R
import com.agrotech.app.data.granjacam.AveDetectada
import com.agrotech.app.data.granjacam.DeteccoesMock
import com.agrotech.app.data.granjacam.GranjaCamConfig
import com.agrotech.app.ui.components.CartaoNovo
import com.agrotech.app.ui.theme.DeepGreen
import com.agrotech.app.ui.theme.FieldBg
import com.agrotech.app.ui.theme.Muted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Opção GranjaCam da tela Fotos.
 *
 * A análise computacional roda no serviço GranjaCam, em outro ambiente. Aqui o app só mostra:
 *  - com `GRANJACAM_BASE_URL` https: o webviewer do serviço;
 *  - sem URL: um mock (vídeo de teste com as caixas que o modelo real gerou offline).
 */
@Composable
fun GranjaCamPane(modifier: Modifier = Modifier) {
    if (GranjaCamConfig.usaServico) {
        GranjaCamWebView(GranjaCamConfig.baseUrl, modifier)
    } else {
        GranjaCamMock(modifier)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun GranjaCamWebView(url: String, modifier: Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        },
        onRelease = { it.destroy() }
    )
}

// Resolução do vídeo de teste em res/raw/granjacam_pintos.mp4.
// Atualizar junto com o vídeo: 768x1024 (retrato, 3:4).
private const val PROPORCAO_VIDEO_W = 768f
private const val PROPORCAO_VIDEO_H = 1024f
private const val PROPORCAO_VIDEO = PROPORCAO_VIDEO_W / PROPORCAO_VIDEO_H

@Composable
private fun GranjaCamMock(modifier: Modifier) {
    val context = LocalContext.current
    val deteccoes by produceState<DeteccoesMock?>(initialValue = null) {
        value = withContext(Dispatchers.Default) { runCatching { DeteccoesMock.carregar(context) }.getOrNull() }
    }
    var video by remember { mutableStateOf<VideoView?>(null) }
    var aves by remember { mutableStateOf<List<AveDetectada>>(emptyList()) }
    var rastreadas by remember { mutableIntStateOf(0) }

    // Acompanha a posição do vídeo e busca as caixas do quadro atual.
    LaunchedEffect(video, deteccoes) {
        val v = video ?: return@LaunchedEffect
        val det = deteccoes ?: return@LaunchedEffect
        val vistos = HashSet<String>()
        var ultimoMs = 0
        while (isActive) {
            val ms = v.currentPosition
            if (ms < ultimoMs - 1000) vistos.clear() // o vídeo reiniciou (loop)
            ultimoMs = ms
            val quadro = det.quadroEm(ms.toLong())
            quadro.forEach { vistos.add("${it.classe}${it.id}") }
            aves = quadro
            rastreadas = vistos.size
            delay(33)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(PROPORCAO_VIDEO)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1A1F1C))
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(Uri.parse("android.resource://${ctx.packageName}/${R.raw.granjacam_pintos}"))
                        setOnPreparedListener { mp ->
                            mp.isLooping = true
                            mp.setVolume(0f, 0f)
                            start()
                        }
                        video = this
                    }
                },
                onRelease = { it.stopPlayback() }
            )
            CaixasDasAves(aves, Modifier.fillMaxSize())
            EtiquetaAoVivo(Modifier.align(Alignment.TopStart).padding(10.dp))
            Text(
                "Pinteiro 1 · detecção por ave",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 8.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Contador("PINTOS", aves.count { it.classe == "pinto" }, Modifier.weight(1f))
            Contador("GALINHAS", aves.count { it.classe != "pinto" }, Modifier.weight(1f))
            Contador("RASTREADAS", rastreadas, Modifier.weight(1f))
        }

        CartaoNovo(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 34.dp)
                        .background(FieldBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.BarChart, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Rastreamento em tempo real", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Cada ave mantém o mesmo número", fontSize = 12.sp, color = Muted)
                }
            }
        }
    }
}

@Composable
private fun Contador(titulo: String, valor: Int, modifier: Modifier) {
    CartaoNovo(modifier = modifier) {
        Text(titulo, fontSize = 11.sp, color = Muted)
        Text("$valor", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EtiquetaAoVivo(modifier: Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xFFD62F2F), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).background(Color.White, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text("AO VIVO", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

private fun corDaClasse(classe: String): Int = when (classe) {
    "pinto" -> 0xFFFFD23F.toInt()
    "galinha" -> 0xFFFF7A45.toInt()
    else -> 0xFFFF4D6D.toInt()
}

/** Desenha a caixa e o número de cada ave por cima do vídeo. */
@Composable
private fun CaixasDasAves(aves: List<AveDetectada>, modifier: Modifier) {
    val texto = remember { Paint().apply { isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD } }
    val fundo = remember { Paint() }
    Canvas(modifier) {
        val traco = 1.5.dp.toPx()
        texto.textSize = 9.dp.toPx()
        val alturaEtiqueta = 12.dp.toPx()
        drawIntoCanvas { canvas ->
            aves.forEach { ave ->
                val x = ave.x1 * size.width
                val y = ave.y1 * size.height
                val largura = (ave.x2 - ave.x1) * size.width
                val altura = (ave.y2 - ave.y1) * size.height
                val cor = corDaClasse(ave.classe)
                drawRect(Color(cor), Offset(x, y), Size(largura, altura), style = Stroke(traco))
                // Só rotula pinto quando a caixa é grande o bastante para caber o número.
                if (ave.classe != "pinto" || largura > 16.dp.toPx()) {
                    val rotulo = (if (ave.classe == "pinto") "" else ave.classe.replaceFirstChar { it.uppercase() } + " ") + "#" + ave.id
                    val larguraRotulo = texto.measureText(rotulo) + 6.dp.toPx()
                    fundo.color = cor
                    canvas.nativeCanvas.drawRect(x, y - alturaEtiqueta, x + larguraRotulo, y, fundo)
                    texto.color = 0xFF1A1A1A.toInt()
                    canvas.nativeCanvas.drawText(rotulo, x + 3.dp.toPx(), y - 2.5.dp.toPx(), texto)
                }
            }
        }
    }
}
