package com.teaphy.testzxing.test

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.entity.LocalMedia
import com.teaphy.testzxing.photos.entity.LocalMediaFolder
import com.teaphy.testzxing.photos.loader.ILocalMediaLoadListener
import com.teaphy.testzxing.photos.loader.LocalMediaLoader
import timber.log.Timber

class PhotosTestActivity : AppCompatActivity() {



	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photos_test)

		queryImages()
	}

	private fun queryImages() {
		LocalMediaLoader(this, true)
				.loadAllMedia(object: ILocalMediaLoadListener{
					override fun loadComplete(localImages: List<LocalMediaFolder>) {
					}
				})
	}
}
