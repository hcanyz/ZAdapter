ZAdapter
===

> 简化RecyclerView Adapter的繁琐写法
> https://github.com/hcanyz/ZAdapter

Support **API v14+**  
Support **androidx** 

## ZAdapter初衷

### recylcerview + adapter有什么问题

    当我们使用RecyclerView的时候，先要创建一个Adapter，  

    在```getItemViewType```中根据数据对象定义很多viewType常量   
    在```onCreateViewHolder```中又根据viewType创建对应的holder   
    每个业务场景的adpter需要用的viewType又不一样，导致了很多重复定义，又有很多不同业务的holder融合在一个adpter。

    holder与容器的事件需要通过adapter层层传递。

    holder没有统一的范式方法，例如holder生命周期

    holder不能很好的被除recylcerview之外的容器使用

### ZAdapter思路

1. 通过```HolderTypeResolverRegistry关联``` bean与creator
2. 通过```ViewHolderHelper```传递容器，在使用ViewModel传递事件
3. 通过lifecycle 定义holder生命周期

## 如何使用

添加依赖
```groovy
//根目录build.gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
//要使用的模块添加:
dependencies {
    implementation 'com.github.hcanyz.ZAdapter:ZAdapter:1.0.0'
}
```

创建adapter
```kotlin
//init listOf
val listOf = arrayListOf<SimpleData>()
val zAdapter = ZAdapter<SimpleData>(listOf, ViewHolderHelper(fragmentActivity = this))

或者

val zAdapter = ZAdapter<SimpleData>()
//init listOf
val listOf = arrayListOf<SimpleData>()
zAdapter.mDatas = listOf
```

注册Creator,绑定RecyclerView
```kotlin
//注册生成器的本质就是 绑定一个 name 和 一个creator方法
//adapter要找到某个holder时会使用bean提供的holderCreatorName找到对应的creator方法，生成holdre
//ZAdapter默认约定一个实现了IHolderCreatorName的数据bean，holderCreatorName返回当前类的全类名
zAdapter.holderCreatorRegistry.registeredCreator(SimpleData::class.java.name) { parent ->
    val testHolder = SimpleHolder(parent)
    testHolder.lifecycle.addObserver(LifecycleObserverTest())
    return@registeredCreator testHolder
}
recylerview.layoutManager = LinearLayoutManager(context)
recylerview.adapter = zAdapter
```
主要部分结束.(具体demo参考app工程)

额外：   
1. holder与容器(fragment|activity)事件传递

    holder传递事件到容器，通过ViewModel
    ```kotlin
    //adapter创建时添加入参 ViewHolderHelper(fragmentActivity = this)
    //可以是 fragment or fragmentActivity

    //容器监听事件
    ViewModelProviders.of(this).get(EventViewModel::class.java).clickEvent.observe(...)

    //holder发送事件
    ViewModelProviders.of(it).get(EventViewModel::class.java).clickEvent.postValue(...)
    ```
    容器通过修改holder的数据bean，notifyDataChanged改变holder
2. 监听holder生命周期
    ```kotlin
    //build.gradle需要添加 kapt androidx.lifecycle:lifecycle-compiler:2.1.0
    //创建holder时添加
    testHolder.lifecycle.addObserver(LifecycleObserverTest())
    class LifecycleObserverTest : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun holderCreated(owner: LifecycleOwner) {
            Log.e(TAG, "HolderCreated -> ${(owner as ZViewHolder<*>).mData}")
        }
    }
    //其他参考Lifecycle.Event.*
    ```
3. 在其他ViewGroup中使用ZAdapter，例如LinearLayout
    ```kotlin
    ...
    //绑定时使用如下方法
    linearlayout.injectViewWithAdapter(zAdapter)
    ```
4. Throttle notifyDataChanged
    ```kotlin
    //某些场景下会多次刷新界面(例如im接收消息)，需要限制频率
    //需要build.gradle添加依赖： 
    //implementation 'com.github.hcanyz.ZAdapter:ZAdapter-Throttle:1.0.0'
    val zAdapter = ZAdapterThrottle<SimpleData>()
    //刷新频率
    zAdapter.openThrottle(1000)
    //调用刷新时
    zAdapter.notifyDataChangedWithThrottle()
    //界面结束 onDestroy
    (recylerview.adapter as ZAdapterThrottle<*>).release()
    ```