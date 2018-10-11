package com.teaphy.testzxing.photos.widget

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet

/**
 * @desc 多页滑动时，取消闪烁
 * @author tiany
 * @time 2018/10/9 下午3:28
 */
class CustomViewPager: ViewPager {

	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

	override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
		super.setCurrentItem(item, smoothScroll)
	}

	override fun setCurrentItem(item: Int) {
		super.setCurrentItem(item, false)
	}
}