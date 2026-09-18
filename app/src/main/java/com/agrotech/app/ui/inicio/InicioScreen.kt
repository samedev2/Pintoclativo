package com.agrotech.app.ui.inicio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.components.AgroTechCard
import com.agrotech.app.ui.components.EntityListCard
import com.agrotech.app.ui.components.SectionLabel

/**
 * Conteúdo da aba "Início" — dashboard simples do app autenticado.
 * Saudação com o nome do user, KPIs do dia, e lista de granjas.
 *
 * **Não tem `Scaffold` próprio** — é conteúdo dentro do
 * [com.agrotech.app.ui.main.MainScaffold], que já provê bottom bar
 * e `containerColor`. O `TopAppBar` aqui é só visual.
 *
 * Não tem botão de check-in nem de sair — o check-in é responsabilidade
 * do fluxo raiz (acionado ao entrar no app) e o logout vive no Perfil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    aoAbrirLote: (Long, String) -> Unit
) {
    val container = rememberAppContainer()
    val email = container.sessionManager.sessaoAtiva() ?: ""
    val unidades by container.unidadeRepository.observarUnidades()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        "AgroTech",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Olá, ${email.substringBefore("@").ifBlank { "usuário" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
            windowInsets = WindowInsets.statusBars
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main summary follows the compact, prominent card in the reference.
            item {
                androidx.compose.material3.Surface(
                    color = com.agrotech.app.ui.theme.GreenPrimary,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    shape = com.agrotech.app.ui.theme.CardShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Visão geral",
                        style = MaterialTheme.typography.headlineSmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Resumo das suas unidades e check-ins de hoje",
                        style = MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }

            }

            // KPIs resumidos
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        titulo = "UNIDADES",
                        valor = "${unidades.size}",
                        subtitulo = if (unidades.size == 1) "granja ativa" else "granjas ativas",
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        titulo = "HOJE",
                        valor = "0",
                        subtitulo = "check-ins",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Lista de unidades (link pra aba Lotes ao clicar)
            if (unidades.isNotEmpty()) {
                item {
                    SectionLabel(
                        "Granjas",
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp)
                    )
                }
                items(unidades, key = { it.id }) { unidade ->
                    EntityListCard(
                        icon = Icons.Filled.Pets,
                        title = unidade.nome,
                        subtitle = "Controle técnico de frango de corte",
                        onClick = { aoAbrirLote(unidade.id, unidade.nome) }
                    )
                }
            } else {
                item {
                    AgroTechCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Nenhuma unidade cadastrada ainda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    titulo: String,
    valor: String,
    subtitulo: String,
    modifier: Modifier = Modifier
) {
    AgroTechCard(modifier = modifier) {
        Text(
            titulo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            valor,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            subtitulo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
