package com.agrotech.app.ui.checkin

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agrotech.app.data.checkin.DirecaoLiveness
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.theme.GreenPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfieCheckinScreen(
    email: String,
    aoAprovado: () -> Unit,
    aoSair: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: SelfieCheckinViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                SelfieCheckinViewModel(
                    email = email,
                    checkinRepository = container.checkinRepository,
                    sessionManager = container.sessionManager,
                    locationProvider = container.locationProvider,
                    faceAnalyzer = container.faceAnalyzer
                )
            }
        }
    )

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var cameraPermitida by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcherPermissaoCamera = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> cameraPermitida = granted }

    var localizacaoPermitida by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcherPermissaoLocal = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        localizacaoPermitida = granted.values.any { it }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermitida) launcherPermissaoCamera.launch(Manifest.permission.CAMERA)
        if (!localizacaoPermitida) launcherPermissaoLocal.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Check-in com selfie", fontWeight = FontWeight.SemiBold)
                        Text(
                            email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = aoSair) {
                        Icon(Icons.Filled.Logout, contentDescription = "Sair", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            // Barra inferior fixa com o botão "Realizar Checkin".
            // Sempre visível (não depende do liveness) porque o
            // detector de rosto pode não funcionar no device e
            // a gente ainda quer permitir o check-in.
            if (!state.aprovado) {
                androidx.compose.material3.Surface(
                    color = MaterialTheme.colorScheme.background,
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        // Badge de status (acima do botão) — informa
                        // se a liveness foi concluída, se o detector
                        // está disponível, ou se houve erro.
                        when {
                            state.processando -> StatusBadge(
                                texto = "Salvando check-in...",
                                cor = GreenPrimary
                            )
                            state.mensagemErro != null -> StatusBadge(
                                texto = state.mensagemErro!!,
                                cor = MaterialTheme.colorScheme.error
                            )
                            state.livenessCompleto -> StatusBadge(
                                texto = "✓ Liveness concluída — pronto pra capturar",
                                cor = GreenPrimary
                            )
                            !state.faceAnalyzerInicializado -> StatusBadge(
                                texto = "Detector facial indisponível — captura simples",
                                cor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            else -> StatusBadge(
                                texto = "Mova a cabeça nas 4 direções (opcional)",
                                cor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                scope.launch { viewModel.capturarEChecar(context, lifecycleOwner) }
                            },
                            enabled = !state.processando,
                            shape = RoundedCornerShape(18.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = GreenPrimary,
                                contentColor = Color.White,
                                disabledContainerColor = GreenPrimary.copy(alpha = 0.4f),
                                disabledContentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                            Text(
                                "Realizar Checkin",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Se o FaceAnalyzer não inicializou, mostra erro e botão
            // de bypass (sem detecção facial) pra não travar o user.
            if (!state.faceAnalyzerInicializado) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Check-in facial indisponível",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            state.faceAnalyzerErro ?: "Detector de rosto não inicializou.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { aoAprovado() },
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Entrar sem check-in facial")
                        }
                    }
                }
            }

            // Preview da câmera em formato quadrado + ImageAnalysis
            // (que faz a detecção de rosto a cada ~300ms e atualiza o
            // LivenessTracker com a posição do nariz).
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (state.faceAnalyzerInicializado) 420.dp else 200.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (cameraPermitida) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { previewView ->
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    val capture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                        .build()

                                    val analyzerExecutor = Executors.newSingleThreadExecutor()
                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also {
                                            it.setAnalyzer(analyzerExecutor) { proxy ->
                                                processarFrameParaLiveness(proxy, viewModel)
                                            }
                                        }

                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_FRONT_CAMERA,
                                        preview,
                                        imageAnalysis,
                                        capture
                                    )
                                } catch (e: Exception) {
                                    Log.e("SelfieCheckin", "Falha ao abrir câmera", e)
                                }
                            }, ContextCompat.getMainExecutor(context))
                        }
                    )
                } else {
                    Text(
                        "Permissão de câmera negada. Volte e habilite para continuar.",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (state.faceAnalyzerInicializado) {
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        "Mova a cabeça na direção indicada",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DirecaoLiveness.entries.forEach { dir ->
                            CheckDirecao(
                                direcao = dir,
                                concluida = state.direcoesConcluidas.contains(dir),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        state.processando -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = GreenPrimary
                                )
                                Spacer(modifier = Modifier.padding(start = 8.dp))
                                Text(
                                    "Analisando selfie...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        state.aprovado -> {
                            Text(
                                "✓ Check-in aprovado! Entrando...",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        state.mensagemErro != null -> {
                            Text(
                                state.mensagemErro!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        state.livenessCompleto -> {
                            Text(
                                "Tudo certo — toque no botão da câmera para capturar.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        else -> {
                            Text(
                                "Aguarde o app detectar seu rosto no preview.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(state.aprovado) {
        if (state.aprovado) {
            delay(800)
            aoAprovado()
        }
    }
}

/**
 * Callback do `ImageAnalysis` — roda a cada frame do preview, detecta
 * rosto com ML Kit, e repassa a posição do nariz pro
 * [SelfieCheckinViewModel.onFaceDetectada] (que alimenta o
 * LivenessTracker).
 */
@SuppressLint("UnsafeOptInUsageError")
private fun processarFrameParaLiveness(
    proxy: ImageProxy,
    viewModel: SelfieCheckinViewModel
) {
    try {
        val bitmap = proxy.toBitmap()
        val nariz = viewModel.detectarNarizNoFrame(bitmap)
        nariz?.let { (x, y) -> viewModel.onFaceDetectada(x, y) }
    } catch (e: Throwable) {
        Log.e("SelfieCheckin", "Falha no frame de liveness", e)
    } finally {
        proxy.close()
    }
}

@Composable
private fun CheckDirecao(
    direcao: DirecaoLiveness,
    concluida: Boolean,
    modifier: Modifier = Modifier
) {
    val background = if (concluida) GreenPrimary else MaterialTheme.colorScheme.surface
    val content = if (concluida) Color.White else MaterialTheme.colorScheme.onSurface
    Column(
        modifier = modifier
            .background(background, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            if (concluida) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            direcao.label,
            style = MaterialTheme.typography.labelSmall,
            color = content,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Badge de status mostrada acima do botão "Realizar Checkin". Informa
 * o user sobre o estado atual: salvando, liveness concluída, detector
 * indisponível, ou aguardando movimentos opcionais.
 */
@Composable
private fun StatusBadge(texto: String, cor: Color) {
    Text(
        texto,
        style = MaterialTheme.typography.bodySmall,
        color = cor,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth()
    )
}
