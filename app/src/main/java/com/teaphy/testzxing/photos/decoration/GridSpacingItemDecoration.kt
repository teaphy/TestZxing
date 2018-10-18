package com.rrs.afcs.photos.decoration

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View


/**
 * @desc 设置Item间距
 * @author tiany
 * @time 2018/9/29 下午4:58
 */
class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int,
                                private val includeEdge: Boolean, private val spaceTop: Int = 0) : RecyclerView.ItemDecoration() {

	override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
		val position = parent.getChildAdapterPosition(view)
		// 行数
		val row = position / spanCount
		// 列数
		val column = position % spanCount
		if (includeEdge) {
			if (row == 0) {
				outRect.left = spacing - column * spacing / spanCount
				outRect.right = (column + 1) * spacing / spanCount
				outRect.top = spaceTop + spacing
				outRect.bottom = spacing
			} else {
				outRect.left = spacing - column * spacing / spanCount
				outRect.right = (column + 1) * spacing / spanCount
				if (position < spanCount) {
					outRect.top = spacing
				}
				outRect.bottom = spacing
			}

		} else {
			if (row == 0) {
				outRect.left = spacing - column * spacing / spanCount
				outRect.right = (column + 1) * spacing / spanCount
				outRect.top = spaceTop
				outRect.bottom = spacing
			} else {
				outRect.left = column * spacing / spanCount
				outRect.right = spacing - (column + 1) * spacing / spanCount
				if (position < spanCount) {
					outRect.top = spacing
				}
				outRect.bottom = spacing
			}
		}
	}
}