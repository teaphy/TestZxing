package com.teaphy.testzxing.photos.observe

/**
 * @desc
 * @author tiany
 * @time 2018/9/30 下午1:49
 */
interface ICancelSubject {
	fun subcribeObserve(observe: ICancelObserve)
	fun unsubcribeObserve(observe: ICancelObserve)
	fun notifyObserver()
}