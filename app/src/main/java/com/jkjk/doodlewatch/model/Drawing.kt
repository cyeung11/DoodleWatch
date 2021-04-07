package com.jkjk.doodlewatch.model

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *Created by chrisyeung on 26/3/2021.
 */
@Entity
data class Drawing(
    var createdDate: Long = 0L,
    var lastEditOn: Long = 0L,
    var name: String? = "Unnamed",
    var base64Image: String? = null,
    var backgroundColor: Int = Color.WHITE,
    var isFlagged: Boolean = false
) : Parcelable {
    @PrimaryKey(autoGenerate = true)
    var dbId: Int = 0

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    ) {
        dbId = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(createdDate)
        parcel.writeLong(lastEditOn)
        parcel.writeString(name)
        parcel.writeString(base64Image)
        parcel.writeInt(backgroundColor)
        parcel.writeByte(if (isFlagged) 1 else 0)
        parcel.writeInt(dbId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Drawing> {
        override fun createFromParcel(parcel: Parcel): Drawing {
            return Drawing(parcel)
        }

        override fun newArray(size: Int): Array<Drawing?> {
            return arrayOfNulls(size)
        }
    }
}