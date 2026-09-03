package com.agrotech.app.data.local

import android.util.Log
import com.agrotech.app.data.local.dao.LoteDao
import com.agrotech.app.data.local.dao.MortalidadeDao
import com.agrotech.app.data.local.dao.PesagemDao
import com.agrotech.app.data.local.dao.RacaoDao
import com.agrotech.app.data.local.dao.UnidadeDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

/**
 * Popula o banco local com os dados de demonstração (Lote 2 da Vitallis
 * com a mortalidade da Semana 4 da ficha anexada) **somente se a base
 * estiver vazia**. Quando o usuário cadastrar suas próprias unidades e
 * lotes, o seed não roda mais.
 */
class SeedRunner(
    private val unidadeDao: UnidadeDao,
    private val loteDao: LoteDao,
    private val mortalidadeDao: MortalidadeDao,
    private val pesagemDao: PesagemDao,
    private val racaoDao: RacaoDao
) {
    private val escopo = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun popularSeVazio() {
        escopo.launch {
            try {
                // 1. Unidade: Vitallis
                if (unidadeDao.observarTodas().first().isEmpty()) {
                    val unidadeId = unidadeDao.inserir(SeedData.unidadeVitallis())

                    // 2. Lote 2 da Vitallis
                    val loteId = loteDao.inserir(SeedData.lote2Vitallis(unidadeId))

                    // 3. Mortalidade da Semana 4
                    SeedData.mortalidadeSemana4(loteId).forEach { reg ->
                        mortalidadeDao.inserir(reg)
                    }

                    // 4. Pesagens registradas
                    SeedData.pesagensLote2(loteId).forEach { p ->
                        pesagemDao.inserir(p)
                    }

                    // 5. Recebimentos de ração
                    SeedData.recebimentosLote2(loteId).forEach { r ->
                        racaoDao.inserir(r)
                    }
                }
            } catch (e: Exception) {
                Log.e("SeedRunner", "Falha ao popular dados de demonstração", e)
            }
        }
    }
}
