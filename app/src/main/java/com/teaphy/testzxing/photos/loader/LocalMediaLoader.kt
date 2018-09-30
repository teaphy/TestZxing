package com.teaphy.testzxing.photos.loader

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import com.teaphy.testzxing.photos.entity.LocalMedia
import timber.log.Timber
import android.text.TextUtils
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.constant.PhotosConstant
import com.teaphy.testzxing.photos.entity.LocalMediaFolder
import java.io.File
import java.util.*


/**
 * @desc 图片查看Loader
 * @author tiany
 * @time 2018/9/29 上午9:33
 */
class LocalMediaLoader(private val activity: FragmentActivity, private val isGif: Boolean) {

	companion object {

		val QUERY_URI: Uri = MediaStore.Files.getContentUri("external")
		// 根据ID降序排列
		val ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC"

		val DURATION = "duration"
	}

	private val NOT_GIF = "!='image/gif'"

	// 媒体文件数据库字段
	private val PROJECTION = arrayOf(MediaStore.Files.FileColumns._ID,
			MediaStore.MediaColumns.DATA,
			MediaStore.MediaColumns.DISPLAY_NAME,
			MediaStore.MediaColumns.MIME_TYPE,
			MediaStore.MediaColumns.WIDTH,
			MediaStore.MediaColumns.HEIGHT,
			MediaStore.MediaColumns.DATE_MODIFIED)

	// 图片
	private val SELECTION = (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
			+ " AND " + MediaStore.MediaColumns.SIZE + ">0")

	private val SELECTION_NOT_GIF = (MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
			+ " AND " + MediaStore.MediaColumns.SIZE + ">0"
			+ " AND " + MediaStore.MediaColumns.MIME_TYPE + NOT_GIF)


	public fun loadAllMedia(localMediaLoadListener: ILocalMediaLoadListener) {
		activity.supportLoaderManager.initLoader(0,
				null,
				object : LoaderManager.LoaderCallbacks<Cursor> {
					/**
					 * 创建一个可查询ContentProvider的loader
					 */
					override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
						var cursorLoader: CursorLoader? = null

						// 只获取图片
						val mediaImageType = getSelectionArgsForSingleMediaType(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
						cursorLoader = CursorLoader(
								activity,
								QUERY_URI,
								PROJECTION,
								if (isGif) SELECTION else SELECTION_NOT_GIF,
								mediaImageType,
								ORDER_BY)

						return cursorLoader
					}

					/*
					 * loader完成查询时调用，通常用于在查询到的cursor中提取数据
					 */
					override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {

						try {
							// 当前所有图片列表
							val localImages = mutableListOf<LocalMedia>()
							// 本地所有图片的文件夹
							val localImageFolders = mutableListOf<LocalMediaFolder>()
							// `最近添加`文件夹
							val localLatelyFolder = LocalMediaFolder(activity.getString(R.string.photos_folder_latest),
									"", "")
							// `所有图片`文件夹
							val localAllFolder = LocalMediaFolder(activity.getString(R.string.photos_folder_all),
									"", "")


							data?.let {
								val count = data.count

								if (count <= 0) {
									return@let
								}

								data.moveToFirst()

								do {
									val id = data.getString(data.getColumnIndexOrThrow(PROJECTION[0]))
									val path = data.getString(data.getColumnIndexOrThrow(PROJECTION[1]))
									val name = data.getString(data.getColumnIndexOrThrow(PROJECTION[2]))
									val pictureType = data.getString(data.getColumnIndexOrThrow(PROJECTION[3]))
									val width = data.getInt(data.getColumnIndexOrThrow(PROJECTION[4]))
									val height = data.getInt(data.getColumnIndexOrThrow(PROJECTION[5]))
									val lastModify = data.getLong(data.getColumnIndexOrThrow(PROJECTION[6]))

									val image = LocalMedia(id, path, name, pictureType, width, height, lastModify)

									// 获取文件夹名称
									val folder = getImageFolder(path, localImageFolders)
									// 将该图片添加到 文件夹中的图片列表
									folder.images.add(image)

									// 判断当前图片是否为7天内的图片，若是，添加至`最近添加`列表
									val isLatest = checkLately(lastModify)
									if (isLatest) {
										if (localLatelyFolder.firstImagePath.isEmpty()) {
											localLatelyFolder.firstImagePath = path
										}
										localLatelyFolder.images.add(image)
									}

									// 将图片添加到所有图片列表
									localImages.add(image)
								} while (data.moveToNext())
							}

							/**
							 * 将本地图片列表 添加 `所有图片文件夹`
							 */
							if (localImages.isNotEmpty()) {
								localAllFolder.firstImagePath = localImages.first().path
								localAllFolder.images.addAll(localImages)
							}

							// 设置 localAllFolder在localImageFolders中的index为0
							// 设置 localLatelyFolder在localImageFolders中的index为1
							localImageFolders.add(0, localLatelyFolder)

							localImageFolders.add(0, localAllFolder)

							localMediaLoadListener.loadComplete(localImageFolders)
						} catch (e: Exception) {
							Timber.e("$e")
						}
					}

					/**
					 * loader被重置而什其数据无效时被调用
					 */
					override fun onLoaderReset(loader: Loader<Cursor>) {
					}

				})
	}

	/**
	 * 获取当前图片的
	 */
	private fun getImageFolder(path: String?, localImageFolders: MutableList<LocalMediaFolder>): LocalMediaFolder {

		if (path == null) {
			throw Throwable("The path of the image is null")
		}

		path.let {
			val imageFile = File(it)
			val folderFile = imageFile.parentFile

			for (folder in localImageFolders) {
				if (TextUtils.equals(folder.name, folderFile.name)) {
					return folder
				}
			}

			val localMediaFolder = LocalMediaFolder(folderFile.name,
					folderFile.path,
					path)
			localImageFolders.add(localMediaFolder)
			return localMediaFolder
		}
	}

	/**
	 * 获取指定类型的文件
	 *
	 * @param mediaType
	 * @return
	 */
	private fun getSelectionArgsForSingleMediaType(mediaType: Int): Array<String> {
		return arrayOf(mediaType.toString())
	}

	/**
	 * 判断当前图片是否为7天内的图片，若是，添加至`最近添加`列表
	 */
	private fun checkLately(lastModify: Long): Boolean {
		val curCalendar = Calendar.getInstance() ?: return false

		// 设置日期为LATELY_DYS天前
		curCalendar.set(Calendar.DATE, curCalendar.get(Calendar.DATE) - PhotosConstant.LATELY_DYS)
		return Date(lastModify * 1000).after(curCalendar.time)
	}
}