package com.jkjk.doodlewatch.core.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jkjk.doodlewatch.core.model.DrawingHistory

/**
 *Created by chrisyeung on 26/3/2021.
 */
@Dao
interface DrawingHistoryDao {

    @Query("SELECT * FROM DrawingHistory")
    fun getAllSync(): List<DrawingHistory>
    @Query("SELECT * FROM DrawingHistory")
    fun getAllAsync(): LiveData<List<DrawingHistory>>

    @Query("SELECT * FROM DrawingHistory WHERE dbId = :id")
    fun getSync(id: Int): DrawingHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(drawingHistory: DrawingHistory)

    @Query("DELETE FROM DrawingHistory WHERE dbId = :id")
    fun remove(id: Int)
    @Query("DELETE FROM DrawingHistory WHERE dbId IN (:ids)")
    fun remove(ids: List<Int>)
}