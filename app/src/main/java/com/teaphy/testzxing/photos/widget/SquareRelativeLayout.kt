package com.rrs.afcs.photos.widget

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.widget.RelativeLayout

/**
 * @author tiany
 * @desc 正方形布局
 * @time 2018/9/29 下午5:09
 */
class SquareRelativeLayout : RelativeLayout {

	constructor(context: Context) : super(context) {}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		// Set a square layout.
		super.onMeasure(widthMeasureSpec, widthMeasureSpec)
	}

}

