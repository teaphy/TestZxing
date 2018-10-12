package com.teaphy.testzxing.photos.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.rrs.afcs.picture.PictureHelper
import com.rrs.afcs.view.IItemCallback
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.config.PictureConfig
import com.teaphy.testzxing.photos.config.PictureSelectConfig
import com.teaphy.testzxing.photos.decoration.SpacesItemDecoration
import com.teaphy.testzxing.photos.entity.LocalMedia
import com.teaphy.testzxing.photos.observe.CancelSubject
import com.teaphy.testzxing.photos.widget.CustomViewPager

abstract class BasePreviewMediaActivity : BasePhotosActivity() {

	protected lateinit var mediaViewPager: CustomViewPager
	protected lateinit var backLayout: View
	protected lateinit var numText: TextView
	protected lateinit var percentText: TextView
	private lateinit var selectButton: Button
	protected lateinit var mediaRecyclerView: RecyclerView
	private lateinit var bottomLayout: ConstraintLayout
	private lateinit var titleLayout: ConstraintLayout

	protected val listImage = mutableListOf<LocalMedia>()
	protected val listImageSelected = mutableListOf<LocalMedia>()

	private val mediaAdapter = SingleMediaAdapter(listImage)
	protected val selectedPreviewAdapter = SelectedPreviewAdapter()

	// 默认显示的图片
	private var indexPreview = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_preview_midia_layout)

		initView()

		setListener()

		loadMediaData()

		setCurrentItem(indexPreview)

		updateNumberChangeUI()
	}

	private fun initView() {
		mediaViewPager = findViewById(R.id.mediaViewPager)
		backLayout = findViewById(R.id.backLayout)
		numText = findViewById(R.id.numText)
		percentText = findViewById(R.id.percentText)
		selectButton = findViewById(R.id.selectButton)
		mediaRecyclerView = findViewById(R.id.mediaRecyclerView)
		bottomLayout = findViewById(R.id.bottomLayout)
		titleLayout = findViewById(R.id.titleLayout)

		mediaViewPager.adapter = mediaAdapter

		with(mediaRecyclerView) {
			layoutManager = LinearLayoutManager(this@BasePreviewMediaActivity, LinearLayoutManager.HORIZONTAL, false)
			addItemDecoration(SpacesItemDecoration(16))
			adapter = selectedPreviewAdapter
		}
	}

	open fun setListener() {

		backLayout.setOnClickListener {
			finish()
		}

		selectButton.setOnClickListener {
			forSelectResult()
		}

		mediaAdapter.itemClickListener = object : IItemCallback<LocalMedia>{
			override fun onItemClick(item: LocalMedia) {
				hideOrShow(bottomLayout)
				hideOrShow(titleLayout)
			}
		}

		mediaViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
			override fun onPageSelected(position: Int) {

				indexPreview = position

				updateSelectChangeUI(position)
				updateNumberChangeUI()
			}
		})

		selectedPreviewAdapter.itemClickListener = object : IItemCallback<LocalMedia> {
			override fun onItemClick(item: LocalMedia) {
				val index = listImage.indexOf(item)
				setCurrentItem(index)
			}

		}
	}

	private fun loadMediaData() {
		val bundle = intent.extras

		if (null != bundle) {
			val images = bundle.getParcelableArrayList<LocalMedia>(PictureConfig.KEY_CONTENT)
			val imagesSelected = bundle.getParcelableArrayList<LocalMedia>(PictureConfig.KEY_MEDIA_SELECTED)
			indexPreview = bundle.getInt(PictureConfig.KEY_POSITION, 0)

			imagesSelected?.let {
				if (it.isNotEmpty()) {
					listImageSelected.addAll(it)
					selectedPreviewAdapter.refreshList(listImageSelected)
				}
			}

			images?.let {
				if (it.isNotEmpty()) {
					listImage.addAll(it)
				}
			}

			mediaAdapter.notifyDataSetChanged()
			selectedPreviewAdapter.localMediaCurrent = listImage[0]
		} else {
			indexPreview = -1
		}

	}

	override fun finish() {

		setMediaResult()

		super.finish()
	}

	/**
	 * 创建 选择的图片Result
	 */
	private fun setMediaResult() {

		val listSelect = listImageSelected.filter {
			it.isChecked
		} as ArrayList<LocalMedia>
		val intent = Intent()
		val bundle = Bundle()

		bundle.putParcelableArrayList(PictureConfig.KEY_CONTENT, listSelect)

		intent.putExtras(bundle)
		setResult(Activity.RESULT_OK, intent)
	}

	protected fun setCurrentItem(position: Int) {
		mediaViewPager.setCurrentItem(position, false)
	}

	fun updateTabUI(tab: TabLayout.Tab, localMedia: LocalMedia): TabLayout.Tab {

		tab.let {
			var view = it.customView
			if (view == null) {
				view = LayoutInflater.from(this)
						.inflate(R.layout.tab_item_preview_media, null, false)
				it.customView = view
			}

			updateTabUIByStatus(tab, localMedia)

		}
		return tab
	}

	/**
	 * 根据LocalMedia的状态更新tabUI
	 */
	fun updateTabUIByStatus(tab: TabLayout.Tab, localMedia: LocalMedia) {
		val customView = tab.customView
		if (null != customView) {
			val image = customView.findViewById<ImageView>(R.id.image)
			val squareView = customView.findViewById<View>(R.id.squareView)

			PictureHelper().loadLocalImage(image, localMedia.path)

			if (localMedia.isChecked) {
				image.alpha = 1.0f
			} else {
				image.alpha = 0.5f
			}

			squareView.visibility = if (isDisplay(localMedia)) View.VISIBLE else View.GONE
		}

	}

	/**
	 * 判断当前LocalMedia是否处于前台
	 */
	private fun isDisplay(localMedia: LocalMedia): Boolean {
		val lmCur = listImage[mediaViewPager.currentItem]
		return lmCur.path == localMedia.path
	}

	/**
	 * 当选择位于position的LocalMedia时更新TabLayout的UI
	 */
	fun updateSelectChangeUI(position: Int) {

		val localMedia = listImage[position]
		selectedPreviewAdapter.localMediaCurrent = localMedia
	}

	/**
	 * 更新选择数量相关的UI
	 */
	fun updateNumberChangeUI() {
		// 更新 当前选中状态 UI
		updateSelectedStatusUI()

		// 更新选择按钮UI
		updateSelectButtonUI()

		// 更新百分比显示
		updatePercentText()
	}

	/**
	 * 更新 当前选中状态 UI
	 */
	private fun updateSelectedStatusUI() {


		val localMedia = listImage[mediaViewPager.currentItem]

		if (localMedia.isChecked) {
			numText.setBackgroundResource(R.mipmap.ic_pigeon_selected)
			val pos = listImageSelected.filter { it.isChecked }.indexOf(localMedia)
			numText.text = (pos + 1).toString()
		} else {
			numText.setBackgroundResource(R.mipmap.ic_pigeon)
			numText.text = null
		}
	}

	/**
	 *  更新选择按钮UI
	 */
	private fun updateSelectButtonUI() {

		val countSelect = listImageSelected.filter { it.isChecked }.size

		val selectDesc = if (countSelect > 0) {
			getString(R.string.photos_select_num, countSelect)
		} else {
			getString(R.string.select)
		}

		selectButton.text = selectDesc
	}


	/**
	 * 更新当前图片位置百分比显示
	 * @author tiany
	 * @date 2018/10/8 上午10:34
	 */
	private fun updatePercentText() {
		val percent = getString(R.string.percent_preview_media, mediaViewPager.currentItem + 1, listImage.size)
		percentText.text = percent!!
	}

	private fun hideOrShow(view: View) {
		if (view.visibility == View.VISIBLE) {
			view.visibility = View.GONE
		} else {
			view.visibility = View.VISIBLE
		}
	}

	/**
	 * 图片选择结果
	 */
	private fun forSelectResult() {
		val listSelected = listImageSelected.filter { it.isChecked }
		if (listSelected.isNotEmpty()) {
			PictureSelectConfig.getInstance()
					.localMediaLoaderListener?.onSelected(listSelected)
		}

		CancelSubject.obtain().notifyObserver()
	}

	abstract fun updateSelectStatus(position: Int)
}
