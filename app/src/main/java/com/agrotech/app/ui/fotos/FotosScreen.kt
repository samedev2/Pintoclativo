package com.agrotech.app.ui.fotos

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.agrotech.app.R
import com.agrotech.app.ui.components.CabecalhoVerde
import com.agrotech.app.ui.components.CartaoNovo
import com.agrotech.app.ui.theme.DeepGreen
import com.agrotech.app.ui.theme.FieldBg
import com.agrotech.app.ui.theme.Hairline
import com.agrotech.app.ui.theme.Ink
import com.agrotech.app.ui.theme.Muted

/**
 * Aba "Fotos" (acessada por Mais): registra fotos de aviários, ração ou equipamentos pela câmera
 * do celular ou pela galeria. A câmera ao vivo (GranjaCam) saiu daqui — agora fica no card
 * "Acompanhar em tempo real" do Início ([com.agrotech.app.ui.inicio.InicioScreen]).
 */
@Composable
fun FotosScreen(aoVoltar: () -> Unit) {
    val context = LocalContext.current
    // Começa com a foto de exemplo (pintos na granja), como no design, até o usuário tirar outra.
    var foto by remember { mutableStateOf<Any?>(R.drawable.foto_granja_exemplo) }
    var total by rememberSaveable { mutableIntStateOf(1) }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            foto = bitmap
            total++
        }
    }
    val permissao = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
        if (concedida) {
            camera.launch(null)
        } else {
            Toast.makeText(context, "Permita o uso da câmera para tirar fotos.", Toast.LENGTH_SHORT).show()
        }
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            foto = uri
            total++
        }
    }
    val tirarFoto = {
        val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (ok) camera.launch(null) else permissao.launch(Manifest.permission.CAMERA)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        CabecalhoVerde(titulo = "Foto da granja", aoVoltar = aoVoltar)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Registre fotos de aviários, ração ou equipamentos.",
                fontSize = 13.sp,
                color = Muted
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(262f / 364f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FieldBg),
                contentAlignment = Alignment.Center
            ) {
                Previa(foto, Modifier.fillMaxSize())
                if (foto == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = Muted, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Nenhuma foto ainda", color = Muted, fontSize = 14.sp)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(DeepGreen, RoundedCornerShape(10.dp))
                        .clickable { tirarFoto() }
                        .padding(vertical = 15.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Foto", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(FieldBg, RoundedCornerShape(10.dp))
                        .border(BorderStroke(1.dp, Hairline), RoundedCornerShape(10.dp))
                        .clickable { galeria.launch("image/*") }
                        .padding(vertical = 15.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = Ink, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Galeria", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (total > 0) {
                CartaoNovo(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Previa(
                            foto,
                            Modifier
                                .size(width = 64.dp, height = 52.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Foto registrada!", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Adicionar outra",
                                fontSize = 13.sp,
                                color = DeepGreen,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { tirarFoto() }.padding(top = 2.dp)
                            )
                        }
                        Box(
                            modifier = Modifier.size(38.dp).background(DeepGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Previa(foto: Any?, modifier: Modifier) {
    when (foto) {
        is Bitmap -> Image(
            bitmap = foto.asImageBitmap(),
            contentDescription = "Foto da granja",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        null -> Box(modifier)
        is Int -> Image(
            painter = painterResource(foto),
            contentDescription = "Foto da granja",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        else -> AsyncImage(
            model = foto,
            contentDescription = "Foto da granja",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
