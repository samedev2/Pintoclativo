package com.agrotech.app.data.mock

/**
 * Guarda o resultado do último lançamento (fechamento diário ou recebimento de ração) para a tela
 * de sucesso mostrar. É um holder simples porque o fluxo inteiro é mockado (sem Room nem rede) —
 * evita carregar argumentos grandes pela navegação por enquanto.
 */
object UltimoLancamento {
    var titulo: String = ""
    var subtitulo: String = ""
    var linhas: List<Pair<String, String>> = emptyList()
}
