package com.teaphy.testzxing.test

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.PictureSelectors
import com.teaphy.testzxing.photos.entity.LocalMedia
import com.teaphy.testzxing.photos.entity.LocalMediaFolder
import com.teaphy.testzxing.photos.loader.ILocalMediaLoadListener
import com.teaphy.testzxing.photos.loader.IMediaSelectListener
import com.teaphy.testzxing.photos.loader.LocalMediaLoader
import timber.log.Timber

class PhotosTestActivity : AppCompatActivity() {

	lateinit var previewButton: Button
	lateinit var mediaText: TextView

	val mListMedias = mutableListOf<LocalMedia>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photos_test)

		previewButton = findViewById(R.id.preview_button)
		mediaText = findViewById(R.id.media_text)

		findViewById<Button>(R.id.select_button).setOnClickListener {
			queryImages()
		}

		previewButton.setOnClickListener {
			PictureSelectors.create(this@PhotosTestActivity)
					.openExternalPreview(0, mListMedias)
		}
	}

	private fun queryImages() {
		PictureSelectors.create(this)
				.openGallery()
				.requestMededias(object : IMediaSelectListener{
					override fun onSelected(listMedias: List<LocalMedia>) {
						if (mListMedias.isNotEmpty()) {
							mListMedias.clear()
						}

						mListMedias.addAll(listMedias)
						mediaText.text = mListMedias.toString()
					}

				})
	}
}
