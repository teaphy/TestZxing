package com.teaphy.testzxing.photos.ui

import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.entity.LocalMedia


class PreviewMediaActivity : BasePreviewMediaActivity() {


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

	/**
	 * 更新当前LocalMedia的状态
	 * @param position 当前localMedia处于所有LocalMedial列表的位置
	 */
	override fun updateSelectStatus(position: Int) {
		val localMedia = listImage[position]

		// 更新LocalMedia的选中状态
		localMedia.isChecked = !localMedia.isChecked
		listImage[position] = localMedia

		val posSelected = listImageSelected.indexOf(localMedia)
		listImageSelected[posSelected] = localMedia

		selectedPreviewAdapter.updateStatus(localMedia)
	}
}
