package com.agrotech.app.ui.fotos

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.agrotech.app.R
import com.agrotech.app.data.granjacam.AveDetectada
import com.agrotech.app.data.granjacam.CLASSE_BARRA
import com.agrotech.app.data.granjacam.CLASSE_COMEDOURO
import com.agrotech.app.data.granjacam.DeteccoesMock
import com.agrotech.app.data.granjacam.GranjaCamConfig
import com.agrotech.app.data.granjacam.ehEstrutura
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
 *
 * No mock há um botão de tela cheia: abre o **modo paisagem** (imersivo, com zoom e arrastar) para
 * ver as câmeras com mais detalhe, como no painel de treino do GranjaCam.
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
private const val ZOOM_MAXIMO = 4f

// Janela paisagem da tela normal: mesma região e escala do print aprovado (recorte 768x410 do vídeo
// 768x1024, começando na linha 120, onde ficam os pintos). Para ver o resto: modo paisagem em tela cheia.
private const val PROPORCAO_JANELA = 768f / 410f
private const val JANELA_INICIO_Y = 120f

// Filtro de visão: cada tipo de item é um bit. Guardado como Int para sobreviver à rotação da tela.
private const val BIT_PINTO = 1
private const val BIT_GALINHA = 2
private const val BIT_COMEDOURO = 4
private const val BIT_BARRA = 8
private const val BIT_NOMES = 16
private const val FILTRO_PADRAO = BIT_PINTO or BIT_GALINHA or BIT_COMEDOURO or BIT_BARRA or BIT_NOMES

private fun bitDaClasse(classe: String): Int = when (classe) {
    "pinto" -> BIT_PINTO
    "galinha" -> BIT_GALINHA
    CLASSE_COMEDOURO -> BIT_COMEDOURO
    CLASSE_BARRA -> BIT_BARRA
    else -> 0
}

private fun classeVisivel(filtro: Int, classe: String): Boolean {
    val bit = bitDaClasse(classe)
    return bit == 0 || (filtro and bit) != 0
}

private tailrec fun Context.acharActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.acharActivity()
    else -> null
}

@Composable
private fun GranjaCamMock(modifier: Modifier) {
    val context = LocalContext.current
    val deteccoes by produceState<DeteccoesMock?>(initialValue = null) {
        value = withContext(Dispatchers.Default) { runCatching { DeteccoesMock.carregar(context) }.getOrNull() }
    }
    var aves by remember { mutableStateOf<List<AveDetectada>>(emptyList()) }
    var rastreadas by remember { mutableIntStateOf(0) }
    var telaCheia by rememberSaveable { mutableStateOf(false) }
    var filtro by rememberSaveable { mutableIntStateOf(FILTRO_PADRAO) }
    // Última posição do vídeo, para o modo paisagem continuar de onde parou (não precisa recompor a tela).
    val ultimaPosicao = remember { IntArray(1) }

    // Modo paisagem: força a orientação enquanto estiver aberto e devolve ao normal ao sair.
    val cheia = telaCheia
    DisposableEffect(cheia) {
        val activity = context.acharActivity()
        if (cheia) activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            if (cheia) activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    if (cheia) {
        ModoPaisagem(
            deteccoes = deteccoes,
            posicaoInicialMs = ultimaPosicao[0],
            filtro = filtro,
            aoAlternarFiltro = { filtro = filtro xor it },
            aoFechar = { ms ->
                ultimaPosicao[0] = ms
                telaCheia = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(PROPORCAO_JANELA)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1A1F1C))
        ) {
            val larguraPx = constraints.maxWidth.toFloat()
            val alturaVideoPx = larguraPx / PROPORCAO_VIDEO
            val deslocY = -(JANELA_INICIO_Y / PROPORCAO_VIDEO_H) * alturaVideoPx
            val densidade = LocalDensity.current
            // O vídeo inteiro (retrato) é maior que a janela: requiredSize deixa ele passar da janela
            // e o deslocamento mostra só o recorte do print.
            Box(
                modifier = Modifier
                    .requiredSize(
                        width = with(densidade) { larguraPx.toDp() },
                        height = with(densidade) { alturaVideoPx.toDp() }
                    )
                    .graphicsLayer { translationY = deslocY }
            ) {
                if (!cheia) {
                    VideoComDeteccoes(
                        modifier = Modifier.fillMaxSize(),
                        deteccoes = deteccoes,
                        posicaoInicialMs = ultimaPosicao[0],
                        filtro = filtro
                    ) { quadro, total, ms ->
                        aves = quadro
                        rastreadas = total
                        ultimaPosicao[0] = ms
                    }
                }
            }
            EtiquetaAoVivo(Modifier.align(Alignment.TopStart).padding(10.dp))
            BotaoRedondo(
                icone = Icons.Filled.Fullscreen,
                descricao = "Ver em modo paisagem",
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                aoClicar = { telaCheia = true }
            )
            Text(
                "Pinteiro 1 · detecção por ave",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 8.dp)
            )
        }

        FiltroDeItens(filtro = filtro, aoAlternar = { filtro = filtro xor it })

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Contador("PINTOS", aves.count { it.classe == "pinto" }, Modifier.weight(1f))
            Contador("GALINHAS", aves.count { it.classe == "galinha" }, Modifier.weight(1f))
            Contador("COMEDOUROS", aves.count { it.classe == CLASSE_COMEDOURO }, Modifier.weight(1f))
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
                    Text("$rastreadas aves rastreadas · cada ave mantém o mesmo número", fontSize = 12.sp, color = Muted)
                }
            }
        }
    }
}

/** Botão redondo escuro e translúcido, para ficar por cima do vídeo. */
@Composable
private fun BotaoRedondo(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    descricao: String,
    modifier: Modifier = Modifier,
    tamanho: androidx.compose.ui.unit.Dp = 36.dp,
    aoClicar: () -> Unit
) {
    Box(
        modifier = modifier
            .size(tamanho)
            .background(Color.Black.copy(alpha = 0.55f), CircleShape)
            .clickable(onClick = aoClicar),
        contentAlignment = Alignment.Center
    ) {
        Icon(icone, contentDescription = descricao, tint = Color.White, modifier = Modifier.size(tamanho * 0.6f))
    }
}

/**
 * Vídeo de teste com as caixas por cima. Acompanha a posição do vídeo, busca as caixas do quadro atual
 * e avisa quem chamou (lista do quadro, aves rastreadas até agora, posição em ms).
 */
@Composable
private fun VideoComDeteccoes(
    modifier: Modifier,
    deteccoes: DeteccoesMock?,
    posicaoInicialMs: Int,
    filtro: Int,
    aoAtualizar: (List<AveDetectada>, Int, Int) -> Unit
) {
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var aves by remember { mutableStateOf<List<AveDetectada>>(emptyList()) }
    val atualizar by rememberUpdatedState(aoAtualizar)

    LaunchedEffect(player, deteccoes) {
        val p = player ?: return@LaunchedEffect
        val det = deteccoes ?: return@LaunchedEffect
        val vistos = HashSet<String>()
        var ultimoMs = 0
        while (isActive) {
            val ms = try {
                p.currentPosition
            } catch (e: IllegalStateException) {
                break // o player foi liberado
            }
            if (ms < ultimoMs - 1000) vistos.clear() // o vídeo reiniciou (loop)
            ultimoMs = ms
            val quadro = det.quadroEm(ms.toLong())
            quadro.forEach { if (!ehEstrutura(it.classe)) vistos.add("${it.classe}${it.id}") }
            aves = quadro
            atualizar(quadro, vistos.size, ms)
            delay(33)
        }
    }

    Box(modifier) {
        VideoTextura(Modifier.fillMaxSize(), posicaoInicialMs) { player = it }
        // O filtro só muda o que é desenhado; os contadores continuam contando tudo.
        val desenhar = remember(aves, filtro) { aves.filter { classeVisivel(filtro, it.classe) } }
        CaixasDasAves(desenhar, (filtro and BIT_NOMES) != 0, Modifier.fillMaxSize())
    }
}

/**
 * Player do vídeo de teste em `TextureView` (e não `VideoView`): o TextureView aceita zoom, arrastar e
 * recorte, o que o modo paisagem precisa. [aoPronto] recebe o player já preparado (ou null ao liberar).
 */
@Composable
private fun VideoTextura(
    modifier: Modifier,
    posicaoInicialMs: Int,
    aoPronto: (MediaPlayer?) -> Unit
) {
    val playerAtual = remember { arrayOfNulls<MediaPlayer>(1) }
    val avisar by rememberUpdatedState(aoPronto)
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextureView(ctx).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                        val mp = MediaPlayer()
                        try {
                            mp.setDataSource(ctx, Uri.parse("android.resource://${ctx.packageName}/${R.raw.granjacam_pintos}"))
                            mp.setSurface(Surface(st))
                            mp.isLooping = true
                            mp.setVolume(0f, 0f)
                            mp.setOnPreparedListener { p ->
                                if (posicaoInicialMs > 0) p.seekTo(posicaoInicialMs)
                                p.start()
                                avisar(p)
                            }
                            mp.prepareAsync()
                            playerAtual[0] = mp
                        } catch (e: Exception) {
                            mp.release()
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) = Unit

                    override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                        playerAtual[0]?.release()
                        playerAtual[0] = null
                        avisar(null)
                        return true
                    }

                    override fun onSurfaceTextureUpdated(st: SurfaceTexture) = Unit
                }
            }
        },
        onRelease = {
            playerAtual[0]?.release()
            playerAtual[0] = null
        }
    )
}

/**
 * Modo paisagem em tela cheia (imersivo): o vídeo ocupa a largura da tela e dá para arrastar e ampliar
 * com dois dedos (1x a 4x). Contadores e legenda ficam por cima; o botão no canto (ou o voltar) fecha.
 */
@Composable
private fun ModoPaisagem(
    deteccoes: DeteccoesMock?,
    posicaoInicialMs: Int,
    filtro: Int,
    aoAlternarFiltro: (Int) -> Unit,
    aoFechar: (Int) -> Unit
) {
    var aves by remember { mutableStateOf<List<AveDetectada>>(emptyList()) }
    var rastreadas by remember { mutableIntStateOf(0) }
    val posicao = remember { intArrayOf(posicaoInicialMs) }

    Dialog(
        onDismissRequest = { aoFechar(posicao[0]) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false
        )
    ) {
        // Tela cheia de verdade: janela do diálogo ocupa tudo e as barras do sistema ficam escondidas.
        val view = LocalView.current
        SideEffect {
            val janela = (view.parent as? DialogWindowProvider)?.window
            if (janela != null) {
                janela.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                WindowCompat.setDecorFitsSystemWindows(janela, false)
                WindowCompat.getInsetsController(janela, view).apply {
                    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    hide(WindowInsetsCompat.Type.systemBars())
                }
            }
        }

        BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
            val larguraPx = constraints.maxWidth.toFloat()
            val alturaPx = constraints.maxHeight.toFloat()
            // O vídeo (retrato) ocupa a largura da tela; a parte que passa da altura se vê arrastando.
            val alturaConteudo = larguraPx / PROPORCAO_VIDEO
            var escala by remember { mutableFloatStateOf(1f) }
            var desloc by remember { mutableStateOf(Offset.Zero) }

            fun limitar(o: Offset, e: Float): Offset {
                val w = larguraPx * e
                val h = alturaConteudo * e
                val x = if (w <= larguraPx) (larguraPx - w) / 2f else o.x.coerceIn(larguraPx - w, 0f)
                val y = if (h <= alturaPx) (alturaPx - h) / 2f else o.y.coerceIn(alturaPx - h, 0f)
                return Offset(x, y)
            }

            // Começa mostrando a região de cima do meio do cercado (onde ficam os pintos).
            LaunchedEffect(larguraPx, alturaPx) {
                desloc = limitar(Offset(0f, -(alturaConteudo - alturaPx) * 0.35f), escala)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .pointerInput(larguraPx, alturaPx) {
                        detectTransformGestures { centro, pan, zoom, _ ->
                            val nova = (escala * zoom).coerceIn(1f, ZOOM_MAXIMO)
                            val fator = nova / escala
                            desloc = limitar(centro - (centro - desloc) * fator + pan, nova)
                            escala = nova
                        }
                    }
            ) {
                val densidade = LocalDensity.current
                Box(
                    modifier = Modifier
                        .requiredSize(
                            width = with(densidade) { larguraPx.toDp() },
                            height = with(densidade) { alturaConteudo.toDp() }
                        )
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0f, 0f)
                            scaleX = escala
                            scaleY = escala
                            translationX = desloc.x
                            translationY = desloc.y
                        }
                ) {
                    VideoComDeteccoes(
                        modifier = Modifier.fillMaxSize(),
                        deteccoes = deteccoes,
                        posicaoInicialMs = posicaoInicialMs,
                        filtro = filtro
                    ) { quadro, total, ms ->
                        aves = quadro
                        rastreadas = total
                        posicao[0] = ms
                    }
                }
            }

            EtiquetaAoVivo(Modifier.align(Alignment.TopStart).padding(12.dp))
            BotaoRedondo(
                icone = Icons.Filled.FullscreenExit,
                descricao = "Sair do modo paisagem",
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                tamanho = 44.dp,
                aoClicar = { aoFechar(posicao[0]) }
            )
            Row(
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipContador("PINTOS", aves.count { it.classe == "pinto" })
                ChipContador("GALINHAS", aves.count { it.classe == "galinha" })
                ChipContador("COMEDOUROS", aves.count { it.classe == CLASSE_COMEDOURO })
                ChipContador("RASTREADAS", rastreadas)
            }
            FiltroDeItens(
                filtro = filtro,
                aoAlternar = aoAlternarFiltro,
                escuro = true,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ChipContador(titulo: String, valor: Int) {
    Row(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(titulo, fontSize = 10.sp, color = Color.White.copy(alpha = 0.75f))
        Spacer(Modifier.width(6.dp))
        Text("$valor", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

/**
 * Filtro de visão: um botão por tipo de item (pinto, galinha, comedouro, barra de separação) mais "Nomes"
 * (rótulos das aves). Botão apagado = tipo escondido. A cor do quadradinho é a cor da caixa no vídeo.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FiltroDeItens(
    filtro: Int,
    aoAlternar: (Int) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    escuro: Boolean = false
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BotaoFiltro("Pinto", corDaClasse("pinto"), (filtro and BIT_PINTO) != 0, escuro) { aoAlternar(BIT_PINTO) }
        BotaoFiltro("Galinha", corDaClasse("galinha"), (filtro and BIT_GALINHA) != 0, escuro) { aoAlternar(BIT_GALINHA) }
        BotaoFiltro("Comedouro", corDaClasse(CLASSE_COMEDOURO), (filtro and BIT_COMEDOURO) != 0, escuro) { aoAlternar(BIT_COMEDOURO) }
        BotaoFiltro("Barra de separação", corDaClasse(CLASSE_BARRA), (filtro and BIT_BARRA) != 0, escuro) { aoAlternar(BIT_BARRA) }
        BotaoFiltro("Nomes", null, (filtro and BIT_NOMES) != 0, escuro) { aoAlternar(BIT_NOMES) }
    }
}

@Composable
private fun BotaoFiltro(nome: String, cor: Int?, ativo: Boolean, escuro: Boolean, aoClicar: () -> Unit) {
    val corTexto = if (escuro) Color.White else MaterialTheme.colorScheme.onSurface
    val corBorda = cor?.let { Color(it) } ?: corTexto
    Row(
        modifier = Modifier
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(
                when {
                    ativo && !escuro -> corBorda.copy(alpha = 0.12f)
                    ativo && escuro -> Color.White.copy(alpha = 0.18f)
                    else -> Color.Transparent
                }
            )
            .border(1.dp, if (ativo) corBorda else corBorda.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .clickable(onClick = aoClicar)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (cor != null) {
            Box(
                Modifier
                    .size(11.dp)
                    .then(
                        if (ativo) Modifier.background(Color(cor), RoundedCornerShape(2.dp))
                        else Modifier.border(1.5.dp, Color(cor).copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    )
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            nome,
            fontSize = 12.sp,
            fontWeight = if (ativo) FontWeight.SemiBold else FontWeight.Normal,
            color = corTexto.copy(alpha = if (ativo) 1f else 0.45f)
        )
    }
}

@Composable
private fun Contador(titulo: String, valor: Int, modifier: Modifier) {
    CartaoNovo(modifier = modifier) {
        Text(titulo, fontSize = 10.sp, color = Muted, maxLines = 1, softWrap = false)
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
    "pinto" -> 0xFFD08A1D.toInt()
    "galinha" -> 0xFF2F7FD1.toInt()
    CLASSE_COMEDOURO -> 0xFFA855F7.toInt() // violeta: nada na cena tem essa cor
    CLASSE_BARRA -> 0xFF84CC16.toInt() // verde-limão
    else -> 0xFFFF4D6D.toInt()
}

/** Desenha a caixa de cada ave (com o nome da classe) e de cada estrutura (só a caixa) por cima do vídeo. */
@Composable
private fun CaixasDasAves(aves: List<AveDetectada>, mostrarNomes: Boolean, modifier: Modifier) {
    val texto = remember { Paint().apply { isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD } }
    val fundo = remember { Paint() }
    // Estruturas primeiro, para as aves ficarem por cima.
    val ordenadas = remember(aves) { aves.sortedByDescending { ehEstrutura(it.classe) } }
    Canvas(modifier) {
        val traco = 1.5.dp.toPx()
        drawIntoCanvas { canvas ->
            fun etiqueta(rotulo: String, x: Float, y: Float, cor: Int, corTexto: Int, tamanhoSp: Float, altura: Float) {
                texto.textSize = tamanhoSp.dp.toPx()
                val largura = texto.measureText(rotulo) + 6.dp.toPx()
                fundo.color = cor
                canvas.nativeCanvas.drawRect(x, y - altura, x + largura, y, fundo)
                texto.color = corTexto
                canvas.nativeCanvas.drawText(rotulo, x + 3.dp.toPx(), y - (altura - tamanhoSp.dp.toPx()) / 2f - 1.dp.toPx(), texto)
            }
            ordenadas.forEach { ave ->
                val x = ave.x1 * size.width
                val y = ave.y1 * size.height
                val largura = (ave.x2 - ave.x1) * size.width
                val altura = (ave.y2 - ave.y1) * size.height
                val cor = corDaClasse(ave.classe)
                if (ehEstrutura(ave.classe)) {
                    // Estruturas em traço tracejado e violeta/verde; aves em traço contínuo laranja/azul.
                    drawRect(
                        Color(cor), Offset(x, y), Size(largura, altura),
                        style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())))
                    )
                } else {
                    drawRect(Color(cor), Offset(x, y), Size(largura, altura), style = Stroke(traco))
                    // Rótulo com o nome da classe (pinto / galinha), como no painel do GranjaCam.
                    if (mostrarNomes) etiqueta(ave.classe, x, y, cor, 0xFFFFFFFF.toInt(), 8f, 11.dp.toPx())
                }
            }
        }
    }
}
