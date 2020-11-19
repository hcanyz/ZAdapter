package com.hcanyz.zadapter.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.codemonkeylabs.fpslibrary.TinyDancer
import com.hcanyz.zadapter.ZAdapterThrottle
import com.hcanyz.zadapter.helper.bindZAdapter
import com.hcanyz.zadapter.hodler.ViewHolderHelper
import com.hcanyz.zadapter.registry.IHolderCreatorName
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_test_zadapter_throttle.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class TestZAdapterThrottleActivity : AppCompatActivity() {

    companion object {
        const val TAG = "testZAdapterActivity"
    }

    private val compositeDisposable by lazy { CompositeDisposable() }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_zadapter_throttle)

        val listOf = arrayListOf<IHolderCreatorName>()
        //simple
        repeat(2) { listOf.add(SimpleData(R.mipmap.ic_launcher, "SimpleData_$it")) }
        //MultiData + R.layout.holder_multi_1
        repeat(27) { listOf.add(MultiData(R.mipmap.ic_launcher, "multiData_$it", false)) }
        //MultiData + R.layout.holder_multi_2
        repeat(27) { listOf.add(MultiData(R.mipmap.ic_launcher_round, "", true)) }
        //MultiData2 + R.layout.holder_multi_1
        repeat(27) { listOf.add(MultiData2(R.mipmap.ic_launcher, "MultiData2_$it")) }

        val zAdapter = ZAdapterThrottle<IHolderCreatorName>(mViewHolderHelper = ViewHolderHelper(fragmentActivity = this))
        //registry SimpleData + R.layout.holder_simple > SimpleHolder
        zAdapter.registry.registered(SimpleData::class.java.name) { parent ->
            return@registered SimpleHolder(parent)
        }
        //registry MultiData + R.layout.holder_multi_1 > MultiHolder
        zAdapter.registry.registered(MultiData::class.java.name) { parent ->
            return@registered MultiHolder(parent, R.layout.holder_multi_1)
        }
        //registry MultiData + R.layout.holder_multi_2 > MultiHolder
        zAdapter.registry.registered("${MultiData::class.java.name}_${R.layout.holder_multi_2}") { parent ->
            return@registered MultiHolder(parent, R.layout.holder_multi_2)
        }
        zAdapter.openThrottle(1000)
        zAdapter.datas = listOf
        recylerview.bindZAdapter(zAdapter)

        //listen clickEvent
        ViewModelProviders.of(this).get(EventViewModel::class.java).clickEvent.observe(this, Observer {
            when (it) {
                is MultiData -> {
                    Toast.makeText(this, "click -> $it", Toast.LENGTH_SHORT).show()
                }
                is MultiData2 -> {
                    Toast.makeText(this, "click -> $it", Toast.LENGTH_SHORT).show()
                }
            }
        })

        val subscribe = Observable.interval(10, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    for (mData in zAdapter.datas) {
                        if (mData is SimpleData) {
                            if (mData.key.isNotEmpty()) {
                                mData.key = mData.key.replaceRange(mData.key.length - 1, mData.key.length, Random.nextInt(9).toString())
                            }
                        } else if (mData is IMulti) {
                            if (mData.text.isNotEmpty()) {
                                mData.text = mData.text.replaceRange(mData.text.length - 1, mData.text.length, Random.nextInt(9).toString())
                            }
                        }
                    }

                    if (isOpenThrottle) {
                        zAdapter.notifyDataChangedWithThrottle()
                    } else {
                        zAdapter.notifyDataSetChanged()
                    }

                    requestNotifyCount++
                    test_info.text = "requestNotifyCount:$requestNotifyCount \n" +
                            "realNotifyCount:$realNotifyCount \n"
                }

        compositeDisposable.add(subscribe)

        zAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                Log.e(TAG, "onChanged -> ${System.currentTimeMillis()}")

                realNotifyCount++
                test_info.text = "requestNotifyCount:$requestNotifyCount \n" +
                        "realNotifyCount:$realNotifyCount \n"
            }
        })

        test_switch.setOnCheckedChangeListener { _, isChecked ->
            isOpenThrottle = isChecked
        }

        //alternatively
        TinyDancer.create()
                .redFlagPercentage(.1f) // set red indicator for 10%....different from default
                .startingXPosition(500)
                .startingYPosition(500)
                .show(this)
    }

    //--- test info ---
    private var isOpenThrottle = true
    private var requestNotifyCount = 0
    private var realNotifyCount = 0
    //--- test info ---

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
        //需要释放。 否则有内存泄漏问题
        (recylerview.adapter as ZAdapterThrottle<*>).release()
    }
}