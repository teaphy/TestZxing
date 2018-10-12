package com.teaphy.testzxing.photos.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.rrs.afcs.picture.PictureHelper
import com.rrs.afcs.view.IItemCallback
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.entity.LocalMedia

/**
 * @desc
 * @author tiany
 * @time 2018/10/10 上午10:16
 */
class SelectedPreviewAdapter() : RecyclerView.Adapter<SelectedPreviewAdapter.ViewHolder>() {

	private val listSelected = mutableListOf<LocalMedia>()
	var itemClickListener: IItemCallback<LocalMedia>? = null
	var localMediaCurrent: LocalMedia? = null
		set(value) {
			field = value
			notifyDataSetChanged()
		}

	fun submitList(list: List<LocalMedia>) {
		listSelected.addAll(list)
		notifyDataSetChanged()
	}

	fun refreshList(list: List<LocalMedia>) {
		listSelected.clear()
		listSelected.addAll(list)
		notifyDataSetChanged()
	}

	fun updateStatus(localMedia: LocalMedia) {
		val index = listSelected.indexOf(localMedia)
		listSelected[index].isChecked = localMedia.isChecked
		notifyItemChanged(index)
	}

	override fun onCreateViewHolder(parent: ViewGroup, type: Int): ViewHolder {
		val view = LayoutInflater.from(parent.context)
				.inflate(R.layout.tab_item_preview_media, parent, false)

		return ViewHolder(view)
	}

	override fun getItemCount(): Int {
		return listSelected.size
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val localMedia = listSelected[position]

		with(holder) {
			PictureHelper().loadLocalImage(image, localMedia.path)

			if (localMedia.isChecked) {
				image.alpha = 1.0f
			} else {
				image.alpha = 0.5f
			}
			squareView.visibility = if (isDisplay(localMedia)) View.VISIBLE else View.GONE

			holder.itemView.setOnClickListener {
				itemClickListener?.onItemClick(localMedia)
			}
		}
	}

	/**
	 * 判断当前LocalMedia是否处于前台
	 */
	private fun isDisplay(localMedia: LocalMedia): Boolean {

		if (localMediaCurrent == null) {
			return false
		}

		return localMediaCurrent!!.path == localMedia.path
	}


	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val image = view.findViewById<ImageView>(R.id.image)
		val squareView = view.findViewById<View>(R.id.squareView)
	}
}