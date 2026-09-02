package com.agrotech.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TipoRacao(val label: String) {
    PRE_INICIAL("Pré-inicial"),
    INICIAL("Inicial"),
    ENGORDA_1("Engorda 1"),
    ENGORDA_2("Engorda 2"),
    FINAL_1("Final 1"),
    FINAL_2("Final 2")
}

@Entity(
    tableName = "recebimento_racao",
    foreignKeys = [
        ForeignKey(
            entity = LoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["loteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("loteId")]
)
data class RecebimentoRacaoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loteId: Long,
    val data: Long,
    val numeroNota: String,
    val tipoRacao: TipoRacao,
    val quantidadeKg: Double
)
