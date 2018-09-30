package com.teaphy.testzxing.photos.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.teaphy.testzxing.photos.observe.CancelSubject
import com.teaphy.testzxing.photos.observe.ICancelObserve

/**
 * @desc
 * @author tiany
 * @time 2018/9/30 下午2:02
 */
abstract class BasePhotosActivity : AppCompatActivity(), ICancelObserve {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		CancelSubject.obtain().subcribeObserve(this)
	}

	override fun onDestroy() {
		super.onDestroy()
		CancelSubject.obtain().unsubcribeObserve(this)
	}

	override fun doCancel() {
		finish()
	}
}