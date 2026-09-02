package com.agrotech.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.agrotech.app.data.local.entities.LoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoteDao {
    @Query("SELECT * FROM lotes WHERE unidadeId = :unidadeId ORDER BY dataAlojamento DESC")
    fun observarPorUnidade(unidadeId: Long): Flow<List<LoteEntity>>

    @Query("SELECT * FROM lotes WHERE id = :id")
    fun observarPorId(id: Long): Flow<LoteEntity?>

    @Insert
    suspend fun inserir(lote: LoteEntity): Long

    @Update
    suspend fun atualizar(lote: LoteEntity)

    @Delete
    suspend fun remover(lote: LoteEntity)
}
