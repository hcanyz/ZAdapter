package com.hcanyz.zadapter.hodler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView

/**
 * @author hcanyz
 *
 * LifecycleOwner的使用方式参考androidx.lifecycle.LifecycleObserver
 * holder.getLifecycle().addObserver(new LifecycleObserver())
 * class LifecycleObserver { @OnLifecycleEvent(Lifecycle.Event.ON_CREATE) xxx{}}
 *
 * 需要依赖 kapt deps.android.lifecycleCompiler ，自动生成 xxx_LifecycleObserverTest_LifecycleAdapter
 */
open class ZViewHolder<DATA : Any> : LifecycleOwner, View.OnAttachStateChangeListener {

    @Suppress("LeakingThis")
    constructor(context: Context, rootView: View) {
        this.mContext = context
        this.mRootView = rootView

        mRootView.addOnAttachStateChangeListener(this)
        mLifecycleRegistry = LifecycleRegistry(this)
    }

    constructor(context: Context, @LayoutRes layoutId: Int) : this(context, View.inflate(context, layoutId, null))

    constructor(parent: ViewGroup, @LayoutRes layoutId: Int) : this(parent.context,
            LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
    )

    val recyclerViewHolder: ZRecyclerViewHolder<DATA> by lazy {
        object : ZRecyclerViewHolder<DATA>(this, mRootView) {
            override fun update(data: DATA, payloads: List<Any>) {
                super.update(data, payloads)
                this@ZViewHolder.performUpdate(data, payloads)
            }

            override fun onViewRecycled() {
                super.onViewRecycled()
                this@ZViewHolder.onViewRecycled()
            }
        }
    }

    protected val mContext: Context
    private val mRootView: View

    lateinit var mData: DATA

    //用于传递事件源和其他消息
    var mViewHolderHelper: ViewHolderHelper? = null

    lateinit var zAdapter: RecyclerView.Adapter<ZRecyclerViewHolder<DATA>>

    private var mLifecycleRegistry: LifecycleRegistry

    // findViewById cache
    private val mFindViewByIdCache by lazy { HashMap<Int, View?>() }

    //需要this对象构造函数走完才能进行init
    private val initTask by lazy {
        initView(rootView())
        initListener(rootView())
    }

    internal fun init() {
        initTask
    }

    @CallSuper
    open fun initView(rootView: View) {
    }

    @CallSuper
    open fun initListener(rootView: View) {
    }

    @CallSuper
    protected open fun update(data: DATA, payloads: List<Any> = arrayListOf()) {
        this.mData = data
    }

    @CallSuper
    open fun onViewRecycled() {
    }

    @CallSuper
    open fun onViewAttachedToWindow() {
    }

    @CallSuper
    open fun onViewDetachedFromWindow() {
    }

    override fun getLifecycle(): Lifecycle {
        return mLifecycleRegistry
    }

    internal fun getLifecycleRegistry(): LifecycleRegistry {
        return mLifecycleRegistry
    }

    fun rootView(): View {
        return mRootView
    }

    fun <T : View> fv(@IdRes id: Int): T {
        return fvNullable<T>(id) as T
    }

    fun <T : View> fvNullable(@IdRes id: Int): T? {
        @Suppress("UNCHECKED_CAST")
        return mFindViewByIdCache.getOrPutNullable(id, {
            rootView().findViewById<T>(id)
        }) as T?
    }

    fun addSelf2ViewGroup(viewGroup: ViewGroup): ZViewHolder<DATA> {
        viewGroup.addView(rootView())
        return this
    }

    fun performUpdate(data: DATA, payloads: List<Any> = arrayListOf()): ZViewHolder<DATA> {
        update(data, payloads)
        return this
    }

    final override fun onViewAttachedToWindow(v: View?) {
        this@ZViewHolder.onViewAttachedToWindow()
        getLifecycleRegistry().handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    final override fun onViewDetachedFromWindow(v: View?) {
        getLifecycleRegistry().handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        this@ZViewHolder.onViewDetachedFromWindow()
        getLifecycleRegistry().handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    private inline fun <K, V> MutableMap<K, V?>.getOrPutNullable(key: K, defaultValue: () -> V?): V? {
        val value = get(key)
        return if (value == null) {
            val answer = defaultValue()
            put(key, answer)
            answer
        } else {
            value
        }
    }
}