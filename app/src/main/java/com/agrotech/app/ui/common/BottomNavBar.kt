package com.agrotech.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.agrotech.app.ui.theme.DeepGreen
import com.agrotech.app.ui.theme.Hairline
import com.agrotech.app.ui.theme.Muted

/**
 * Abas da barra de navegação inferior (novo design AgroTech Granja): Início, Lotes, Lançamentos
 * e Mais. Ícones de linha; a aba ativa fica em verde profundo com um traço embaixo.
 *
 * A câmera ao vivo (GranjaCam) e a foto pelo celular não são mais abas — a câmera ao vivo abre
 * pelo card "Acompanhar em tempo real" do Início, e a foto fica dentro de Mais.
 */
enum class AbaNav(
    val rota: String,
    val label: String,
    val icone: ImageVector
) {
    INICIO("inicio", "Início", Icons.Outlined.Home),
    LOTES("lotes", "Lotes", Icons.Outlined.FormatListBulleted),
    LANCAMENTOS("lancamentos", "Lançamentos", Icons.Outlined.Edit),
    MAIS("mais", "Mais", Icons.Outlined.MoreHoriz);

    companion object {
        /**
         * Mapeia a rota atual para a aba correspondente. Sub-telas de unidade/lote ficam com
         * Lotes destacada; sub-telas de lançamento ficam com Lançamentos destacada. Telas
         * empilhadas por cima (câmera ao vivo, foto, scanner, perfil, relatório) não destacam
         * nenhuma aba, porque são acessadas a partir de mais de um lugar.
         */
        fun daRota(rotaAtual: String?): AbaNav? {
            if (rotaAtual == null) return null
            return when {
                rotaAtual == INICIO.rota -> INICIO
                rotaAtual == LOTES.rota || rotaAtual.startsWith("unidades/") || rotaAtual.startsWith("lotes/") -> LOTES
                rotaAtual == LANCAMENTOS.rota || rotaAtual.startsWith("${LANCAMENTOS.rota}/") -> LANCAMENTOS
                rotaAtual == MAIS.rota -> MAIS
                else -> null
            }
        }
    }
}

@Composable
fun BottomNavBar(
    abaAtual: AbaNav?,
    aoSelecionar: (AbaNav) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(10f)
            .navigationBarsPadding(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Hairline)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AbaNav.entries.forEach { aba ->
                    ItemNav(
                        aba = aba,
                        selecionada = aba == abaAtual,
                        aoClicar = { aoSelecionar(aba) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemNav(
    aba: AbaNav,
    selecionada: Boolean,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cor = if (selecionada) DeepGreen else Muted
    Column(
        modifier = modifier
            .clickable(onClick = aoClicar)
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = aba.icone,
            contentDescription = aba.label,
            tint = cor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            aba.label,
            fontSize = 11.sp,
            fontWeight = if (selecionada) FontWeight.SemiBold else FontWeight.Normal,
            color = cor
        )
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(3.dp)
                .background(
                    if (selecionada) DeepGreen else Color.Transparent,
                    RoundedCornerShape(2.dp)
                )
        )
    }
}
