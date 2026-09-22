package com.agrotech.app.data.mock

/**
 * Dados de demonstração do novo design (Início, Lançamentos). Ficam fixos até existir backend:
 * o modelo Room atual tem Unidade e Lote, mas ainda não tem "Aviário" nem fechamento diário.
 */
object MockData {
    const val LOTES_ATIVOS = 4
    const val TOTAL_AVIARIOS = 6
    const val MORTALIDADE_7D = "0,8%"
    const val RACAO_HOJE = "1.250 kg"
    const val PESO_MEDIO = "1,82 kg"

    /** Aviário → lote em andamento nele. */
    val aviarios: List<Pair<String, String>> = listOf(
        "Aviário 1" to "Lote 24",
        "Aviário 2" to "Lote 25",
        "Aviário 3" to "Lote 26",
        "Aviário 4" to "Lote 27",
        "Aviário 5" to "Lote 28",
        "Aviário 6" to "Lote 29"
    )

    // === Fechamento diário / recebimento de ração (telas do Figma) ===
    const val LOTE_ATUAL = "02"
    const val DIA_ATUAL = 15
    const val SEMANA_ATUAL = 3
    const val SALDO_ANTES_DO_FECHAMENTO = 44000

    val tiposRacao: List<String> = listOf("Pré-inicial", "Inicial", "Engorda 1", "Engorda 2", "Final 1", "Final 2")
    val fornecedores: List<String> = listOf("AgroNutri", "Nutriave", "Ração Vale Verde")
}
