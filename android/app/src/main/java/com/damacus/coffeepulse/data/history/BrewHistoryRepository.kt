package com.damacus.coffeepulse.data.history

import com.damacus.coffeepulse.domain.model.BrewHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrewHistoryRepository(
    private val dao: BrewHistoryDao,
) {
    val entries: Flow<List<BrewHistoryEntry>> = dao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    fun observeById(id: String): Flow<BrewHistoryEntry?> {
        return dao.observeById(id).map { it?.toDomain() }
    }

    suspend fun save(entry: BrewHistoryEntry) {
        dao.insert(entry.toEntity())
    }

    suspend fun delete(id: String) {
        dao.delete(id)
    }
}
