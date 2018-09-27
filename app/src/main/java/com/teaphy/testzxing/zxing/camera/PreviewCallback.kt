/*
 * Copyright (C) 2010 ZXing authors
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

package com.teaphy.testzxing.zxing.camera

import android.hardware.Camera
import android.os.Handler
import android.os.Message
import android.util.Log

internal// camera APIs
class PreviewCallback(private val configManager: CameraConfigurationManager) : Camera.PreviewCallback {
	private var previewHandler: Handler? = null
	private var previewMessage: Int = 0

	fun setHandler(previewHandler: Handler?, previewMessage: Int) {
		this.previewHandler = previewHandler
		this.previewMessage = previewMessage
	}

	override fun onPreviewFrame(data: ByteArray, camera: Camera) {
		val cameraResolution = configManager.cameraResolution
		val thePreviewHandler = previewHandler
		if (cameraResolution != null && thePreviewHandler != null) {

			val screenResolution = configManager.screenResolution
			val message: Message
			if (screenResolution!!.x < screenResolution.y) {
				// portrait
				message = thePreviewHandler
						.obtainMessage(previewMessage, cameraResolution.y, cameraResolution.x, data)
			} else {
				// landscape
				message = thePreviewHandler
						.obtainMessage(previewMessage, cameraResolution.x, cameraResolution.y, data)
			}

			//			Message message = thePreviewHandler
			//				.obtainMessage(previewMessage, cameraResolution.x,
			//					cameraResolution.y, data);
			message.sendToTarget()
			previewHandler = null
		} else {
			Log.d(TAG, "Got preview callback, but no handler or resolution available")
		}
	}

	companion object {

		private val TAG = PreviewCallback::class.java.simpleName
	}

}
