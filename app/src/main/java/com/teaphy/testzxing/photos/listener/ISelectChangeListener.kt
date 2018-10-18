package com.rrs.afcs.photos.listener

import com.rrs.afcs.photos.entity.LocalMedia

/**
 * @desc
 * @author tiany
 * @time 2018/9/29 下午6:22
 */
@FunctionalInterface
interface ISelectChangeListener {
	fun onSelectChange(localMedia: LocalMedia)
}