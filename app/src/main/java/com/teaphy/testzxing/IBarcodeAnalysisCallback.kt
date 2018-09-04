package com.teaphy.testzxing

import android.os.Bundle
import com.google.zxing.Result

/**
 * @desc 条形码解析回调
 * @author tiany
 * @time 2018/9/4 下午2:28
 */
interface IBarcodeAnalysisCallback {

	fun onAnalysisSuccess(rawResult: Result, bundle: Bundle)

	fun onAnalysisFailure()
}