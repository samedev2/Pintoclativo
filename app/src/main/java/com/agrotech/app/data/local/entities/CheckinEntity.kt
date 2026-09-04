package com.agrotech.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Check-in com selfie. Sempre guarda apenas UM registro por usuário —
 * quando um novo check-in é aprovado, o anterior é apagado (igual ao
 * "fotos antigas são substituídas" que o usuário pediu, sem inchar
 * o banco).
 *
 * `fotoPath` é o caminho absoluto do arquivo JPEG dentro de
 * `context.filesDir/photos/`. `latitude`/`longitude` são opcionais
 * (null se o usuário negou permissão de localização).
 *
 * `faceHash` (perceptual hash 64 bits em hex) e `faceAssinatura`
 * (string com 6 floats separados por `;` — razões geométricas de
 * landmarks) são os dois sinais usados pra comparar com a selfie
 * anterior. Quanto mais próximos, mais provável que seja a mesma
 * pessoa.
 */
@Entity(tableName = "checkins")
data class CheckinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val fotoPath: String,
    val dataHora: Long,
    val latitude: Double?,
    val longitude: Double?,
    val faceHash: String,
    val faceAssinatura: String
)
