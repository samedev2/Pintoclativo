package com.agrotech.app.data.granjacam

import android.content.Context
import org.json.JSONObject

/** Classe dos baldes amarelos: comedouros fixos, que aparecem nas detecções mas não são aves. */
const val CLASSE_COMEDOURO = "comedouro"

/** Linhas verticais de separação do cercado (haste, fios e linhas de contas vermelhas): também não são aves. */
const val CLASSE_BARRA = "barra_separacao"

/** Comedouros e barras são estruturas fixas: aparecem nas detecções mas não entram na contagem de aves. */
fun ehEstrutura(classe: String): Boolean = classe == CLASSE_COMEDOURO || classe == CLASSE_BARRA

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
 * persistente) sobre os vídeos de teste. Só existe para o modo mock: em produção quem detecta é o serviço
 * GranjaCam, em outro ambiente.
 *
 * Os vídeos tocam em sequência (lista de reprodução) e as detecções de todos ficam encadeadas aqui:
 * [quadroEm] recebe a posição total (soma dos clipes anteriores + posição dentro do clipe atual).
 */
class DeteccoesMock(
    private val fps: Float,
    private val quadros: List<List<AveDetectada>>,
    private val quadrosPorClipe: List<Int> = listOf(quadros.size)
) {
    /** Onde o clipe [indice] começa, em ms, na linha do tempo total. */
    fun inicioDoClipeMs(indice: Int): Int =
        (quadrosPorClipe.take(indice).sum() / fps * 1000f).toInt()

    fun quadroEm(posicaoMs: Long): List<AveDetectada> {
        if (quadros.isEmpty()) return emptyList()
        val indice = (posicaoMs / 1000f * fps + 0.001f).toInt().coerceIn(0, quadros.lastIndex)
        return quadros[indice]
    }

    companion object {
        /** Um arquivo de detecções por vídeo, na mesma ordem dos vídeos em res/raw. */
        private val ARQUIVOS = listOf("granjacam/deteccoes.json", "granjacam/deteccoes_2.json")

        /** Aves de clipes diferentes são aves diferentes: os ids de cada clipe ganham um deslocamento. */
        private const val DESLOCAMENTO_ID_POR_CLIPE = 100
        private const val PRIMEIRO_ID_ESTRUTURA = 1000

        fun carregar(context: Context): DeteccoesMock = carregarSequencia(context)

        fun carregarSequencia(context: Context): DeteccoesMock {
            var fps = 24f
            val todos = ArrayList<List<AveDetectada>>()
            val porClipe = ArrayList<Int>()
            ARQUIVOS.forEachIndexed { indice, arquivo ->
                val texto = context.assets.open(arquivo).bufferedReader().use { it.readText() }
                val raiz = JSONObject(texto)
                fps = raiz.getDouble("fps").toFloat()
                val quadrosJson = raiz.getJSONArray("quadros")
                for (i in 0 until quadrosJson.length()) {
                    val lista = quadrosJson.getJSONObject(i).getJSONArray("d")
                    val aves = ArrayList<AveDetectada>(lista.length())
                    for (j in 0 until lista.length()) {
                        val d = lista.getJSONArray(j)
                        val id = d.getInt(0)
                        aves.add(
                            AveDetectada(
                                id = if (id < PRIMEIRO_ID_ESTRUTURA) id + indice * DESLOCAMENTO_ID_POR_CLIPE else id,
                                classe = d.getString(1),
                                x1 = d.getDouble(3).toFloat(),
                                y1 = d.getDouble(4).toFloat(),
                                x2 = d.getDouble(5).toFloat(),
                                y2 = d.getDouble(6).toFloat()
                            )
                        )
                    }
                    todos.add(aves)
                }
                porClipe.add(quadrosJson.length())
            }
            return DeteccoesMock(fps, todos, porClipe)
        }
    }
}
