package com.smarttype.aikeyboard.data.database

import androidx.room.*
import com.smarttype.aikeyboard.data.model.ToneStyle
import kotlinx.coroutines.flow.Flow

@Dao
interface ToneStyleDao {
    
    @Query("SELECT * FROM tone_styles WHERE isEnabled = 1 ORDER BY usageCount DESC, lastUsed DESC")
    fun getEnabledTones(): Flow<List<ToneStyle>>
    
    @Query("SELECT * FROM tone_styles WHERE isCustom = 1 ORDER BY usageCount DESC, lastUsed DESC")
    fun getCustomTones(): Flow<List<ToneStyle>>
    
    @Query("SELECT * FROM tone_styles WHERE id = :id")
    suspend fun getToneById(id: String): ToneStyle?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTone(tone: ToneStyle)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTones(tones: List<ToneStyle>)
    
    @Update
    suspend fun updateTone(tone: ToneStyle)
    
    @Query("UPDATE tone_styles SET usageCount = usageCount + 1, lastUsed = :currentTime WHERE id = :id")
    suspend fun incrementUsage(id: String, currentTime: Long = System.currentTimeMillis())
    
    @Query("UPDATE tone_styles SET isEnabled = :enabled WHERE id = :id")
    suspend fun updateToneEnabled(id: String, enabled: Boolean)
    
    @Query("DELETE FROM tone_styles WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomTone(id: String)
    
    @Query("DELETE FROM tone_styles WHERE isCustom = 1")
    suspend fun deleteAllCustomTones()
}
