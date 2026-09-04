package com.agrotech.app.ui.checkin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrotech.app.ui.theme.CanvasLight
import com.agrotech.app.ui.theme.GreenPrimary
import com.agrotech.app.ui.theme.IconChipBackground
import com.agrotech.app.ui.theme.InkLight
import com.agrotech.app.ui.theme.MutedTextLight
import com.agrotech.app.ui.theme.SurfaceLight

/**
 * Tela de instruções do check-in com selfie. Aparece nos 5 primeiros
 * logins pra familiarizar o user com o fluxo; a partir do 6º login,
 * o NavHost pula direto pra tela da câmera.
 *
 * Mostra 4 passos numerados (luz, posição do rosto, localização,
 * captura) e um botão "Realizar Checkin" que avança pra câmera.
 * O user pode voltar pela seta da topbar — isso NÃO incrementa
 * o contador de logins, só fecha a tela.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun InstrucoesCheckinScreen(
    email: String,
    aoIniciarCheckin: () -> Unit,
    aoVoltar: () -> Unit
) {
    Scaffold(
        containerColor = CanvasLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Check-in com selfie",
                        fontWeight = FontWeight.SemiBold,
                        color = InkLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = InkLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CanvasLight,
                    titleContentColor = InkLight,
                    navigationIconContentColor = InkLight
                ),
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Como funciona",
                style = MaterialTheme.typography.titleLarge,
                color = InkLight,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Vamos tirar uma foto sua pra registrar a entrada. Leva menos de 30 segundos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedTextLight
            )

            Spacer(modifier = Modifier.height(4.dp))

            PassoCheckin(
                numero = 1,
                icone = Icons.Filled.Lightbulb,
                titulo = "Ambiente bem iluminado",
                descricao = "Fique num lugar claro. Evite contraluz (luz atrás de você)."
            )
            PassoCheckin(
                numero = 2,
                icone = Icons.Filled.Face,
                titulo = "Rosto centralizado",
                descricao = "Enquadre o rosto inteiro dentro do círculo. Tire óculos de sol e boné."
            )
            PassoCheckin(
                numero = 3,
                icone = Icons.Filled.LocationOn,
                titulo = "Localização ativada",
                descricao = "Conceda permissão de localização. Vamos registrar onde o check-in foi feito."
            )
            PassoCheckin(
                numero = 4,
                icone = Icons.Filled.CameraAlt,
                titulo = "Toque em \"Realizar Checkin\"",
                descricao = "Posicione o celular na altura do rosto e toque no botão pra capturar."
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botão principal de "Realizar Checkin" — chama o callback
            // que o NavHost usa pra navegar pra SelfieCheckinScreen.
            Button(
                onClick = aoIniciarCheckin,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    "Realizar Checkin",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PassoCheckin(
    numero: Int,
    icone: ImageVector,
    titulo: String,
    descricao: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceLight, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Círculo numerado com ícone
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(IconChipBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icone,
                contentDescription = null,
                tint = GreenPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.padding(start = 12.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            // Pequeno badge "PASSO N" acima do título
            Text(
                "PASSO $numero",
                style = MaterialTheme.typography.labelSmall,
                color = GreenPrimary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp
            )
            Text(
                titulo,
                style = MaterialTheme.typography.titleSmall,
                color = InkLight,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                descricao,
                style = MaterialTheme.typography.bodySmall,
                color = MutedTextLight
            )
        }
    }
}
