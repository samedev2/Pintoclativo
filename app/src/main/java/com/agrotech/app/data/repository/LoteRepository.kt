package com.agrotech.app.data.repository

import com.agrotech.app.data.local.entities.LoteEntity
import kotlinx.coroutines.flow.Flow

interface LoteRepository {
    fun observarLotesPorUnidade(unidadeId: Long): Flow<List<LoteEntity>>
    fun observarLote(loteId: Long): Flow<LoteEntity?>
    suspend fun salvarLote(lote: LoteEntity): Long
    suspend fun removerLote(lote: LoteEntity)
}
