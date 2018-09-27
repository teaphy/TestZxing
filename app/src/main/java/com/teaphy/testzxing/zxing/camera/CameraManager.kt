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

package com.teaphy.testzxing.zxing.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.hardware.Camera
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
import com.google.zxing.PlanarYUVLuminanceSource
import com.teaphy.testzxing.zxing.camera.open.OpenCamera
import com.teaphy.testzxing.zxing.camera.open.OpenCameraInterface

import java.io.IOException

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
// camera APIs
class CameraManager private constructor(private val context: Context) {
	private val configManager: CameraConfigurationManager = CameraConfigurationManager(context)
	var camera: OpenCamera? = null
	private var autoFocusManager: AutoFocusManager? = null
	var framingRect: Rect? = null
		get() {
			if (field == null) {
				if (camera == null) {
					return null
				}
				val screenResolution = configManager.screenResolution
					?: // Called early, before init even finished
					return null

				val width = findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH)
				val height = findDesiredDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT)

				val leftOffset = (screenResolution.x - width) / 2
				val topOffset = (screenResolution.y - height) / 2
				field = Rect(leftOffset, topOffset, leftOffset + width, topOffset + height)
				Log.d(TAG, "Calculated framing rect: " + field!!)
			}
			return field
		}
	var framingRectInPreview: Rect? = null
		get() {
			if (field== null) {
				val framingRect = framingRect ?: return null
				val rect = Rect(framingRect)
				val cameraResolution = configManager.cameraResolution
				val screenResolution = configManager.screenResolution
				if (cameraResolution == null || screenResolution == null) {
					// Called early, before init even finished
					return null
				}


				if (screenResolution.x < screenResolution.y) {
					// 下面为竖屏模式
					rect.left = rect.left * cameraResolution.y / screenResolution.x
					rect.right = rect.right * cameraResolution.y / screenResolution.x
					rect.top = rect.top * cameraResolution.x / screenResolution.y
					rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y
				} else {
					// 下面为横屏模式
					rect.left = rect.left * cameraResolution.x / screenResolution.x
					rect.right = rect.right * cameraResolution.x / screenResolution.x
					rect.top = rect.top * cameraResolution.y / screenResolution.y
					rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y
				}


				field = rect
			}
			return field
		}
	private var initialized: Boolean = false
	private var previewing: Boolean = false
	private var requestedCameraId = OpenCameraInterface.NO_REQUESTED_CAMERA
	private var requestedFramingRectWidth: Int = 0
	private var requestedFramingRectHeight: Int = 0
	/**
	 * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
	 * clear the handler so it will only receive one message.
	 */
	private val previewCallback: PreviewCallback

	val isOpen: Boolean
		@Synchronized get() = camera != null

	init {
		previewCallback = PreviewCallback(configManager)
	}

	/**
	 * Opens the camera driver and initializes the hardware parameters.
	 *
	 * @param holder The surface object which the camera will draw preview frames into.
	 * @throws IOException Indicates the camera driver failed to open.
	 */
	@Synchronized
	@Throws(IOException::class)
	fun openDriver(holder: SurfaceHolder) {
		var theCamera = camera
		if (theCamera == null) {
			theCamera = OpenCameraInterface.open(requestedCameraId)
			if (theCamera == null) {
				throw IOException("Camera.open() failed to return object from driver")
			}
			camera = theCamera
		}

		if (!initialized) {
			initialized = true
			configManager.initFromCameraParameters(theCamera!!)
			if (requestedFramingRectWidth > 0 && requestedFramingRectHeight > 0) {
				setManualFramingRect(requestedFramingRectWidth, requestedFramingRectHeight)
				requestedFramingRectWidth = 0
				requestedFramingRectHeight = 0
			}
		}

		val cameraObject = theCamera.camera
		var parameters: Camera.Parameters? = cameraObject.getParameters()
		val parametersFlattened = parameters?.flatten() // Save these, temporarily
		try {
			configManager.setDesiredCameraParameters(theCamera, false)
		} catch (re: RuntimeException) {
			// Driver failed
			Log.w(TAG, "Camera rejected parameters. Setting only minimal safe-mode parameters")
			Log.i(TAG, "Resetting to saved camera params: " + parametersFlattened!!)
			// Reset:
			if (parametersFlattened != null) {
				parameters = cameraObject.getParameters()
				parameters!!.unflatten(parametersFlattened)
				try {
					cameraObject.setParameters(parameters)
					configManager.setDesiredCameraParameters(theCamera!!, true)
				} catch (re2: RuntimeException) {
					// Well, darn. Give up
					Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration")
				}

			}
		}

		cameraObject.setPreviewDisplay(holder)

	}

	/**
	 * Closes the camera driver if still in use.
	 */
	@Synchronized
	fun closeDriver() {
		if (camera != null) {
			camera!!.camera.release()
			camera = null
			// Make sure to clear these each time we close the camera, so that any scanning rect
			// requested by intent is forgotten.
			framingRect = null
			framingRectInPreview = null
		}
	}

	/**
	 * Asks the camera hardware to begin drawing preview frames to the screen.
	 */
	@Synchronized
	fun startPreview() {
		val theCamera = camera
		if (theCamera != null && !previewing) {
			theCamera.camera.startPreview()
			previewing = true
			autoFocusManager = AutoFocusManager(context, theCamera.camera,true)
		}
	}

	/**
	 * Tells the camera to stop drawing preview frames.
	 */
	@Synchronized
	fun stopPreview() {
		if (autoFocusManager != null) {
			autoFocusManager!!.stop()
			autoFocusManager = null
		}
		if (camera != null && previewing) {
			camera!!.camera.stopPreview()
			previewCallback.setHandler(null, 0)
			previewing = false
		}
	}

	/**
	 * Convenience method for [com.google.zxing.client.android.CaptureActivity]
	 *
	 * @param newSetting if `true`, light should be turned on if currently off. And vice versa.
	 */
	@Synchronized
	fun setTorch(newSetting: Boolean) {
		val theCamera = camera
		if (theCamera != null && newSetting != configManager.getTorchState(theCamera.camera)) {
			val wasAutoFocusManager = autoFocusManager != null
			if (wasAutoFocusManager) {
				autoFocusManager!!.stop()
				autoFocusManager = null
			}
			configManager.setTorch(theCamera.camera, newSetting)
			if (wasAutoFocusManager) {
				autoFocusManager = AutoFocusManager(context, theCamera.camera, true)
				autoFocusManager!!.start()
			}
		}
	}

	/**
	 * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
	 * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
	 * respectively.
	 *
	 * @param handler The handler to send the message to.
	 * @param message The what field of the message to be sent.
	 */
	@Synchronized
	fun requestPreviewFrame(handler: Handler, message: Int) {
		val theCamera = camera
		if (theCamera != null && previewing) {
			previewCallback.setHandler(handler, message)
			theCamera.camera.setOneShotPreviewCallback(previewCallback)
		}
	}

	/**
	 * Calculates the framing rect which the UI should draw to show the user where to place the
	 * barcode. This target helps with alignment as well as forces the user to hold the device
	 * far enough away to ensure the image will be in focus.
	 *
	 * @return The rectangle to draw on screen in window coordinates.
	 */
//	@Synchronized
//	fun getFramingRect(): Rect? {
//		if (framingRect == null) {
//			if (camera == null) {
//				return null
//			}
//			val screenResolution = configManager.screenResolution
//				?: // Called early, before init even finished
//				return null
//
//			val width = findDesiredDimensionInRange(screenResolution.x, MIN_FRAME_WIDTH, MAX_FRAME_WIDTH)
//			val height = findDesiredDimensionInRange(screenResolution.y, MIN_FRAME_HEIGHT, MAX_FRAME_HEIGHT)
//
//			val leftOffset = (screenResolution.x - width) / 2
//			val topOffset = (screenResolution.y - height) / 2
//			framingRect = Rect(leftOffset, topOffset, leftOffset + width, topOffset + height)
//			Log.d(TAG, "Calculated framing rect: " + framingRect!!)
//		}
//		return framingRect
//	}

	/**
	 * Like [.getFramingRect] but coordinates are in terms of the preview frame,
	 * not UI / screen.
	 *
	 * @return [Rect] expressing barcode scan area in terms of the preview size
	 */
//	@Synchronized
//	fun getFramingRectInPreview(): Rect? {
//		if (framingRectInPreview == null) {
//			val framingRect = getFramingRect() ?: return null
//			val rect = Rect(framingRect)
//			val cameraResolution = configManager.cameraResolution
//			val screenResolution = configManager.screenResolution
//			if (cameraResolution == null || screenResolution == null) {
//				// Called early, before init even finished
//				return null
//			}
//			rect.left = rect.left * cameraResolution.x / screenResolution.x
//			rect.right = rect.right * cameraResolution.x / screenResolution.x
//			rect.top = rect.top * cameraResolution.y / screenResolution.y
//			rect.bottom = rect.bottom * cameraResolution.y / screenResolution.y
//			framingRectInPreview = rect
//		}
//		return framingRectInPreview
//	}


	/**
	 * Allows third party apps to specify the camera ID, rather than determine
	 * it automatically based on available cameras and their orientation.
	 *
	 * @param cameraId camera ID of the camera to use. A negative value means "no preference".
	 */
	@Synchronized
	fun setManualCameraId(cameraId: Int) {
		requestedCameraId = cameraId
	}

	/**
	 * Allows third party apps to specify the scanning rectangle dimensions, rather than determine
	 * them automatically based on screen resolution.
	 *
	 * @param width The width in pixels to scan.
	 * @param height The height in pixels to scan.
	 */
	@Synchronized
	fun setManualFramingRect(width: Int, height: Int) {
		var width = width
		var height = height
		if (initialized) {
			val screenResolution = configManager.screenResolution
			if (width > screenResolution!!.x) {
				width = screenResolution.x
			}
			if (height > screenResolution.y) {
				height = screenResolution.y
			}
			val leftOffset = (screenResolution.x - width) / 2
			val topOffset = (screenResolution.y - height) / 2
			framingRect = Rect(leftOffset, topOffset, leftOffset + width, topOffset + height)
			Log.d(TAG, "Calculated manual framing rect: " + framingRect!!)
			framingRectInPreview = null
		} else {
			requestedFramingRectWidth = width
			requestedFramingRectHeight = height
		}
	}

	/**
	 * A factory method to build the appropriate LuminanceSource object based on the format
	 * of the preview buffers, as described by Camera.Parameters.
	 *
	 * @param data A preview frame.
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @return A PlanarYUVLuminanceSource instance.
	 */
	fun buildLuminanceSource(data: ByteArray, width: Int, height: Int): PlanarYUVLuminanceSource? {
		val rect = framingRectInPreview ?: return null
// Go ahead and assume it's YUV rather than die.
		return PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
				rect.width(), rect.height(), false)
	}

	companion object {

		@SuppressLint("StaticFieldLeak")
		private var INSTANCE: CameraManager? = null

		@Synchronized
		fun obtain(context: Context) : CameraManager{
			@Synchronized if (null == INSTANCE) {
				INSTANCE = CameraManager(context)
			}

			return INSTANCE!!
		}

		fun getInstance() : CameraManager {
			return INSTANCE ?: throw Throwable("CameraManager isn't init")
		}

		private val TAG = CameraManager::class.java.simpleName

		private val MIN_FRAME_WIDTH = 240
		private val MIN_FRAME_HEIGHT = 240
		private val MAX_FRAME_WIDTH = 1200 // = 5/8 * 1920
		private val MAX_FRAME_HEIGHT = 675 // = 5/8 * 1080

		private fun findDesiredDimensionInRange(resolution: Int, hardMin: Int, hardMax: Int): Int {
			val dim = 5 * resolution / 8 // Target 5/8 of each dimension
			if (dim < hardMin) {
				return hardMin
			}
			return if (dim > hardMax) {
				hardMax
			} else dim
		}
	}

}
