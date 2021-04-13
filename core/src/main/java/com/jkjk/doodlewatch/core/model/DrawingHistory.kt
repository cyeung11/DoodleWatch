package com.jkjk.doodlewatch.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *Created by chrisyeung on 12/4/2021.
 */
@Entity
data class DrawingHistory(
    @PrimaryKey
    var dbId: Int,
    var lastEditOn: Long = System.currentTimeMillis(),
    var deletedOn: Long = -1L
)