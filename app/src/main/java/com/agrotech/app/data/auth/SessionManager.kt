package com.agrotech.app.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

/**
 * Sessão persistida via [EncryptedSharedPreferences] — equivalente local
 * de cookie HttpOnly (criptografado em repouso, inacessível a outros apps,
 * não exposto via JavaScript Bridge).
 *
 * Quando o backend remoto entrar, esse mesmo `token` vira o JWT/PASETO
 * e a `verificarSessao` faz round-trip com o servidor.
 *
 * Tokens carregam:
 * - `token` (string aleatória)
 * - `email` (do usuário logado)
 * - `deviceId` (UUID gerado na primeira abertura — previne "cross-token
 *   confusion": se um token roubado for colado em outro device, o
 *   deviceId não bate e a sessão é invalidada)
 * - `expiraEm` (timestamp em ms — sessões expiram em 1h)
 * - `tentativasFalhas` e `bloqueadoAte` (rate limit local: 5 falhas →
 *   bloqueio de 5min, escalando)
 */
class SessionManager(context: Context) {

    private val appContext = context.applicationContext

    /**
     * `androidx.security:security-crypto` está em alpha e pode falhar ao
     * gerar/ler a chave do Keystore (emuladores sem StrongBox, chave
     * invalidada após troca de bloqueio de tela, reinstalação sem limpar
     * dados, etc). Se isso acontecer, caímos para um `SharedPreferences`
     * comum em vez de derrubar o app — aqui é auth de demonstração, não
     * protege dado real, então perder a criptografia extra é aceitável;
     * travar o app na tela de splash/login não é.
     */
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            "agrotech_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e("SessionManager", "Falha ao criar EncryptedSharedPreferences, usando fallback sem criptografia", e)
        appContext.getSharedPreferences("agrotech_session_fallback", Context.MODE_PRIVATE)
    }

    val deviceId: String by lazy {
        prefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }
    }

    fun criarSessao(email: String, duracaoMs: Long = DURACAO_SESSAO_MS) {
        val token = UUID.randomUUID().toString()
        val expiraEm = System.currentTimeMillis() + duracaoMs
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_EMAIL, email)
            .putString(KEY_TOKEN_DEVICE_ID, deviceId)
            .putLong(KEY_EXPIRA_EM, expiraEm)
            .putLong(KEY_CRIADA_EM, System.currentTimeMillis())
            // Reset do rate limit ao logar com sucesso.
            .putInt(KEY_TENTATIVAS_FALHAS, 0)
            .putLong(KEY_BLOQUEADO_ATE, 0L)
            .apply()
    }

    /**
     * Verifica se há sessão válida. Retorna o e-mail se sim, null se não.
     * Checa: token existe, deviceId bate, e não expirou.
     */
    fun sessaoAtiva(): String? {
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val tokenDeviceId = prefs.getString(KEY_TOKEN_DEVICE_ID, null)
        val expiraEm = prefs.getLong(KEY_EXPIRA_EM, 0L)
        val agora = System.currentTimeMillis()

        // Cross-token confusion: token gerado em outro device não serve.
        if (tokenDeviceId != deviceId) {
            encerrarSessao()
            return null
        }
        if (agora >= expiraEm) {
            encerrarSessao()
            return null
        }
        return email
    }

    fun encerrarSessao() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_EMAIL)
            .remove(KEY_TOKEN_DEVICE_ID)
            .remove(KEY_EXPIRA_EM)
            .remove(KEY_CRIADA_EM)
            .apply()
    }

    fun minutosRestantes(): Long {
        val expiraEm = prefs.getLong(KEY_EXPIRA_EM, 0L)
        val ms = expiraEm - System.currentTimeMillis()
        return (ms / 60_000L).coerceAtLeast(0L)
    }

    // --- Onboarding de check-in ---
    //
    // Os 5 primeiros logins mostram a tela de instruções do
    // check-in (pra familiarizar o user com o fluxo). A partir do
    // 6º login, vai direto pra câmera. O contador é zerado se o
    // user desinstalar o app (porque é em `agrotech_session`, que
    // some junto com os dados do app).
    fun registrarLogin() {
        val atual = prefs.getInt(KEY_LOGIN_COUNT, 0)
        prefs.edit().putInt(KEY_LOGIN_COUNT, atual + 1).apply()
    }

    fun loginCount(): Int = prefs.getInt(KEY_LOGIN_COUNT, 0)

    /**
     * `true` quando o app ainda deve mostrar a tela de instruções
     * de check-in antes da câmera. Os 5 primeiros logins veem a tela;
     * a partir do 6º, vai direto.
     */
    fun mostrarInstrucoesCheckin(): Boolean = loginCount() <= 5

    // --- Rate limit (5 falhas → 5 min de bloqueio, escala) ---

    fun tentativasFalhas(): Int = prefs.getInt(KEY_TENTATIVAS_FALHAS, 0)
    fun bloqueadoAte(): Long = prefs.getLong(KEY_BLOQUEADO_ATE, 0L)
    fun bloqueado(): Boolean = System.currentTimeMillis() < bloqueadoAte()

    fun registrarFalha() {
        val tentativas = tentativasFalhas() + 1
        val novoBloqueio = when {
            tentativas >= 10 -> 30 * 60_000L   // 10 falhas → 30 min
            tentativas >= 5 -> 5 * 60_000L     // 5 falhas → 5 min
            else -> 0L
        }
        prefs.edit()
            .putInt(KEY_TENTATIVAS_FALHAS, tentativas)
            .putLong(KEY_BLOQUEADO_ATE, System.currentTimeMillis() + novoBloqueio)
            .apply()
    }

    fun resetarFalhas() {
        prefs.edit()
            .putInt(KEY_TENTATIVAS_FALHAS, 0)
            .putLong(KEY_BLOQUEADO_ATE, 0L)
            .apply()
    }

    fun segundosAteDesbloquear(): Long {
        val restante = bloqueadoAte() - System.currentTimeMillis()
        return (restante / 1000L).coerceAtLeast(0L)
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_EMAIL = "email"
        private const val KEY_TOKEN_DEVICE_ID = "token_device_id"
        private const val KEY_EXPIRA_EM = "expira_em"
        private const val KEY_CRIADA_EM = "criada_em"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_TENTATIVAS_FALHAS = "tentativas_falhas"
        private const val KEY_BLOQUEADO_ATE = "bloqueado_ate"
        private const val KEY_LOGIN_COUNT = "login_count"
        const val DURACAO_SESSAO_MS = 60 * 60 * 1000L // 1 hora
    }
}
