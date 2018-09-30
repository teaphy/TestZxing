package com.teaphy.testzxing.photos.ui

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.entity.LocalMedia
import android.support.v7.widget.SimpleItemAnimator
import android.widget.Toast
import com.teaphy.testzxing.photos.constant.PhotosConstant
import com.teaphy.testzxing.photos.constant.TypeConstant
import com.teaphy.testzxing.photos.decoration.GridSpacingItemDecoration
import com.teaphy.testzxing.photos.listener.ISelectChangeListener
import com.teaphy.testzxing.photos.observe.CancelSubject
import java.util.ArrayList


/**
 * 图片选择界面
 */
abstract class BasePhotoSelectorActivity : BasePhotosActivity() {

	protected lateinit var backText: TextView
	protected lateinit var previewText: TextView
	protected lateinit var selectButton: Button
	private lateinit var cancelText: TextView
	private lateinit var recyclerView: RecyclerView

	val listLocalMedia = mutableListOf<LocalMedia>()

	val photosAdapter = PhotosAdapter(listLocalMedia)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photo_selector)

		initData()

		initView()

		setListener()

		loadLocalImage()
	}

	private fun initData() {

	}

	private fun initView() {
		backText = findViewById(R.id.backText)
		cancelText = findViewById(R.id.cancelText)
		previewText = findViewById(R.id.previewText)
		selectButton = findViewById(R.id.selectButton)
		recyclerView = findViewById(R.id.recyclerView)

		with(recyclerView) {
			val manager = GridLayoutManager(this@BasePhotoSelectorActivity, 3)
			addItemDecoration(GridSpacingItemDecoration(3, 16, true))
			layoutManager = manager
			// 解决调用 notifyItemChanged 闪烁问题,取消默认动画
			(itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
			setHasFixedSize(true)
			adapter = photosAdapter
		}
	}

	open fun setListener() {

		/**
		 * 当选择图片列表发生变化时的回调
		 */
		photosAdapter.selectChangeListener = object : ISelectChangeListener {
			override fun onSelectChange() {
				val countSelect = photosAdapter.listSelected.size

				val selectDesc = if (countSelect > 0) {
					getString(R.string.photos_select_num, countSelect)
				} else {
					getString(R.string.select)
				}

				selectButton.text = selectDesc
			}
		}

		cancelText.setOnClickListener {
			CancelSubject.obtain().notifyObserver()
		}

		previewText.setOnClickListener {
			openPreviewMediaActivity()
		}
	}


	/**
	 * 更新本地图片列表
	 */
	fun refreshMedia(list: List<LocalMedia>, isCamera: Boolean) {
		listLocalMedia.clear()
		listLocalMedia.addAll(list)
		// 是否显示相机
		if (isCamera) {
			listLocalMedia.add(0, LocalMedia("", "", "", TypeConstant.TYPE_IMAGE_CAMERA))
		}
		photosAdapter.notifyDataSetChanged()
	}

	private fun openPreviewMediaActivity() {
		val intent = Intent(this, PreviewMediaActivity::class.java)
		val bundle = Bundle()

		bundle.putParcelableArrayList(PhotosConstant.KEY_CONTENT, photosAdapter.listSelected as ArrayList<LocalMedia>)
		bundle.putParcelableArrayList(PhotosConstant.KEY_MEDIA_SELECTED, photosAdapter.listSelected)

		intent.putExtras(bundle)
		startActivity(intent)
	}

	abstract fun loadLocalImage()
}
