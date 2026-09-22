package com.agrotech.app.ui.lancamentos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrotech.app.data.mock.MockData
import com.agrotech.app.ui.components.CartaoNovo
import com.agrotech.app.ui.theme.DeepGreen
import com.agrotech.app.ui.theme.DeepGreenBg
import com.agrotech.app.ui.theme.Muted

/**
 * Aba "Lançamentos": dois atalhos — fechamento diário e recebimento de ração — cada um com fluxo
 * próprio (ver [FechamentoDiarioScreen] e [RecebimentoRacaoScreen]), terminando na mesma tela de
 * sucesso ([LancamentoSalvoScreen]).
 */
@Composable
fun LancamentosScreen(
    aoAbrirFechamento: () -> Unit,
    aoAbrirRecebimento: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
            Text("Lançamentos", fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Lote ${MockData.LOTE_ATUAL} · dia ${MockData.DIA_ATUAL} · semana ${MockData.SEMANA_ATUAL}",
                fontSize = 13.sp,
                color = Muted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BotaoLancamento(
                icone = Icons.Filled.FactCheck,
                titulo = "Fechamento diário",
                subtitulo = "Mortalidade, descarte, ração usada e peso do lote",
                aoClicar = aoAbrirFechamento
            )
            BotaoLancamento(
                icone = Icons.Filled.LocalShipping,
                titulo = "Recebimento de ração",
                subtitulo = "Nota, tipo de ração, quantidade e fornecedor",
                aoClicar = aoAbrirRecebimento
            )
        }
    }
}

@Composable
private fun BotaoLancamento(
    icone: ImageVector,
    titulo: String,
    subtitulo: String,
    aoClicar: () -> Unit
) {
    CartaoNovo(aoClicar = aoClicar, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(DeepGreenBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icone, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitulo, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 2.dp))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Muted)
        }
    }
}
