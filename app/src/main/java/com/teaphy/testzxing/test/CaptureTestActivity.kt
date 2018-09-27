package com.teaphy.testzxing.test

import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.Result
import com.teaphy.testzxing.R
import com.teaphy.testzxing.zxing.CaptureFragment
import com.teaphy.testzxing.zxing.CodeUtils
import com.teaphy.testzxing.zxing.IAnalysisCallback
import kotlinx.android.synthetic.main.activity_capture_test.*

class CaptureTestActivity : AppCompatActivity(), IAnalysisCallback {
	override fun onAnalysisSuccess(rawResult: Result, barcode: Bitmap?) {
		tvResult.text = rawResult.text
	}

	override fun onAnalysisFailure() {
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_capture_test)

		val captureFragment = CaptureFragment()
		CodeUtils.setFragmentArgs(captureFragment, R.layout.fragment_capture_test)
		captureFragment.barcodeCallback = this

		val trans = supportFragmentManager.beginTransaction()
		trans.replace(R.id.flContainer, captureFragment)
		trans.commit()
	}
}
