package com.shyky.library.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用装饰者模式实现的支持添加多个header和footer的包装Adapter
 *
 * @author Shyky
 * @version 1.2
 * @date 2017/3/2
 * @since 1.0
 */
public class HeaderAndFooterWrapperAdapter extends WrapperAdapter {
    /**
     * 取值为Integer.MAX_VALUE + 10是为了防止外部的Adapter，即被包装的Adapter中的view type与其冲突
     */
    private static final int TYPE_HEADER = Integer.MAX_VALUE + 10;
    private static final int TYPE_FOOTER = Integer.MIN_VALUE - 10;
    private final List<View> headerViews;
    private final List<View> footerViews;
    private ViewGroup parent;

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final FrameLayout container;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            container = (FrameLayout) itemView;
        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {
        public final FrameLayout container;

        public FooterViewHolder(View itemView) {
            super(itemView);
            container = (FrameLayout) itemView;
        }
    }

    public HeaderAndFooterWrapperAdapter(Context context, RecyclerView.Adapter adapter) {
        this(context, null, null, adapter);
    }

    public HeaderAndFooterWrapperAdapter(Context context, List<View> headerViews, List<View> footerViews, RecyclerView.Adapter adapter) {
        super(context, adapter);
        this.context = context;
        if (headerViews == null && footerViews == null) {
            this.headerViews = new ArrayList<>();
            this.footerViews = new ArrayList<>();
        } else {
            this.headerViews = headerViews;
            this.footerViews = footerViews;
        }
        this.adapter = adapter;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getHeaderCount()) {
            return TYPE_HEADER;
        }
        if (hasFooter() && position >= getHeaderCount() + adapter.getItemCount()) {
            return TYPE_FOOTER;
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent = parent;
        switch (viewType) {
            case TYPE_HEADER:
                final FrameLayout headerContainer = new FrameLayout(context);
                return new HeaderViewHolder(headerContainer);
            case TYPE_FOOTER:
                final FrameLayout footerContainer = new FrameLayout(context);
                return new FooterViewHolder(footerContainer);
            default:
                return adapter.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case TYPE_HEADER:
                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                // 解决java.lang.IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.
                headerViewHolder.container.removeAllViews();
                headerViewHolder.container.addView(getHeader(position));
                break;
            case TYPE_FOOTER:
                final FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
                // 解决java.lang.IllegalStateException: The specified child already has a parent. You must call removeView() on the child's parent first.
                footerViewHolder.container.removeAllViews();
                footerViewHolder.container.addView(getFooter(position));
                break;
            default:
                final int itemPosition = position - getHeaderCount();

                adapter.onBindViewHolder(holder, itemPosition);
        }
    }

    @Override
    public int getItemCount() {
        return getHeaderCount() + adapter.getItemCount() + getFooterCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        // 解决当RecyclerView的LayoutManager为GridLayoutManager时，header view和footer view显示不正常
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) layoutManager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    // 当item view type为header或footer时返回单位格为1
                    final int itemViewType = getItemViewType(position);
                    return itemViewType == TYPE_HEADER || itemViewType == TYPE_FOOTER ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params != null && params instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) params;
            // 当item view type为header或footer时设置为全屏显示
            final int itemViewType = holder.getItemViewType();
            p.setFullSpan(itemViewType == TYPE_HEADER || itemViewType == TYPE_FOOTER);
        }
    }

    public int getHeaderCount() {
        return headerViews.size();
    }

    public View getHeader(int index) {
        return headerViews.get(index);
    }

    public int getFooterCount() {
        return headerViews.size();
    }

    public boolean hasFooter() {
        return getFooterCount() != 0;
    }

    public View getFooter(int index) {
        final int footerIndex = index - (getHeaderCount() + adapter.getItemCount());
        return footerViews.get(footerIndex);
    }
}