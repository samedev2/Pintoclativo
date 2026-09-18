package com.agrotech.app.di

import android.content.Context
import com.agrotech.app.data.auth.AuthRepository
import com.agrotech.app.data.auth.PasswordHasher
import com.agrotech.app.data.auth.SessionManager
import com.agrotech.app.data.checkin.CheckinRepository
import com.agrotech.app.data.checkin.FaceAnalyzer
import com.agrotech.app.data.checkin.LocationProvider
import com.agrotech.app.data.local.AppDatabase
import com.agrotech.app.data.local.SeedRunner
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

interface AppContainer {
    val unidadeRepository: UnidadeRepository
    val loteRepository: LoteRepository
    val mortalidadeRepository: MortalidadeRepository
    val racaoRepository: RacaoRepository
    val pesagemRepository: PesagemRepository
    val authRepository: AuthRepository
    val sessionManager: SessionManager
    val checkinRepository: CheckinRepository
    val locationProvider: LocationProvider
    val faceAnalyzer: FaceAnalyzer
    /** Exposição do banco pra views que precisam de DAOs específicos
     *  (ex.: `RelatorioDetalheViewModel` precisa de `CheckinDao`
     *  direto pra fazer a query `observarTodos`). */
    val appDatabase: com.agrotech.app.data.local.AppDatabase
    /**
     * DAO do usuário exposto pra permitir o pré-aquecimento (warm-up)
     * do Room na splash — sem isso, a primeira chamada lazy do
     * `AuthRepository` na LoginScreen acontece na main thread e pode
     * causar ANR de 5s+ em devices com base legada.
     */
    val userDao: com.agrotech.app.data.local.dao.UserDao
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val database = AppDatabase.getInstance(context)
    private val appContext = context.applicationContext

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

    override val sessionManager: SessionManager by lazy { SessionManager(appContext) }
    override val authRepository: AuthRepository by lazy {
        AuthRepository(database.userDao(), sessionManager)
    }
    override val checkinRepository: CheckinRepository by lazy {
        CheckinRepository(database.checkinDao(), appContext)
    }
    override val locationProvider: LocationProvider by lazy { LocationProvider(appContext) }
    override val faceAnalyzer: FaceAnalyzer by lazy { FaceAnalyzer(appContext) }
    override val appDatabase: com.agrotech.app.data.local.AppDatabase get() = database
    override val userDao: com.agrotech.app.data.local.dao.UserDao by lazy {
        database.userDao()
    }

    init {
        SeedRunner(
            unidadeDao = database.unidadeDao(),
            loteDao = database.loteDao(),
            mortalidadeDao = database.mortalidadeDao(),
            pesagemDao = database.pesagemDao(),
            racaoDao = database.racaoDao()
        ).popularSeVazio()

        // Bootstrap do admin: garante que admin@agrotech.com existe
        // com a senha agro@2026 (bcrypt). Roda em background, não
        // bloqueia a abertura do app.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (database.userDao().buscarPorEmail(ADMIN_EMAIL) == null) {
                    val hash = PasswordHasher.hash(ADMIN_SENHA)
                    database.userDao().inserir(
                        com.agrotech.app.data.local.entities.UserEntity(
                            email = ADMIN_EMAIL,
                            senhaHash = hash
                        )
                    )
                }
            } catch (_: Throwable) {
                // Falha silenciosa — usuário pode tentar cadastrar
                // manualmente se o bootstrap falhar.
            }
        }
    }

    companion object {
        const val ADMIN_EMAIL = "admin@agrotech.com"
        const val ADMIN_SENHA = "agro@2026"
    }
}
