package com.agrotech.app.data.repository.local

import com.agrotech.app.data.local.dao.PesagemDao
import com.agrotech.app.data.local.entities.CheckpointPeso
import com.agrotech.app.data.local.entities.PesagemEntity
import com.agrotech.app.data.repository.PesagemRepository
import kotlinx.coroutines.flow.Flow

class LocalPesagemRepository(private val dao: PesagemDao) : PesagemRepository {
    override fun observarPorLote(loteId: Long): Flow<List<PesagemEntity>> =
        dao.observarPorLote(loteId)

    override fun observarTodos(): Flow<List<PesagemEntity>> = dao.observarTodos()

    override suspend fun salvarPeso(loteId: Long, checkpoint: CheckpointPeso, pesoKg: Double) {
        val existente = dao.buscarRegistro(loteId, checkpoint)
        if (existente != null) {
            dao.atualizar(existente.copy(pesoKg = pesoKg))
        } else {
            dao.inserir(PesagemEntity(loteId = loteId, checkpoint = checkpoint, pesoKg = pesoKg))
        }
    }
}
