package com.agrotech.app.data.checkin

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.min

/**
 * Hash perceptual (pHash simplificado) usado como "impressão digital"
 * da face do usuário. Não é um embedding facial real (precisaria de
 * modelo treinado tipo FaceNet/ArcFace) — é um proxy baseado em
 * luminância downsampled que funciona bem o suficiente pro demo:
 *
 * - Reduz pra 8x8 em escala de cinza.
 * - Compara a média com cada pixel.
 * - Gera 64 bits (16 chars hex).
 *
 * Similaridade = 1 - (hamming / 64). 95% → ≤ 3 bits diferentes.
 */
object FaceHashUtil {

    private const val LARGURA = 8
    private const val ALTURA = 8
    private const val BITS = LARGURA * ALTURA

    /** Faz o crop central da face (a bounding box vinda do FaceLandmarker) e gera o hash. */
    fun hashDaFace(bitmap: Bitmap): String {
        // 1. Converte pra escala de cinza + downsample 8x8.
        val escalaCinza = ByteArray(BITS)
        val w = bitmap.width
        val h = bitmap.height
        val blocoW = w.toFloat() / LARGURA
        val blocoH = h.toFloat() / ALTURA

        for (y in 0 until ALTURA) {
            for (x in 0 until LARGURA) {
                // Pega o pixel do centro do bloco.
                val px = min((x * blocoW + blocoW / 2).toInt(), w - 1)
                val py = min((y * blocoH + blocoH / 2).toInt(), h - 1)
                val pixel = bitmap.getPixel(px, py)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)
                // Luminância (0..255) — BT.601.
                escalaCinza[y * LARGURA + x] = ((0.299 * r + 0.587 * g + 0.114 * b).toInt()).toByte()
            }
        }

        // 2. Calcula a média dos pixels.
        val media = escalaCinza.map { it.toInt() and 0xFF }.average()

        // 3. Cada bit = 1 se pixel > média, 0 caso contrário.
        val bits = StringBuilder()
        for (b in escalaCinza) {
            bits.append(if ((b.toInt() and 0xFF) > media) '1' else '0')
        }

        // 4. Converte os 64 bits em 16 chars hex.
        val hex = StringBuilder()
        var i = 0
        while (i < BITS) {
            val bloco4 = bits.substring(i, i + 4)
            hex.append(bloco4.toInt(2).toString(16))
            i += 4
        }
        return hex.toString()
    }

    /**
     * @return similaridade 0.0..1.0 (1.0 = idêntico, 0.0 = completamente
     *         diferente). Threshold recomendado: ≥ 0.95 (≥ 95% de match).
     */
    fun similaridade(hashA: String, hashB: String): Float {
        if (hashA.length != hashB.length) return 0f
        // Hamming distance entre os bits.
        val a = hexParaBits(hashA)
        val b = hexParaBits(hashB)
        var diferentes = 0
        for (i in a.indices) if (a[i] != b[i]) diferentes++
        return 1f - (diferentes.toFloat() / BITS)
    }

    private fun hexParaBits(hex: String): String {
        // 16 chars hex → 64 bits
        val bits = StringBuilder(64)
        for (c in hex) {
            val n = c.digitToInt(16)
            bits.append(((n shr 3) and 1).toString())
            bits.append(((n shr 2) and 1).toString())
            bits.append(((n shr 1) and 1).toString())
            bits.append((n and 1).toString())
        }
        return bits.toString()
    }
}
