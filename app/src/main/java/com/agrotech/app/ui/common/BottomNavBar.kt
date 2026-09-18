package com.agrotech.app.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.agrotech.app.R
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.Hairline
import com.agrotech.app.ui.theme.Ink
import com.agrotech.app.ui.theme.Muted

/**
 * Abas da barra de navegação inferior do app (pós-login). Padrão
 * minimalista Ui/shadcn: 4 abas fixas, ícone + label embaixo, item
 * selecionado com texto em negrito + pill sutil de bg.
 */
enum class AbaNav(
    val rota: String,
    val label: String,
    val icone: ImageVector
) {
    INICIO("inicio", "Início", Icons.Filled.Home),
    LOTES("lotes", "Lotes", Icons.AutoMirrored.Filled.ListAlt),
    RELATORIOS("relatorios", "Relatórios", Icons.AutoMirrored.Filled.Assignment),
    PERFIL("perfil", "Perfil", Icons.Filled.Person);

    companion object {
        /**
         * Mapeia rota atual → aba correspondente. Rotas internas
         * (ex.: `lotes/123`, `relatorios/mortalidade`) são reconhecidas
         * como parte da aba correspondente.
         */
        fun daRota(rotaAtual: String?): AbaNav? {
            if (rotaAtual == null) return null
            return when {
                rotaAtual == INICIO.rota -> INICIO
                rotaAtual.startsWith("unidades/") || rotaAtual == "ocr" ||
                    rotaAtual == LOTES.rota || rotaAtual.startsWith("${LOTES.rota}/") -> LOTES
                rotaAtual == RELATORIOS.rota || rotaAtual.startsWith("${RELATORIOS.rota}/") -> RELATORIOS
                rotaAtual == PERFIL.rota -> PERFIL
                else -> null
            }
        }
    }
}

/**
 * Barra de navegação inferior (Ui/shadcn-inspired). Fundo `Canvas`
 * com hairline `Hairline` no topo. Cada item: ícone 22dp + label
 * 11sp Medium; item selecionado com label Bold + pill `Ink.copy(0.06)`
 * de fundo.
 */
@Composable
fun BottomNavBar(
    abaAtual: AbaNav?,
    aoSelecionar: (AbaNav) -> Unit,
    aoEscanear: () -> Unit
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
                    .height(88.dp)
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AbaNav.entries.take(2).forEach { aba ->
                    ItemNav(
                        aba = aba,
                        selecionada = aba == abaAtual,
                        aoClicar = { aoSelecionar(aba) },
                        modifier = Modifier.weight(1f)
                    )
                }
                ScannerButton(
                    aoClicar = aoEscanear,
                    modifier = Modifier.weight(1f)
                )
                AbaNav.entries.drop(2).forEach { aba ->
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
    val corTexto = if (selecionada) Ink else Muted
    Box(
        modifier = modifier
            .clickable(onClick = aoClicar)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = aba.icone,
                contentDescription = aba.label,
                tint = corTexto,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                aba.label,
                fontSize = 11.sp,
                fontWeight = if (selecionada) FontWeight.Bold else FontWeight.Medium,
                color = corTexto
            )
        }
    }
}

/** Ação principal central, inspirada no dock do PDF e usando o Scan QR Code do Lucide. */
@Composable
private fun ScannerButton(
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = aoClicar),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(62.dp),
            shape = CircleShape,
            color = GreenPrimary,
            contentColor = Color.White,
            shadowElevation = 10.dp,
            border = androidx.compose.foundation.BorderStroke(5.dp, Color.White)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_lucide_scan_qr_code),
                    contentDescription = "Escanear QR Code ou nota fiscal",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            text = "Escanear",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = GreenPrimary
        )
    }
}
