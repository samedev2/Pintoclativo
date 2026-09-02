package com.agrotech.app.data.local

import androidx.room.TypeConverter
import com.agrotech.app.data.local.entities.CheckpointPeso
import com.agrotech.app.data.local.entities.DiaSemana
import com.agrotech.app.data.local.entities.Genero
import com.agrotech.app.data.local.entities.TipoRacao

class Converters {
    @TypeConverter
    fun generoToString(valor: Genero): String = valor.name

    @TypeConverter
    fun stringToGenero(valor: String): Genero = Genero.valueOf(valor)

    @TypeConverter
    fun diaSemanaToString(valor: DiaSemana): String = valor.name

    @TypeConverter
    fun stringToDiaSemana(valor: String): DiaSemana = DiaSemana.valueOf(valor)

    @TypeConverter
    fun tipoRacaoToString(valor: TipoRacao): String = valor.name

    @TypeConverter
    fun stringToTipoRacao(valor: String): TipoRacao = TipoRacao.valueOf(valor)

    @TypeConverter
    fun checkpointToString(valor: CheckpointPeso): String = valor.name

    @TypeConverter
    fun stringToCheckpoint(valor: String): CheckpointPeso = CheckpointPeso.valueOf(valor)
}
