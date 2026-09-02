package com.agrotech.app.navigation

import android.net.Uri

object Rotas {
    const val UNIDADES = "unidades"

    const val LOTES = "unidades/{unidadeId}/{unidadeNome}/lotes"
    fun lotes(unidadeId: Long, unidadeNome: String) =
        "unidades/$unidadeId/${Uri.encode(unidadeNome)}/lotes"

    const val NOVO_LOTE = "unidades/{unidadeId}/lotes/novo"
    fun novoLote(unidadeId: Long) = "unidades/$unidadeId/lotes/novo"

    const val LOTE_DETALHE = "lotes/{loteId}"
    fun loteDetalhe(loteId: Long) = "lotes/$loteId"

    const val NOVO_RECEBIMENTO = "lotes/{loteId}/racao/novo"
    fun novoRecebimento(loteId: Long) = "lotes/$loteId/racao/novo"

    const val OCR_CAMERA = "ocr"
}
