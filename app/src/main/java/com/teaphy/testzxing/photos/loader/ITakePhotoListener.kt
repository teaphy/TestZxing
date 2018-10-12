package com.teaphy.testzxing.photos.loader

import com.teaphy.testzxing.photos.entity.LocalMedia

/**
 * @desc
 * @author tiany
 * @time 2018/10/12 下午2:59
 */
interface ITakePhotoListener {
	fun onTakePhoto(localMedia: LocalMedia?)
}