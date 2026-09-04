package com.agrotech.app.data.checkin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Rect

/**
 * Stub do detector de rosto. Foi tentado o uso de **ML Kit Face
 * Detection v16.1.7** (`com.google.mlkit:face-detection`), mas a
 * `libface_detector_v2_jni.so` dessa versão também tem segmentos
 * LOAD não alinhados em 16 KB (LOAD[2] em 0x816430) — mesma classe
 * do problema que afetou a `libmediapipe_tasks_vision_jni.so`.
 *
 * O Android 15+ exige alinhamento de 16 KB nos segmentos LOAD das
 * `.so`; quando o alinhamento falha, o `dlopen` aborta o processo
 * com SIGABRT (não capturável por try-catch Kotlin). O A54 do Ian
 * roda One UI 7 (Android 15), e o sistema exibe a caixa
 * "Compatibilidade de apps Android" + mata o app no startup.
 *
 * Soluções tentadas:
 *  1. MediaPipe 0.10.14 → mesmo problema (.so desalinhado)
 *  2. ML Kit Face Detection 16.1.7 → mesmo problema
 *  3. Tentar versões mais novas do ML Kit / variantes bundled
 *
 * Caminhos a explorar no futuro (quando libs forem atualizadas):
 *  - `com.google.mlkit:face-detection:17.x+` (se sair)
 *  - FaceNet / InsightFace via TFLite (tflite 2.15+ tem .so alinhado)
 *  - Huawei HMS ML Kit (independente do Google Play Services)
 *  - Subprocesso Java isolado pra `dlopen` (workaround pesado)
 *
 * As assinaturas dos métodos foram mantidas iguais à versão ML Kit
 * pra que o `SelfieCheckinViewModel` compile sem mudanças — quando
 * reativarmos a detecção real, basta trocar o construtor.
 */
class FaceAnalyzer(context: Context) {

    var inicializado: Boolean = false
        private set

    var erroInicializacao: String? =
        "Detector facial desabilitado: libs nativas do Google (ML Kit / MediaPipe) " +
        "vêm com segmentos LOAD não alinhados em 16 KB, o que faz o Android 15+ " +
        "abortar o app no startup com SIGABRT. Veja FaceAnalyzer.kt pra detalhes."
        private set

    fun detectar(bitmap: Bitmap): FaceRef? = null
    fun boundingBox(face: FaceRef, largura: Int, altura: Int): Rect =
        Rect(0, 0, largura, altura)
    fun landmarkNariz(face: FaceRef): PointF? = null
    fun assinaturaGeometrica(face: FaceRef): FloatArray? = null
    fun similaridadeGeometrica(a: FloatArray, b: FloatArray): Float = 0f
    fun fechar() {}

    /**
     * Placeholder opaco do tipo `Face` do ML Kit — só existe pra
     * manter a assinatura idêntica à versão real. Quando reativarmos
     * a detecção, voltamos a retornar `com.google.mlkit.vision.face.Face`
     * diretamente e removemos este placeholder.
     */
    object FaceRef
}
