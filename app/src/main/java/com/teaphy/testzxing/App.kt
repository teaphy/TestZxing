package com.teaphy.testzxing

import android.app.Application
import com.blankj.utilcode.util.Utils
import com.rrs.afcs.base.BaseApplication
import timber.log.Timber

/**
 * @desc
 * @author tiany
 * @time 2018/9/27 下午4:11
 */
class App: Application() {

	override fun onCreate() {
		super.onCreate()

		Timber.plant(Timber.DebugTree())

		Utils.init(this)
	}

}