package com.agrotech.app.ui.mais

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
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
import com.agrotech.app.ui.components.CartaoNovo
import com.agrotech.app.ui.theme.DeepGreen
import com.agrotech.app.ui.theme.FieldBg
import com.agrotech.app.ui.theme.Muted

/**
 * Aba "Mais": atalhos para o que não tem aba própria — fotos, desempenho do lote, relatórios,
 * scanner de nota fiscal e perfil. Unidades e lotes viraram a aba Lotes.
 */
@Composable
fun MaisScreen(
    aoAbrirFotos: () -> Unit,
    aoAbrirDesempenho: () -> Unit,
    aoAbrirRelatorios: () -> Unit,
    aoAbrirScanner: () -> Unit,
    aoAbrirPerfil: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
    ) {
        Text(
            "Mais",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Item(Icons.Filled.PhotoCamera, "Fotos", "Registre fotos de aviários e equipamentos", aoAbrirFotos)
            Item(Icons.Filled.BarChart, "Desempenho do lote", "Acompanhe a evolução em tempo real", aoAbrirDesempenho)
            Item(Icons.AutoMirrored.Filled.Assignment, "Relatórios", "Mortalidade, peso, ração e check-ins", aoAbrirRelatorios)
            Item(Icons.Filled.QrCodeScanner, "Escanear nota fiscal", "QR Code ou texto da nota", aoAbrirScanner)
            Item(Icons.Filled.Person, "Perfil", "Conta e sair", aoAbrirPerfil)
        }
    }
}

@Composable
private fun Item(icone: ImageVector, titulo: String, detalhe: String, aoClicar: () -> Unit) {
    CartaoNovo(aoClicar = aoClicar, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 34.dp)
                    .background(FieldBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icone, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(detalhe, fontSize = 12.sp, color = Muted)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}
