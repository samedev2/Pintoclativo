package com.agrotech.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.agrotech.app.data.local.entities.UnidadeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnidadeDao {
    @Query("SELECT * FROM unidades ORDER BY nome ASC")
    fun observarTodas(): Flow<List<UnidadeEntity>>

    @Query("SELECT * FROM unidades WHERE id = :id")
    suspend fun buscarPorId(id: Long): UnidadeEntity?

    @Insert
    suspend fun inserir(unidade: UnidadeEntity): Long

    @Update
    suspend fun atualizar(unidade: UnidadeEntity)

    @Delete
    suspend fun remover(unidade: UnidadeEntity)
}
