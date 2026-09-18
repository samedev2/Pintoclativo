package com.agrotech.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Usuário local. A senha NUNCA é guardada em texto puro — só o hash
 * bcrypt (salt incluso, formato `$2a$10$...`). Quando o backend remoto
 * entrar, a entidade pode ser substituída por um `remoteId` apenas.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val senhaHash: String,
    val dataCriacao: Long = System.currentTimeMillis()
)
