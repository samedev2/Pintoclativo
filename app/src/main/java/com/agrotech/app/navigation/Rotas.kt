package com.agrotech.app.navigation

import android.net.Uri

object Rotas {
    const val SPLASH = "splash"

    const val LOGIN = "login"

    const val INSTRUCOES_CHECKIN = "instrucoes-checkin/{email}"
    fun instrucoesCheckin(email: String) = "instrucoes-checkin/${Uri.encode(email)}"

    const val CHECKIN = "checkin/{email}"
    fun checkin(email: String) = "checkin/${Uri.encode(email)}"

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

    /** Raiz da lista de unidades/lotes (agora dentro da aba Mais). */
    const val LOTES_RAIZ = "lotes"

    /** Perfil e sair (agora dentro da aba Mais). */
    const val PERFIL = "perfil"

    const val OCR_CAMERA = "ocr"

    /** Scanner de demonstração aberto pelo botão central: QR Code ou OCR. */
    const val SCANNER = "scanner"

    /** Tela principal (autenticado). Hospeda a BottomNavBar com 5 abas
     *  (Início, Lançar, Fotos, Relatórios, Mais) + sub-telas de Lotes. */
    const val MAIN = "main"

    /** Tela de detalhe de um relatório (mortalidade, peso, ração, check-ins).
     *  [tipo] identifica qual relatório mostrar — ver [RelatorioTipo]. */
    const val RELATORIO_DETALHE = "relatorios/{tipo}"
    fun relatorioDetalhe(tipo: String) = "relatorios/$tipo"
}
