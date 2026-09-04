package com.agrotech.app.data.auth

import com.agrotech.app.data.local.dao.UserDao
import com.agrotech.app.data.local.entities.UserEntity

/**
 * Resultado do login — sealed pra forçar a UI a tratar todos os casos
 * (autenticação OK, credenciais inválidas, conta bloqueada por rate limit).
 */
sealed class LoginResult {
    data class Sucesso(val email: String) : LoginResult()
    object CredenciaisInvalidas : LoginResult()
    data class Bloqueado(val segundosRestantes: Long) : LoginResult()
    object UsuarioInexistente : LoginResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {

    suspend fun login(email: String, senhaPura: String): LoginResult {
        if (sessionManager.bloqueado()) {
            return LoginResult.Bloqueado(sessionManager.segundosAteDesbloquear())
        }
        val user = userDao.buscarPorEmail(email.trim().lowercase())
            ?: return LoginResult.UsuarioInexistente.also {
                sessionManager.registrarFalha()
            }
        val senhaOk = PasswordHasher.verificar(senhaPura, user.senhaHash)
        if (!senhaOk) {
            sessionManager.registrarFalha()
            return LoginResult.CredenciaisInvalidas
        }
        sessionManager.criarSessao(user.email)
        sessionManager.registrarLogin()
        return LoginResult.Sucesso(user.email)
    }

    suspend fun cadastrarUsuario(email: String, senhaPura: String) {
        val hash = PasswordHasher.hash(senhaPura)
        userDao.inserir(
            UserEntity(
                email = email.trim().lowercase(),
                senhaHash = hash
            )
        )
    }

    fun sessaoAtiva(): String? = sessionManager.sessaoAtiva()

    fun encerrarSessao() = sessionManager.encerrarSessao()

    fun minutosRestantes(): Long = sessionManager.minutosRestantes()
}
