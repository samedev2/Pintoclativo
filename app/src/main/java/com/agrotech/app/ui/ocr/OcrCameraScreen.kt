package com.agrotech.app.ui.ocr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.PillShape
import kotlinx.coroutines.launch
import java.io.File

private enum class ModoLeitura { QR_CODE, NOTA_FISCAL }

@Composable
fun OcrCameraScreen(
    navController: NavController,
    modoDemonstracao: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val escopo = rememberCoroutineScope()

    var permissaoConcedida by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val lancadorPermissao = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { concedida -> permissaoConcedida = concedida }

    DisposableEffect(Unit) {
        if (!permissaoConcedida) lancadorPermissao.launch(Manifest.permission.CAMERA)
        onDispose {}
    }

    var processando by remember { mutableStateOf(false) }
    var modo by remember {
        mutableStateOf(if (modoDemonstracao) ModoLeitura.QR_CODE else ModoLeitura.NOTA_FISCAL)
    }
    var resultado by remember { mutableStateOf<String?>(null) }
    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    if (permissaoConcedida) {
        DisposableEffect(Unit) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val listener = Runnable {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val capture = ImageCapture.Builder().build()
                imageCapture = capture
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    capture
                )
            }
            cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))
            onDispose {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (permissaoConcedida) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        } else {
            Text(
                "É necessário conceder acesso à câmera para ler a nota fiscal.",
                modifier = Modifier.align(Alignment.Center).padding(24.dp)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.72f)).statusBarsPadding().padding(12.dp)
        ) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
                Text(
                    if (modoDemonstracao) "Scanner AgroTech" else "Ler nota fiscal",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                if (modo == ModoLeitura.QR_CODE)
                    "Enquadre o QR Code e toque no botão para ler."
                else
                    "Enquadre a nota inteira, com boa iluminação e texto nítido.",
                color = Color.White, style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
            if (modoDemonstracao) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModoButton(
                        texto = "QR Code",
                        selecionado = modo == ModoLeitura.QR_CODE,
                        aoClicar = { modo = ModoLeitura.QR_CODE; resultado = null },
                        modifier = Modifier.weight(1f)
                    )
                    ModoButton(
                        texto = "Nota fiscal (OCR)",
                        selecionado = modo == ModoLeitura.NOTA_FISCAL,
                        aoClicar = { modo = ModoLeitura.NOTA_FISCAL; resultado = null },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (processando) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = {
                    val capture = imageCapture ?: return@FloatingActionButton
                    if (processando) return@FloatingActionButton
                    processando = true
                    val arquivoTemp = File(context.cacheDir, "nf_${System.currentTimeMillis()}.jpg")
                    val opcoesSaida = ImageCapture.OutputFileOptions.Builder(arquivoTemp).build()
                    capture.takePicture(
                        opcoesSaida,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(saida: ImageCapture.OutputFileResults) {
                                escopo.launch {
                                    try {
                                        val bitmap = BitmapFactory.decodeFile(arquivoTemp.absolutePath)
                                        if (modo == ModoLeitura.QR_CODE) {
                                            resultado = QrCodeRecognizer.reconhecer(bitmap)
                                                ?: "Nenhum QR Code encontrado. Tente aproximar a câmera."
                                            processando = false
                                        } else {
                                            val linhas = NfTextRecognizer.reconhecerLinhas(bitmap)
                                            if (modoDemonstracao) {
                                                resultado = linhas.take(8).joinToString("\n")
                                                    .ifBlank { "Nenhum texto encontrado na nota." }
                                                processando = false
                                            } else {
                                                navController.previousBackStackEntry
                                                    ?.savedStateHandle
                                                    ?.set("ocr_lines", ArrayList(linhas))
                                                navController.popBackStack()
                                            }
                                        }
                                    } catch (erro: Exception) {
                                        processando = false
                                        Toast.makeText(context, "Falha ao ler o texto da imagem.", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        arquivoTemp.delete()
                                    }
                                }
                            }

                            override fun onError(excecao: ImageCaptureException) {
                                processando = false
                                Toast.makeText(context, "Falha ao capturar a foto.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                },
                containerColor = GreenPrimary,
                contentColor = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .size(72.dp)
            ) {
                Icon(Icons.Filled.Camera, contentDescription = "Capturar")
            }
        }

        resultado?.let { texto ->
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .widthIn(max = 440.dp),
                shape = MaterialTheme.shapes.large,
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        if (modo == ModoLeitura.QR_CODE) "Resultado do QR Code" else "Texto reconhecido",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        texto,
                        modifier = Modifier.padding(top = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { resultado = null },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = PillShape
                    ) {
                        Text("Escanear novamente")
                    }
                }
            }
        }
    }
}

@Composable
private fun ModoButton(
    texto: String,
    selecionado: Boolean,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selecionado) {
        Button(
            onClick = aoClicar,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            shape = PillShape
        ) { Text(texto) }
    } else {
        OutlinedButton(
            onClick = aoClicar,
            modifier = modifier,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            shape = PillShape
        ) { Text(texto) }
    }
}
