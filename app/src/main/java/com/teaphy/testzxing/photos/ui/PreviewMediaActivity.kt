package com.teaphy.testzxing.photos.ui

import com.teaphy.testzxing.R


class PreviewMediaActivity : BasePreviewMediaActivity() {


	override fun setListener() {
		super.setListener()

		numText.setOnClickListener {
			val position = mediaViewPager.currentItem

			val localMedia = listImage[position]

			localMedia.isChecked = !localMedia.isChecked

			updateSelectChangeUI(position)

			updateSelectButtonUI()
		}
	}

	override fun updateSelectChangeUI(position: Int) {
		val localMedia = listImage[position]

		val indexSelect = listImageSelected.indexOf(localMedia)
		listImageSelected[indexSelect].isChecked = localMedia.isChecked
		imageTab.getTabAt(indexSelect)!!.select()

		val percent = getString(R.string.percent_preview_media, position + 1, listImage.size)
		percentText.text = percent!!

		updateTabUI(imageTab.getTabAt(indexSelect)!!, localMedia, true)
		updateNumUI(localMedia)
	}
}
