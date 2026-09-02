package com.agrotech.app.data.repository.local

import com.agrotech.app.data.local.dao.RacaoDao
import com.agrotech.app.data.local.entities.RecebimentoRacaoEntity
import com.agrotech.app.data.repository.RacaoRepository
import kotlinx.coroutines.flow.Flow

class LocalRacaoRepository(private val dao: RacaoDao) : RacaoRepository {
    override fun observarPorLote(loteId: Long): Flow<List<RecebimentoRacaoEntity>> =
        dao.observarPorLote(loteId)

    override suspend fun salvarRecebimento(recebimento: RecebimentoRacaoEntity): Long =
        if (recebimento.id == 0L) {
            dao.inserir(recebimento)
        } else {
            dao.atualizar(recebimento)
            recebimento.id
        }

    override suspend fun removerRecebimento(recebimento: RecebimentoRacaoEntity) {
        dao.remover(recebimento)
    }
}
