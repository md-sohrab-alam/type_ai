package com.smarttype.aikeyboard.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.smarttype.aikeyboard.data.model.TextSuggestion
import com.smarttype.aikeyboard.data.model.ToneStyle
import com.smarttype.aikeyboard.data.model.UserPreferences

@Database(
    entities = [
        UserPreferences::class,
        TextSuggestion::class,
        ToneStyle::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SmartTypeDatabase : RoomDatabase() {
    
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun textSuggestionDao(): TextSuggestionDao
    abstract fun toneStyleDao(): ToneStyleDao
    
    companion object {
        @Volatile
        private var INSTANCE: SmartTypeDatabase? = null
        
        fun getDatabase(context: Context): SmartTypeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartTypeDatabase::class.java,
                    "smarttype_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
