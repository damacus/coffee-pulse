package com.damacus.coffeepulse.data.history

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BrewHistoryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CoffeePulseDatabase : RoomDatabase() {
    abstract fun brewHistoryDao(): BrewHistoryDao

    companion object {
        fun create(context: Context): CoffeePulseDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                CoffeePulseDatabase::class.java,
                "coffee_pulse.db",
            ).build()
        }
    }
}
