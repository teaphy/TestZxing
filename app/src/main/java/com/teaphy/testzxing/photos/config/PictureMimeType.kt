package com.teaphy.testzxing.photos.config

import android.text.TextUtils
import java.io.File


/**
 * @desc
 * @author tiany
 * @time 2018/10/12 下午5:04
 */
class PictureMimeType {
	companion object {
		fun createImageType(path: String): String {
			try {
				if (!TextUtils.isEmpty(path)) {
					val file = File(path)
					val fileName = file.getName()
					val last = fileName.lastIndexOf(".") + 1
					val temp = fileName.substring(last, fileName.length)
					return "image/$temp"
				}
			} catch (e: Exception) {
				e.printStackTrace()
				return "image/jpeg"
			}
			return "image/jpeg"
		}
	}

}