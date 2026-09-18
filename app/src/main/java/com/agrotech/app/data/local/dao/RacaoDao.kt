package com.agrotech.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.agrotech.app.data.local.entities.RecebimentoRacaoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RacaoDao {
    @Query("SELECT * FROM recebimento_racao WHERE loteId = :loteId ORDER BY data DESC")
    fun observarPorLote(loteId: Long): Flow<List<RecebimentoRacaoEntity>>

    @Query("SELECT * FROM recebimento_racao ORDER BY data DESC")
    fun observarTodos(): Flow<List<RecebimentoRacaoEntity>>

    @Insert
    suspend fun inserir(recebimento: RecebimentoRacaoEntity): Long

    @Update
    suspend fun atualizar(recebimento: RecebimentoRacaoEntity)

    @Delete
    suspend fun remover(recebimento: RecebimentoRacaoEntity)
}
