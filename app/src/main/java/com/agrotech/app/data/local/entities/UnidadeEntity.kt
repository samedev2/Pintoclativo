package com.agrotech.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unidades")
data class UnidadeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String
)
