package com.hcanyz.zadapter.helper

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hcanyz.zadapter.ZAdapter
import com.hcanyz.zadapter.hodler.ZRecyclerViewHolder
import com.hcanyz.zadapter.hodler.ZViewHolder
import com.hcanyz.zadapter.registry.IHolderCreatorName

fun RecyclerView.bindZAdapter(zAdapter: RecyclerView.Adapter<out ZRecyclerViewHolder<*>>) {
    layoutManager = LinearLayoutManager(context)
    adapter = zAdapter
}

fun <DATA : Any> ViewGroup.injectViewWithAdapter(adapter: ZAdapter<DATA>, startInjectPosition: Int = 0, iInjectHook: IInjectHook? = null) {
    // 取消之前的注册。防止多次刷新
    (tag as? RecyclerView.AdapterDataObserver)?.let {
        adapter.unregisterAdapterDataObserver(it)
    }
    onChanged(adapter, startInjectPosition, iInjectHook)
    //5. 观察 adapter
    val value = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            onChanged(adapter, startInjectPosition, iInjectHook)
        }
    }
    adapter.registerAdapterDataObserver(value)
    tag = value
}

private fun <DATA : Any> ViewGroup.onChanged(adapter: ZAdapter<DATA>, startInjectPosition: Int, iInjectHook: IInjectHook?) {
    //初始化容器中每个view
    val itemCount = adapter.itemCount
    for (position in startInjectPosition until itemCount) {
        val childAt = getChildAt(position)
        //1. 当前位置view不存在，从adapter中获取，加入容器中，tag置为holder
        if (childAt == null) {
            createHolder7hook(adapter, position, iInjectHook)
        } else {
            //2. view存在，判断tag是否正常
            if (childAt.tag !is ZViewHolder<*>) {
                throw IllegalStateException("tag error")
            }
            @Suppress("UNCHECKED_CAST")
            val zViewHolder = childAt.tag as? ZViewHolder<DATA>
            val oldItemData = zViewHolder?.mData
            val newItemData = adapter.datas[position]
            if (oldItemData != null && oldItemData is IHolderCreatorName && newItemData is IHolderCreatorName
                    && oldItemData.holderCreatorName() == newItemData.holderCreatorName()) {
                //3. 给每个holder刷新数据
                adapter.onBindViewHolder(zViewHolder.recyclerViewHolder, position)
                continue
            } else if (oldItemData === newItemData) {
                //3. 给每个holder刷新数据
                adapter.onBindViewHolder(zViewHolder.recyclerViewHolder, position)
                continue
            }
            createHolder7hook(adapter, position, iInjectHook)
        }
    }
    //3. 删除多余view
    if (childCount > itemCount && childCount > startInjectPosition) {
        removeViewsInLayout(itemCount, childCount - itemCount)
    }
}

private fun <DATA : Any> ViewGroup.createHolder7hook(adapter: ZAdapter<DATA>, position: Int, iInjectHook: IInjectHook?) {
    val findViewType = adapter.registry.findItemTypeByPosition(adapter.datas, position)
    val holder = adapter.registry.createHolderByHolderBean<DATA>(findViewType, this, adapter.viewHolderHelper)
    holder.init()
    val view = holder.rootView()
    iInjectHook?.hookViewCreated(holder, view, position)

    //3. 给每个holder刷新数据
    adapter.onBindViewHolder(holder.recyclerViewHolder, position)

    addView(view, position)
    view.tag = holder
}

interface IInjectHook {
    fun hookViewCreated(holder: ZViewHolder<*>, view: View, position: Int) {
    }
}