package com.teaphy.testzxing.photos

import android.content.Intent
import com.teaphy.testzxing.photos.config.PictureSelectConfig
import com.teaphy.testzxing.photos.entity.LocalMedia
import com.teaphy.testzxing.photos.loader.ILocalMediaLoadListener
import com.teaphy.testzxing.photos.loader.IMediaSelectListener
import com.teaphy.testzxing.photos.ui.PhotoSelectActivity
import com.teaphy.testzxing.photos.utils.DoubleUtils

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
	fun maxSelectNumber(maxNum: Int): PictureSelectConfig {
		pictureSelectConfig.maxSelectNumber = maxNum
		return pictureSelectConfig
	}

	/**
	 * 设置最少选择图片的数量
	 */
	fun minSelectNumber(minNum: Int): PictureSelectConfig {
		pictureSelectConfig.minSelectNumber = minNum
		return  pictureSelectConfig
	}

	/**
	 * 设置在图片选择列表中是否显示拍照图标
	 */
	fun isCamera(isCamera: Boolean): PictureSelectConfig {
		pictureSelectConfig.isCamera = isCamera
		return  pictureSelectConfig
	}

	/**
	 * 设置拍照后图片的保存路径
	 */
	fun outputCameraPath(path: String): PictureSelectConfig {
		pictureSelectConfig.outputCameraPath = path
		return  pictureSelectConfig
	}

	/**
	 * 已选择的图片列表
	 */
	fun selectMedias(list: List<LocalMedia>): PictureSelectConfig {
		if (null == pictureSelectConfig.selectMedias) {
			pictureSelectConfig.selectMedias = mutableListOf()
		} else {
			pictureSelectConfig.selectMedias!!.clear()
		}

		pictureSelectConfig.selectMedias!!.addAll(list)
		return  pictureSelectConfig
	}

	/**
	 * 图片选择的模式
	 */
	fun selectModel(model: PictureSelectConfig.SelectModel): PictureSelectConfig {
		pictureSelectConfig.selectModel = model
		return  pictureSelectConfig
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