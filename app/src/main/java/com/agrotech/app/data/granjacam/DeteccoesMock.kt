package com.agrotech.app.data.granjacam

import android.content.Context
import org.json.JSONObject

/** Classe dos baldes amarelos: comedouros fixos, que aparecem nas detecções mas não são aves. */
const val CLASSE_COMEDOURO = "comedouro"

/** Uma detecção num quadro (ave ou comedouro). Caixa normalizada (0 a 1) em relação ao tamanho do vídeo. */
data class AveDetectada(
    val id: Int,
    val classe: String,
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float
)

/**
 * Detecções pré-calculadas pelo modelo real do GranjaCam (`pinteiro.pt` + ByteTrack + identidade
 * persistente) sobre o vídeo de teste `granjacam_pintos.mp4`. Só existe para o modo mock: em produção
 * quem detecta é o serviço GranjaCam, em outro ambiente.
 */
class DeteccoesMock(
    private val fps: Float,
    private val quadros: List<List<AveDetectada>>
) {
    fun quadroEm(posicaoMs: Long): List<AveDetectada> {
        if (quadros.isEmpty()) return emptyList()
        val indice = (posicaoMs / 1000f * fps + 0.001f).toInt().coerceIn(0, quadros.lastIndex)
        return quadros[indice]
    }

    companion object {
        private const val ARQUIVO = "granjacam/deteccoes.json"

        fun carregar(context: Context): DeteccoesMock {
            val texto = context.assets.open(ARQUIVO).bufferedReader().use { it.readText() }
            val raiz = JSONObject(texto)
            val quadrosJson = raiz.getJSONArray("quadros")
            val quadros = ArrayList<List<AveDetectada>>(quadrosJson.length())
            for (i in 0 until quadrosJson.length()) {
                val lista = quadrosJson.getJSONObject(i).getJSONArray("d")
                val aves = ArrayList<AveDetectada>(lista.length())
                for (j in 0 until lista.length()) {
                    val d = lista.getJSONArray(j)
                    aves.add(
                        AveDetectada(
                            id = d.getInt(0),
                            classe = d.getString(1),
                            x1 = d.getDouble(3).toFloat(),
                            y1 = d.getDouble(4).toFloat(),
                            x2 = d.getDouble(5).toFloat(),
                            y2 = d.getDouble(6).toFloat()
                        )
                    )
                }
                quadros.add(aves)
            }
            return DeteccoesMock(raiz.getDouble("fps").toFloat(), quadros)
        }
    }
}
