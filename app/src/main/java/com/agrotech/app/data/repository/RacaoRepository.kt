package com.agrotech.app.data.repository

import com.agrotech.app.data.local.entities.RecebimentoRacaoEntity
import kotlinx.coroutines.flow.Flow

interface RacaoRepository {
    fun observarPorLote(loteId: Long): Flow<List<RecebimentoRacaoEntity>>
    fun observarTodos(): Flow<List<RecebimentoRacaoEntity>>
    suspend fun salvarRecebimento(recebimento: RecebimentoRacaoEntity): Long
    suspend fun removerRecebimento(recebimento: RecebimentoRacaoEntity)
}
