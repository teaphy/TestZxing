/*
 * Copyright (C) 2015 ZXing authors
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

package com.teaphy.testzxing.zxing.camera.open

import android.hardware.Camera


/**
 * Represents an open [Camera] and its metadata, like facing direction and orientation.
 */
// camera APIs
class OpenCamera(private val index: Int, val camera: Camera, val facing: CameraFacing, val orientation: Int) {

	override fun toString(): String {
		return "Camera #" + index + " : " + facing + ','.toString() + orientation
	}

}
