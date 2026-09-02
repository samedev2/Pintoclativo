package com.agrotech.app.data.repository.local

import com.agrotech.app.data.local.dao.LoteDao
import com.agrotech.app.data.local.entities.LoteEntity
import com.agrotech.app.data.repository.LoteRepository
import kotlinx.coroutines.flow.Flow

class LocalLoteRepository(private val dao: LoteDao) : LoteRepository {
    override fun observarLotesPorUnidade(unidadeId: Long): Flow<List<LoteEntity>> =
        dao.observarPorUnidade(unidadeId)

    override fun observarLote(loteId: Long): Flow<LoteEntity?> =
        dao.observarPorId(loteId)

    override suspend fun salvarLote(lote: LoteEntity): Long =
        if (lote.id == 0L) {
            dao.inserir(lote)
        } else {
            dao.atualizar(lote)
            lote.id
        }

    override suspend fun removerLote(lote: LoteEntity) {
        dao.remover(lote)
    }
}
