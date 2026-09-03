package com.damacus.coffeepulse.data.history

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.damacus.coffeepulse.domain.model.BrewHistoryEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrewHistoryDaoTest {
    private lateinit var database: CoffeePulseDatabase
    private lateinit var dao: BrewHistoryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, CoffeePulseDatabase::class.java).build()
        dao = database.brewHistoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadHistoryEntry() = runBlocking {
        val entry = BrewHistoryEntry(
            id = "brew-1",
            startedAtMillis = 1_000L,
            finishedAtMillis = 91_000L,
            totalSeconds = 90,
            bloomSeconds = 30,
            pulseIntervalSeconds = 5,
            coffeeGrams = 15.0,
            waterRatio = 15.5,
            totalWaterGrams = 233,
            themeId = "instrument",
            rating = 4,
            notes = "Sweet and balanced",
        )

        dao.insert(entry.toEntity())

        assertEquals(listOf(entry), dao.observeAll().first().map { it.toDomain() })
    }
}
