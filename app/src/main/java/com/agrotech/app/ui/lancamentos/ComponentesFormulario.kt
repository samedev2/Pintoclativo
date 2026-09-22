package com.agrotech.app.ui.lancamentos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrotech.app.ui.theme.DeepGreen
import com.agrotech.app.ui.theme.FieldBg
import com.agrotech.app.ui.theme.Hairline
import com.agrotech.app.ui.theme.Ink
import com.agrotech.app.ui.theme.Muted

/**
 * Campos de formulário compartilhados pelas telas de Lançamentos (fechamento diário e recebimento
 * de ração): mesmo visual do antigo "Novo lançamento" — ícone à esquerda, campo com borda, rótulo
 * acima e divisor entre campos.
 */
@Composable
internal fun Rotulo(texto: String) {
    Text(
        texto,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
}

@Composable
internal fun Divisor() {
    HorizontalDivider(color = Hairline, modifier = Modifier.padding(top = 14.dp))
}

@Composable
internal fun CaixaIcone(icone: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .border(BorderStroke(1.dp, Hairline), RoundedCornerShape(10.dp))
            .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icone, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(22.dp))
    }
}

@Composable
internal fun CampoSelecao(
    icone: ImageVector,
    valor: String,
    opcoes: List<String>,
    aoEscolher: (String) -> Unit
) {
    var aberto by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CaixaIcone(icone, modifier = Modifier.align(Alignment.Top))
        Box(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .background(FieldBg, RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, Hairline), RoundedCornerShape(10.dp))
                    .clickable { aberto = true }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(valor, fontSize = 15.sp, color = Ink)
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Muted)
            }
            DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
                opcoes.forEach { opcao ->
                    DropdownMenuItem(
                        text = { Text(opcao) },
                        onClick = {
                            aoEscolher(opcao)
                            aberto = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun CampoTexto(
    icone: ImageVector,
    valor: String,
    aoMudar: (String) -> Unit,
    sufixo: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    minAltura: Int = 48,
    linhaUnica: Boolean = true
) {
    var focado by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CaixaIcone(icone, modifier = Modifier.align(Alignment.Top))
        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = minAltura.dp)
                .background(FieldBg, RoundedCornerShape(10.dp))
                .border(
                    BorderStroke(if (focado) 1.5.dp else 1.dp, if (focado) DeepGreen else Hairline),
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = if (linhaUnica) Alignment.CenterVertically else Alignment.Top
        ) {
            BasicTextField(
                value = valor,
                onValueChange = aoMudar,
                singleLine = linhaUnica,
                textStyle = TextStyle(fontSize = 15.sp, color = Ink),
                cursorBrush = SolidColor(DeepGreen),
                keyboardOptions = KeyboardOptions(keyboardType = teclado),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focado = it.isFocused }
            )
            if (sufixo != null) {
                Text(sufixo, fontSize = 14.sp, color = Muted)
            }
        }
    }
}

/** Chip de contexto (Lote 02 · Dia 15 · Semana 3) no topo do fechamento diário. */
@Composable
internal fun ChipContexto(icone: ImageVector, texto: String) {
    Row(
        modifier = Modifier
            .background(FieldBg, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icone, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(15.dp))
        Text(texto, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Ink)
    }
}
