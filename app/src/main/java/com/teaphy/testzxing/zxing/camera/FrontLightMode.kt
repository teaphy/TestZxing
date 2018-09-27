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

package com.teaphy.testzxing.zxing.camera

import android.content.SharedPreferences
import com.teaphy.testzxing.zxing.ZxingKeyConstant

/**
 * Enumerates settings of the preference controlling the front light.
 */
enum class FrontLightMode {

	/** Always on.  */
	ON,
	/** On only when ambient light is low.  */
	AUTO,
	/** Always off.  */
	OFF;


	companion object {

		private fun parse(modeString: String?): FrontLightMode {
			return if (modeString == null) OFF else valueOf(modeString)
		}

		fun readPref(sharedPrefs: SharedPreferences): FrontLightMode {
			return parse(sharedPrefs.getString(ZxingKeyConstant.FRONT_LIGHT_MODE, OFF.toString()))
		}
	}

}
