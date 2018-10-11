package com.teaphy.testzxing.test

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.teaphy.testzxing.R

class TestLaunchActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_test_launch)

		findViewById<Button>(R.id.picture_button).setOnClickListener {
			val intent = Intent(this@TestLaunchActivity, PhotosTestActivity::class.java)
			startActivity(intent)
		}

		findViewById<Button>(R.id.zxing_button).setOnClickListener {
			val intent = Intent(this@TestLaunchActivity, CaptureTestActivity::class.java)
			startActivity(intent)
		}
	}
}
