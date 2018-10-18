package com.rrs.afcs.photos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.BundleCompat
import android.support.v4.app.Fragment
import com.rrs.afcs.photos.config.PictureConfig
import com.rrs.afcs.photos.config.PictureSelectConfig
import com.rrs.afcs.photos.entity.LocalMedia
import com.rrs.afcs.photos.ui.ExternalPreviewActivity
import java.lang.ref.WeakReference
import java.util.ArrayList

/**
 * @desc
 * @author tiany
 * @time 2018/10/10 下午4:44
 */
class PictureSelectors private constructor(activity: Activity?, fragment: Fragment?) {

	private val activityWeak: WeakReference<Activity> = WeakReference<Activity>(activity)
	private val fragmentWeak: WeakReference<Fragment> = WeakReference<Fragment>(fragment)

	private constructor(activity: Activity) : this(activity, null)

	private constructor(fragment: Fragment) : this(fragment.activity, fragment)

	companion object {

		fun create(activity: Activity): PictureSelectors {
			return PictureSelectors(activity)
		}

		fun create(fragment: Fragment): PictureSelectors {
			return PictureSelectors(fragment)
		}
	}

	fun getActivity(): Activity? {
		return activityWeak.get()
	}

	fun getFragment(): Fragment? {
		return fragmentWeak.get()
	}

	/**
	 * 打开图片选择列表
	 */
	fun openGallery() : PictureSelectorModel{
		return PictureSelectorModel(this)
	}

	/**
	 * 打开图片选择列表并可以拍照
	 */
	fun openCamera(): PictureSelectorModel {
		return PictureSelectorModel(this, true)
	}

	/**
	 * 提供外部预览图片
	 */
	fun openExternalPreview(indexPreview: Int, listMedia: List<LocalMedia>) {
		val activity = getActivity() ?: return

		val intent = Intent(activity, ExternalPreviewActivity::class.java)
		val bundle = Bundle()

		bundle.putInt(PictureConfig.KEY_POSITION, indexPreview)
		bundle.putParcelableArrayList(PictureConfig.KEY_CONTENT, listMedia as ArrayList<LocalMedia>)

		intent.putExtras(bundle)
		activity.startActivity(intent)
	}
}