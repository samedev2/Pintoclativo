package com.agrotech.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.agrotech.app.data.local.dao.CheckinDao
import com.agrotech.app.data.local.dao.LoteDao
import com.agrotech.app.data.local.dao.MortalidadeDao
import com.agrotech.app.data.local.dao.PesagemDao
import com.agrotech.app.data.local.dao.RacaoDao
import com.agrotech.app.data.local.dao.UnidadeDao
import com.agrotech.app.data.local.dao.UserDao
import com.agrotech.app.data.local.entities.CheckinEntity
import com.agrotech.app.data.local.entities.LoteEntity
import com.agrotech.app.data.local.entities.MortalidadeDiariaEntity
import com.agrotech.app.data.local.entities.PesagemEntity
import com.agrotech.app.data.local.entities.RecebimentoRacaoEntity
import com.agrotech.app.data.local.entities.UnidadeEntity
import com.agrotech.app.data.local.entities.UserEntity

@Database(
    entities = [
        UnidadeEntity::class,
        LoteEntity::class,
        MortalidadeDiariaEntity::class,
        RecebimentoRacaoEntity::class,
        PesagemEntity::class,
        UserEntity::class,
        CheckinEntity::class
    ],
    // v2: adicionou users e checkins pro login + selfie check-in.
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun unidadeDao(): UnidadeDao
    abstract fun loteDao(): LoteDao
    abstract fun mortalidadeDao(): MortalidadeDao
    abstract fun racaoDao(): RacaoDao
    abstract fun pesagemDao(): PesagemDao
    abstract fun userDao(): UserDao
    abstract fun checkinDao(): CheckinDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agrotech.db"
                )
                    // Como ainda não estamos em produção, destruímos o banco
                    // antigo ao subir o schema novo. Quando sair da fase de
                    // demo, troca por uma Migration explícita.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instancia = it }
            }
    }
}
