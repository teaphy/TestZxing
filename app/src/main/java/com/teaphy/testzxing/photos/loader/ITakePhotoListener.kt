package com.rrs.afcs.photos.loader

import com.rrs.afcs.photos.entity.LocalMedia

/**
 * @desc
 * @author tiany
 * @time 2018/10/12 下午2:59
 */
interface ITakePhotoListener {
	fun onTakePhoto(localMedia: LocalMedia?)
}