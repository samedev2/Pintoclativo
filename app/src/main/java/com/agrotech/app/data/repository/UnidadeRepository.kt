package com.agrotech.app.data.repository

import com.agrotech.app.data.local.entities.UnidadeEntity
import kotlinx.coroutines.flow.Flow

interface UnidadeRepository {
    fun observarUnidades(): Flow<List<UnidadeEntity>>
    suspend fun buscarUnidade(id: Long): UnidadeEntity?
    suspend fun criarUnidade(nome: String): Long
    suspend fun renomearUnidade(unidade: UnidadeEntity, novoNome: String)
    suspend fun removerUnidade(unidade: UnidadeEntity)
}
