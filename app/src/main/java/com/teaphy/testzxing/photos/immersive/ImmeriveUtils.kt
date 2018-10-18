package com.rrs.afcs.photos.immersive

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.*
import android.widget.FrameLayout

/**
 * @author tiany
 * @desc
 * @time 2018/10/16 下午4:45
 */
class ImmeriveUtils {

	companion object {
		private val TAG_FAKE_STATUS_BAR_VIEW = "statusBarView"
		private val TAG_MARGIN_ADDED = "marginAdded"
		// 重置状态栏。即把状态栏颜色恢复为系统默认的黑色
		fun reset(activity: Activity) {
			immersive(activity, Color.BLACK, Color.BLACK)
		}

		// 设置状态栏的背景色。对于Android4.4和Android5.0以上版本要区分处理
		fun immersive(activity: Activity, colorStatus: Int, colorNavigate: Int) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					//5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
					val decorView = activity.window.decorView
					//两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
					val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					decorView.systemUiVisibility = option
					activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
					activity.window.statusBarColor = colorStatus
					//导航栏颜色也可以正常设置
					activity.window.navigationBarColor = colorNavigate
				} else {
					val attributes: WindowManager.LayoutParams = activity.window.attributes;
					val flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					val flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
					attributes.flags = attributes.flags or flagTranslucentStatus
					activity.window.attributes = attributes
				}
			}
		}

		// 添加顶部间隔，留出状态栏的位置
		private fun addMarginTop(activity: Activity) {
			val window = activity.window
			val contentView = window.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
			val child = contentView.getChildAt(0)
			if (TAG_MARGIN_ADDED != child.tag) {
				val params = child.layoutParams as FrameLayout.LayoutParams
				// 添加的间隔大小就是状态栏的高度
				params.topMargin += getStatusBarHeight(activity)
				child.layoutParams = params
				child.tag = TAG_MARGIN_ADDED
			}
		}

		// 移除顶部间隔，霸占状态栏的位置
		private fun removeMarginTop(activity: Activity) {
			val window = activity.window
			val contentView = window.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
			val child = contentView.getChildAt(0)
			if (TAG_MARGIN_ADDED == child.tag) {
				val params = child.layoutParams as FrameLayout.LayoutParams
				// 移除的间隔大小就是状态栏的高度
				params.topMargin -= getStatusBarHeight(activity)
				child.layoutParams = params
				child.tag = null
			}
		}

		// 对于Android4.4，系统没有提供设置状态栏颜色的方法，只能手工搞个假冒的状态栏来占坑
		private fun setKitKatStatusBarColor(activity: Activity, statusBarColor: Int) {
			val window = activity.window
			val decorView = window.decorView as ViewGroup
			// 先移除已有的冒牌状态栏
			val fakeView = decorView.findViewWithTag<View>(TAG_FAKE_STATUS_BAR_VIEW)
			if (fakeView != null) {
				decorView.removeView(fakeView)
			}
			// 再添加新来的冒牌状态栏
			val statusBarView = View(activity)
			val params = FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity))
			params.gravity = Gravity.TOP
			statusBarView.layoutParams = params
			statusBarView.setBackgroundColor(statusBarColor)
			statusBarView.tag = TAG_FAKE_STATUS_BAR_VIEW
			decorView.addView(statusBarView)
		}

		fun getStatusBarHeight(activity: Activity): Int {
			var height = 0
			val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
			if (resourceId > 0) {
				height = activity.resources.getDimensionPixelSize(resourceId)
			}
			return height
		}
	}
}
