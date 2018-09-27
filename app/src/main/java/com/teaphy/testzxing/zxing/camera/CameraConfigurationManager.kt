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

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import android.hardware.Camera
import android.preference.PreferenceManager
import android.util.Log
import android.view.Surface
import android.view.WindowManager

import com.google.zxing.client.android.camera.CameraConfigurationUtils
import com.teaphy.testzxing.zxing.camera.open.CameraFacing
import com.teaphy.testzxing.zxing.camera.open.OpenCamera
import java.lang.reflect.Method

/**
 * A class which deals with reading, parsing, and setting the camera parameters which are used to
 * configure the camera hardware. 用于处理读取，解析和设置用于配置相机硬件的相机参数的类。
 */
internal open// camera APIs
class CameraConfigurationManager(private val context: Context) {
	var cwNeededRotation: Int = 0
		private set
	private var cwRotationFromDisplayToCamera: Int = 0
	// 屏幕的尺寸(高度和宽度)
	var screenResolution: Point? = null
		private set
	// 相机分辨率
	var cameraResolution: Point? = null
		private set
	var bestPreviewSize: Point? = null
		private set
	var previewSizeOnScreen: Point? = null
		private set

	/**
	 * Reads, one time, values from the camera that are needed by the app.
	 */
	fun initFromCameraParameters(camera: OpenCamera) {
		val parameters = camera.camera.parameters
		val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
		val display = manager.defaultDisplay

		val displayRotation = display.rotation
		val cwRotationFromNaturalToDisplay: Int
		when (displayRotation) {
			Surface.ROTATION_0 -> cwRotationFromNaturalToDisplay = 0
			Surface.ROTATION_90 -> cwRotationFromNaturalToDisplay = 90
			Surface.ROTATION_180 -> cwRotationFromNaturalToDisplay = 180
			Surface.ROTATION_270 -> cwRotationFromNaturalToDisplay = 270
			else ->
				// Have seen this return incorrect values like -90
				if (displayRotation % 90 == 0) {
					cwRotationFromNaturalToDisplay = (360 + displayRotation) % 360
				} else {
					throw IllegalArgumentException("Bad rotation: $displayRotation")
				}
		}
		Log.i(TAG, "Display at: $cwRotationFromNaturalToDisplay")

		var cwRotationFromNaturalToCamera = camera.orientation
		Log.i(TAG, "Camera at: $cwRotationFromNaturalToCamera")

		// Still not 100% sure about this. But acts like we need to flip this:
		if (camera.facing == CameraFacing.FRONT) {
			cwRotationFromNaturalToCamera = (360 - cwRotationFromNaturalToCamera) % 360
			Log.i(TAG, "Front camera overriden to: $cwRotationFromNaturalToCamera")
		}

		/*
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String overrideRotationString;
    if (camera.getFacing() == CameraFacing.FRONT) {
      overrideRotationString = prefs.getString(PreferencesActivity.KEY_FORCE_CAMERA_ORIENTATION_FRONT, null);
    } else {
      overrideRotationString = prefs.getString(PreferencesActivity.KEY_FORCE_CAMERA_ORIENTATION, null);
    }
    if (overrideRotationString != null && !"-".equals(overrideRotationString)) {
      Log.i(TAG, "Overriding camera manually to " + overrideRotationString);
      cwRotationFromNaturalToCamera = Integer.parseInt(overrideRotationString);
    }
     */

		cwRotationFromDisplayToCamera = (360 + cwRotationFromNaturalToCamera - cwRotationFromNaturalToDisplay) % 360
		Log.i(TAG, "Final display orientation: $cwRotationFromDisplayToCamera")
		if (camera.facing == CameraFacing.FRONT) {
			Log.i(TAG, "Compensating rotation for front camera")
			cwNeededRotation = (360 - cwRotationFromDisplayToCamera) % 360
		} else {
			cwNeededRotation = cwRotationFromDisplayToCamera
		}
		Log.i(TAG, "Clockwise rotation from display to camera: $cwNeededRotation")

		val theScreenResolution = Point()
		display.getSize(theScreenResolution)
		screenResolution = theScreenResolution
		Log.i(TAG, "Screen resolution in current orientation: " + screenResolution!!)

		//为竖屏添加
		val screenResolutionForCamera = Point()
		screenResolutionForCamera.x = screenResolution!!.x
		screenResolutionForCamera.y = screenResolution!!.y
		if (screenResolution!!.x < screenResolution!!.y) {
			screenResolutionForCamera.x = screenResolution!!.y
			screenResolutionForCamera.y = screenResolution!!.x
		}
		// 下句第二参数要根据竖屏修改
		cameraResolution = CameraConfigurationUtils
				.findBestPreviewSizeValue(parameters, screenResolutionForCamera)


		Log.i(TAG, "Camera resolution: " + cameraResolution!!)
		bestPreviewSize = CameraConfigurationUtils
				.findBestPreviewSizeValue(parameters, screenResolution)
		Log.i(TAG, "Best available preview size: " + bestPreviewSize!!)

		val isScreenPortrait = screenResolution!!.x < screenResolution!!.y
		val isPreviewSizePortrait = bestPreviewSize!!.x < bestPreviewSize!!.y

		if (isScreenPortrait == isPreviewSizePortrait) {
			previewSizeOnScreen = bestPreviewSize
		} else {
			previewSizeOnScreen = Point(bestPreviewSize!!.y, bestPreviewSize!!.x)
		}
		Log.i(TAG, "Preview size on screen: " + previewSizeOnScreen!!)
	}

	fun setDesiredCameraParameters(camera: OpenCamera, safeMode: Boolean) {

		val theCamera = camera.camera
		val parameters = theCamera.parameters ?: return

		// 使摄像头旋转90度
		setDisplayOrientation(theCamera, 90)

		if (safeMode) {
		}

		val prefs = PreferenceManager.getDefaultSharedPreferences(context)

		initializeTorch(parameters, prefs, safeMode)

		//		CameraConfigurationUtils.setFocus(
		//			parameters,
		//			prefs.getBoolean(PreferencesActivity.KEY_AUTO_FOCUS, true),
		//			prefs.getBoolean(PreferencesActivity.KEY_DISABLE_CONTINUOUS_FOCUS, true),
		//			safeMode);

		if (!safeMode) {
			//			if (prefs.getBoolean(PreferencesActivity.KEY_INVERT_SCAN, false)) {
			//				CameraConfigurationUtils.setInvertColor(parameters);
			//			}
			//
			//			if (!prefs.getBoolean(PreferencesActivity.KEY_DISABLE_BARCODE_SCENE_MODE, true)) {
			//				CameraConfigurationUtils.setBarcodeSceneMode(parameters);
			//			}
			//
			//			if (!prefs.getBoolean(PreferencesActivity.KEY_DISABLE_METERING, true)) {
			//				CameraConfigurationUtils.setVideoStabilization(parameters);
			//				CameraConfigurationUtils.setFocusArea(parameters);
			//				CameraConfigurationUtils.setMetering(parameters);
			//			}

			//SetRecordingHint to true also a workaround for low framerate on Nexus 4
			//https://stackoverflow.com/questions/14131900/extreme-camera-lag-on-nexus-4
			parameters.setRecordingHint(true)

		}

		parameters.setPreviewSize(bestPreviewSize!!.x, bestPreviewSize!!.y)

		theCamera.parameters = parameters

		theCamera.setDisplayOrientation(cwRotationFromDisplayToCamera)

		val afterParameters = theCamera.parameters
		val afterSize = afterParameters.previewSize
		if (afterSize != null && (bestPreviewSize!!.x != afterSize.width || bestPreviewSize!!.y != afterSize.height)) {
			bestPreviewSize!!.x = afterSize.width
			bestPreviewSize!!.y = afterSize.height
		}
	}

	fun setDisplayOrientation(camera: Camera, angle: Int) {
		var  downPolymorphic: Method? = null

		try {
			downPolymorphic = camera.javaClass.getMethod("setDisplayOrientation", Int::class.java)
			downPolymorphic?.invoke(camera, angle);
		} catch (e: Exception) {
			e.printStackTrace();
		}
	}

	fun getTorchState(camera: Camera?): Boolean {
		if (camera != null) {
			val parameters = camera.parameters
			if (parameters != null) {
				val flashMode = parameters.flashMode
				return Camera.Parameters.FLASH_MODE_ON == flashMode || Camera.Parameters.FLASH_MODE_TORCH == flashMode
			}
		}
		return false
	}

	fun setTorch(camera: Camera, newSetting: Boolean) {
		val parameters = camera.parameters
		doSetTorch(parameters, newSetting, false)
		camera.parameters = parameters
	}

	private fun initializeTorch(parameters: Camera.Parameters, prefs: SharedPreferences,
	                            safeMode: Boolean) {
		val currentSetting = FrontLightMode.readPref(prefs) == FrontLightMode.ON
		doSetTorch(parameters, currentSetting, safeMode)
	}

	private fun doSetTorch(parameters: Camera.Parameters, newSetting: Boolean, safeMode: Boolean) {
		CameraConfigurationUtils.setTorch(parameters, newSetting)
		//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		//		if (!safeMode && !prefs.getBoolean(PreferencesActivity.KEY_DISABLE_EXPOSURE, true)) {
		//			CameraConfigurationUtils.setBestExposure(parameters, newSetting);
		//		}
	}

	companion object {

		private val TAG = "CameraConfiguration"
	}

}
