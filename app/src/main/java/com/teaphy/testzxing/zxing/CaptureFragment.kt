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

import com.google.zxing.Result
import com.google.zxing.ResultPoint

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import com.teaphy.testzxing.R
import com.teaphy.testzxing.zxing.camera.CameraManager

import java.io.IOException

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
class CaptureFragment : Fragment(), SurfaceHolder.Callback {

	var cameraManager: CameraManager? = null
	private var handler: CaptureActivityHandler? = null
	internal var viewfinderView: ViewfinderView? = null
		private set
	private var hasSurface: Boolean = false
	private var inactivityTimer: InactivityTimer? = null
	private var beepManager: BeepManager? = null
	private var ambientLightManager: AmbientLightManager? = null

	lateinit var rootView: View

	var barcodeCallback: IAnalysisCallback? = null

	fun getHandler(): Handler? {
		return handler
	}

	public override fun onCreate(icicle: Bundle?) {
		super.onCreate(icicle)

		hasSurface = false
		inactivityTimer = InactivityTimer(activity!!)
		beepManager = BeepManager(activity!!)
		ambientLightManager = AmbientLightManager(activity!!)

		PreferenceManager.setDefaultValues(activity, R.xml.preferences, false)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

		val bundle = arguments
		var layoutId = -1
		if (bundle != null) {
			layoutId = bundle.getInt(CodeUtils.LAYOUT_ID)

		}

		rootView = if (layoutId != -1) {
			inflater.inflate(layoutId, container, false)
		} else {
			inflater.inflate(R.layout.fragment_capture, container, false)
		}

		return rootView
	}

	override fun onResume() {
		super.onResume()

		// 在onResume中初始化 CameraManager，而不能在onCreate中
		// 在首次启动时，并不是打开驱动程序并测量屏幕尺寸而是显示帮助
		cameraManager = CameraManager.obtain(activity!!)

		// 获取取景器矩形，并设置CameraManager
		viewfinderView = rootView.findViewById(R.id.viewFinderView) as ViewfinderView
		viewfinderView!!.setCameraManager(cameraManager!!)

		handler = null

		beepManager!!.updatePrefs()
		ambientLightManager!!.start(cameraManager!!)

		inactivityTimer!!.onResume()


		val surfaceView = rootView.findViewById(R.id.previewView) as SurfaceView
		val surfaceHolder = surfaceView.holder
		if (hasSurface) {
			// 在Activity经过Pause，但未进入stopped时， SurfaceView始终存在
			// 故而在这里初始化Camera
			initCamera(surfaceHolder)
		} else {
			// 当surfaceView不存在时，添加监听回调，以便初始化Camera
			surfaceHolder.addCallback(this)
		}
	}

	override fun onPause() {
		if (handler != null) {
			handler!!.quitSynchronously()
			handler = null
		}
		inactivityTimer!!.onPause()
		ambientLightManager!!.stop()
		beepManager!!.close()
		cameraManager!!.closeDriver()
		//historyManager = null; // Keep for onActivityResult
		if (!hasSurface) {
			val surfaceView = rootView.findViewById(R.id.previewView) as SurfaceView
			val surfaceHolder = surfaceView.holder
			surfaceHolder.removeCallback(this)
		}
		super.onPause()
	}

	override fun onDestroy() {
		inactivityTimer!!.shutdown()
		super.onDestroy()
	}

	public fun openTorch() {
		cameraManager!!.setTorch(true)
	}

	fun closeTorch() {
		cameraManager!!.setTorch(false)
	}


	override fun surfaceCreated(holder: SurfaceHolder?) {
		if (holder == null) {
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!")
		}
		if (!hasSurface) {
			hasSurface = true
			initCamera(holder)
		}
	}

	override fun surfaceDestroyed(holder: SurfaceHolder) {
		hasSurface = false
	}

	override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
		// do nothing
	}

	/**
	 * 扫描结果回调 一个有效的条形码已经知道，给出一个成功的提示并显示结果
	 */
	fun handleDecode(rawResult: Result, barcode: Bitmap?, scaleFactor: Float) {
		inactivityTimer!!.onActivity()

		barcodeCallback?.onAnalysisSuccess(rawResult, barcode)

		restartPreviewAfterDelay(500)
	}

	/**
	 * 扫描失败
	 */
	fun handleScanFail() {
		barcodeCallback?.onAnalysisFailure()

		restartPreviewAfterDelay(500)
	}

	/**
	 * 初始化Camera
	 */
	private fun initCamera(surfaceHolder: SurfaceHolder?) {
		if (surfaceHolder == null) {
			throw IllegalStateException("No SurfaceHolder provided")
		}
		if (cameraManager!!.isOpen) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?")
			return
		}
		try {
			cameraManager!!.openDriver(surfaceHolder)
			// Creating the handler starts the preview, which can also throw a RuntimeException.
			if (handler == null) {
				handler = CaptureActivityHandler(this, cameraManager!!)
			}
//			decodeOrStoreSavedBitmap(null, null)
		} catch (ioe: IOException) {
			Log.w(TAG, ioe)
		} catch (e: RuntimeException) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e)
		}

	}


	fun restartPreviewAfterDelay(delayMS: Long) {
		if (handler != null) {
			handler!!.sendEmptyMessageDelayed(R.id.restart_preview, delayMS)
		}
	}

	fun drawViewfinder() {
		viewfinderView!!.drawViewfinder()
	}

	companion object {

		private val TAG = CaptureFragment::class.java.simpleName

		private fun drawLine(canvas: Canvas, paint: Paint, a: ResultPoint?, b: ResultPoint?, scaleFactor: Float) {
			if (a != null && b != null) {
				canvas.drawLine(scaleFactor * a.x,
						scaleFactor * a.y,
						scaleFactor * b.x,
						scaleFactor * b.y,
						paint)
			}
		}
	}
}
