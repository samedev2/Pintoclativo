package com.agrotech.app.ui.lancamentos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrotech.app.data.mock.UltimoLancamento
import com.agrotech.app.ui.components.CartaoNovo
import com.agrotech.app.ui.theme.DeepGreen
import com.agrotech.app.ui.theme.DeepGreenBg
import com.agrotech.app.ui.theme.GreenChipBg
import com.agrotech.app.ui.theme.GreenPrimaryDark
import com.agrotech.app.ui.theme.Hairline
import com.agrotech.app.ui.theme.Muted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Confirmação de sucesso, comum ao fechamento diário e ao recebimento de ração (o Figma mostra a
 * mesma tela para os dois fluxos). Lê o resultado de [UltimoLancamento], preenchido pela tela
 * anterior antes de navegar para cá.
 */
@Composable
fun LancamentoSalvoScreen(aoVoltarAoLote: () -> Unit) {
    val agora = remember { SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")).format(Date()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(DeepGreenBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(UltimoLancamento.titulo, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Text(
            UltimoLancamento.subtitulo,
            fontSize = 14.sp,
            color = Muted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)
        )

        Spacer(Modifier.height(24.dp))
        CartaoNovo(modifier = Modifier.fillMaxWidth()) {
            UltimoLancamento.linhas.forEachIndexed { indice, (rotulo, valor) ->
                if (indice > 0) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(rotulo, fontSize = 14.sp, color = Muted)
                    Text(valor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenChipBg, RoundedCornerShape(10.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(Icons.Filled.CloudDone, contentDescription = null, tint = GreenPrimaryDark, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Sincronizado", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GreenPrimaryDark)
                Text("Dados enviados para a nuvem $agora", fontSize = 12.sp, color = GreenPrimaryDark)
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepGreen, RoundedCornerShape(10.dp))
                .clickable(onClick = aoVoltarAoLote)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(10.dp))
            Text("Voltar ao lote", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
