package com.agrotech.app.domain.calculo

import com.agrotech.app.data.local.entities.MortalidadeDiariaEntity

data class ResumoSemana(
    val semana: Int,
    val totalMortalidade: Int,
    val totalDescarte: Int,
    val total: Int,
    val percentualSemana: Double,
    val percentualAcumulado: Double,
    val saldo: Int
)

object MortalidadeCalculator {

    /**
     * Saldo = qtdAves - total acumulado (mortalidade + descarte) até a semana.
     * %Sem = total da semana / qtdAves. %Acum = soma dos %Sem até a semana.
     */
    fun calcularResumoPorSemana(
        registros: List<MortalidadeDiariaEntity>,
        qtdAves: Int
    ): List<ResumoSemana> {
        if (qtdAves <= 0) return emptyList()

        val porSemana = registros.groupBy { it.semana }.toSortedMap()

        var saldoAtual = qtdAves
        var percAcumulado = 0.0
        val resultado = mutableListOf<ResumoSemana>()

        for ((semana, registrosSemana) in porSemana) {
            val totalMort = registrosSemana.sumOf { it.mortalidade }
            val totalDesc = registrosSemana.sumOf { it.descarte }
            val total = totalMort + totalDesc
            val percSemana = total.toDouble() / qtdAves
            percAcumulado += percSemana
            saldoAtual -= total

            resultado += ResumoSemana(
                semana = semana,
                totalMortalidade = totalMort,
                totalDescarte = totalDesc,
                total = total,
                percentualSemana = percSemana,
                percentualAcumulado = percAcumulado,
                saldo = saldoAtual
            )
        }
        return resultado
    }
}
