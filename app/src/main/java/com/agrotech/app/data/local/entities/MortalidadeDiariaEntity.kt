package com.agrotech.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DiaSemana(val label: String) {
    SEXTA("Sexta"),
    SABADO("Sábado"),
    DOMINGO("Domingo"),
    SEGUNDA("Segunda"),
    TERCA("Terça"),
    QUARTA("Quarta"),
    QUINTA("Quinta")
}

@Entity(
    tableName = "mortalidade_diaria",
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
        Index(value = ["loteId", "semana", "diaSemana"], unique = true)
    ]
)
data class MortalidadeDiariaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loteId: Long,
    val semana: Int,
    val diaSemana: DiaSemana,
    val mortalidade: Int = 0,
    val descarte: Int = 0
)
