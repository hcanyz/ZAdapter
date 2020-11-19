package com.hcanyz.zadapter

import com.hcanyz.zadapter.hodler.ViewHolderHelper
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


/**
 * @author hcanyz
 */
class ZAdapterThrottle<DATA : Any>(mDatas: MutableList<DATA> = arrayListOf(),
                                   mViewHolderHelper: ViewHolderHelper? = null) :
        ZAdapter<DATA>(mDatas, mViewHolderHelper) {

    private var intervalDuration = 0L
    private var isOpenThrottle = false

    private lateinit var notifyDataSetChangedEmitter: ObservableEmitter<Int>

    private val notifyDataSetChangedDispose: Disposable by lazy {
        Observable.create(ObservableOnSubscribe<Int> {
            notifyDataSetChangedEmitter = it
        }).compose {
            it.throttleLast(intervalDuration, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
        }.subscribe {
            notifyDataSetChanged()
        }
    }

    /**
     * 节流刷新
     */
    fun notifyDataChangedWithThrottle() {
        check(isOpenThrottle) { "openThrottle before notifyDataChangedWithThrottle" }
        notifyDataSetChangedEmitter.onNext(1)
    }

    fun openThrottle(intervalDuration: Long): ZAdapter<DATA> {
        if (!isOpenThrottle) {
            isOpenThrottle = true
            this.intervalDuration = intervalDuration
            notifyDataSetChangedDispose
        }
        return this
    }

    fun release() {
        if (isOpenThrottle) {
            notifyDataSetChangedDispose.dispose()
        }
    }
}