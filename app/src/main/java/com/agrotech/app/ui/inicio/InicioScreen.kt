package com.agrotech.app.ui.inicio

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrotech.app.R
import com.agrotech.app.data.mock.MockData
import com.agrotech.app.ui.components.CartaoNovo
import com.agrotech.app.ui.components.QuadroIcone
import com.agrotech.app.ui.theme.AmberBg
import com.agrotech.app.ui.theme.AmberIcon
import com.agrotech.app.ui.theme.DeepGreen
import com.agrotech.app.ui.theme.DeepGreenBg
import com.agrotech.app.ui.theme.FieldBg
import com.agrotech.app.ui.theme.Muted
import java.util.Calendar

/**
 * Aba "Início" do novo design (AgroTech Granja): saudação, banner, quatro indicadores e o atalho
 * de desempenho do lote. Os números vêm de [MockData] (dados mockados por enquanto).
 *
 * Não tem `Scaffold` próprio: é conteúdo dentro do [com.agrotech.app.ui.main.MainScaffold].
 */
@Composable
fun InicioScreen(
    aoAbrirLotes: () -> Unit,
    aoAbrirRelatorios: () -> Unit,
    aoAbrirPerfil: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        // Topo: logo + nome + avatar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Eco, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                "AgroTech Granja",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DeepGreenBg)
                    .clickable(onClick = aoAbrirPerfil),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = "Perfil", tint = DeepGreen, modifier = Modifier.size(20.dp))
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 2.dp, vertical = 6.dp)) {
                Text(saudacao(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Juntos por uma granja mais produtiva.",
                    fontSize = 14.sp,
                    color = Muted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Banner()

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Indicador(
                    icone = Icons.Filled.Pets,
                    fundoIcone = DeepGreenBg,
                    corIcone = DeepGreen,
                    titulo = "Lotes ativos",
                    valor = "${MockData.LOTES_ATIVOS}",
                    detalhe = "de ${MockData.TOTAL_AVIARIOS} aviários",
                    aoClicar = aoAbrirLotes,
                    modifier = Modifier.weight(1f)
                )
                Indicador(
                    icone = Icons.Filled.Warning,
                    fundoIcone = AmberBg,
                    corIcone = AmberIcon,
                    titulo = "Mortalidade",
                    valor = MockData.MORTALIDADE_7D,
                    detalhe = "últimos 7 dias",
                    aoClicar = aoAbrirRelatorios,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Indicador(
                    icone = Icons.Filled.Inventory2,
                    fundoIcone = Color.Transparent,
                    corIcone = DeepGreen,
                    titulo = "Ração consumida",
                    valor = MockData.RACAO_HOJE,
                    detalhe = "hoje",
                    aoClicar = aoAbrirRelatorios,
                    modifier = Modifier.weight(1f)
                )
                Indicador(
                    icone = Icons.Filled.Scale,
                    fundoIcone = Color.Transparent,
                    corIcone = DeepGreen,
                    titulo = "Peso médio",
                    valor = MockData.PESO_MEDIO,
                    detalhe = "lote atual",
                    aoClicar = aoAbrirRelatorios,
                    modifier = Modifier.weight(1f)
                )
            }

            CartaoNovo(aoClicar = aoAbrirRelatorios, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 34.dp)
                            .background(FieldBg, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.BarChart, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Desempenho do lote", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Acompanhe a evolução em tempo real", fontSize = 12.sp, color = Muted)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

private fun saudacao(): String {
    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hora < 12 -> "Bom dia!"
        hora < 18 -> "Boa tarde!"
        else -> "Boa noite!"
    }
}

/**
 * Banner de boas-vindas: a foto do pinto do design (sem texto na imagem) com o texto e a barrinha verde
 * desenhados pelo app, para ficarem nítidos em qualquer tela.
 */
@Composable
private fun Banner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
    ) {
        Image(
            painter = painterResource(R.drawable.banner_pinto),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Avicultura\nde resultados\ntodos os dias.",
            color = Color.White,
            fontSize = 19.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.SemiBold,
            style = TextStyle(shadow = Shadow(color = Color.Black.copy(alpha = 0.35f), offset = Offset(0f, 2f), blurRadius = 6f)),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 10.dp)
                .size(width = 26.dp, height = 3.dp)
                .background(Color(0xFF5BD88A), RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun Indicador(
    icone: ImageVector,
    fundoIcone: Color,
    corIcone: Color,
    titulo: String,
    valor: String,
    detalhe: String,
    aoClicar: () -> Unit,
    modifier: Modifier = Modifier
) {
    CartaoNovo(aoClicar = aoClicar, modifier = modifier) {
        QuadroIcone(icone, fundoIcone, corIcone, tamanho = 36.dp)
        Spacer(Modifier.height(8.dp))
        Text(titulo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(valor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
        }
        Text(detalhe, fontSize = 12.sp, color = Muted)
    }
}
