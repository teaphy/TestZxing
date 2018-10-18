package com.rrs.afcs.photos.loader

import com.rrs.afcs.photos.entity.LocalMediaFolder

/**
 * @desc
 * @author tiany
 * @time 2018/9/29 上午9:46
 */
@FunctionalInterface
interface ILocalMediaLoadListener {
	fun loadComplete(localImageFolders: List<LocalMediaFolder>)
}