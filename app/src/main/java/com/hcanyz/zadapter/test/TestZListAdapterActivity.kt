package com.hcanyz.zadapter.test

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import com.hcanyz.zadapter.ZListAdapter
import com.hcanyz.zadapter.helper.bindZAdapter
import com.hcanyz.zadapter.hodler.ViewHolderHelper
import com.hcanyz.zadapter.hodler.ZViewHolder
import com.hcanyz.zadapter.registry.IHolderCreatorName
import kotlinx.android.synthetic.main.activity_test_zadapter.*
import kotlin.random.Random

class TestZListAdapterActivity : AppCompatActivity() {

    companion object {
        const val TAG = "TestZListAdapter"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_zadapter)

        val listOf = arrayListOf<IHolderCreatorName>()
        //simple
        repeat(2) { listOf.add(SimpleData(R.mipmap.ic_launcher, "SimpleData_$it")) }
        //MultiData + R.layout.holder_multi_1
        repeat(7) { listOf.add(MultiData(R.mipmap.ic_launcher, "multiData_$it", false)) }
        //MultiData + R.layout.holder_multi_2
        repeat(7) { listOf.add(MultiData(R.mipmap.ic_launcher_round, "multiData_$it", true)) }
        //MultiData2 + R.layout.holder_multi_1
        repeat(7) { listOf.add(MultiData2(R.mipmap.ic_launcher, "MultiData2_$it")) }

        val zListAdapter = ZListAdapter(object : DiffUtil.ItemCallback<IHolderCreatorName>() {
            override fun areItemsTheSame(oldItem: IHolderCreatorName, newItem: IHolderCreatorName): Boolean {
                if (oldItem is SimpleData && newItem is SimpleData) {
                    return oldItem.key == newItem.key
                }
                if (oldItem is MultiData && newItem is MultiData) {
                    return oldItem.text == newItem.text
                }
                if (oldItem is MultiData2 && newItem is MultiData2) {
                    return oldItem.data2Text == newItem.data2Text
                }
                return false
            }

            override fun areContentsTheSame(oldItem: IHolderCreatorName, newItem: IHolderCreatorName): Boolean {
                if (oldItem is SimpleData && newItem is SimpleData) {
                    return oldItem.key == newItem.key
                }
                if (oldItem is MultiData && newItem is MultiData) {
                    return oldItem.text == newItem.text
                }
                if (oldItem is MultiData2 && newItem is MultiData2) {
                    return oldItem.data2Text == newItem.data2Text
                }
                return false
            }
        }, ViewHolderHelper(fragmentActivity = this))
        //registry SimpleData + R.layout.holder_simple > SimpleHolder
        zListAdapter.registry.registered(SimpleData::class.java.name) { parent ->
            val testHolder = SimpleHolder(parent)
            testHolder.lifecycle.addObserver(LifecycleObserverTest())
            return@registered testHolder
        }
        //registry MultiData + R.layout.holder_multi_1 > MultiHolder
        zListAdapter.registry.registered(MultiData::class.java.name) { parent ->
            val multiHolder = MultiHolder(parent, R.layout.holder_multi_1)
            multiHolder.lifecycle.addObserver(LifecycleObserverTest())
            return@registered multiHolder
        }
        //registry MultiData + R.layout.holder_multi_2 > MultiHolder
        zListAdapter.registry.registered("${MultiData::class.java.name}_${R.layout.holder_multi_2}") { parent ->
            val multiHolder = MultiHolder(parent, R.layout.holder_multi_2)
            multiHolder.lifecycle.addObserver(LifecycleObserverTest())
            return@registered multiHolder
        }
        recylerview.bindZAdapter(zListAdapter)

        zListAdapter.submitList(listOf)

        //listen clickEvent
        ViewModelProviders.of(this).get(EventViewModel::class.java).clickEvent.observe(this, Observer {
            when (it) {
                is MultiData -> {
                    Toast.makeText(this, "click -> $it", Toast.LENGTH_SHORT).show()
                    repeat(7) { index -> listOf.add(MultiData2(R.mipmap.ic_launcher, "MultiData2_${index}_${Random.nextInt(1000)}")) }
                    zListAdapter.submitList(ArrayList(listOf))
                }
                is MultiData2 -> {
                    Toast.makeText(this, "click -> $it", Toast.LENGTH_SHORT).show()
                    repeat(7) { index -> listOf.removeAt(index) }
                    zListAdapter.submitList(ArrayList(listOf))
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