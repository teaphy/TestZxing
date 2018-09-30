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
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.constant.TypeConstant
import com.teaphy.testzxing.photos.entity.LocalMedia
import com.teaphy.testzxing.photos.listener.ISelectChangeListener

/**
 * @desc 图片列表Adapter
 * @author tiany
 * @time 2018/9/29 下午2:56
 */
class PhotosAdapter(private val list: List<LocalMedia>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	val listSelected = mutableListOf<LocalMedia>()
	var selectChangeListener: ISelectChangeListener? = null
	private lateinit var context: Context

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		context = parent.context
		val view: View
		val inflater = LayoutInflater.from(context)
		if (viewType == TypeConstant.TYPE_CAMERA) {
			view = inflater.inflate(R.layout.item_camera_layout, parent, false)
			return CameraViewHolder(view)
		}

		view = inflater.inflate(R.layout.item_photo_layout, parent, false)

		return PhotoViewHolder(view)
	}

	override fun getItemCount(): Int {
		return list.size
	}

	override fun getItemViewType(position: Int): Int {

		if (TextUtils.equals(TypeConstant.TYPE_IMAGE_CAMERA, list[position].pictureType)) {
			return TypeConstant.TYPE_CAMERA
		}

		return TypeConstant.TYPE_IMAGE
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
			Toast.makeText(holder.itemView.context, "拍照", Toast.LENGTH_SHORT).show()
		}
	}

	/**
	 * 显示图片
	 */
	private fun handleImageUI(holder: PhotoViewHolder, position: Int) {

		val localMedia = list[position]


		with(localMedia) {
			PictureHelper().loadLocalImage(holder.image, path)
			val backgroundRes = if (isChecked) R.mipmap.ic_pigeon_selected else R.mipmap.ic_pigeon
			holder.checkText.setBackgroundResource(backgroundRes)

			if (listSelected.contains(localMedia)) {
				val index = listSelected.indexOf(localMedia)
				holder.checkText.text = (index + 1).toString()
			} else {
				holder.checkText.text = ""
			}
		}

		holder.itemView.setOnClickListener {
			Toast.makeText(holder.itemView.context, "查看图片：$position", Toast.LENGTH_SHORT).show()
		}

		holder.checkLayout.setOnClickListener {
			localMedia.isChecked = !localMedia.isChecked

			// 将localMedia添加或移除
			if (localMedia.isChecked) {
				if (!listSelected.contains(localMedia)) {
					listSelected.add(localMedia)
				}

			} else {
				if (listSelected.contains(localMedia)) {
					listSelected.remove(localMedia)
				}
				notifyItemChanged(position)
			}

			// 更新 已选择的数量
			for (media in listSelected) {
				notifyItemChanged(list.indexOf(media))
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