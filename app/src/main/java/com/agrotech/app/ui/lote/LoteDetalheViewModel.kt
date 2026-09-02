package com.agrotech.app.ui.lote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrotech.app.data.local.entities.CheckpointPeso
import com.agrotech.app.data.local.entities.DiaSemana
import com.agrotech.app.data.local.entities.LoteEntity
import com.agrotech.app.data.local.entities.MortalidadeDiariaEntity
import com.agrotech.app.data.local.entities.PesagemEntity
import com.agrotech.app.data.local.entities.RecebimentoRacaoEntity
import com.agrotech.app.data.local.entities.TipoRacao
import com.agrotech.app.data.repository.LoteRepository
import com.agrotech.app.data.repository.MortalidadeRepository
import com.agrotech.app.data.repository.PesagemRepository
import com.agrotech.app.data.repository.RacaoRepository
import com.agrotech.app.domain.calculo.MortalidadeCalculator
import com.agrotech.app.domain.calculo.ResumoSemana
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoteDetalheViewModel(
    private val loteId: Long,
    private val loteRepository: LoteRepository,
    private val mortalidadeRepository: MortalidadeRepository,
    private val racaoRepository: RacaoRepository,
    private val pesagemRepository: PesagemRepository
) : ViewModel() {

    val lote: StateFlow<LoteEntity?> = loteRepository.observarLote(loteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val mortalidadeRegistros: StateFlow<List<MortalidadeDiariaEntity>> =
        mortalidadeRepository.observarPorLote(loteId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resumoSemanas: StateFlow<List<ResumoSemana>> =
        combine(mortalidadeRegistros, lote) { registros, loteAtual ->
            MortalidadeCalculator.calcularResumoPorSemana(registros, loteAtual?.qtdAves ?: 0)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recebimentos: StateFlow<List<RecebimentoRacaoEntity>> = racaoRepository.observarPorLote(loteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pesagens: StateFlow<List<PesagemEntity>> = pesagemRepository.observarPorLote(loteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun salvarRegistroDiario(semana: Int, dia: DiaSemana, mortalidade: Int, descarte: Int) {
        viewModelScope.launch {
            mortalidadeRepository.salvarRegistroDiario(loteId, semana, dia, mortalidade, descarte)
        }
    }

    fun salvarRecebimento(data: Long, numeroNota: String, tipo: TipoRacao, quantidadeKg: Double) {
        viewModelScope.launch {
            racaoRepository.salvarRecebimento(
                RecebimentoRacaoEntity(
                    loteId = loteId,
                    data = data,
                    numeroNota = numeroNota,
                    tipoRacao = tipo,
                    quantidadeKg = quantidadeKg
                )
            )
        }
    }

    fun removerRecebimento(recebimento: RecebimentoRacaoEntity) {
        viewModelScope.launch { racaoRepository.removerRecebimento(recebimento) }
    }

    fun salvarPeso(checkpoint: CheckpointPeso, pesoKg: Double) {
        viewModelScope.launch { pesagemRepository.salvarPeso(loteId, checkpoint, pesoKg) }
    }
}
