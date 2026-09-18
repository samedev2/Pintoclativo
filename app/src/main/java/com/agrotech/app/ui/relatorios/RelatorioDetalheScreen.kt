package com.agrotech.app.ui.relatorios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.agrotech.app.ui.common.rememberAppContainer
import com.agrotech.app.ui.components.AgroTechCard
import com.agrotech.app.ui.theme.GreenPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tipos de relatório disponíveis na aba Relatórios. Cada tipo mapeia
 * pra um conjunto de cards de entrada e uma tela de detalhe com os
 * dados consolidados.
 */
enum class RelatorioTipo(
    val chave: String,
    val titulo: String,
    val subtitulo: String,
    val icone: ImageVector
) {
    MORTALIDADE("mortalidade", "Mortalidade", "Registros diários de mortalidade e descarte", Icons.Filled.Edit),
    PESO("peso", "Ganho de peso", "Checkpoints de pesagem dos lotes", Icons.Filled.Star),
    RACAO("racao", "Consumo de ração", "Recebimentos de ração por lote e tipo", Icons.Filled.Build),
    CHECKINS("checkins", "Check-ins", "Histórico de check-ins com selfie", Icons.Filled.Email);

    companion object {
        fun daChave(chave: String?): RelatorioTipo? = entries.firstOrNull { it.chave == chave }
    }
}

/**
 * Tela de detalhe de um relatório. Mostra os registros reais do
 * banco (Room) com data, hora e usuário de cada um:
 * - Mortalidade: cada registro (semana, dia, mortalidade, descarte)
 * - Peso: cada pesagem (checkpoint + peso)
 * - Ração: cada recebimento (data, nota, tipo, quantidade)
 * - Check-ins: cada check-in (data/hora, email, local se houver)
 *
 * **Não tem `Scaffold` próprio** — é conteúdo dentro do
 * [com.agrotech.app.ui.main.MainScaffold], que já provê bottom bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioDetalheScreen(
    tipo: String,
    aoVoltar: () -> Unit
) {
    val tipoRelatorio = RelatorioTipo.daChave(tipo) ?: RelatorioTipo.MORTALIDADE
    val container = rememberAppContainer()
    val viewModel: RelatorioDetalheViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                RelatorioDetalheViewModel(
                    tipo = tipoRelatorio,
                    mortalidadeRepository = container.mortalidadeRepository,
                    pesagemRepository = container.pesagemRepository,
                    racaoRepository = container.racaoRepository,
                    loteRepository = container.loteRepository,
                    checkinDao = container.appDatabase.checkinDao(),
                    sessionManager = container.sessionManager
                )
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        tipoRelatorio.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        tipoRelatorio.subtitulo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = aoVoltar) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface
            ),
            windowInsets = WindowInsets.statusBars
        )

        if (state.itens.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Nenhum registro ainda.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header com total
                item {
                    AgroTechCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                "Total de registros",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${state.itens.size}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Usuário: ${state.emailUsuario}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Gerado em: ${state.dataGeracaoFmt}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                items(state.itens, key = { it.id }) { item ->
                    ItemRelatorioCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun ItemRelatorioCard(item: ItemRelatorio) {
    AgroTechCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                item.titulo,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            if (item.subtitulo.isNotBlank()) {
                Text(
                    item.subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            SpacerH(6.dp)
            LinhaMeta("Data", item.dataFmt)
            LinhaMeta("Hora", item.horaFmt)
            LinhaMeta("Usuário", item.usuario)
        }
    }
}

@Composable
private fun LinhaMeta(label: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            valor,
            modifier = Modifier.weight(1f).padding(start = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SpacerH(h: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = h))
}

/** Formato de data/hora compartilhado entre os relatórios. */
internal val formatoDataRel: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
internal val formatoHoraRel: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale("pt", "BR"))

internal fun formatarDataHora(millis: Long): String =
    "${formatoDataRel.format(Date(millis))} · ${formatoHoraRel.format(Date(millis))}"
