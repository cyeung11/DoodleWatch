package com.jkjk.doodlewatch.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jkjk.doodlewatch.core.model.Drawing
import com.jkjk.doodlewatch.core.model.DrawingHistory

/**
 *Created by chrisyeung on 26/3/2021.
 */

@Database(entities = [Drawing::class, DrawingHistory::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getDrawingDao(): DrawingDao
    abstract fun getDrawingHistoryDao(): DrawingHistoryDao

    companion object {
        private const val DATABASE_NAME = "doodledatabase"

        private var _INSTANCE : AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (_INSTANCE == null) {
                _INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).allowMainThreadQueries()
                    .build()
            }
            return _INSTANCE!!
        }
    }
}