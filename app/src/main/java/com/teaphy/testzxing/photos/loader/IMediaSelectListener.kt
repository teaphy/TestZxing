package com.teaphy.testzxing.photos.loader

import com.teaphy.testzxing.photos.entity.LocalMedia

/**
 * @desc
 * @author tiany
 * @time 2018/10/11 下午1:47
 */
@FunctionalInterface
interface IMediaSelectListener {
	fun onSelected(listMedias: List<LocalMedia>)
}