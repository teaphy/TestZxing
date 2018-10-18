package com.rrs.afcs.photos.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Button
import android.widget.TextView
import com.teaphy.testzxing.R
import com.rrs.afcs.photos.entity.LocalMedia
import android.support.v7.widget.SimpleItemAnimator
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.rrs.afcs.permissions.IGrantedFailure
import com.rrs.afcs.permissions.IGrantedSuccess
import com.rrs.afcs.permissions.RxPermissionUtil
import com.rrs.afcs.view.IItemCallback
import com.tbruyelle.rxpermissions2.RxPermissions
import com.rrs.afcs.photos.config.PictureConfig
import com.rrs.afcs.photos.config.PictureSelectConfig
import com.rrs.afcs.photos.constant.PictureTypeConstant
import com.rrs.afcs.photos.decoration.GridSpacingItemDecoration
import com.rrs.afcs.photos.listener.ISelectChangeListener
import com.rrs.afcs.photos.observe.CancelSubject
import java.io.File
import java.io.IOException
import java.util.ArrayList
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.support.constraint.ConstraintLayout
import com.rrs.afcs.photos.loader.ITakePhotoListener
import com.rrs.afcs.photos.loader.LocalMediaLoader
import com.umeng.socialize.utils.DeviceConfig.context
import android.support.v4.content.FileProvider
import com.blankj.utilcode.util.SizeUtils
import com.rrs.afcs.photos.config.PictureMimeType


/**
 * 图片选择界面
 */
abstract class BasePhotoSelectorActivity : BasePhotosActivity() {

	protected lateinit var backText: TextView
	protected lateinit var previewText: TextView
	protected lateinit var selectButton: Button
	private lateinit var cancelText: TextView
	private lateinit var recyclerView: RecyclerView
	private lateinit var bottomLayout: ConstraintLayout

	val listLocalMedia = mutableListOf<LocalMedia>()

	val photosAdapter = PhotosAdapter()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_photo_selector)

		initData()

		initView()

		initPreAndSelView()

		setListener()

		RxPermissionUtil.getInstance(this)
				.requestCamera(object : IGrantedSuccess{
					override fun onGrantedSuccess() {
						loadLocalImage()
					}

				},object : IGrantedFailure{
					override fun onGrantedFailure() {
						Toast.makeText(this@BasePhotoSelectorActivity,
								R.string.camera_permission,
								Toast.LENGTH_SHORT).show()
					}

				})

	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		when (requestCode) {
			PictureConfig.CODE_PREVIEW_MEDIA -> {
				if (null != data) {
					updateMediaSelectedUI(data)
				}
			}
			PictureConfig.CODE_TAKE_PHOTO -> handlePhoto()
		}
	}

	private fun initData() {

	}

	private fun initView() {
		backText = findViewById(R.id.backText)
		cancelText = findViewById(R.id.cancelText)
		recyclerView = findViewById(R.id.recyclerView)

		with(recyclerView) {
			val manager = GridLayoutManager(this@BasePhotoSelectorActivity, 3)
			addItemDecoration(GridSpacingItemDecoration(3,
					SizeUtils.dp2px(6f),
					true,
					SizeUtils.dp2px(64f)))
			layoutManager = manager
			// 解决调用 notifyItemChanged 闪烁问题,取消默认动画
			(itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
			setHasFixedSize(true)
			adapter = photosAdapter
		}
	}

	/**
	 * 初始化 预览和选择按钮
	 */
	private fun initPreAndSelView() {
		// 单选模式
		if (PictureSelectConfig.getInstance().selectModel == PictureSelectConfig.SelectModel.SINGLE) {
			bottomLayout = findViewById(R.id.bottomLayout)

			bottomLayout.visibility = View.GONE
		} else { // 多选模式
			bottomLayout = findViewById(R.id.bottomLayout)

			bottomLayout.visibility = View.VISIBLE
			previewText = findViewById(R.id.previewText)
			selectButton = findViewById(R.id.selectButton)


			previewText.setOnClickListener {
				openPreviewMediaActivity()
			}

			selectButton.setOnClickListener {
				forSelectResult()
			}
		}
	}


	open fun setListener() {

		/**
		 * 当选择图片列表发生变化时的回调
		 */
		photosAdapter.selectChangeListener = object : ISelectChangeListener {
			override fun onSelectChange(localMedia: LocalMedia) {

				// 单选模式
				if (PictureSelectConfig.getInstance().selectModel == PictureSelectConfig.SelectModel.SINGLE) {
					forSelectResult()
				} else { // 多选模式
					updatePreAndSelUI()
				}


			}
		}

		photosAdapter.itemClickListener = object : IItemCallback<LocalMedia> {
			override fun onItemClick(item: LocalMedia) {
				openPreviewAllActivity(item)
			}
		}

		cancelText.setOnClickListener {
			CancelSubject.obtain().notifyObserver()
		}


		photosAdapter.cameraClickListener = View.OnClickListener { openCamera() }
	}

	/**
	 * 更新本地图片列表
	 */
	fun refreshMedia(list: List<LocalMedia>, isCamera: Boolean) {

		if (list.isNotEmpty()) {
			listLocalMedia.clear()
			listLocalMedia.addAll(list)
		}

		// 是否显示相机
		if (isCamera) {
			listLocalMedia.add(0, LocalMedia("", "", PictureTypeConstant.TYPE_IMAGE_CAMERA))
		}
		photosAdapter.updateData(listLocalMedia)
	}

	private fun openPreviewMediaActivity() {

		val indexPreview = 0

		val intent = Intent(this, PreviewMediaActivity::class.java)
		val bundle = Bundle()

		bundle.putParcelableArrayList(PictureConfig.KEY_CONTENT, photosAdapter.listSelected as ArrayList<LocalMedia>)
		bundle.putParcelableArrayList(PictureConfig.KEY_MEDIA_SELECTED, photosAdapter.listSelected)
		bundle.putInt(PictureConfig.KEY_POSITION, indexPreview)

		intent.putExtras(bundle)
		startActivityForResult(intent, PictureConfig.CODE_PREVIEW_MEDIA)
	}

	/**
	 * 图片选择结果
	 */
	private fun forSelectResult() {
		if (photosAdapter.listSelected.isNotEmpty()) {
			PictureSelectConfig.getInstance()
					.localMediaLoaderListener?.onSelected(photosAdapter.listSelected as List<LocalMedia>)
		}

		CancelSubject.obtain().notifyObserver()
	}

	/**
	 * 拍照
	 */
	private fun openCamera() {
		try {
			val file = getOutPath()

			// 获取当前系统版本
			val currentVersion = android.os.Build.VERSION.SDK_INT
			// 激活相机
			val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

			if (currentVersion >= android.os.Build.VERSION_CODES.M) {
				RxPermissionUtil.getInstance(this)
						.requestCamera(object : IGrantedSuccess {
							override fun onGrantedSuccess() {
								if (currentVersion >= android.os.Build.VERSION_CODES.N) {
									openCamera27(file, cameraIntent)
								} else {
									openCamera26(file, cameraIntent)
								}
							}
						}, object : IGrantedFailure {
							override fun onGrantedFailure() {
								Toast.makeText(this@BasePhotoSelectorActivity, R.string.camera_permission, Toast.LENGTH_SHORT).show()
							}
						})
			} else {
				openCamera26(file, cameraIntent)
			}
		} catch (e: IOException) {
			Toast.makeText(this@BasePhotoSelectorActivity, R.string.camera_permission, Toast.LENGTH_SHORT).show()
		}

	}

	/**
	 * 拍照适配 26 及以下
	 */
	private fun openCamera26(file: File, intent: Intent) {
		val imageUri = Uri.fromFile(file)
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
		startActivityForResult(intent, PictureConfig.CODE_TAKE_PHOTO)
	}

	/**
	 * 拍照适配 27及以上
	 */
	private fun openCamera27(file: File, intent: Intent) {

		val imageUri = FileProvider.getUriForFile(context, "com.teaphy.testzxing", file)//通过FileProvider创建一个content类型的Uri

		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
		startActivityForResult(intent, PictureConfig.CODE_TAKE_PHOTO)
	}

	@SuppressLint("CheckResult")
	private fun updateMediaSelectedUI(data: Intent) {

		val listSelect = data.extras.getParcelableArrayList<LocalMedia>(PictureConfig.KEY_CONTENT)

		photosAdapter.listSelected.clear()
		photosAdapter.listSelected.addAll(listSelect)


		for (item in listLocalMedia) {
			item.isChecked = listSelect.contains(item)
		}

		photosAdapter.updateData(listLocalMedia)

		updatePreAndSelUI()
	}

	private fun openPreviewAllActivity(item: LocalMedia) {

		val listNotNull = listLocalMedia.filter { it.path.isNotEmpty() }
		val indexAll = listNotNull.indexOf(item)

		val intent = Intent(this, PreviewAllActivity::class.java)
		val bundle = Bundle()

		bundle.putParcelableArrayList(PictureConfig.KEY_CONTENT, listNotNull as ArrayList<LocalMedia>)
		bundle.putParcelableArrayList(PictureConfig.KEY_MEDIA_SELECTED, photosAdapter.listSelected as ArrayList<LocalMedia>)
		bundle.putInt(PictureConfig.KEY_POSITION, indexAll)

		intent.putExtras(bundle)
		startActivityForResult(intent, PictureConfig.CODE_PREVIEW_MEDIA)
	}

	/**
	 * 获取拍照后图片的保存路径
	 */
	private fun getOutPath(): File {
		return if (TextUtils.isEmpty(PictureSelectConfig.getInstance().outputCameraPath)) {
			val outImage = File(Environment.getExternalStorageDirectory(), "${System.currentTimeMillis()}.png")
			try {
				if (outImage.exists()) {
					outImage.delete()
				}
				outImage.createNewFile()
			} catch (e: IOException) {
				throw IOException("create the image file failure")
			}
			PictureSelectConfig.getInstance().outputCameraPath = outImage.path
			outImage
		} else {
			File(PictureSelectConfig.getInstance().outputCameraPath)
		}
	}

	/**
	 * 判断SDCard是否挂载
	 */
	private fun hasSdCard(): Boolean {
		return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
	}

	/**
	 * 处理拍照后的图片处理
	 */
	private fun handlePhoto() {

		val path = PictureSelectConfig.getInstance().outputCameraPath
		addIntoGallery(path)

		val file = File(path)
		val options = BitmapFactory.Options()
		BitmapFactory.decodeFile(path, options)

		val localMedia = LocalMedia(
				path,
				file.name,
				PictureMimeType.createImageType(path),
				options.outWidth,
				options.outHeight,
				file.lastModified(),
				true)

		PictureSelectConfig.getInstance()
				.localMediaLoaderListener?.onSelected(listOf<LocalMedia>(localMedia))

		// 关闭所有的Activity
		CancelSubject.obtain()
				.notifyObserver()
	}

	/**
	 * 将拍照后的图片保存到图库
	 */
	private fun addIntoGallery(path: String) {
		val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
		val f = File(path)
		val contentUri = Uri.fromFile(f)
		mediaScanIntent.data = contentUri
		this.sendBroadcast(mediaScanIntent)
	}


	/**
	 * 更新预览和选择 按钮
	 */
	private fun updatePreAndSelUI() {
		val countSelect = photosAdapter.listSelected.size
		val isSelected = countSelect > 0
		val preEnable: Boolean = isSelected
		val preClickable: Boolean = isSelected
		val preAlpha: Float = if (isSelected) {
			1.0F
		} else {
			0.5F
		}

		val selectDesc = if (countSelect > 0) {
			getString(R.string.photos_select_num, countSelect)
		} else {
			getString(R.string.select)
		}

		with(previewText) {
			isEnabled = preEnable
			isClickable = preClickable
			alpha = preAlpha
		}

		with(selectButton) {
			isEnabled = preEnable
			isClickable = preClickable
			alpha = preAlpha
		}


		selectButton.text = selectDesc
	}

	abstract fun loadLocalImage()
}
