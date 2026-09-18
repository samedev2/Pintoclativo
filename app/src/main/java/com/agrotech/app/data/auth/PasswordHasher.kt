package com.agrotech.app.data.auth

import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * Wrapper de bcrypt (cost 10). Mesmo formato `$2a$10$...` do bcrypt padrão
 * — quando migrarmos pra backend, o servidor usa a mesma string hash e
 * a verificação continua funcionando byte a byte.
 *
 * Em produção: nunca loggar o hash nem a senha. Aqui só usamos
 * internamente.
 */
object PasswordHasher {

    /** Cost 10 ~ 100ms por hash num celular médio — bom equilíbrio. */
    private const val COST = 10

    fun hash(senhaPura: String): String =
        BCrypt.withDefaults().hashToString(COST, senhaPura.toCharArray())

    fun verificar(senhaPura: String, hashArmazenado: String): Boolean =
        BCrypt.verifyer().verify(senhaPura.toCharArray(), hashArmazenado).verified
}
