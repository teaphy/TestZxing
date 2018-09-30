package com.teaphy.testzxing.photos.ui

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.design.widget.TabLayout.MODE_SCROLLABLE
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.rrs.afcs.picture.PictureHelper
import com.teaphy.testzxing.R
import com.teaphy.testzxing.photos.constant.PhotosConstant
import com.teaphy.testzxing.photos.entity.LocalMedia
import kotlinx.android.synthetic.main.activity_photo_selector.*
import kotlinx.android.synthetic.main.activity_preview_midia_layout.*

class PreviewMediaActivity : BasePreviewMediaActivity() {


	override fun setListener() {
		super.setListener()

		numText.setOnClickListener {
			val position = mediaViewPager.currentItem

			val localMedia = listImageSelected[position]

			localMedia.isChecked = !localMedia.isChecked

			val positionSelected = listImageSelected.indexOf(localMedia)
			listImageSelected[positionSelected] = localMedia

			updateNumUI(localMedia)

			updateTabUI(imageTab.getTabAt(position), position, localMedia, true)
		}
	}

	override fun updateTabUI(tab: TabLayout.Tab?, index: Int, localMedia: LocalMedia, isSelect: Boolean) {

		tab?.let {
			var view = it.customView
			if (view == null) {
				view = LayoutInflater.from(this)
						.inflate(R.layout.tab_item_preview_media, null, false)
				it.customView = view
			}
			val image = view!!.findViewById<ImageView>(R.id.image)
			val squareView = view.findViewById<View>(R.id.squareView)
			if (null != image) {
				PictureHelper().loadLocalImage(image, localMedia.path)

				if (localMedia.isChecked) {
					image.alpha = 1.0f
				} else {
					image.alpha = 0.2f
				}
			}

			if (null != squareView) {
				squareView.visibility = if (isSelect) View.VISIBLE else View.GONE
			}

			setCurrentItem(index)
		}
	}


}
