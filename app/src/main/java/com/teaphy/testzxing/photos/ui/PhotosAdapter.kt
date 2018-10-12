package com.teaphy.testzxing.photos.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.rrs.afcs.picture.PictureHelper
import com.rrs.afcs.view.IItemCallback
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.config.PictureConfig
import com.teaphy.testzxing.photos.config.PictureSelectConfig
import com.teaphy.testzxing.photos.constant.PictureTypeConstant
import com.teaphy.testzxing.photos.entity.LocalMedia
import com.teaphy.testzxing.photos.listener.ISelectChangeListener

/**
 * @desc 图片列表Adapter
 * @author tiany
 * @time 2018/9/29 下午2:56
 */
class PhotosAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	val listSelected = mutableListOf<LocalMedia>()
	var selectChangeListener: ISelectChangeListener? = null
	var itemClickListener: IItemCallback<LocalMedia>? = null
	var cameraClickListener: View.OnClickListener? = null
    val listMedia: MutableList<LocalMedia> = mutableListOf()
	private lateinit var context: Context

    // 获取到数据进行更新
    fun updateData(list: List<LocalMedia>) {
        listMedia.clear()
        listMedia.addAll(list)
        notifyDataSetChanged()
    }
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		context = parent.context
		val view: View
		val inflater = LayoutInflater.from(context)
		if (viewType == PictureTypeConstant.TYPE_CAMERA) {
			view = inflater.inflate(R.layout.item_camera_layout, parent, false)
			return CameraViewHolder(view)
		}

		view = inflater.inflate(R.layout.item_photo_layout, parent, false)

		return PhotoViewHolder(view)
	}

	override fun getItemCount(): Int {
		return listMedia.size
	}

	override fun getItemViewType(position: Int): Int {

		if (TextUtils.equals(PictureTypeConstant.TYPE_IMAGE_CAMERA, listMedia[position].pictureType)) {
			return PictureTypeConstant.TYPE_CAMERA
		}

		return PictureTypeConstant.TYPE_IMAGE
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is CameraViewHolder -> handleCameraUI(holder)
			is PhotoViewHolder -> handleImageUI(holder, position)
		}
	}

	/**
	 * 拍照
	 */
	private fun handleCameraUI(holder: CameraViewHolder) {
		holder.itemView.setOnClickListener {
			cameraClickListener?.onClick(it)
		}
	}

	/**
	 * 显示图片
	 */
	private fun handleImageUI(holder: PhotoViewHolder, position: Int) {

		val localMedia = listMedia[position]
        localMedia.isChecked = listSelected.contains(localMedia)

		with(localMedia) {
			PictureHelper().loadLocalImage(holder.image, path)
			val backgroundRes:Int

			if (localMedia.isChecked) {
				backgroundRes = R.mipmap.ic_pigeon_selected
				val index = listSelected.indexOf(localMedia)
				holder.checkText.text = (index + 1).toString()
			} else {
				holder.checkText.text = ""
				backgroundRes = R.mipmap.ic_pigeon
			}

			holder.checkText.setBackgroundResource(backgroundRes)
		}

		holder.itemView.setOnClickListener {
			itemClickListener?.onItemClick(localMedia)
		}

		holder.checkLayout.setOnClickListener {

			// 将localMedia添加或移除
			if (localMedia.isChecked) {
                localMedia.isChecked = false
                if (listSelected.contains(localMedia)) {
                    listSelected.remove(localMedia)
                    notifyItemChanged(position)
                }

			} else {
				if (listSelected.size >= PictureSelectConfig.getInstance().maxSelectNumber) {
					Toast.makeText(context,
							context.getString(R.string.select_max_prompt, PictureSelectConfig.getInstance().maxSelectNumber),
							Toast.LENGTH_SHORT).show()
					return@setOnClickListener
				}

                localMedia.isChecked = true
                if (!listSelected.contains(localMedia)) {
                    listSelected.add(localMedia)
                }
			}

			// 更新 已选择的数量
			for (media in listSelected) {
				notifyItemChanged(listMedia.indexOf(media))
			}

			selectChangeListener?.onSelectChange()
		}
	}

	class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val image: ImageView = view.findViewById(R.id.image)
		val checkText: TextView = view.findViewById(R.id.checkText)
		val checkLayout: FrameLayout = view.findViewById(R.id.checkLayout)
	}

	class CameraViewHolder(view: View) : RecyclerView.ViewHolder(view)
}