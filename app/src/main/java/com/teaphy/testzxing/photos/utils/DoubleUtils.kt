package com.rrs.afcs.photos.utils

/**
 * @author tiany
 * @desc
 * @time 2018/10/11 下午1:30
 */
class DoubleUtils {


	companion object {

		private var lastClickTime: Long = 0
		private val TIME: Long = 800

		val isFastDoubleClick: Boolean
			get() {
				val time = System.currentTimeMillis()
				if (time - lastClickTime < TIME) {
					return true
				}
				lastClickTime = time
				return false
			}
	}


}

