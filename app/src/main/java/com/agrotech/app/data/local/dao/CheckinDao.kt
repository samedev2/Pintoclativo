package com.agrotech.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.agrotech.app.data.local.entities.CheckinEntity

@Dao
interface CheckinDao {
    @Query("SELECT * FROM checkins WHERE email = :email ORDER BY dataHora DESC LIMIT 1")
    suspend fun ultimoCheckin(email: String): CheckinEntity?

    @Query("SELECT * FROM checkins ORDER BY dataHora DESC")
    fun observarTodos(): kotlinx.coroutines.flow.Flow<List<CheckinEntity>>

    @Query("SELECT * FROM checkins WHERE email = :email ORDER BY dataHora DESC")
    fun observarPorEmail(email: String): kotlinx.coroutines.flow.Flow<List<CheckinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(checkin: CheckinEntity): Long

    @Delete
    suspend fun remover(checkin: CheckinEntity)

    @Query("DELETE FROM checkins WHERE email = :email")
    suspend fun removerTodos(email: String)
}
