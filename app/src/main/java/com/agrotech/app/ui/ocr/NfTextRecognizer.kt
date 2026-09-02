package com.agrotech.app.ui.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

object NfTextRecognizer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun reconhecerLinhas(bitmap: Bitmap): List<String> =
        suspendCancellableCoroutine { continuacao ->
            val imagem = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(imagem)
                .addOnSuccessListener { resultado ->
                    val linhas = resultado.textBlocks
                        .flatMap { bloco -> bloco.lines.map { it.text.trim() } }
                        .filter { it.isNotBlank() }
                    continuacao.resume(linhas)
                }
                .addOnFailureListener { erro -> continuacao.resumeWithException(erro) }
        }
}
