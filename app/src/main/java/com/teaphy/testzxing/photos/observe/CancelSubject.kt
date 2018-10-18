package com.rrs.afcs.photos.observe

/**
 * @desc
 * @author tiany
 * @time 2018/9/30 下午1:54
 */
class CancelSubject private constructor(): ICancelSubject {

	companion object {
		private var INSTANCE : CancelSubject? = null

		@Synchronized
		fun obtain(): CancelSubject {
			if (null == INSTANCE) {
				INSTANCE = CancelSubject()
			}

			return INSTANCE ?: throw Throwable("init the instance of CancelSubject failure")
		}
	}

	private val listObserve = mutableListOf<ICancelObserve>()

	override fun subcribeObserve(observe: ICancelObserve) {
		listObserve.add(observe)
	}

	override fun unsubcribeObserve(observe: ICancelObserve) {
		listObserve.remove(observe)
	}

	fun observeCount() : Int{
		return listObserve.size
	}

	override fun notifyObserver() {
		for (observe in listObserve) {
			observe.doCancel()
		}
	}

}