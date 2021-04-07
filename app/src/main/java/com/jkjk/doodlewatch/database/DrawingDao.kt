package com.jkjk.doodlewatch.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jkjk.doodlewatch.model.Drawing

/**
 *Created by chrisyeung on 26/3/2021.
 */
@Dao
interface DrawingDao {

    @Query("SELECT * FROM Drawing ORDER BY lastEditOn DESC")
    fun getAllSync(): List<Drawing>
    @Query("SELECT * FROM Drawing ORDER BY lastEditOn DESC")
    fun getAllAsync(): LiveData<List<Drawing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(drawing: Drawing)

    @Query("DELETE FROM Drawing WHERE dbId = :id")
    fun remove(id: Int)
}