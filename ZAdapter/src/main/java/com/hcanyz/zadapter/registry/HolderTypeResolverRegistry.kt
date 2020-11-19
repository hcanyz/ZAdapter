package com.hcanyz.zadapter.registry

import android.view.ViewGroup
import android.widget.TextView
import com.hcanyz.zadapter.hodler.ViewHolderHelper
import com.hcanyz.zadapter.hodler.ZViewHolder
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashSet

/**
 * @author hcanyz
 */
class HolderTypeResolverRegistry {

    private val creatorMaps: MutableMap<String, HolderCreatorInfo> = HashMap()

    private val counter: AtomicInteger by lazy { AtomicInteger(1) }

    private val position2CreatorNameFunList: MutableSet<(position: Int) -> String?> = HashSet()

    /**
     * 注册holder生成器
     * @param creatorName String
     * @param creator Function1<[@kotlin.ParameterName] ViewGroup, ZViewHolder<*>>
     */
    fun registered(creatorName: String, creator: (parent: ViewGroup) -> ZViewHolder<*>) {
        val mutableMap = creatorMaps
        mutableMap[creatorName]?.let {
            throw IllegalStateException("Duplicate registration type：$creatorName $creator")
        }
        mutableMap[creatorName] = HolderCreatorInfo(counter.getAndIncrement(), creator)
    }

    /**
     * 注册position对应生成器名称
     * 同时需要registered(java.lang.String, Function1<? super ViewGroup,? extends ZViewHolder<?>>) ,优先级会低于数据对象IHolderCreatorName的实现
     * 若数据对象没有继承IHolderCreatorName则根据此自动搜寻生成器名称
     * @param position2CreatorName Function1<[@kotlin.ParameterName] Int, String?>
     */
    fun registered(position2CreatorName: (position: Int) -> String?) {
        val add = position2CreatorNameFunList.add(position2CreatorName)
        if (!add) {
            throw IllegalStateException("Duplicate registration type：$position2CreatorName")
        }
    }

    internal fun findItemTypeByPosition(datas: MutableList<out Any>, position: Int): Int {
        var itemType: Int? = null

        val any = datas[position]
        if (any is IHolderCreatorName) {
            itemType = creatorMaps[any.holderCreatorName()]?.itemType
        }

        if (itemType == null) {
            for (position2CreatorName in position2CreatorNameFunList) {
                val creatorName = position2CreatorName(position)
                if (creatorName != null) {
                    itemType = creatorMaps[creatorName]?.itemType
                    break
                }
            }
        }
        return itemType ?: 0
    }

    internal fun <DATA : Any> createHolderByHolderBean(viewType: Int, parent: ViewGroup, viewHolderHelper: ViewHolderHelper? = null): ZViewHolder<DATA> {
        //0说明没有找到
        if (viewType != 0) {
            creatorMaps.forEach { entry ->
                if (entry.value.itemType == viewType) {
                    @Suppress("UNCHECKED_CAST")
                    val zViewHolder = entry.value.creator(parent) as ZViewHolder<DATA>
                    zViewHolder.mViewHolderHelper = viewHolderHelper
                    return zViewHolder
                }
            }
        }

        val rootView = TextView(parent.context)
        @Suppress("UsePropertyAccessSyntax", "SetTextI18n")
        rootView.setText("Not registration type：$viewType")
        return ZViewHolder(parent.context, rootView)
    }

    private data class HolderCreatorInfo(val itemType: Int, val creator: (parent: ViewGroup) -> ZViewHolder<*>)
}