package com.agrotech.app.data.repository.local

import com.agrotech.app.data.local.dao.UnidadeDao
import com.agrotech.app.data.local.entities.UnidadeEntity
import com.agrotech.app.data.repository.UnidadeRepository
import kotlinx.coroutines.flow.Flow

class LocalUnidadeRepository(private val dao: UnidadeDao) : UnidadeRepository {
    override fun observarUnidades(): Flow<List<UnidadeEntity>> = dao.observarTodas()

    override suspend fun buscarUnidade(id: Long): UnidadeEntity? = dao.buscarPorId(id)

    override suspend fun criarUnidade(nome: String): Long =
        dao.inserir(UnidadeEntity(nome = nome))

    override suspend fun renomearUnidade(unidade: UnidadeEntity, novoNome: String) {
        dao.atualizar(unidade.copy(nome = novoNome))
    }

    override suspend fun removerUnidade(unidade: UnidadeEntity) {
        dao.remover(unidade)
    }
}
