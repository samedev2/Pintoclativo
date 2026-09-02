package com.agrotech.app.data.repository

import com.agrotech.app.data.local.entities.DiaSemana
import com.agrotech.app.data.local.entities.MortalidadeDiariaEntity
import kotlinx.coroutines.flow.Flow

interface MortalidadeRepository {
    fun observarPorLote(loteId: Long): Flow<List<MortalidadeDiariaEntity>>
    suspend fun salvarRegistroDiario(
        loteId: Long,
        semana: Int,
        dia: DiaSemana,
        mortalidade: Int,
        descarte: Int
    )
}
