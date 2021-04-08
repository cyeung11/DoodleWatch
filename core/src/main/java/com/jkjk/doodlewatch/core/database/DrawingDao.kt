package com.jkjk.doodlewatch.core.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jkjk.doodlewatch.core.model.Drawing

/**
 *Created by chrisyeung on 26/3/2021.
 */
@Dao
interface DrawingDao {

    @Query("SELECT * FROM Drawing ORDER BY lastEditOn DESC")
    fun getAllSync(): List<Drawing>
    @Query("SELECT * FROM Drawing ORDER BY lastEditOn DESC")
    fun getAllAsync(): LiveData<List<Drawing>>

//    @Query("SELECT MAX(lastEditOn) FROM Drawing")
//    fun getLatestTime(): Long?

//    @Query("SELECT * FROM Drawing WHERE lastEditOn > :lastEditOn")
//    fun getLaterThan(lastEditOn: Long): List<Drawing>

    @Query("SELECT * FROM Drawing WHERE dbId = :dbId AND lastEditOn > :lastEditOn")
    fun getNewer(dbId: Int, lastEditOn: Long): Drawing?

    @Query("SELECT * FROM Drawing WHERE dbId NOT IN (:ids)")
    fun getNotExist(ids: IntArray): List<Drawing>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(drawing: Drawing)

    @Query("DELETE FROM Drawing WHERE dbId = :id")
    fun remove(id: Int)
}