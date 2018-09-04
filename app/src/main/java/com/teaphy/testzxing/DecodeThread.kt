/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teaphy.testzxing

import android.os.Message
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.ResultPointCallback

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log

import com.teaphy.testzxing.camera.CameraManager
import java.util.EnumMap
import java.util.EnumSet
import java.util.concurrent.CountDownLatch

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
internal class DecodeThread(resultPointCallback: ResultPointCallback,
                            private val mBarcodeAnalysisCallback: IBarcodeAnalysisCallback,
                            private val cameraManager: CameraManager) : Thread() {

	// 封装一种提示，调用者可以将其传递给条形码阅读器，以帮助其更快或更准确地对其进行解码。
	private val hints: MutableMap<DecodeHintType, Any>
	var handler: Handler? = null
		get() {
			handlerInitLatch.await()
			return field
		}
	private val handlerInitLatch: CountDownLatch = CountDownLatch(1)

	init {
		hints = EnumMap(DecodeHintType::class.java)

		hints[DecodeHintType.NEED_RESULT_POINT_CALLBACK] = resultPointCallback
	}

//	fun getHandler(): Handler? {
//		try {
//			handlerInitLatch.await()
//		} catch (ie: InterruptedException) {
//			// continue?
//		}
//
//		return handler
//	}

	override fun run() {
		Looper.prepare()
		handler = DecodeHandler(hints, mBarcodeAnalysisCallback, cameraManager)
		handlerInitLatch.countDown()
		Looper.loop()
	}

	fun quitSynchronously() {
		cameraManager.stopPreview()
		val quit = Message.obtain(handler, R.id.quit)
		quit.sendToTarget()
		try {
			// Wait at most half a second; should be enough time, and onPause() will timeout quickly
			join(500L)
		} catch (e: InterruptedException) {
			// continue
		}

	}

	companion object {

		val BARCODE_BITMAP = "barcode_bitmap"
		val BARCODE_SCALED_FACTOR = "barcode_scaled_factor"
	}
}
