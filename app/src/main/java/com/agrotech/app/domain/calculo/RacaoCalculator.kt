package com.agrotech.app.domain.calculo

import com.agrotech.app.data.local.entities.RecebimentoRacaoEntity
import java.util.concurrent.TimeUnit

object RacaoCalculator {

    /**
     * Agrupa os recebimentos por semana desde o alojamento (semana 1 = dias 1-7),
     * mesma convenção usada em [MortalidadeCalculator]. Semanas sem recebimento não
     * aparecem no resultado.
     */
    fun agruparPorSemana(
        recebimentos: List<RecebimentoRacaoEntity>,
        dataAlojamento: Long
    ): List<Pair<Int, Double>> {
        if (recebimentos.isEmpty()) return emptyList()

        return recebimentos
            .groupBy { semanaDoRecebimento(it.data, dataAlojamento) }
            .toSortedMap()
            .map { (semana, itens) -> semana to itens.sumOf { it.quantidadeKg } }
    }

    fun semanaAtual(dataAlojamento: Long, agora: Long = System.currentTimeMillis()): Int =
        semanaDoRecebimento(agora, dataAlojamento)

    private fun semanaDoRecebimento(data: Long, dataAlojamento: Long): Int {
        val dias = TimeUnit.MILLISECONDS.toDays(data - dataAlojamento).coerceAtLeast(0)
        return (dias / 7).toInt() + 1
    }
}
