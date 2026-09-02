package com.agrotech.app.ui.lotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrotech.app.data.local.entities.LoteEntity
import com.agrotech.app.data.repository.LoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LotesViewModel(
    private val unidadeId: Long,
    private val repository: LoteRepository
) : ViewModel() {

    val lotes: StateFlow<List<LoteEntity>> = repository.observarLotesPorUnidade(unidadeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun salvarNovoLote(lote: LoteEntity, aoSalvar: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.salvarLote(lote.copy(unidadeId = unidadeId))
            aoSalvar(id)
        }
    }
}
