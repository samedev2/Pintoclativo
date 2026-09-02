package com.agrotech.app.di

import android.content.Context
import com.agrotech.app.data.local.AppDatabase
import com.agrotech.app.data.repository.LoteRepository
import com.agrotech.app.data.repository.MortalidadeRepository
import com.agrotech.app.data.repository.PesagemRepository
import com.agrotech.app.data.repository.RacaoRepository
import com.agrotech.app.data.repository.UnidadeRepository
import com.agrotech.app.data.repository.local.LocalLoteRepository
import com.agrotech.app.data.repository.local.LocalMortalidadeRepository
import com.agrotech.app.data.repository.local.LocalPesagemRepository
import com.agrotech.app.data.repository.local.LocalRacaoRepository
import com.agrotech.app.data.repository.local.LocalUnidadeRepository

/**
 * Fonte única das dependências do app. Hoje aponta para as implementações
 * locais (Room); quando a sincronização com o PostgreSQL na nuvem for
 * implementada, basta trocar as implementações aqui sem mexer nas telas.
 */
interface AppContainer {
    val unidadeRepository: UnidadeRepository
    val loteRepository: LoteRepository
    val mortalidadeRepository: MortalidadeRepository
    val racaoRepository: RacaoRepository
    val pesagemRepository: PesagemRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val database = AppDatabase.getInstance(context)

    override val unidadeRepository: UnidadeRepository by lazy {
        LocalUnidadeRepository(database.unidadeDao())
    }
    override val loteRepository: LoteRepository by lazy {
        LocalLoteRepository(database.loteDao())
    }
    override val mortalidadeRepository: MortalidadeRepository by lazy {
        LocalMortalidadeRepository(database.mortalidadeDao())
    }
    override val racaoRepository: RacaoRepository by lazy {
        LocalRacaoRepository(database.racaoDao())
    }
    override val pesagemRepository: PesagemRepository by lazy {
        LocalPesagemRepository(database.pesagemDao())
    }
}
