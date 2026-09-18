package com.agrotech.app.ui.lancar

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrotech.app.data.mock.MockData
import com.agrotech.app.ui.components.CabecalhoVerde
import com.agrotech.app.ui.theme.DeepGreen
import com.agrotech.app.ui.theme.FieldBg
import com.agrotech.app.ui.theme.Hairline
import com.agrotech.app.ui.theme.Ink
import com.agrotech.app.ui.theme.Muted

private const val LIMITE_OBSERVACAO = 200

/**
 * Aba "Lançar" do novo design (Novo lançamento): aviário, lote, quantidade de ração, mortalidade
 * e observação. Por enquanto o registro é só simulado (mock): valida, avisa e limpa o formulário,
 * porque o modelo Room ainda não tem "Aviário".
 */
@Composable
fun LancarScreen(aoVoltar: () -> Unit) {
    val context = LocalContext.current
    val foco = LocalFocusManager.current

    var aviario by rememberSaveable { mutableStateOf(MockData.aviarios.first().first) }
    var lote by rememberSaveable { mutableStateOf(MockData.aviarios.first().second) }
    var racao by rememberSaveable { mutableStateOf("") }
    var mortalidade by rememberSaveable { mutableStateOf("") }
    var observacao by rememberSaveable { mutableStateOf("") }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        CabecalhoVerde(titulo = "Novo lançamento", aoVoltar = aoVoltar)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            Rotulo("Aviário")
            CampoSelecao(
                icone = Icons.Outlined.Home,
                valor = aviario,
                opcoes = MockData.aviarios.map { it.first },
                aoEscolher = { escolhido ->
                    aviario = escolhido
                    MockData.aviarios.firstOrNull { it.first == escolhido }?.let { lote = it.second }
                }
            )
            Divisor()

            Rotulo("Lote")
            CampoSelecao(
                icone = Icons.Filled.Pets,
                valor = lote,
                opcoes = MockData.aviarios.map { it.second },
                aoEscolher = { lote = it }
            )
            Divisor()

            Rotulo("Quantidade de ração (kg)")
            CampoTexto(
                icone = Icons.Filled.Inventory2,
                valor = racao,
                aoMudar = { racao = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(9); erro = null },
                sufixo = "kg",
                teclado = KeyboardType.Decimal
            )
            Divisor()

            Rotulo("Mortalidade (aves)")
            CampoTexto(
                icone = Icons.Filled.Add,
                valor = mortalidade,
                aoMudar = { mortalidade = it.filter { c -> c.isDigit() }.take(6); erro = null },
                sufixo = "aves",
                teclado = KeyboardType.Number
            )
            Divisor()

            Rotulo("Observação")
            CampoTexto(
                icone = Icons.Outlined.Description,
                valor = observacao,
                aoMudar = { observacao = it.take(LIMITE_OBSERVACAO) },
                minAltura = 78,
                linhaUnica = false
            )
            Text(
                "${observacao.length}/$LIMITE_OBSERVACAO",
                fontSize = 12.sp,
                color = Muted,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )

            erro?.let {
                Text(it, color = com.agrotech.app.ui.theme.Destructive, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepGreen, RoundedCornerShape(10.dp))
                    .clickable {
                        foco.clearFocus()
                        val kg = racao.replace(',', '.').toDoubleOrNull()
                        val aves = mortalidade.toIntOrNull()
                        if (kg == null && aves == null) {
                            erro = "Informe a ração ou a mortalidade."
                        } else {
                            erro = null
                            Toast.makeText(context, "Registro salvo (dados mockados)", Toast.LENGTH_SHORT).show()
                            racao = ""
                            mortalidade = ""
                            observacao = ""
                        }
                    }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(10.dp))
                Text("Salvar registro", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Rotulo(texto: String) {
    Text(
        texto,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
}

@Composable
private fun Divisor() {
    HorizontalDivider(color = Hairline, modifier = Modifier.padding(top = 14.dp))
}

@Composable
private fun CaixaIcone(icone: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .border(BorderStroke(1.dp, Hairline), RoundedCornerShape(10.dp))
            .background(Color.White, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icone, contentDescription = null, tint = DeepGreen, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun CampoSelecao(
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
private fun CampoTexto(
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
