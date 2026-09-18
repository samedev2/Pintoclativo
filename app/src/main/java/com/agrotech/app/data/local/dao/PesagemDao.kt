package com.agrotech.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.agrotech.app.data.local.entities.CheckpointPeso
import com.agrotech.app.data.local.entities.PesagemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PesagemDao {
    @Query("SELECT * FROM pesagens WHERE loteId = :loteId")
    fun observarPorLote(loteId: Long): Flow<List<PesagemEntity>>

    @Query("SELECT * FROM pesagens ORDER BY id DESC")
    fun observarTodos(): Flow<List<PesagemEntity>>

    @Query("SELECT * FROM pesagens WHERE loteId = :loteId AND checkpoint = :checkpoint LIMIT 1")
    suspend fun buscarRegistro(loteId: Long, checkpoint: CheckpointPeso): PesagemEntity?

    @Insert
    suspend fun inserir(pesagem: PesagemEntity): Long

    @Update
    suspend fun atualizar(pesagem: PesagemEntity)
}
