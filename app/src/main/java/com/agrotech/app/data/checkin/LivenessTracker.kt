package com.agrotech.app.data.checkin

import android.graphics.PointF
import kotlin.math.abs

/**
 * Direções do liveness check. Cada uma corresponde a um movimento
 * da cabeça que faz o nariz se deslocar em uma direção no plano da
 * imagem.
 *
 * Convenção:
 *  - "Direita" → usuário vira a cabeça pra DIREITA → nariz se desloca
 *    pra ESQUERDA no eixo X da imagem.
 *  - "Esquerda" → usuário vira a cabeça pra ESQUERDA → nariz se desloca
 *    pra DIREITA no eixo X.
 *  - "Cima" → usuário levanta a cabeça → nariz sobe → Y diminui.
 *  - "Baixo" → usuário abaixa a cabeça → nariz desce → Y aumenta.
 */
enum class DirecaoLiveness(val label: String) {
    DIREITA("Direita"),
    ESQUERDA("Esquerda"),
    CIMA("Cima"),
    BAIXO("Baixo")
}

/**
 * Acompanha a posição do nariz (landmark do [FaceAnalyzer]) e marca
 * uma direção como "concluída" quando o usuário moveu o nariz o
 * suficiente nessa direção a partir da posição neutra.
 *
 * A posição neutra é calibrada no primeiro frame e re-recalibrada
 * cada vez que uma direção é concluída (pra exigir movimento novo
 * da posição atual). Cada direção exige deslocamento mínimo de
 * [THRESHOLD_PX] pixels.
 */
class LivenessTracker {

    private var posicaoNeutraX: Float? = null
    private var posicaoNeutraY: Float? = null
    private var ultimaPosicaoX: Float? = null
    private var ultimaPosicaoY: Float? = null
    private val concluidas = mutableSetOf<DirecaoLiveness>()

    val isCompleto: Boolean
        get() = concluidas.size == DirecaoLiveness.entries.size

    fun concluidasSet(): Set<DirecaoLiveness> = concluidas.toSet()

    /**
     * Alimenta o tracker com a posição do nariz no frame atual. Retorna
     * `true` se a direção pedida acabou de ser concluída NESTE frame.
     */
    fun alimentar(ponto: PointF, direcaoEsperada: DirecaoLiveness?): Boolean {
        return alimentar(ponto.x, ponto.y, direcaoEsperada)
    }

    /**
     * Overload com x/y separados — útil pra testes ou quando a posição
     * vem de uma fonte que não é PointF.
     */
    fun alimentar(x: Float, y: Float, direcaoEsperada: DirecaoLiveness?): Boolean {
        if (posicaoNeutraX == null) {
            posicaoNeutraX = x
            posicaoNeutraY = y
            ultimaPosicaoX = x
            ultimaPosicaoY = y
            return false
        }
        if (concluidas.contains(direcaoEsperada)) return false
        val neutraX = posicaoNeutraX ?: return false
        val neutraY = posicaoNeutraY ?: return false

        val deltaX = x - neutraX
        val deltaY = y - neutraY

        val atingiu = when (direcaoEsperada) {
            DirecaoLiveness.DIREITA -> deltaX < -THRESHOLD_PX
            DirecaoLiveness.ESQUERDA -> deltaX > THRESHOLD_PX
            DirecaoLiveness.CIMA -> deltaY < -THRESHOLD_PX
            DirecaoLiveness.BAIXO -> deltaY > THRESHOLD_PX
            null -> false
        }

        if (atingiu) {
            concluidas.add(direcaoEsperada!!)
            posicaoNeutraX = x
            posicaoNeutraY = y
            ultimaPosicaoX = x
            ultimaPosicaoY = y
            return true
        }

        // Se o usuário voltou pra perto do centro, recalibra a neutra
        // pra que o próximo deslocamento seja medido daqui.
        val voltouX = abs(x - neutraX) < THRESHOLD_PX / 2
        val voltouY = abs(y - neutraY) < THRESHOLD_PX / 2
        if (voltouX && voltouY) {
            posicaoNeutraX = x
            posicaoNeutraY = y
        }
        ultimaPosicaoX = x
        ultimaPosicaoY = y
        return false
    }

    /** Reseta o estado (re-começa o liveness do zero). */
    fun resetar() {
        posicaoNeutraX = null
        posicaoNeutraY = null
        ultimaPosicaoX = null
        ultimaPosicaoY = null
        concluidas.clear()
    }

    companion object {
        /**
         * 30px de deslocamento mínimo no espaço da imagem. Ajustado
         * pra funcionar em preview típico de câmera (640×480) sem
         * exigir rotação extrema da cabeça.
         */
        const val THRESHOLD_PX = 30f
    }
}
