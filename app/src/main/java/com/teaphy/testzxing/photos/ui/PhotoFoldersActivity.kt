package com.rrs.afcs.photos.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.rrs.afcs.view.IItemCallback
import com.teaphy.testzxing.R
import com.rrs.afcs.photos.config.PictureConfig
import com.rrs.afcs.photos.decoration.SpacesItemDecoration
import com.rrs.afcs.photos.entity.LocalMedia
import com.rrs.afcs.photos.entity.LocalMediaFolder
import com.rrs.afcs.photos.loader.ILocalMediaLoadListener
import com.rrs.afcs.photos.loader.LocalMediaLoader
import com.rrs.afcs.photos.observe.CancelSubject
import java.util.ArrayList

class PhotoFoldersActivity : BasePhotosActivity() {

	private lateinit var recyclerView: RecyclerView
	private lateinit var cancelText: TextView

	private val folderList = mutableListOf<LocalMediaFolder>()
	private val folderAdapter = MediaFolderAdapter(folderList)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photo_folders_layout)

		initView()

		setListener()

		loadLocalImage()
	}

	private fun initView() {
		recyclerView = findViewById(R.id.recyclerView)
		cancelText = findViewById(R.id.cancelText)

		with(recyclerView) {
			layoutManager = LinearLayoutManager(this@PhotoFoldersActivity, LinearLayoutManager.VERTICAL, false)
			setHasFixedSize(true)
			addItemDecoration(SpacesItemDecoration(SizeUtils.dp2px(0f), SizeUtils.dp2px(64f)))
			adapter = folderAdapter
		}
	}

	private fun setListener() {

		folderAdapter.itemClickListener = object : IItemCallback<LocalMediaFolder> {
			override fun onItemClick(item: LocalMediaFolder) {
				openPhotosSelectByFolderActivity(item.images)
			}

		}

		cancelText.setOnClickListener {
			CancelSubject.obtain().notifyObserver()
		}
	}

	private fun openPhotosSelectByFolderActivity(images: List<LocalMedia>) {
		val intent = Intent(this, PhotosSelectByFolderActivity::class.java)
		val bundle = Bundle()

		bundle.putParcelableArrayList(PictureConfig.KEY_CONTENT, images as ArrayList<LocalMedia>)

		intent.putExtras(bundle)
		startActivity(intent)
	}

	/**
	 * 从本地Media数据库读取图片列表
	 */
	private fun loadLocalImage() {
		LocalMediaLoader(this, false)
				.loadAllMedia(object : ILocalMediaLoadListener {
					override fun loadComplete(localImageFolders: List<LocalMediaFolder>) {
						folderList.clear()
						folderList.addAll(localImageFolders)
						folderAdapter.notifyDataSetChanged()
					}
				})
	}
}
