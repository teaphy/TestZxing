package com.teaphy.testzxing.photos.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.widget.LinearLayout
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.config.PictureConfig
import com.teaphy.testzxing.photos.entity.LocalMedia

class ExternalPreviewActivity : AppCompatActivity() {

	private lateinit var backLayout: LinearLayout
	private lateinit var numberText: TextView
	private lateinit var previewPager : ViewPager

	private val listMedias = mutableListOf<LocalMedia>()
	private val mediaAdapter = SingleMediaAdapter(listMedias)

	private var indexPreview: Int = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_external_preview)

		initData()

		initView()

		setListener()
	}

	private fun initData() {
		val bundle = intent.extras

		bundle?.let {
			val list = bundle.getParcelableArrayList<LocalMedia>(PictureConfig.KEY_CONTENT)
			indexPreview = bundle.getInt(PictureConfig.KEY_POSITION, 0)
			if (null != list && list.isNotEmpty()) {
				listMedias.addAll(list)
			}
		}
	}

	private fun initView() {
		backLayout = findViewById(R.id.back_layout)
		numberText = findViewById(R.id.number_text)
		previewPager = findViewById(R.id.preview_view_pager)

		previewPager.adapter = mediaAdapter

		if (listMedias.isNotEmpty()) {
			mediaAdapter.notifyDataSetChanged()
			previewPager.setCurrentItem(indexPreview, false)
			updatePositionUI()
		}
	}

	private fun setListener() {
		backLayout.setOnClickListener {
			finish()
		}

		previewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
			override fun onPageSelected(position: Int) {
				super.onPageSelected(position)
				indexPreview = position
				updatePositionUI()
			}
		})

	}

	private fun updatePositionUI() {
		val percent = getString(R.string.percent_preview_media, previewPager.currentItem + 1, listMedias.size)
		numberText.text = percent
	}
}
