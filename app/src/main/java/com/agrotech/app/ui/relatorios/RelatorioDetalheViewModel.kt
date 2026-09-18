package com.agrotech.app.ui.relatorios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrotech.app.data.auth.SessionManager
import com.agrotech.app.data.local.dao.CheckinDao
import com.agrotech.app.data.repository.LoteRepository
import com.agrotech.app.data.repository.MortalidadeRepository
import com.agrotech.app.data.repository.PesagemRepository
import com.agrotech.app.data.repository.RacaoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel da tela de detalhe de relatório. Combina as 4 fontes
 * (mortalidade, pesagem, ração, check-in) + o nome dos lotes, e
 * normaliza tudo pra uma lista de [ItemRelatorio] exibida na UI.
 *
 * A "data/hora" do item é:
 * - Mortalidade: data/hora atual (registro de mortalidade não tem
 *   timestamp próprio — ficaria rico adicionar `criadoEm` no schema
 *   depois).
 * - Peso: idem.
 * - Ração: campo `data` do recebimento.
 * - Check-in: campo `dataHora` do check-in (timestamp real do
 *   momento em que o usuário bateu o ponto).
 *
 * O "usuário" sempre é o usuário logado (`SessionManager.sessaoAtiva()`).
 */
class RelatorioDetalheViewModel(
    private val tipo: RelatorioTipo,
    mortalidadeRepository: MortalidadeRepository,
    pesagemRepository: PesagemRepository,
    racaoRepository: RacaoRepository,
    private val loteRepository: LoteRepository,
    private val checkinDao: CheckinDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val dataHoraGeracao = System.currentTimeMillis()
    private val dataGeracaoFmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
        .format(Date(dataHoraGeracao))
    private val emailUsuario: String = sessionManager.sessaoAtiva() ?: "—"

    val state: StateFlow<RelatorioDetalheState> = combine(
        mortalidadeRepository.observarTodos(),
        pesagemRepository.observarTodos(),
        racaoRepository.observarTodos(),
        checkinDao.observarTodos()
    ) { mortalidade, pesagens, racoes, checkins ->
        val agora = System.currentTimeMillis()
        val itens: List<ItemRelatorio> = when (tipo) {
            RelatorioTipo.MORTALIDADE -> mortalidade.map { reg ->
                ItemRelatorio(
                    id = "mort_${reg.id}",
                    titulo = "Semana ${reg.semana} · ${reg.diaSemana.label}",
                    subtitulo = "Mortes: ${reg.mortalidade} · Descartes: ${reg.descarte} · Total: ${reg.mortalidade + reg.descarte}",
                    dataMillis = agora,
                    usuario = emailUsuario
                )
            }
            RelatorioTipo.PESO -> pesagens.map { p ->
                ItemRelatorio(
                    id = "peso_${p.id}",
                    titulo = "Checkpoint ${p.checkpoint.label}",
                    subtitulo = "Peso médio: ${"%.3f".format(p.pesoKg)} kg",
                    dataMillis = agora,
                    usuario = emailUsuario
                )
            }
            RelatorioTipo.RACAO -> racoes.map { r ->
                ItemRelatorio(
                    id = "racao_${r.id}",
                    titulo = "${r.tipoRacao.label} · Nota ${r.numeroNota}",
                    subtitulo = "Quantidade: ${"%.0f".format(r.quantidadeKg)} kg",
                    dataMillis = r.data,
                    usuario = emailUsuario
                )
            }
            RelatorioTipo.CHECKINS -> checkins.map { c ->
                ItemRelatorio(
                    id = "checkin_${c.id}",
                    titulo = "Check-in de ${c.email}",
                    subtitulo = buildString {
                        if (c.latitude != null && c.longitude != null) {
                            append("Local: ${"%.4f".format(c.latitude)}, ${"%.4f".format(c.longitude)}")
                        } else {
                            append("Local: não informado")
                        }
                    },
                    dataMillis = c.dataHora,
                    usuario = c.email
                )
            }
        }
        RelatorioDetalheState(
            itens = itens,
            emailUsuario = emailUsuario,
            dataGeracaoFmt = dataGeracaoFmt
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        RelatorioDetalheState(emptyList(), emailUsuario, dataGeracaoFmt)
    )
}

/** Estado da tela de detalhe de relatório. */
data class RelatorioDetalheState(
    val itens: List<ItemRelatorio>,
    val emailUsuario: String,
    val dataGeracaoFmt: String
)

/** Um item do relatório exibido na lista. */
data class ItemRelatorio(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val dataMillis: Long,
    val usuario: String
) {
    val dataFmt: String
        get() = formatoDataRel.format(Date(dataMillis))
    val horaFmt: String
        get() = formatoHoraRel.format(Date(dataMillis))
}
