package com.hcanyz.zadapter.test

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.hcanyz.zadapter.ZAdapter
import com.hcanyz.zadapter.helper.injectViewWithAdapter
import com.hcanyz.zadapter.hodler.ViewHolderHelper
import com.hcanyz.zadapter.hodler.ZViewHolder
import kotlinx.android.synthetic.main.activity_test_zadapter_other_layout.*

class TestZAdapterInOtherLayoutActivity : AppCompatActivity() {

    companion object {
        const val TAG = "testZAdapterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_zadapter_other_layout)

        val listOf = arrayListOf<Any>()
        // String
        repeat(10) { listOf.add("$it") }
        //simple
        repeat(2) { listOf.add(SimpleData(R.mipmap.ic_launcher, "SimpleData_$it")) }
        //MultiData + R.layout.holder_multi_1
        repeat(7) { listOf.add(MultiData(R.mipmap.ic_launcher, "multiData_$it", false)) }
        //MultiData + R.layout.holder_multi_2
        repeat(7) { listOf.add(MultiData(R.mipmap.ic_launcher_round, "", true)) }
        //MultiData2 + R.layout.holder_multi_1
        repeat(70) { listOf.add(MultiData2(R.mipmap.ic_launcher, "MultiData2_$it")) }

        val zAdapter = ZAdapter(listOf, ViewHolderHelper(fragmentActivity = this))

        // registry data not implements IHolderCreatorName
        zAdapter.registry.registered { position -> if (position < 10) String::class.java.canonicalName else null }
        zAdapter.registry.registered(String::class.java.name) { parent: ViewGroup ->
            return@registered object : ZViewHolder<String>(parent.context, TextView(parent.context)) {
                override fun update(data: String, payloads: List<Any>) {
                    super.update(data, payloads)
                    (rootView() as TextView).text = data
                }
            }
        }

        //registry SimpleData + R.layout.holder_simple > SimpleHolder
        zAdapter.registry.registered(SimpleData::class.java.name) { parent ->
            val testHolder = SimpleHolder(parent)
            testHolder.lifecycle.addObserver(LifecycleObserverTest())
            return@registered testHolder
        }
        //registry MultiData + R.layout.holder_multi_1 > MultiHolder
        zAdapter.registry.registered(MultiData::class.java.name) { parent ->
            val multiHolder = MultiHolder(parent, R.layout.holder_multi_1)
            multiHolder.lifecycle.addObserver(LifecycleObserverTest())
            return@registered multiHolder
        }
        //registry MultiData + R.layout.holder_multi_2 > MultiHolder
        zAdapter.registry.registered("${MultiData::class.java.name}_${R.layout.holder_multi_2}") { parent ->
            val multiHolder = MultiHolder(parent, R.layout.holder_multi_2)
            multiHolder.lifecycle.addObserver(LifecycleObserverTest())
            return@registered multiHolder
        }
        ll_test.injectViewWithAdapter(zAdapter)

        //listen clickEvent
        ViewModelProviders.of(this).get(EventViewModel::class.java).clickEvent.observe(this, Observer {
            zAdapter.notifyDataSetChanged()
            when (it) {
                is MultiData -> {
                    Toast.makeText(this, "click -> $it", Toast.LENGTH_SHORT).show()
                }
                is MultiData2 -> {
                    Toast.makeText(this, "click -> $it", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    class LifecycleObserverTest : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun holderCreated(owner: LifecycleOwner) {
            Log.e(TAG, "HolderCreated -> can't use data")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun holderBindData(owner: LifecycleOwner) {
            Log.e(TAG, "HolderBindData -> ${(owner as ZViewHolder<*>).mData}")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun holderAttachedToWindow(owner: LifecycleOwner) {
            Log.e(TAG, "HolderAttachedToWindow -> ${(owner as ZViewHolder<*>).mData}")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun holderDetachedFromWindow(owner: LifecycleOwner) {
            Log.e(TAG, "HolderDetachedFromWindow -> ${(owner as ZViewHolder<*>).mData}")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun holderDetachedFromWindowToo(owner: LifecycleOwner) {
            Log.e("tag", "HolderDetachedFromWindowToo -> ${(owner as ZViewHolder<*>).mData}")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun holderRecycled(owner: LifecycleOwner) {
            Log.e(TAG, "HolderRecycled -> ${(owner as ZViewHolder<*>).mData}")
        }
    }
}