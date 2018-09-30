package com.teaphy.testzxing.photos.entity

import android.os.Parcel
import android.os.Parcelable


/**
 * @desc 本地图片实体类
 * @author tiany
 * @time 2018/9/29 上午10:00
 */
data class LocalMedia(val id: String,
                      val path: String,
                      val name: String,
                      val pictureType: String,
                      val width: Int = 0,
                      val height: Int= 0,
                      val lastModify: Long = 0,
                      var isChecked: Boolean = false) : Parcelable {

	constructor(parcel: Parcel) : this(
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readString(),
			parcel.readInt(),
			parcel.readInt(),
			parcel.readLong(),
			parcel.readByte() != 0.toByte())

	override fun equals(other: Any?): Boolean {

		if (null == other) {
			return false
		}

		if (other !is LocalMedia) {
			return false
		}

		return this.id == other.id
	}

	override fun hashCode(): Int {
		return id.hashCode()
	}

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(id)
		parcel.writeString(path)
		parcel.writeString(name)
		parcel.writeString(pictureType)
		parcel.writeInt(width)
		parcel.writeInt(height)
		parcel.writeLong(lastModify)
		parcel.writeByte(if (isChecked) 1 else 0)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<LocalMedia> {
		override fun createFromParcel(parcel: Parcel): LocalMedia {
			return LocalMedia(parcel)
		}

		override fun newArray(size: Int): Array<LocalMedia?> {
			return arrayOfNulls(size)
		}
	}
}