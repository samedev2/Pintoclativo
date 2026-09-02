package com.agrotech.app.ui.unidades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrotech.app.data.local.entities.UnidadeEntity
import com.agrotech.app.data.repository.UnidadeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UnidadesViewModel(private val repository: UnidadeRepository) : ViewModel() {

    val unidades: StateFlow<List<UnidadeEntity>> = repository.observarUnidades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun criarUnidade(nome: String) {
        val nomeLimpo = nome.trim()
        if (nomeLimpo.isEmpty()) return
        viewModelScope.launch { repository.criarUnidade(nomeLimpo) }
    }
}
