package com.rrs.afcs.photos.loader

import com.rrs.afcs.photos.entity.LocalMedia

/**
 * @desc
 * @author tiany
 * @time 2018/10/11 下午1:47
 */
@FunctionalInterface
interface IMediaSelectListener {
	fun onSelected(listMedias: List<LocalMedia>)
}