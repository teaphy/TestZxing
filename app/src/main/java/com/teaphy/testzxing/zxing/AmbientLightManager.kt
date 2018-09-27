/*
 * Copyright (C) 2012 ZXing authors
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

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.teaphy.testzxing.zxing.camera.CameraManager

/**
 * Detects ambient light and switches on the front light when very dark, and off again when sufficiently light.
 *
 * @author Sean Owen
 * @author Nikolaus Huber
 */
internal class AmbientLightManager(private val context: Context) : SensorEventListener {
	private var cameraManager: CameraManager? = null
	private var lightSensor: Sensor? = null

	fun start(cameraManager: CameraManager) {
		this.cameraManager = cameraManager
		val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
		lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
		if (lightSensor != null) {
			sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
		}
	}

	fun stop() {
		if (lightSensor != null) {
			val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
			sensorManager.unregisterListener(this)
			cameraManager = null
			lightSensor = null
		}
	}

	override fun onSensorChanged(sensorEvent: SensorEvent) {
		val ambientLightLux = sensorEvent.values[0]
		if (cameraManager != null) {
			if (ambientLightLux <= TOO_DARK_LUX) {
				cameraManager!!.setTorch(true)
			} else if (ambientLightLux >= BRIGHT_ENOUGH_LUX) {
				cameraManager!!.setTorch(false)
			}
		}
	}

	override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
		// do nothing
	}

	companion object {

		private val TOO_DARK_LUX = 45.0f
		private val BRIGHT_ENOUGH_LUX = 450.0f
	}

}
