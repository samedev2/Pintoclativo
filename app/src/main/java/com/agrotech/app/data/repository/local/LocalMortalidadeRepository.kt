package com.agrotech.app.data.repository.local

import com.agrotech.app.data.local.dao.MortalidadeDao
import com.agrotech.app.data.local.entities.DiaSemana
import com.agrotech.app.data.local.entities.MortalidadeDiariaEntity
import com.agrotech.app.data.repository.MortalidadeRepository
import kotlinx.coroutines.flow.Flow

class LocalMortalidadeRepository(private val dao: MortalidadeDao) : MortalidadeRepository {
    override fun observarPorLote(loteId: Long): Flow<List<MortalidadeDiariaEntity>> =
        dao.observarPorLote(loteId)

    override suspend fun salvarRegistroDiario(
        loteId: Long,
        semana: Int,
        dia: DiaSemana,
        mortalidade: Int,
        descarte: Int
    ) {
        val existente = dao.buscarRegistro(loteId, semana, dia)
        if (existente != null) {
            dao.atualizar(existente.copy(mortalidade = mortalidade, descarte = descarte))
        } else {
            dao.inserir(
                MortalidadeDiariaEntity(
                    loteId = loteId,
                    semana = semana,
                    diaSemana = dia,
                    mortalidade = mortalidade,
                    descarte = descarte
                )
            )
        }
    }
}
