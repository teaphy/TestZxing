package com.teaphy.testzxing.photos.ui

import android.content.Intent
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.constant.PhotosConstant
import com.teaphy.testzxing.photos.entity.LocalMedia
import com.teaphy.testzxing.photos.entity.LocalMediaFolder
import com.teaphy.testzxing.photos.loader.ILocalMediaLoadListener
import com.teaphy.testzxing.photos.loader.LocalMediaLoader

/**
 * @desc 选择图片 - 从本地Media数据库读取图片列表
 * @author tiany
 * @time 2018/9/30 上午9:37
 */
class PhotosSelectByFolderActivity : BasePhotoSelectorActivity() {

	override fun setListener() {
		super.setListener()

		backText.setOnClickListener {
			finish()
		}
	}

	/**
	 * 从本地Media数据库读取图片列表
	 */
	override fun loadLocalImage() {
		val bundle = intent.extras

		val images = bundle.getParcelableArrayList<LocalMedia>(PhotosConstant.KEY_CONTENT)

		images?.let {
			if (it.isNotEmpty()) {
				refreshMedia(it, false)
			}
		}
	}


}