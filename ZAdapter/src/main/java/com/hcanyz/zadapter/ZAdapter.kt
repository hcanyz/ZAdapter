package com.hcanyz.zadapter

import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.hcanyz.zadapter.hodler.ViewHolderHelper
import com.hcanyz.zadapter.hodler.ZRecyclerViewHolder
import com.hcanyz.zadapter.registry.HolderTypeResolverRegistry


/**
 * @author hcanyz
 */
open class ZAdapter<DATA : Any>(var datas: MutableList<DATA> = arrayListOf(),
                                val viewHolderHelper: ViewHolderHelper? = null) :
        RecyclerView.Adapter<ZRecyclerViewHolder<DATA>>() {

    val registry by lazy { HolderTypeResolverRegistry() }

    var showItemCount = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZRecyclerViewHolder<DATA> {
        val holder = registry.createHolderByHolderBean<DATA>(viewType, parent, viewHolderHelper)
        holder.initTask
        holder.zAdapter = this
        holder.getLifecycleRegistry().handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        return holder.recyclerViewHolder
    }

    override fun getItemViewType(position: Int): Int {
        return registry.findItemTypeByPosition(datas, position)
    }

    override fun getItemCount(): Int {
        if (showItemCount > -1) {
            return showItemCount
        }
        return datas.size
    }

    override fun onBindViewHolder(holder: ZRecyclerViewHolder<DATA>, position: Int) {
        onBindViewHolderInner(holder, position)
    }

    override fun onBindViewHolder(holder: ZRecyclerViewHolder<DATA>, position: Int, payloads: List<Any>) {
        onBindViewHolderInner(holder, position, payloads)
    }

    private fun onBindViewHolderInner(holder: ZRecyclerViewHolder<DATA>, position: Int, payloads: List<Any> = emptyList()) {
        holder.update(datas[position], payloads)
        holder.holder.getLifecycleRegistry().handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onViewRecycled(holder: ZRecyclerViewHolder<DATA>) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
        holder.holder.getLifecycleRegistry().handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}