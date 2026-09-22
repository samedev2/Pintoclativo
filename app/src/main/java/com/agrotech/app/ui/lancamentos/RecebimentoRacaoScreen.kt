package com.agrotech.app.ui.lancamentos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Scale
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrotech.app.data.mock.MockData
import com.agrotech.app.data.mock.UltimoLancamento
import com.agrotech.app.ui.components.CabecalhoVerde
import com.agrotech.app.ui.theme.DeepGreen
import com.agrotech.app.ui.theme.Destructive
import com.agrotech.app.ui.theme.GreenChipBg
import com.agrotech.app.ui.theme.GreenPrimaryDark
import com.agrotech.app.ui.theme.Muted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Recebimento de ração (print do Figma): data, número da nota, tipo de ração, quantidade recebida
 * e fornecedor. Fluxo separado do fechamento diário — ao salvar, também abre [LancamentoSalvoScreen].
 */
@Composable
fun RecebimentoRacaoScreen(
    aoVoltar: () -> Unit,
    aoSalvar: () -> Unit
) {
    val foco = LocalFocusManager.current
    val hoje = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date()) }

    var data by rememberSaveable { mutableStateOf(hoje) }
    var numeroNota by rememberSaveable { mutableStateOf("") }
    var tipoRacao by rememberSaveable { mutableStateOf(MockData.tiposRacao.first()) }
    var quantidade by rememberSaveable { mutableStateOf("") }
    var fornecedor by rememberSaveable { mutableStateOf(MockData.fornecedores.first()) }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        CabecalhoVerde(titulo = "Recebimento de ração", aoVoltar = aoVoltar)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .background(GreenChipBg, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = GreenPrimaryDark, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Fluxo separado do fechamento diário", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = GreenPrimaryDark)
                    Text("Registre aqui a entrada de ração no lote.", fontSize = 12.sp, color = GreenPrimaryDark)
                }
            }

            Rotulo("Data do recebimento")
            CampoTexto(
                icone = Icons.Filled.CalendarMonth,
                valor = data,
                aoMudar = { data = it.take(10) }
            )
            Divisor()

            Rotulo("Número da nota")
            CampoTexto(
                icone = Icons.Filled.Receipt,
                valor = numeroNota,
                aoMudar = { numeroNota = it.filter { c -> c.isDigit() }.take(12); erro = null },
                teclado = KeyboardType.Number
            )
            Divisor()

            Rotulo("Tipo de ração")
            CampoSelecao(
                icone = Icons.Filled.Inventory2,
                valor = tipoRacao,
                opcoes = MockData.tiposRacao,
                aoEscolher = { tipoRacao = it }
            )
            Divisor()

            Rotulo("Quantidade recebida (kg)")
            CampoTexto(
                icone = Icons.Filled.Scale,
                valor = quantidade,
                aoMudar = { quantidade = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(9); erro = null },
                sufixo = "kg",
                teclado = KeyboardType.Decimal
            )
            Divisor()

            Rotulo("Fornecedor")
            CampoSelecao(
                icone = Icons.Filled.Groups,
                valor = fornecedor,
                opcoes = MockData.fornecedores,
                aoEscolher = { fornecedor = it }
            )

            erro?.let {
                Text(it, color = Destructive, fontSize = 13.sp, modifier = Modifier.padding(top = 16.dp))
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepGreen, RoundedCornerShape(10.dp))
                    .clickable {
                        foco.clearFocus()
                        val kg = quantidade.replace(',', '.').toDoubleOrNull()
                        if (kg == null || kg <= 0.0) {
                            erro = "Informe a quantidade recebida."
                            return@clickable
                        }
                        if (numeroNota.isBlank()) {
                            erro = "Informe o número da nota."
                            return@clickable
                        }
                        erro = null
                        UltimoLancamento.foto = null // recebimento não tem foto; evita mostrar a de um fechamento anterior
                        UltimoLancamento.titulo = "Recebimento salvo"
                        UltimoLancamento.subtitulo =
                            "O recebimento de ração foi registrado com sucesso no lote ${MockData.LOTE_ATUAL}."
                        UltimoLancamento.linhas = listOf(
                            "Data" to data,
                            "Nota" to numeroNota,
                            "Tipo de ração" to tipoRacao,
                            "Quantidade" to "${formatarKg(kg)} kg",
                            "Fornecedor" to fornecedor
                        )
                        aoSalvar()
                    }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(10.dp))
                Text("Salvar recebimento", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun formatarKg(valor: Double): String =
    if (valor == valor.toLong().toDouble()) valor.toLong().toString() else valor.toString()
