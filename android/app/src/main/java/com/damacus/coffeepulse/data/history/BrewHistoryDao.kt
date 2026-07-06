package com.damacus.coffeepulse.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BrewHistoryDao {
    @Query("SELECT * FROM brew_history ORDER BY finishedAtMillis DESC")
    fun observeAll(): Flow<List<BrewHistoryEntity>>

    @Query("SELECT * FROM brew_history WHERE id = :id")
    fun observeById(id: String): Flow<BrewHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BrewHistoryEntity)

    @Query("DELETE FROM brew_history WHERE id = :id")
    suspend fun delete(id: String)
}
