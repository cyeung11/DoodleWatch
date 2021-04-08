package com.jkjk.doodlewatch.core

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.jkjk.doodlewatch.core.database.AppDatabase
import com.jkjk.doodlewatch.core.database.DrawingDao
import dagger.Module
import dagger.Provides

/**
 *Created by chrisyeung on 8/4/2021.
 */
@Module
class CommunicationModule(private val context: Context) {

    @Provides
    fun provideDataClient(): DataClient {
        return Wearable.getDataClient(context)
    }

    @Provides
    fun provideMessageClient(): MessageClient {
        return Wearable.getMessageClient(context)
    }

    @Provides
    fun provideDrawingDao(): DrawingDao {
        return AppDatabase.getInstance(context).getDrawingDao()
    }
}