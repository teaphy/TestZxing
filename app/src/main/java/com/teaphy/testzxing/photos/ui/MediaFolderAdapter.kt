package com.teaphy.testzxing.photos.ui

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.rrs.afcs.picture.PictureHelper
import com.rrs.afcs.view.IItemCallback
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.entity.LocalMediaFolder

/**
 * @desc
 * @author tiany
 * @time 2018/9/30 上午10:14
 */
class MediaFolderAdapter(val list: List<LocalMediaFolder>) : RecyclerView.Adapter<MediaFolderAdapter.FolderViewHolder>() {

	private lateinit var context: Context
	var itemClickListener: IItemCallback<LocalMediaFolder>? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
		context = parent.context
		val view = LayoutInflater.from(context)
				.inflate(R.layout.item_media_folder_layout, parent, false)
		return MediaFolderAdapter.FolderViewHolder(view)
	}

	override fun getItemCount(): Int {
		return list.size
	}

	override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
		val folder = list[position]

		with(folder) {
			PictureHelper().loadLocalImage(holder.image, folder.firstImagePath)
			holder.folderText.text = context.getString(R.string.folder_with_media_num, folder.name, images.size)
		}

		holder.itemView.setOnClickListener {
			itemClickListener?.onItemClick(folder)
		}
	}

	class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val image: ImageView = view.findViewById(R.id.image)
		val folderText: TextView = view.findViewById(R.id.folderText)
	}
}