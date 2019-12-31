package com.hcanyz.zadapter.test

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.hcanyz.zadapter.hodler.ZViewHolder
import com.hcanyz.zadapter.registry.IHolderCreatorName

data class SimpleData(val iconId: Int, val key: String) : IHolderCreatorName

class SimpleHolder(parent: ViewGroup, layoutId: Int = R.layout.holder_simple) : ZViewHolder<SimpleData>(parent, layoutId) {

    private lateinit var iv_test: ImageView

    override fun initView(rootView: View) {
        super.initView(rootView)
        iv_test = rootView.findViewById(R.id.iv_test)
    }

    override fun initListener(rootView: View) {
        super.initListener(rootView)
        iv_test.setOnClickListener {
            Toast.makeText(mContext, "click", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewAttachedToWindow() {
        super.onViewAttachedToWindow()
        Log.e(TestZAdapterActivity.TAG, "onViewAttachedToWindow ${mData?.key}")
    }

    override fun onViewDetachedFromWindow() {
        super.onViewDetachedFromWindow()
        Log.e(TestZAdapterActivity.TAG, "onViewDetachedFromWindow ${mData?.key}")
    }

    override fun onViewRecycled() {
        super.onViewRecycled()
        Log.e(TestZAdapterActivity.TAG, "onViewRecycled ${mData?.key}")
    }

    override fun update(data: SimpleData, payloads: List<Any>) {
        super.update(data, payloads)
        iv_test.setImageResource(data.iconId)
    }
}