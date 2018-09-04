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

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import com.teaphy.testzxing.camera.CameraManager
import java.io.IOException
import java.util.EnumSet

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
class CaptureActivity : Activity(), SurfaceHolder.Callback, IBarcodeAnalysisCallback {

	internal var cameraManager: CameraManager? = null
		private set
	internal var viewfinderView: ViewfinderView? = null
		private set
	private var hasSurface: Boolean = false
	private var inactivityTimer: InactivityTimer? = null
	// 管理响铃和震动
	private var beepManager: BeepManager? = null
	// 亮度低时，是否自动打开闪光灯
	private var ambientLightManager: AmbientLightManager? = null

	private var mDecodeThread: DecodeThread? = null

	public override fun onCreate(icicle: Bundle?) {
		super.onCreate(icicle)

		// 设置 保持屏幕常亮
		val window = window
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		setContentView(R.layout.capture)

		hasSurface = false

		// 管理Activity
		// 当设备使用电池时，若一段时间内没有交互，关闭当前Activity
		inactivityTimer = InactivityTimer(this)
		// 管理响铃和震动
		beepManager = BeepManager(this)
		// 亮度低时，是否自动打开闪光灯
		ambientLightManager = AmbientLightManager(this)

	}

	override fun onResume() {
		super.onResume()

		// 在onResume中初始化 CameraManager，而不能在onCreate中
		// 在首次启动时，并不是打开驱动程序并测量屏幕尺寸而是显示帮助
		cameraManager = CameraManager(application)

		// 获取取景器矩形，并设置CameraManager
		viewfinderView = findViewById(R.id.viewfinder_view)
		viewfinderView!!.setCameraManager(cameraManager!!)

		beepManager!!.updatePrefs()
		ambientLightManager!!.start(cameraManager!!)

		inactivityTimer!!.onResume()

		val surfaceView = findViewById<SurfaceView>(R.id.preview_view)
		val surfaceHolder = surfaceView.holder
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			// 在Activity经过Pause，但未进入stopped时， SurfaceView始终存在
			// 故而在这里初始化Camera
			initCamera(surfaceHolder)
		} else {
			// Install the callback and wait for surfaceCreated() to init the camera.
			// 当surfaceView不存在时，添加监听回调，以便初始化Camera
			surfaceHolder.addCallback(this)
		}
	}

	override fun onPause() {
		if (mDecodeThread != null) {
			mDecodeThread!!.quitSynchronously()
			mDecodeThread = null
		}
		inactivityTimer!!.onPause()
		ambientLightManager!!.stop()
		beepManager!!.close()
		cameraManager!!.closeDriver()
		//historyManager = null; // Keep for onActivityResult
		if (!hasSurface) {
			val surfaceView = findViewById<SurfaceView>(R.id.preview_view)
			val surfaceHolder = surfaceView.holder
			surfaceHolder.removeCallback(this)
		}
		super.onPause()
	}

	override fun onDestroy() {
		inactivityTimer!!.shutdown()
		super.onDestroy()
	}


	/**
	 * SurfaceView回调
	 */
	override fun surfaceCreated(holder: SurfaceHolder?) {
		if (holder == null) {
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!")
		}
		if (!hasSurface) {
			hasSurface = true
			// 初始化Camera
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
	fun handleDecode(rawResult: Result, barcode: Bitmap, scaleFactor: Float) {
		inactivityTimer!!.onActivity()
		Toast.makeText(this, rawResult.toString(), Toast.LENGTH_SHORT).show()

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

			if (mDecodeThread == null) {
				mDecodeThread = DecodeThread(
						ViewfinderResultPointCallback(viewfinderView!!),
						this, cameraManager!!)
				mDecodeThread!!.start()
			}

			cameraManager!!.startPreview()
			restartPreviewAndDecode()

			//  Creating the handler starts the preview, which can also throw a RuntimeException.
			//			if (handler == null) {
			//				// 初始化Handler
			//				handler = new CaptureActivityHandler(this, cameraManager);
			//			}
		} catch (ioe: IOException) {
			Log.w(TAG, ioe)
		} catch (e: RuntimeException) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e)
		}

	}

	/**
	 * 重置Preview并解码
	 */
	fun restartPreviewAfterDelay(delayMS: Long) {
		restartPreviewAndDecode()
	}


	fun drawViewfinder() {
		viewfinderView!!.drawViewfinder()
	}

	override fun onAnalysisSuccess(rawResult: Result, bundle: Bundle) {
		Toast.makeText(this, rawResult.toString(), Toast.LENGTH_SHORT).show()
	}

	/**
	 * 重置Preview并解码
	 */
	private fun restartPreviewAndDecode() {
		cameraManager!!.requestPreviewFrame(mDecodeThread!!.handler!!, R.id.decode)
		drawViewfinder()
	}

	override fun onAnalysisFailure() {
		restartPreviewAndDecode()
	}

	companion object {

		private val TAG = CaptureActivity::class.java.simpleName
	}
}
