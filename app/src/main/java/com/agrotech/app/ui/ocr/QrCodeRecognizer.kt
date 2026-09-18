package com.agrotech.app.ui.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

object QrCodeRecognizer {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    suspend fun reconhecer(bitmap: Bitmap): String? =
        suspendCancellableCoroutine { continuation ->
            scanner.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { codes ->
                    continuation.resume(codes.firstNotNullOfOrNull { it.rawValue })
                }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
}
