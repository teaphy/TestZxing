package com.teaphy.testzxing.photos.entity

/**
 * @desc 图片文件夹列表
 * @author tiany
 * @time 2018/9/29 上午10:45
 */
data class LocalMediaFolder(val name: String,
                            val path: String,
                            var firstImagePath: String,
                            val images: MutableList<LocalMedia> = mutableListOf()) {
	override fun equals(other: Any?): Boolean {

		if (null == other) {
			return false
		}

		if (other !is LocalMediaFolder) {
			return false
		}

		return this.path == other.path
	}

	override fun hashCode(): Int {
		return this.path.hashCode()
	}


}