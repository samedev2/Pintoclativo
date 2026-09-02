package com.agrotech.app.domain.calculo

import com.agrotech.app.data.local.entities.DiaSemana
import com.agrotech.app.data.local.entities.MortalidadeDiariaEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MortalidadeCalculatorTest {

    @Test
    fun `calcula saldo e percentuais acumulados ao longo de duas semanas`() {
        val registros = listOf(
            MortalidadeDiariaEntity(loteId = 1, semana = 1, diaSemana = DiaSemana.SEXTA, mortalidade = 20, descarte = 60),
            MortalidadeDiariaEntity(loteId = 1, semana = 1, diaSemana = DiaSemana.SABADO, mortalidade = 17, descarte = 30),
            MortalidadeDiariaEntity(loteId = 1, semana = 2, diaSemana = DiaSemana.SEXTA, mortalidade = 12, descarte = 14)
        )

        val resumo = MortalidadeCalculator.calcularResumoPorSemana(registros, qtdAves = 44000)

        assertEquals(2, resumo.size)

        val semana1 = resumo[0]
        assertEquals(1, semana1.semana)
        assertEquals(127, semana1.total)
        assertEquals(44000 - 127, semana1.saldo)

        val semana2 = resumo[1]
        assertEquals(26, semana2.total)
        assertEquals(semana1.saldo - 26, semana2.saldo)
        assertEquals(
            semana1.percentualAcumulado + semana2.percentualSemana,
            semana2.percentualAcumulado,
            1e-9
        )
    }

    @Test
    fun `retorna lista vazia quando quantidade de aves e invalida`() {
        val resumo = MortalidadeCalculator.calcularResumoPorSemana(emptyList(), qtdAves = 0)
        assertEquals(0, resumo.size)
    }
}
