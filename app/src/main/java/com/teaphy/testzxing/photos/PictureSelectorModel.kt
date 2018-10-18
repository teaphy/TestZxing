package com.rrs.afcs.photos

import android.content.Intent
import com.rrs.afcs.photos.config.PictureSelectConfig
import com.rrs.afcs.photos.entity.LocalMedia
import com.rrs.afcs.photos.loader.ILocalMediaLoadListener
import com.rrs.afcs.photos.loader.IMediaSelectListener
import com.rrs.afcs.photos.ui.PhotoSelectActivity
import com.rrs.afcs.photos.utils.DoubleUtils

/**
 * @desc
 * @author tiany
 * @time 2018/10/11 上午11:04
 */
class PictureSelectorModel(val pictureSelectors: PictureSelectors) {
	private val pictureSelectConfig: PictureSelectConfig = PictureSelectConfig.getCleanInstance()

	constructor(pictureSelectors: PictureSelectors, isCamera: Boolean) : this(pictureSelectors) {
		pictureSelectConfig.isCamera = isCamera
	}

	/**
	 * 设置最多选择图片数量
	 */
	fun maxSelectNumber(maxNum: Int): PictureSelectorModel {
		pictureSelectConfig.maxSelectNumber = maxNum
		return this
	}

	/**
	 * 设置最少选择图片的数量
	 */
	fun minSelectNumber(minNum: Int): PictureSelectorModel {
		pictureSelectConfig.minSelectNumber = minNum
		return  this
	}

	/**
	 * 设置在图片选择列表中是否显示拍照图标
	 */
	fun isCamera(isCamera: Boolean): PictureSelectorModel {
		pictureSelectConfig.isCamera = isCamera
		return  this
	}

	/**
	 * 设置拍照后图片的保存路径
	 */
	fun outputCameraPath(path: String): PictureSelectorModel {
		pictureSelectConfig.outputCameraPath = path
		return  this
	}


	/**
	 * 图片选择的模式
	 */
	fun selectModel(model: PictureSelectConfig.SelectModel): PictureSelectorModel {
		pictureSelectConfig.selectModel = model
		return  this
	}

	/**
	 * 开启选择图片并将回调结果
	 */
	fun requestMedias(loadListener: IMediaSelectListener) {
		pictureSelectConfig.localMediaLoaderListener = loadListener

		if (!DoubleUtils.isFastDoubleClick) {
			val activity = pictureSelectors.getActivity() ?: return
			val intent = Intent(activity, PhotoSelectActivity::class.java)
			activity.startActivity(intent)
		}
	}

	/**
	 * 提供外部预览图片
	 */
	fun openExternalPreview(indexPreview: Int, listMedia: List<LocalMedia>) {

	}
}