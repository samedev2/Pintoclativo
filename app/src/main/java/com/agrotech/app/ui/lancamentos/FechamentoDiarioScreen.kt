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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.agrotech.app.ui.theme.Muted

private const val LIMITE_OBSERVACAO = 200

/**
 * Fechamento diário do lote (print do Figma): mortalidade normal, descarte, ração usada no dia e
 * peso do lote (opcional), mais observações. Ao salvar, calcula o saldo (mockado — o modelo Room
 * ainda não tem fechamento diário) e abre [LancamentoSalvoScreen].
 */
@Composable
fun FechamentoDiarioScreen(
    aoVoltar: () -> Unit,
    aoSalvar: () -> Unit
) {
    val foco = LocalFocusManager.current

    var mortalidade by rememberSaveable { mutableStateOf("") }
    var descarte by rememberSaveable { mutableStateOf("") }
    var racaoUsada by rememberSaveable { mutableStateOf("") }
    var pesoLote by rememberSaveable { mutableStateOf("") }
    var observacao by rememberSaveable { mutableStateOf("") }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        CabecalhoVerde(titulo = "Fechamento diário", aoVoltar = aoVoltar)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
            ) {
                ChipContexto(Icons.Filled.Pets, "Lote ${MockData.LOTE_ATUAL}")
                ChipContexto(Icons.Filled.CalendarMonth, "Dia ${MockData.DIA_ATUAL}")
                ChipContexto(Icons.Outlined.QueryStats, "Semana ${MockData.SEMANA_ATUAL}")
            }

            Rotulo("Mortalidade normal (M)")
            CampoTexto(
                icone = Icons.Filled.Pets,
                valor = mortalidade,
                aoMudar = { mortalidade = it.filter { c -> c.isDigit() }.take(6); erro = null },
                sufixo = "aves",
                teclado = KeyboardType.Number
            )
            Divisor()

            Rotulo("Descarte (D)")
            CampoTexto(
                icone = Icons.Filled.Delete,
                valor = descarte,
                aoMudar = { descarte = it.filter { c -> c.isDigit() }.take(6); erro = null },
                sufixo = "aves",
                teclado = KeyboardType.Number
            )
            Divisor()

            Rotulo("Ração usada no dia")
            CampoTexto(
                icone = Icons.Filled.Inventory2,
                valor = racaoUsada,
                aoMudar = { racaoUsada = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(9) },
                sufixo = "kg",
                teclado = KeyboardType.Decimal
            )
            Divisor()

            Rotulo("Peso do lote (opcional)")
            CampoTexto(
                icone = Icons.Filled.Scale,
                valor = pesoLote,
                aoMudar = { pesoLote = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(6) },
                sufixo = "kg",
                teclado = KeyboardType.Decimal
            )
            Divisor()

            Rotulo("Observações (opcional)")
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
                Text(it, color = Destructive, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepGreen, RoundedCornerShape(10.dp))
                    .clickable {
                        foco.clearFocus()
                        val m = mortalidade.toIntOrNull() ?: 0
                        val d = descarte.toIntOrNull() ?: 0
                        val kg = racaoUsada.replace(',', '.').toDoubleOrNull()
                        if (mortalidade.isBlank() && descarte.isBlank() && kg == null) {
                            erro = "Informe ao menos mortalidade, descarte ou ração usada."
                            return@clickable
                        }
                        erro = null
                        val saldo = MockData.SALDO_ANTES_DO_FECHAMENTO - m - d
                        UltimoLancamento.titulo = "Fechamento salvo"
                        UltimoLancamento.subtitulo =
                            "Os dados do fechamento diário foram registrados com sucesso no lote ${MockData.LOTE_ATUAL}."
                        UltimoLancamento.linhas = buildList {
                            add("Mortalidade" to "$m aves")
                            add("Descarte" to "$d aves")
                            add("Saldo" to "$saldo aves")
                            if (kg != null) add("Ração usada" to "${formatarKg(kg)} kg")
                            pesoLote.replace(',', '.').toDoubleOrNull()?.let { add("Peso do lote" to "${formatarKg(it)} kg") }
                        }
                        aoSalvar()
                    }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(Icons.Filled.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(10.dp))
                Text("Salvar fechamento", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun formatarKg(valor: Double): String =
    if (valor == valor.toLong().toDouble()) valor.toLong().toString() else valor.toString()
