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

package com.teaphy.testzxing.zxing

import com.google.zxing.DecodeHintType
import com.google.zxing.ResultPointCallback

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.EnumMap
import java.util.concurrent.CountDownLatch

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
internal class DecodeThread(private val fragment: CaptureFragment,
                            resultPointCallback: ResultPointCallback) : Thread() {
	private val hints: MutableMap<DecodeHintType, Any>
	var handler: Handler? = null
		get() {
			try {
				handlerInitLatch.await()
			} catch (ie: InterruptedException) {
				// continue?
			}

			return field
		}
	private val handlerInitLatch: CountDownLatch

	init {
		handlerInitLatch = CountDownLatch(1)

		hints = EnumMap(DecodeHintType::class.java)


		hints[DecodeHintType.NEED_RESULT_POINT_CALLBACK] = resultPointCallback
		Log.i("DecodeThread", "Hints: $hints")
	}

	override fun run() {
		Looper.prepare()
		handler = DecodeHandler(fragment, hints)
		handlerInitLatch.countDown()
		Looper.loop()
	}

	companion object {

		const val BARCODE_BITMAP = "barcode_bitmap"
		const val BARCODE_SCALED_FACTOR = "barcode_scaled_factor"
	}

}
