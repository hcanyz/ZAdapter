package com.hcanyz.zadapter

import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.hcanyz.zadapter.hodler.ViewHolderHelper
import com.hcanyz.zadapter.hodler.ZRecyclerViewHolder
import com.hcanyz.zadapter.registry.HolderTypeResolverRegistry
import com.hcanyz.zadapter.registry.IHolderCreatorName
import java.util.*

class ZListAdapter<DATA : IHolderCreatorName> : ListAdapter<DATA, ZRecyclerViewHolder<DATA>> {

    constructor(diffCallback: DiffUtil.ItemCallback<DATA>, mViewHolderHelper: ViewHolderHelper? = null) : super(diffCallback) {
        this.mViewHolderHelper = mViewHolderHelper
    }

    constructor(config: AsyncDifferConfig<DATA>, mViewHolderHelper: ViewHolderHelper? = null) : super(config) {
        this.mViewHolderHelper = mViewHolderHelper
    }

    private val mViewHolderHelper: ViewHolderHelper?

    val registry by lazy { HolderTypeResolverRegistry() }

    val adapterUUID: String by lazy { UUID.randomUUID().toString() }

    var showItemCount = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZRecyclerViewHolder<DATA> {
        val holder = registry.createHolderByHolderBean<DATA>(viewType, parent, mViewHolderHelper)
        holder.initTask
        holder.zAdapter = this
        holder.getLifecycleRegistry().handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        return holder.recyclerViewHolder
    }

    override fun getItemViewType(position: Int): Int {
        return registry.findViewTypeByCreatorName(currentList[position].holderCreatorName())
    }

    override fun getItemCount(): Int {
        if (showItemCount > -1) {
            return showItemCount
        }
        return currentList.size
    }

    override fun onBindViewHolder(holder: ZRecyclerViewHolder<DATA>, position: Int) {
        onBindViewHolderInner(holder, position)
    }

    override fun onBindViewHolder(holder: ZRecyclerViewHolder<DATA>, position: Int, payloads: List<Any>) {
        onBindViewHolderInner(holder, position, payloads)
    }

    private fun onBindViewHolderInner(holder: ZRecyclerViewHolder<DATA>, position: Int, payloads: List<Any> = emptyList()) {
        holder.update(currentList[position], payloads)
        holder.holder.getLifecycleRegistry().handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onViewRecycled(holder: ZRecyclerViewHolder<DATA>) {
        super.onViewRecycled(holder)
        holder.onViewRecycled()
        holder.holder.getLifecycleRegistry().handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}