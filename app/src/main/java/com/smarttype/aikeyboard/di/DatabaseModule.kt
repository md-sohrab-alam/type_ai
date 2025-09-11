package com.smarttype.aikeyboard.di

import android.content.Context
import androidx.room.Room
import com.smarttype.aikeyboard.data.database.SmartTypeDatabase
import com.smarttype.aikeyboard.data.database.TextSuggestionDao
import com.smarttype.aikeyboard.data.database.ToneStyleDao
import com.smarttype.aikeyboard.data.database.UserPreferencesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
    
    @Provides
    @Singleton
    fun provideSmartTypeDatabase(@ApplicationContext context: Context): SmartTypeDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SmartTypeDatabase::class.java,
            "smarttype_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideUserPreferencesDao(database: SmartTypeDatabase): UserPreferencesDao {
        return database.userPreferencesDao()
    }
    
    @Provides
    fun provideTextSuggestionDao(database: SmartTypeDatabase): TextSuggestionDao {
        return database.textSuggestionDao()
    }
    
    @Provides
    fun provideToneStyleDao(database: SmartTypeDatabase): ToneStyleDao {
        return database.toneStyleDao()
    }
}
