package com.agrotech.app.data.granjacam

import com.agrotech.app.BuildConfig

/**
 * Configuração da opção GranjaCam.
 *
 * A análise computacional (detecção e rastreamento de pintos, galinhas e galo) NÃO roda
 * no app: ela fica no serviço GranjaCam, em outro ambiente (VPS ou mini PC na granja).
 * O app só abre o webviewer desse serviço.
 *
 *  - `GRANJACAM_BASE_URL` vazia  → modo mock (vídeo de teste + caixas pré-calculadas).
 *  - `GRANJACAM_BASE_URL` https  → abre o webviewer do serviço.
 *
 * Definida no build: `./gradlew assembleDebug -PgranjacamUrl=https://cam.exemplo.com`.
 * O Android bloqueia HTTP puro por padrão, então só `https://` conta como serviço.
 */
object GranjaCamConfig {
    val baseUrl: String = BuildConfig.GRANJACAM_BASE_URL.trim()

    val usaServico: Boolean = baseUrl.startsWith("https://")
}
