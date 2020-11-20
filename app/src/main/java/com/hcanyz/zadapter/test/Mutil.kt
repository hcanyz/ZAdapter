package com.hcanyz.zadapter.test

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.hcanyz.zadapter.hodler.ZViewHolder
import com.hcanyz.zadapter.registry.IHolderCreatorName

interface IMulti : IHolderCreatorName {
    val iconId: Int
    var text: String
}

data class MultiData(override val iconId: Int, override var text: String, val isLayout2: Boolean) : IMulti {
    override fun holderCreatorName(): String {
        if (isLayout2) {
            return "${MultiData::class.java.name}_${R.layout.holder_multi_2}"
        }
        return MultiData::class.java.name
    }
}

data class MultiData2(val data2IconId: Int, val data2Text: String) : IMulti {

    override val iconId: Int = data2IconId

    override var text: String = data2Text

    override fun holderCreatorName(): String {
        return MultiData::class.java.name
    }
}

class MultiHolder(parent: ViewGroup, layoutId: Int = R.layout.holder_multi_1) : ZViewHolder<IMulti>(parent, layoutId) {

    private val iv_test by lazy { fv<ImageView>(R.id.iv_test) }
    private val tv_test by lazy { fvNullable<TextView>(R.id.tv_test) }

    override fun initListener(rootView: View) {
        super.initListener(rootView)
        iv_test.setOnClickListener {
            mViewHolderHelper?.requireFragmentActivity()?.let {
                ViewModelProviders.of(it).get(EventViewModel::class.java).clickEvent.postValue(mData)
            }
        }
    }

    override fun update(data: IMulti, payloads: List<Any>) {
        super.update(data, payloads)
        iv_test.setImageResource(data.iconId)
        tv_test?.text = data.text
    }
}