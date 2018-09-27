package com.teaphy.testzxing.zxing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.os.Bundle
import android.text.TextUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.Result
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.teaphy.testzxing.zxing.camera.BitmapLuminanceSource
import com.teaphy.testzxing.zxing.camera.CameraManager
import java.util.Hashtable
import java.util.Vector

object CodeUtils {

	val RESULT_TYPE = "result_type"
	val RESULT_STRING = "result_string"
	val RESULT_SUCCESS = 1
	val RESULT_FAILED = 2

	val LAYOUT_ID = "layout_id"


	/**
	 * 解析二维码图片工具类
	 * @param analyzeCallback
	 */
	fun analyzeBitmap(path: String, analyzeCallback: IAnalysisCallback?) {

		/**
		 * 首先判断图片的大小,若图片过大,则执行图片的裁剪操作,防止OOM
		 */
		val options = BitmapFactory.Options()
		options.inJustDecodeBounds = true // 先获取原大小
		var mBitmap = BitmapFactory.decodeFile(path, options)
		options.inJustDecodeBounds = false // 获取新的大小

		var sampleSize = (options.outHeight / 400.toFloat()).toInt()

		if (sampleSize <= 0)
			sampleSize = 1
		options.inSampleSize = sampleSize
		mBitmap = BitmapFactory.decodeFile(path, options)

		val multiFormatReader = MultiFormatReader()

		// 解码的参数
		val hints = Hashtable<DecodeHintType, Any>(2)
		// 可以解析的编码类型
		var decodeFormats = Vector<BarcodeFormat>()
		if (decodeFormats.isEmpty()) {
			decodeFormats = Vector()

			// 这里设置可扫描的类型，我这里选择了都支持
			decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS)
			decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS)
		}
		hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
		// 设置继续的字符编码格式为UTF8
		// hints.put(DecodeHintType.CHARACTER_SET, "UTF8");
		// 设置解析配置参数
		multiFormatReader.setHints(hints)

		// 开始对图像资源解码
		var rawResult: Result? = null
		try {
			rawResult = multiFormatReader.decodeWithState(BinaryBitmap(HybridBinarizer(BitmapLuminanceSource(mBitmap))))
		} catch (e: Exception) {
			e.printStackTrace()
		}

		if (rawResult != null) {
			analyzeCallback?.onAnalysisSuccess(rawResult, mBitmap)
		} else {
			analyzeCallback?.onAnalysisFailure()
		}
	}

	/**
	 * 生成二维码图片
	 * @param text
	 * @param w
	 * @param h
	 * @param logo
	 * @return
	 */
	fun createImage(text: String, w: Int, h: Int, logo: Bitmap): Bitmap? {
		if (TextUtils.isEmpty(text)) {
			return null
		}
		try {
			val scaleLogo = getScaleLogo(logo, w, h)

			var offsetX = w / 2
			var offsetY = h / 2

			var scaleWidth = 0
			var scaleHeight = 0
			if (scaleLogo != null) {
				scaleWidth = scaleLogo.width
				scaleHeight = scaleLogo.height
				offsetX = (w - scaleWidth) / 2
				offsetY = (h - scaleHeight) / 2
			}
			val hints = Hashtable<EncodeHintType, Any>()
			hints[EncodeHintType.CHARACTER_SET] = "utf-8"
			//容错级别
			hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
			//设置空白边距的宽度
			hints[EncodeHintType.MARGIN] = 0
			val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, w, h, hints)
			val pixels = IntArray(w * h)
			for (y in 0 until h) {
				for (x in 0 until w) {
					if (x >= offsetX && x < offsetX + scaleWidth && y >= offsetY && y < offsetY + scaleHeight) {
						var pixel = scaleLogo!!.getPixel(x - offsetX, y - offsetY)
						if (pixel == 0) {
							if (bitMatrix.get(x, y)) {
								pixel = -0x1000000
							} else {
								pixel = -0x1
							}
						}
						pixels[y * w + x] = pixel
					} else {
						if (bitMatrix.get(x, y)) {
							pixels[y * w + x] = -0x1000000
						} else {
							pixels[y * w + x] = -0x1
						}
					}
				}
			}
			val bitmap = Bitmap.createBitmap(w, h,
					Bitmap.Config.ARGB_8888)
			bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
			return bitmap
		} catch (e: WriterException) {
			e.printStackTrace()
		}

		return null
	}

	private fun getScaleLogo(logo: Bitmap?, w: Int, h: Int): Bitmap? {
		if (logo == null) return null
		val matrix = Matrix()
		val scaleFactor = Math.min(w * 1.0f / 5f / logo.width.toFloat(), h * 1.0f / 5f / logo.height.toFloat())
		matrix.postScale(scaleFactor, scaleFactor)
		return Bitmap.createBitmap(logo, 0, 0, logo.width, logo.height, matrix, true)
	}

	/**
	 * 为CaptureFragment设置layout参数
	 * @param captureFragment
	 * @param layoutId
	 */
	fun setFragmentArgs(captureFragment: CaptureFragment?, layoutId: Int) {
		if (captureFragment == null || layoutId == -1) {
			return
		}

		val bundle = Bundle()
		bundle.putInt(LAYOUT_ID, layoutId)
		captureFragment.arguments = bundle
	}

	fun isLightEnable(isEnable: Boolean) {
		if (isEnable) {
			val camera = CameraManager.getInstance().camera?.camera
			if (camera != null) {
				val parameter = camera.parameters
				parameter.flashMode = Camera.Parameters.FLASH_MODE_TORCH
				camera.parameters = parameter
			}
		} else {
			val camera = CameraManager.getInstance().camera?.camera
			if (camera != null) {
				val parameter = camera.parameters
				parameter.flashMode = Camera.Parameters.FLASH_MODE_OFF
				camera.parameters = parameter
			}
		}
	}
}
