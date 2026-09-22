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

    /** Câmera ao vivo (GranjaCam), aberta pelo card "Acompanhar em tempo real" do Início. */
    const val CAMERA_AO_VIVO = "camera-ao-vivo"

    /** Foto da granja pelo celular (câmera/galeria), acessada pela aba Mais. */
    const val FOTO_CELULAR = "foto-celular"

    /** Sub-tela: fechamento diário (mortalidade, descarte, ração usada, peso opcional). */
    const val FECHAMENTO_DIARIO = "lancamentos/fechamento"

    /** Sub-tela: recebimento de ração (nota, tipo, quantidade, fornecedor). */
    const val RECEBIMENTO_RACAO = "lancamentos/recebimento"

    /** Sub-tela: confirmação de sucesso, comum ao fechamento diário e ao recebimento de ração. */
    const val LANCAMENTO_SALVO = "lancamentos/salvo"

    /** Relatórios (mortalidade, peso, ração, check-ins), acessados pela aba Mais. */
    const val RELATORIOS_RAIZ = "relatorios"

    /** Tela principal (autenticado). Hospeda a BottomNavBar com 5 abas
     *  (Início, Lançar, Fotos, Relatórios, Mais) + sub-telas de Lotes. */
    const val MAIN = "main"

    /** Tela de detalhe de um relatório (mortalidade, peso, ração, check-ins).
     *  [tipo] identifica qual relatório mostrar — ver [RelatorioTipo]. */
    const val RELATORIO_DETALHE = "relatorios/{tipo}"
    fun relatorioDetalhe(tipo: String) = "relatorios/$tipo"
}
