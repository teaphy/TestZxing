package com.teaphy.testzxing.photos.ui

import android.widget.Toast
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.config.PictureSelectConfig
import com.teaphy.testzxing.photos.entity.LocalMedia


class PreviewAllActivity : BasePreviewMediaActivity() {


	override fun setListener() {
		super.setListener()

		numText.setOnClickListener {
			val position = mediaViewPager.currentItem

			// 更新当前LocalMedia的状态
			updateSelectStatus(position)

			// 更新选择数量相关的UI
			updateNumberChangeUI()
		}
	}

	override fun updateSelectStatus(position: Int) {

		val localMedia = listImage[position]

		// 更新LocalMedia的选中状态
		localMedia.isChecked = !localMedia.isChecked
		listImage[position] = localMedia


		if (localMedia.isChecked) {
			// 如果处于选中状态，若选择列表没有该LocalMedia，将其添加至选择列表
			if (!listImageSelected.contains(localMedia)) {
				listImageSelected.add(localMedia)
			}

		} else {
			// 如果处于未选中状态，若选择列表有该LocalMedia，将其从选择列表移除
			if (listImageSelected.contains(localMedia)) {
				val positionSelected = listImageSelected.indexOf(localMedia)
				listImageSelected.removeAt(positionSelected)
			}
		}

		selectedPreviewAdapter.refreshList(listImageSelected)
	}
}
