package com.teaphy.testzxing.photos.ui

import com.teaphy.testzxing.R


class PreviewAllActivity : BasePreviewMediaActivity() {

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

        if (listImageSelected.contains(localMedia)) {
            val indexSelect = listImageSelected.indexOf(localMedia)
            listImageSelected[indexSelect].isChecked = localMedia.isChecked
            imageTab.getTabAt(indexSelect)!!.select()

            updateTabUI(imageTab.getTabAt(indexSelect)!!, localMedia, true)
        }

        val percent = getString(R.string.percent_preview_media, position + 1, listImage.size)
        percentText.text = percent!!

        updateNumUI(localMedia)

    }
}
