package com.agrotech.app.domain.calculo

import com.agrotech.app.data.local.entities.RecebimentoRacaoEntity
import com.agrotech.app.data.local.entities.TipoRacao
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class RacaoCalculatorTest {

    private val umDia = TimeUnit.DAYS.toMillis(1)

    @Test
    fun `agrupa recebimentos por semana desde o alojamento`() {
        val alojamento = 0L
        val recebimentos = listOf(
            RecebimentoRacaoEntity(loteId = 1, data = 2 * umDia, numeroNota = "1", tipoRacao = TipoRacao.PRE_INICIAL, quantidadeKg = 100.0),
            RecebimentoRacaoEntity(loteId = 1, data = 5 * umDia, numeroNota = "2", tipoRacao = TipoRacao.PRE_INICIAL, quantidadeKg = 50.0),
            RecebimentoRacaoEntity(loteId = 1, data = 9 * umDia, numeroNota = "3", tipoRacao = TipoRacao.INICIAL, quantidadeKg = 200.0)
        )

        val resultado = RacaoCalculator.agruparPorSemana(recebimentos, alojamento)

        assertEquals(listOf(1 to 150.0, 2 to 200.0), resultado)
    }

    @Test
    fun `retorna lista vazia quando nao ha recebimentos`() {
        assertEquals(emptyList<Pair<Int, Double>>(), RacaoCalculator.agruparPorSemana(emptyList(), 0L))
    }

    @Test
    fun `semana atual e calculada a partir de agora`() {
        val alojamento = 0L
        val agora = 10 * umDia
        assertEquals(2, RacaoCalculator.semanaAtual(alojamento, agora))
    }
}
