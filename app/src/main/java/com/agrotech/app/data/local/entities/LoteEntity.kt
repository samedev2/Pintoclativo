package com.agrotech.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Genero(val label: String) {
    MACHO("Macho"), FEMEA("Fêmea")
}

@Entity(
    tableName = "lotes",
    foreignKeys = [
        ForeignKey(
            entity = UnidadeEntity::class,
            parentColumns = ["id"],
            childColumns = ["unidadeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("unidadeId")]
)
data class LoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unidadeId: Long,
    val numeroLote: String,
    val genero: Genero,
    val metragem: Double,
    val qtdAves: Int,
    val linhagem: String,
    val dataAlojamento: Long,
    val densidade: Double,
    val pesoInicial: Double,
    val percMortTransp: Double,
    val diasVazio: Int = 0,
    val distribuicaoLote: String = ""
)
