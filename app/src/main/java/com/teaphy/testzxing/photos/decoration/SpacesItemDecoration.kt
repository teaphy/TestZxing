package com.teaphy.testzxing.photos.decoration

import android.graphics.Rect
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * @author tiany
 * @desc
 * @time 2018/10/10 下午1:27
 */
class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

		val layoutManager = parent.layoutManager as LinearLayoutManager

		if (LinearLayoutManager.VERTICAL == layoutManager.orientation) {

			// 最后一个Item
			if (parent.getChildLayoutPosition(view) == layoutManager.itemCount - 1) {
				outRect.set(0, space, 0, space)
			} else {
				outRect.set(0, space, 0, 0)
			}
		} else {
			// 最后一个Item
			if (parent.getChildLayoutPosition(view) == layoutManager.itemCount - 1) {
				outRect.set(space, 0, space, 0)
			} else {
				outRect.set(space, 0, 0, 0)
			}
		}
	}
}
