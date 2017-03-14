package com.shyky.library.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

/**
 * 抽象的包装Adapter
 * <p>用于包装另一个Adapter</p>
 *
 * @author Shyky
 * @version 1.1
 * @date 2017/3/6
 * @since 1.0
 */
public abstract class WrapperAdapter extends RecyclerView.Adapter {
    protected Context context;
    protected RecyclerView.Adapter adapter;

    public WrapperAdapter(Context context, RecyclerView.Adapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }
}