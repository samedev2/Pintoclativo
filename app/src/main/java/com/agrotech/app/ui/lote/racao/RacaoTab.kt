package com.agrotech.app.ui.lote.racao

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.agrotech.app.navigation.Rotas
import com.agrotech.app.ui.components.AgroTechCard
import com.agrotech.app.ui.components.dashedBorder
import com.agrotech.app.ui.lote.LoteDetalheViewModel
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.PillShape
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RacaoTab(
    loteId: Long,
    viewModel: LoteDetalheViewModel,
    navController: NavController
) {
    val recebimentos by viewModel.recebimentos.collectAsStateWithLifecycle()
    val formatoData = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Linha fixa no topo: "Ler NF pela câmera" (à esquerda) + botão "+"
            // (à direita, com o mesmo verde-claro do IconChip dos score cards).
            // IntrinsicSize.Max garante que os dois botões fiquem com a mesma
            // altura mesmo sem definir explicitamente.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .dashedBorder(color = MaterialTheme.colorScheme.primary, shape = PillShape)
                        .clickable { navController.navigate(Rotas.OCR_CAMERA) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            tint = GreenPrimary
                        )
                        Text(
                            "  Ler NF pela câmera",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                // Botão "+" fixo com fundo IconChip (verde-claro) e ícone preto
                // pra contrastar com o fundo claro. Abre a tela de cadastro
                // de recebimento (mesma rota que o "Ler NF").
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primaryContainer, PillShape)
                        .clickable { navController.navigate(Rotas.novoRecebimento(loteId)) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Novo recebimento",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        if (recebimentos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum recebimento de ração registrado.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(recebimentos, key = { it.id }) { recebimento ->
                AgroTechCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                "${formatoData.format(Date(recebimento.data))} · Nota ${recebimento.numeroNota}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                recebimento.tipoRacao.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${recebimento.quantidadeKg} kg",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { viewModel.removerRecebimento(recebimento) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
