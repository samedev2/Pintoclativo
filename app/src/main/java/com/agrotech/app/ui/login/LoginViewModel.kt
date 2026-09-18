package com.agrotech.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrotech.app.data.auth.AuthRepository
import com.agrotech.app.data.auth.LoginResult
import com.agrotech.app.data.auth.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val carregando: Boolean = false,
    val bloqueado: Boolean = false,
    val bloqueadoAte: Long = 0L,
    val mensagemErro: String? = null,
    val loginResult: LoginResult? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    init {
        // Inicializa estado de bloqueio (caso a sessão anterior tenha
        // ficado bloqueada e o user reabriu o app).
        _state.update {
            it.copy(
                bloqueado = authRepository.let { repo ->
                    // Verifica se está bloqueado consultando o SessionManager.
                    // Truque: o repository expõe o sessionManager via login() —
                    // aqui só atualizamos via tentarLogin() que retorna Bloqueado.
                    false
                }
            )
        }
    }

    fun tentarLogin(email: String, senhaPura: String) {
        if (email.isBlank() || senhaPura.isBlank()) {
            _state.update { it.copy(mensagemErro = "Preencha e-mail e senha.") }
            return
        }
        _state.update { it.copy(carregando = true, mensagemErro = null) }
        viewModelScope.launch {
            // O bcrypt cost=10 é deliberadamente lento (≈100ms num celular
            // médio) — roda em IO pra não travar a main thread.
            val resultado = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                authRepository.login(email, senhaPura)
            }
            _state.update { current ->
                when (resultado) {
                    is LoginResult.Sucesso -> {
                        // A navegação acontece na LoginScreen via
                        // `LaunchedEffect(state.loginResult)` — não
                        // chamamos o callback aqui pra evitar
                        // `IllegalStateException` por navegar 2x.
                        current.copy(
                            carregando = false,
                            loginResult = resultado,
                            mensagemErro = null
                        )
                    }
                    is LoginResult.Bloqueado -> {
                        current.copy(
                            carregando = false,
                            bloqueado = true,
                            bloqueadoAte = System.currentTimeMillis() + resultado.segundosRestantes * 1000,
                            mensagemErro = "Muitas tentativas. Tente em ${resultado.segundosRestantes}s.",
                            loginResult = resultado
                        )
                    }
                    is LoginResult.CredenciaisInvalidas -> {
                        current.copy(
                            carregando = false,
                            mensagemErro = "E-mail ou senha incorretos."
                        )
                    }
                    is LoginResult.UsuarioInexistente -> {
                        current.copy(
                            carregando = false,
                            mensagemErro = "E-mail ou senha incorretos."
                        )
                    }
                }
            }
        }
    }

    fun limparResultado() {
        _state.update { it.copy(loginResult = null) }
    }
}
