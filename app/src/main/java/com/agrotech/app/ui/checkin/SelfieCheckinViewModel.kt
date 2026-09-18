package com.agrotech.app.ui.checkin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrotech.app.data.auth.SessionManager
import com.agrotech.app.data.checkin.CheckinRepository
import com.agrotech.app.data.checkin.DirecaoLiveness
import com.agrotech.app.data.checkin.FaceAnalyzer
import com.agrotech.app.data.checkin.FaceHashUtil
import com.agrotech.app.data.checkin.FaceSignatureCodec
import com.agrotech.app.data.checkin.LivenessTracker
import com.agrotech.app.data.checkin.LocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class SelfieCheckinUiState(
    val livenessCompleto: Boolean = false,
    val direcoesConcluidas: Set<DirecaoLiveness> = emptySet(),
    val processando: Boolean = false,
    val aprovado: Boolean = false,
    val faceAnalyzerInicializado: Boolean = false,
    val faceAnalyzerErro: String? = null,
    val mensagemErro: String? = null
)

/**
 * ViewModel do check-in com selfie. Usa [FaceAnalyzer] (ML Kit Face
 * Detection) pra:
 *  1. Detectar rosto em cada frame (callback do preview).
 *  2. Acompanhar o nariz (liveness de 4 direções).
 *  3. Ao capturar, gerar a "assinatura geométrica" e comparar com a
 *     selfie anterior. Threshold: ≥ 85% de similaridade geométrica
 *     OU ≥ 95% de similaridade perceptual.
 *
 * Se o analyzer não inicializar (sem Play Services, modelo ausente),
 * a UI mostra mensagem amigável + botão de bypass.
 */
class SelfieCheckinViewModel(
    val email: String,
    private val checkinRepository: CheckinRepository,
    val sessionManager: SessionManager,
    val locationProvider: LocationProvider,
    private val faceAnalyzer: FaceAnalyzer
) : ViewModel() {

    private val liveness = LivenessTracker()
    private val _state = MutableStateFlow(SelfieCheckinUiState())
    val state: StateFlow<SelfieCheckinUiState> = _state.asStateFlow()

    init {
        _state.update {
            it.copy(
                faceAnalyzerInicializado = faceAnalyzer.inicializado,
                faceAnalyzerErro = faceAnalyzer.erroInicializacao
            )
        }
    }

    /**
     * Chamado pela `SelfieCheckinScreen` em cada frame do preview
     * quando o ML Kit detecta um rosto. Atualiza o tracker de
     * liveness com a posição do nariz.
     */
    fun onFaceDetectada(narizX: Float, narizY: Float) {
        if (_state.value.processando || _state.value.aprovado) return
        val proxima = DirecaoLiveness.entries.firstOrNull {
            it !in _state.value.direcoesConcluidas
        }
        val concluiu = liveness.alimentar(narizX, narizY, proxima)
        val set = liveness.concluidasSet()
        _state.update {
            it.copy(
                direcoesConcluidas = set,
                livenessCompleto = liveness.isCompleto
            )
        }
    }

    /**
     * Detecta rosto no bitmap do frame de preview. Retorna `Pair(x, y)`
     * com a posição do nariz, ou `null` se não houver rosto. Usado
     * pelo `ImageAnalysis.Analyzer` da câmera frontal pra alimentar
     * o tracker de liveness.
     */
    fun detectarNarizNoFrame(bitmap: android.graphics.Bitmap): Pair<Float, Float>? {
        val face = faceAnalyzer.detectar(bitmap) ?: return null
        return faceAnalyzer.landmarkNariz(face)?.let { it.x to it.y }
    }

    fun resetarLiveness() {
        liveness.resetar()
        _state.update { it.copy(direcoesConcluidas = emptySet(), livenessCompleto = false) }
    }

    /**
     * Captura a foto via CameraX, valida rosto + similaridade, e salva.
     *
     * Importante: NÃO exige liveness completo. Se o detector facial
     * não estiver disponível (FaceAnalyzer stub ou falha de init),
     * a foto é capturada e salva do mesmo jeito — o liveness é um
     * "nice to have" visual, não um pré-requisito. Isso garante que
     * o usuário consegue fazer check-in mesmo em devices onde a
     * detecção facial nativa não funciona.
     */
    suspend fun capturarEChecar(context: Context, lifecycleOwner: LifecycleOwner) {
        if (!faceAnalyzer.inicializado) {
            // Marca o erro no estado (pra UI mostrar a mensagem), mas
            // NÃO bloqueia a captura. A foto + GPS + timestamp são
            // salvos normalmente.
            _state.update {
                it.copy(
                    faceAnalyzerErro = faceAnalyzer.erroInicializacao
                        ?: "Detector de rosto não inicializou."
                )
            }
        }
        _state.update { it.copy(processando = true, mensagemErro = null) }
        try {
            val bitmap = withContext(Dispatchers.IO) { capturarFoto(context, lifecycleOwner) }
                ?: run {
                    _state.update {
                        it.copy(
                            processando = false,
                            mensagemErro = "Não foi possível capturar a foto."
                        )
                    }
                    return
                }

            val face = withContext(Dispatchers.Default) { faceAnalyzer.detectar(bitmap) }
            if (face == null && faceAnalyzer.inicializado) {
                // Detector inicializou mas não achou rosto → mostra erro.
                // Se o detector NÃO inicializou (stub), segue sem rosto
                // (FaceAnalyzer é no-op e a foto inteira vira o checkin).
                _state.update {
                    it.copy(
                        processando = false,
                        mensagemErro = "Nenhum rosto detectado. Centralize o rosto e tente de novo."
                    )
                }
                return
            }

            // Se `face` é null (FaceAnalyzer stub ou detector não
            // disponível), salva a foto inteira sem bounding box e
            // sem assinatura geométrica. Similaridade é pulada.
            val (faceBitmap, assinaturaAtual) = if (face != null) {
                val bbox = faceAnalyzer.boundingBox(face, bitmap.width, bitmap.height)
                val cropped = if (bbox != null && bbox.width() > 0 && bbox.height() > 0) {
                    val safeLeft = bbox.left.coerceAtLeast(0)
                    val safeTop = bbox.top.coerceAtLeast(0)
                    val safeRight = (bbox.left + bbox.width()).coerceAtMost(bitmap.width)
                    val safeBottom = (bbox.top + bbox.height()).coerceAtMost(bitmap.height)
                    if (safeRight > safeLeft && safeBottom > safeTop) {
                        Bitmap.createBitmap(
                            bitmap, safeLeft, safeTop,
                            safeRight - safeLeft, safeBottom - safeTop
                        )
                    } else bitmap
                } else bitmap
                cropped to faceAnalyzer.assinaturaGeometrica(face)
            } else {
                bitmap to null
            }

            // Compara com a selfie anterior. Pula a comparação se o
            // detector facial não inicializou OU se não há assinatura
            // disponível — nesse caso, qualquer check-in novo passa.
            val checkinAnterior = checkinRepository.ultimoCheckin(email)
            val passouSimilaridade = if (
                checkinAnterior == null ||
                !faceAnalyzer.inicializado ||
                assinaturaAtual == null
            ) {
                true
            } else {
                val geomAnterior = FaceSignatureCodec.decode(checkinAnterior.faceAssinatura)
                val geomScore = if (geomAnterior != null) {
                    faceAnalyzer.similaridadeGeometrica(geomAnterior, assinaturaAtual)
                } else 0f
                val percepAnterior = withContext(Dispatchers.IO) {
                    val anteriorBitmap = BitmapFactory.decodeFile(checkinAnterior.fotoPath)
                    if (anteriorBitmap != null) {
                        FaceHashUtil.similaridade(
                            FaceHashUtil.hashDaFace(anteriorBitmap),
                            FaceHashUtil.hashDaFace(faceBitmap)
                        )
                    } else 1f
                }
                // Threshold combinado: geom ≥ 0.85 OU perceptual ≥ 0.95.
                geomScore >= 0.85f || percepAnterior >= 0.95f
            }

            if (!passouSimilaridade) {
                val geom = if (
                    checkinAnterior != null && assinaturaAtual != null &&
                    faceAnalyzer.inicializado
                ) {
                    faceAnalyzer.similaridadeGeometrica(
                        FaceSignatureCodec.decode(checkinAnterior.faceAssinatura)
                            ?: FloatArray(0),
                        assinaturaAtual
                    )
                } else 0f
                _state.update {
                    it.copy(
                        processando = false,
                        mensagemErro = "Selfie não confere com a anterior (${String.format("%.1f", geom * 100)}% — mínimo 85%)."
                    )
                }
                return
            }

            val localizacao = locationProvider.localizacaoAtual()
            checkinRepository.salvar(
                email = email,
                bitmap = faceBitmap,
                latitude = localizacao?.first,
                longitude = localizacao?.second,
                faceAssinatura = assinaturaAtual
            )
            _state.update { it.copy(processando = false, aprovado = true) }
        } catch (e: Exception) {
            Log.e("SelfieCheckin", "Falha no check-in", e)
            _state.update {
                it.copy(
                    processando = false,
                    mensagemErro = "Erro ao processar: ${e.message ?: "desconhecido"}"
                )
            }
        }
    }

    private suspend fun capturarFoto(
        context: Context,
        lifecycleOwner: LifecycleOwner
    ): Bitmap? = suspendCancellableCoroutine { cont ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = try {
                cameraProviderFuture.get()
            } catch (e: Exception) {
                cont.resumeWithException(e)
                return@addListener
            }
            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    imageCapture
                )
            } catch (e: Exception) {
                cont.resumeWithException(e)
                return@addListener
            }
            val arquivoTemp = File(context.cacheDir, "selfie_${System.currentTimeMillis()}.jpg")
            val opcoes = ImageCapture.OutputFileOptions.Builder(arquivoTemp).build()
            imageCapture.takePicture(
                opcoes,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(saida: ImageCapture.OutputFileResults) {
                        try {
                            val raw = BitmapFactory.decodeFile(arquivoTemp.absolutePath)
                            val matrix = Matrix().apply { preScale(-1f, 1f) }
                            val bitmap = Bitmap.createBitmap(
                                raw, 0, 0, raw.width, raw.height, matrix, false
                            )
                            arquivoTemp.delete()
                            cont.resume(bitmap)
                        } catch (e: Exception) {
                            cont.resumeWithException(e)
                        }
                    }
                    override fun onError(excecao: ImageCaptureException) {
                        cont.resumeWithException(excecao)
                    }
                }
            )
        }, ContextCompat.getMainExecutor(context))
    }

    override fun onCleared() {
        super.onCleared()
        faceAnalyzer.fechar()
    }
}
