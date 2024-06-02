package io.github.abdulroufsidhu.tasveer.data

import android.os.Parcel
import android.os.Parcelable

data class ViewPhoto(
    val id: String,
    val thumbnailUrl: String,
    val fullUrl: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(thumbnailUrl)
        parcel.writeString(fullUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ViewPhoto> {
        override fun createFromParcel(parcel: Parcel): ViewPhoto {
            return ViewPhoto(parcel)
        }

        override fun newArray(size: Int): Array<ViewPhoto?> {
            return arrayOfNulls(size)
        }
    }
}