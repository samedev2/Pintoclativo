package com.agrotech.app.data.checkin

import android.content.Context
import android.graphics.Bitmap
import com.agrotech.app.data.local.dao.CheckinDao
import com.agrotech.app.data.local.entities.CheckinEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Persiste o check-in com selfie. Padrão "1 usuário, 1 foto": quando um
 * novo check-in é aprovado, o anterior (arquivo JPEG + linha no Room) é
 * apagado — não incha o banco nem o filesystem. Quando o backend remoto
 * entrar, basta trocar o destino do upload mantendo essa mesma regra de
 * retenção.
 */
class CheckinRepository(
    private val dao: CheckinDao,
    private val context: Context
) {
    private val dirFotos: File by lazy {
        File(context.filesDir, "photos").also { it.mkdirs() }
    }

    /**
     * Salva a selfie (JPEG no filesystem + linha no Room) substituindo
     * qualquer check-in anterior do mesmo email. Retorna a entidade
     * persistida com o `id` definitivo.
     */
    suspend fun salvar(
        email: String,
        bitmap: Bitmap,
        latitude: Double?,
        longitude: Double?,
        faceAssinatura: FloatArray? = null
    ): CheckinEntity = withContext(Dispatchers.IO) {
        // Apaga o anterior (arquivo + linha) antes de salvar o novo.
        dao.ultimoCheckin(email)?.let { antigo ->
            try { File(antigo.fotoPath).delete() } catch (_: Throwable) {}
            dao.remover(antigo)
        }

        val agora = System.currentTimeMillis()
        val arquivo = File(dirFotos, "selfie_${email.hashCode()}_$agora.jpg")
        FileOutputStream(arquivo).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        val faceHash = FaceHashUtil.hashDaFace(bitmap)
        val faceAssinaturaStr = faceAssinatura?.let { FaceSignatureCodec.encode(it) } ?: ""
        val entity = CheckinEntity(
            email = email,
            fotoPath = arquivo.absolutePath,
            dataHora = agora,
            latitude = latitude,
            longitude = longitude,
            faceHash = faceHash,
            faceAssinatura = faceAssinaturaStr
        )
        val id = dao.inserir(entity)
        entity.copy(id = id)
    }

    suspend fun ultimoCheckin(email: String): CheckinEntity? = withContext(Dispatchers.IO) {
        dao.ultimoCheckin(email)
    }

    suspend fun limpar(email: String) = withContext(Dispatchers.IO) {
        dao.ultimoCheckin(email)?.let { antigo ->
            try { File(antigo.fotoPath).delete() } catch (_: Throwable) {}
        }
        dao.removerTodos(email)
    }
}

/**
 * Codec simples pra `FloatArray` (assinatura geométrica) ↔ String.
 * Formato: "0.42;0.18;0.55;..." (6 valores separados por `;`).
 *
 * Por que `;` e não JSON? Porque JSON parsing é overkill pra 6 floats
 * e a precisão decimal (4 casas) é suficiente pra comparar rostos.
 */
object FaceSignatureCodec {
    fun encode(sig: FloatArray): String =
        sig.joinToString(";") { "%.4f".format(it) }

    fun decode(str: String): FloatArray? {
        if (str.isBlank()) return null
        return try {
            str.split(";").map { it.toFloat() }.toFloatArray()
        } catch (_: Throwable) {
            null
        }
    }
}
