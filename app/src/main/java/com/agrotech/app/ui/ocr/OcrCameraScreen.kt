package com.agrotech.app.ui.ocr

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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun OcrCameraScreen(navController: NavController) {
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

        if (processando) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
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
                                        val linhas = NfTextRecognizer.reconhecerLinhas(bitmap)
                                        navController.previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.set("ocr_lines", ArrayList(linhas))
                                        navController.popBackStack()
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
    }
}
