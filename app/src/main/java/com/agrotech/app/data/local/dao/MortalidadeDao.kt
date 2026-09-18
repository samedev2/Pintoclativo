package com.agrotech.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.agrotech.app.data.local.entities.DiaSemana
import com.agrotech.app.data.local.entities.MortalidadeDiariaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MortalidadeDao {
    @Query("SELECT * FROM mortalidade_diaria WHERE loteId = :loteId ORDER BY semana ASC")
    fun observarPorLote(loteId: Long): Flow<List<MortalidadeDiariaEntity>>

    @Query("SELECT * FROM mortalidade_diaria ORDER BY id DESC")
    fun observarTodos(): Flow<List<MortalidadeDiariaEntity>>

    @Query("SELECT * FROM mortalidade_diaria WHERE loteId = :loteId AND semana = :semana AND diaSemana = :dia LIMIT 1")
    suspend fun buscarRegistro(loteId: Long, semana: Int, dia: DiaSemana): MortalidadeDiariaEntity?

    @Insert
    suspend fun inserir(registro: MortalidadeDiariaEntity): Long

    @Update
    suspend fun atualizar(registro: MortalidadeDiariaEntity)
}
