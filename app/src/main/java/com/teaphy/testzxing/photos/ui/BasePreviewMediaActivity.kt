package com.teaphy.testzxing.photos.ui

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.rrs.afcs.picture.PictureHelper
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.constant.PhotosConstant
import com.teaphy.testzxing.photos.entity.LocalMedia

abstract class BasePreviewMediaActivity : BasePhotosActivity() {

	private lateinit var mediaViewPager: ViewPager
	private lateinit var backImage: ImageView
	protected lateinit var numText: TextView
	private lateinit var selectButton: Button
	private lateinit var imageTab: TabLayout

	protected val listImage = mutableListOf<LocalMedia>()
	protected val listImageSelected = mutableListOf<LocalMedia>()

	protected val mediaAdapter = SingleMediaAdapter(listImage)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_preview_midia_layout)

		initView()

		setListener()

		loadMediaData()

		defineTabItem()

		setCurrentItem(0)
	}

	private fun initView() {
		mediaViewPager = findViewById(R.id.mediaViewPager)
		backImage = findViewById(R.id.backImage)
		numText = findViewById(R.id.numText)
		selectButton = findViewById(R.id.selectButton)
		imageTab = findViewById(R.id.imageTab)

		mediaViewPager.adapter = mediaAdapter

		imageTab.setupWithViewPager(mediaViewPager)

		imageTab.tabMode = TabLayout.MODE_SCROLLABLE

	}

	open fun setListener() {

		backImage.setOnClickListener {
			finish()
		}

		imageTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
			override fun onTabReselected(tab: TabLayout.Tab?) {
			}

			override fun onTabUnselected(tab: TabLayout.Tab?) {
				tab?.let {
					val position= it.position
					updateTabUI(it, position, listImage[position], false)
				}
			}

			override fun onTabSelected(tab: TabLayout.Tab?) {
				tab?.let {
					val position= it.position
					updateTabUI(it, position, listImage[position], true)
				}
			}

		})

		mediaViewPager.addOnPageChangeListener( object : ViewPager.SimpleOnPageChangeListener (){
			override fun onPageSelected(position: Int) {
				super.onPageSelected(position)
				val localMedia = listImage[position]
				updateNumUI(localMedia)
			}
		})
	}

	private fun loadMediaData() {
		val bundle = intent.extras

		val images = bundle.getParcelableArrayList<LocalMedia>(PhotosConstant.KEY_CONTENT)
		val imagesSelected = bundle.getParcelableArrayList<LocalMedia>(PhotosConstant.KEY_MEDIA_SELECTED)

		imagesSelected?.let {
			if (it.isNotEmpty()) {
				listImageSelected.addAll(it)
			}
		}

		images?.let {
			if (it.isNotEmpty()) {
				listImage.addAll(it)
			}
		}

		mediaAdapter.notifyDataSetChanged()
	}


	private fun defineTabItem() {

		listImageSelected.forEachIndexed{
			index, localMedia ->
			val tab = imageTab.getTabAt(index)

			updateTabUI(tab, index, localMedia, false)

		}
	}

//	private fun updateTabUI(tab: TabLayout.Tab?, index: Int, localMedia: LocalMedia, isSelect: Boolean) {
//
//		numText.text = (index + 1).toString()
//
//		tab?.let {
//			var view = it.customView
//			if (view == null) {
//				view = LayoutInflater.from(this)
//						.inflate(R.layout.tab_item_preview_media, null, false)
//				it.customView = view
//			}
//			val image = view!!.findViewById<ImageView>(R.id.image)
//			val squareView = view.findViewById<View>(R.id.squareView)
//			if (null != image) {
//				PictureHelper().loadLocalImage(image, localMedia.path)
//			}
//
//			if (null != squareView) {
//				squareView.visibility = if (isSelect) View.VISIBLE else View.GONE
//			}
//
//			setCurrentItem(index)
//		}
//	}

	protected fun setCurrentItem(position: Int) {
		mediaViewPager.setCurrentItem(position, false)
	}

	fun updateNumUI(localMedia: LocalMedia) {

		if (localMedia.isChecked) {
			numText.setBackgroundResource(R.mipmap.ic_pigeon_selected)
			val pos = listImageSelected.filter {
				it.isChecked
			}.indexOf(localMedia)
			numText.text = (pos + 1).toString()
		} else {
			numText.setBackgroundResource(R.mipmap.ic_pigeon)
			numText.text = null
		}
	}

	abstract fun updateTabUI(tab: TabLayout.Tab?, index: Int, localMedia: LocalMedia, isSelect: Boolean)
}
