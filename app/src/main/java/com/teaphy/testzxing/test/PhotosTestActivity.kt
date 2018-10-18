package com.teaphy.testzxing.test

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import com.teaphy.testzxing.R
import com.rrs.afcs.photos.PictureSelectors
import com.rrs.afcs.photos.config.PictureSelectConfig
import com.rrs.afcs.photos.entity.LocalMedia
import com.rrs.afcs.photos.loader.IMediaSelectListener

class PhotosTestActivity : AppCompatActivity() {

	private lateinit var previewButton: Button
	private lateinit var mediaText: TextView
	private lateinit var selectModelGroup: RadioGroup

	val mListMedias = mutableListOf<LocalMedia>()

	var selectModel = PictureSelectConfig.SelectModel.SINGLE

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photos_test)

		previewButton = findViewById(R.id.preview_button)
		mediaText = findViewById(R.id.media_text)
		selectModelGroup = findViewById(R.id.select_model_group)

		findViewById<Button>(R.id.select_button).setOnClickListener {
			queryImages()
		}

		previewButton.setOnClickListener {
			PictureSelectors.create(this@PhotosTestActivity)
					.openExternalPreview(0, mListMedias)
		}

		selectModelGroup.setOnCheckedChangeListener { _, checkedId ->
			selectModel = when (checkedId) {
				R.id.single_radio -> PictureSelectConfig.SelectModel.SINGLE
				R.id.multiple_radio -> PictureSelectConfig.SelectModel.MULTIPLE
				else -> PictureSelectConfig.SelectModel.SINGLE
			}
		}
	}

	private fun queryImages() {
		PictureSelectors.create(this)
				.openGallery()
				.selectModel(selectModel)
				.requestMedias(object : IMediaSelectListener {
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
