package com.dynasty.cfb.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "save_slots")
data class SaveSlotEntity(
    @PrimaryKey val slotId: Int,
    val coachName: String,
    val collegeName: String,
    val seasonYear: Int,
    val currentWeek: Int,
    val recordText: String,
    val updatedAtMillis: Long,
    val serializedCareerJson: String
)

@Dao
interface SaveSlotDao {
    @Query("SELECT * FROM save_slots ORDER BY slotId ASC")
    fun observeAllSaveSlots(): Flow<List<SaveSlotEntity>>

    @Query("SELECT * FROM save_slots WHERE slotId = :slotId LIMIT 1")
    suspend fun getSaveSlot(slotId: Int): SaveSlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSaveSlot(entity: SaveSlotEntity)

    @Query("DELETE FROM save_slots WHERE slotId = :slotId")
    suspend fun deleteSaveSlot(slotId: Int)
}

@Database(entities = [SaveSlotEntity::class], version = 1, exportSchema = false)
abstract class DynastyDatabase : RoomDatabase() {
    abstract fun saveSlotDao(): SaveSlotDao

    companion object {
        @Volatile
        private var INSTANCE: DynastyDatabase? = null

        fun getInstance(context: Context): DynastyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DynastyDatabase::class.java,
                    "college_football_dynasty.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

private val Context.dataStore by preferencesDataStore(name = "dynasty_prefs")

class PreferencesManager(private val context: Context) {
    companion object {
        val LAST_ACTIVE_SLOT = intPreferencesKey("last_active_slot")
        val GLOBAL_SOUND = booleanPreferencesKey("global_sound")
    }

    val lastActiveSlotFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[LAST_ACTIVE_SLOT] ?: 1
    }

    suspend fun setLastActiveSlot(slotId: Int) {
        context.dataStore.edit { prefs ->
            prefs[LAST_ACTIVE_SLOT] = slotId
        }
    }
}
