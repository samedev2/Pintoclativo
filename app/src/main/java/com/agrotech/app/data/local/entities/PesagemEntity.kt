package com.agrotech.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CheckpointPeso(val dias: Int, val label: String) {
    D07(7, "07 dias"),
    D21(21, "21 dias"),
    D28(28, "28 dias"),
    D35(35, "35 dias"),
    D40(40, "40 dias")
}

@Entity(
    tableName = "pesagens",
    foreignKeys = [
        ForeignKey(
            entity = LoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["loteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("loteId"),
        Index(value = ["loteId", "checkpoint"], unique = true)
    ]
)
data class PesagemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loteId: Long,
    val checkpoint: CheckpointPeso,
    val pesoKg: Double
)
