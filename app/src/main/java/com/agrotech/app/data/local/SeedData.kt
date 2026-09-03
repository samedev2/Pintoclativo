package com.agrotech.app.data.local

import com.agrotech.app.data.local.entities.CheckpointPeso
import com.agrotech.app.data.local.entities.DiaSemana
import com.agrotech.app.data.local.entities.Genero
import com.agrotech.app.data.local.entities.LoteEntity
import com.agrotech.app.data.local.entities.MortalidadeDiariaEntity
import com.agrotech.app.data.local.entities.PesagemEntity
import com.agrotech.app.data.local.entities.RecebimentoRacaoEntity
import com.agrotech.app.data.local.entities.TipoRacao
import com.agrotech.app.data.local.entities.UnidadeEntity
import java.util.Calendar
import java.util.TimeZone

/**
 * Dados de demonstração baseados na ficha "Controle Técnico de Frango de
 * Corte" (MRACHO / Vitallis, Lote 2) usada como referência pelo usuário.
 *
 * O [SeedRunner.popularSeVazio] só insere se as tabelas estiverem vazias
 * — uma vez que o usuário cadastrar uma unidade ou lote próprio, o seed
 * não é reaplicado (o estado local do app é preservado).
 */
object SeedData {

    fun unidadeVitallis(): UnidadeEntity = UnidadeEntity(nome = "Vitallis")

    /**
     * Lote 2: macho, ROSS AP95, 44.000 aves, metragem 3.240 m²,
     * densidade 13,58, alojado em 23/07/2026, peso inicial 41,68 kg.
     * Vazia 12 dias antes do alojamento, mortalidade de transporte 0,
     * distribuição 1. Reproduz o cabeçalho da ficha.
     */
    fun lote2Vitallis(unidadeId: Long): LoteEntity = LoteEntity(
        unidadeId = unidadeId,
        numeroLote = "2",
        genero = Genero.MACHO,
        metragem = 3240.0,
        qtdAves = 44000,
        linhagem = "ROSS AP95",
        dataAlojamento = millis("23/07/2026"),
        densidade = 13.58,
        pesoInicial = 41.68,
        percMortTransp = 0.0,
        diasVazio = 12,
        distribuicaoLote = "1"
    )

    /**
     * Mortalidade da Semana 4 do Lote 2 (a mais recente, equivalente ao
     * que aparece no cabeçalho "Sem. 4" da ficha anexada).
     *  Sex:    M=70  D=19 → 89
     *  Sáb:    M=42  D=18 → 60
     *  Dom:    M=60  D=39 → 99
     *  Seg:    M=55  D=20 → 75
     *  Ter:    M=65  D=30 → 95
     *  Qua:    M=20  D=30 → 50
     *  Qui:    M=14  D=16 → 30
     *  Total: 498 aves (1,13% do lote).
     */
    fun mortalidadeSemana4(loteId: Long): List<MortalidadeDiariaEntity> = listOf(
        MortalidadeDiariaEntity(loteId = loteId, semana = 4, diaSemana = DiaSemana.SEXTA,   mortalidade = 70, descarte = 19),
        MortalidadeDiariaEntity(loteId = loteId, semana = 4, diaSemana = DiaSemana.SABADO,  mortalidade = 42, descarte = 18),
        MortalidadeDiariaEntity(loteId = loteId, semana = 4, diaSemana = DiaSemana.DOMINGO, mortalidade = 60, descarte = 39),
        MortalidadeDiariaEntity(loteId = loteId, semana = 4, diaSemana = DiaSemana.SEGUNDA, mortalidade = 55, descarte = 20),
        MortalidadeDiariaEntity(loteId = loteId, semana = 4, diaSemana = DiaSemana.TERCA,   mortalidade = 65, descarte = 30),
        MortalidadeDiariaEntity(loteId = loteId, semana = 4, diaSemana = DiaSemana.QUARTA,  mortalidade = 20, descarte = 30),
        MortalidadeDiariaEntity(loteId = loteId, semana = 4, diaSemana = DiaSemana.QUINTA,  mortalidade = 14, descarte = 16)
    )

    /**
     * Checkpoints de pesagem da ficha. Os 3 primeiros lançados, os 2
     * últimos ficam pendentes (esmaecidos na UI, idêntico ao mockup).
     */
    fun pesagensLote2(loteId: Long): List<PesagemEntity> = listOf(
        PesagemEntity(loteId = loteId, checkpoint = CheckpointPeso.D07, pesoKg = 0.234),
        PesagemEntity(loteId = loteId, checkpoint = CheckpointPeso.D21, pesoKg = 1.365),
        PesagemEntity(loteId = loteId, checkpoint = CheckpointPeso.D28, pesoKg = 2.200)
    )

    /**
     * Recebimentos de ração mais recentes do Lote 2 (do bloco
     * "Recebimento de Ração" da ficha). Estão em ordem decrescente de
     * data — o mais recente primeiro, igual à consulta do DAO.
     */
    fun recebimentosLote2(loteId: Long): List<RecebimentoRacaoEntity> = listOf(
        RecebimentoRacaoEntity(loteId = loteId, data = millis("19/08/2026"), numeroNota = "161246", tipoRacao = TipoRacao.ENGORDA_2, quantidadeKg = 18080.0),
        RecebimentoRacaoEntity(loteId = loteId, data = millis("17/08/2026"), numeroNota = "161647", tipoRacao = TipoRacao.ENGORDA_2, quantidadeKg = 12850.0),
        RecebimentoRacaoEntity(loteId = loteId, data = millis("15/08/2026"), numeroNota = "161580", tipoRacao = TipoRacao.ENGORDA_2, quantidadeKg = 13710.0),
        RecebimentoRacaoEntity(loteId = loteId, data = millis("12/08/2026"), numeroNota = "161434", tipoRacao = TipoRacao.ENGORDA_2, quantidadeKg =  3900.0),
        RecebimentoRacaoEntity(loteId = loteId, data = millis("12/08/2026"), numeroNota = "161438", tipoRacao = TipoRacao.ENGORDA_1, quantidadeKg = 12180.0)
    )

    private fun millis(ddMMyyyy: String): Long {
        val partes = ddMMyyyy.split("/")
        val cal = Calendar.getInstance(TimeZone.getTimeZone("America/Fortaleza"))
        cal.clear()
        cal.set(partes[2].toInt(), partes[1].toInt() - 1, partes[0].toInt(), 8, 0, 0)
        return cal.timeInMillis
    }
}
