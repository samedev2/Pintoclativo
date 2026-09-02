package com.agrotech.app.data.repository

import com.agrotech.app.data.local.entities.CheckpointPeso
import com.agrotech.app.data.local.entities.PesagemEntity
import kotlinx.coroutines.flow.Flow

interface PesagemRepository {
    fun observarPorLote(loteId: Long): Flow<List<PesagemEntity>>
    suspend fun salvarPeso(loteId: Long, checkpoint: CheckpointPeso, pesoKg: Double)
}
