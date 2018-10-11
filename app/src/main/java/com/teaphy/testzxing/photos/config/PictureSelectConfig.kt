package com.teaphy.testzxing.photos.config

import com.teaphy.testzxing.photos.entity.LocalMedia
import com.teaphy.testzxing.photos.loader.ILocalMediaLoadListener
import com.teaphy.testzxing.photos.loader.IMediaSelectListener
import kotlin.math.min

/**
 * @desc 图片选择配置
 * @author tiany
 * @time 2018/10/10 下午3:45
 */
class PictureSelectConfig private constructor() {

	// 最多选择的图片数量，默认为9张
	var maxSelectNumber: Int = 9
	// 最少选择的图片数量，默认为0张
	var minSelectNumber:Int = 0

	// 是否可以拍照,默认不可以拍照
	var isCamera: Boolean = false
	// 拍照后，图片的保存路径
	var outputCameraPath: String = ""

	// 已选择的图片
	var selectMedias: MutableList<LocalMedia>?= null

	// 图片选择的模式
	var selectModel: SelectModel = SelectModel.SINGLE

	var localMediaLoaderListener: IMediaSelectListener? = null

	enum class SelectModel {
		// 单张
		SINGLE,
		// 多张
		MULTIPLE
	}

	private class INNER {
		companion object {
			@JvmStatic
			var pictureSelectConfig: PictureSelectConfig? = null
		}
	}

	companion object {

		@Synchronized
		fun getInstance() : PictureSelectConfig {
			if (null == INNER.pictureSelectConfig) {
				INNER.pictureSelectConfig = PictureSelectConfig()
			}

			return INNER.pictureSelectConfig ?: throw Throwable("init PictureSelectConfig failure")
		}

		fun getCleanInstance(): PictureSelectConfig  {
			val pictureSelectConfig = getInstance()
			pictureSelectConfig.reset()
			return pictureSelectConfig
		}
	}

	fun reset() {
		maxSelectNumber = 9
		minSelectNumber = 0
		isCamera = true
		outputCameraPath = ""
		selectMedias = null
		localMediaLoaderListener = null
	}
}