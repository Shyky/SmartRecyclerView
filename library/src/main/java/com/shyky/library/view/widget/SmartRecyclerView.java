package com.shyky.library.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shyky.library.R;
import com.shyky.library.adapter.ArrayAdapter;
import com.shyky.library.adapter.HeaderAndFooterWrapperAdapter;
import com.shyky.library.adapter.WrapperAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义实现支持添加多个header view和footer view的RecyclerView
 *
 * @author Shyky
 * @version 1.2
 * @date 2017/3/2
 * @since 1.0
 */
public class SmartRecyclerView extends RecyclerView {
    /**
     * Listen to changes in the data set
     */
    protected InternalAdapterDataObserver dataSetObserver;
    /**
     * The adapter containing the data to be displayed by this view
     */
    protected Adapter internalAdapter;
    private ArrayList<View> headerViews;
    private ArrayList<View> footerViews;
    private final LayoutInflater layoutInflater;
    private Drawable divider;
    private int dividerHeight;
    /**
     * The listener that receives notifications when an item is clicked.
     */
    private OnItemClickListener onItemClickListener;

    protected class InternalAdapterDataObserver extends AdapterDataObserver {
        protected AdapterDataObserver internalDataObserver;

        public InternalAdapterDataObserver() {
            // 通过反射获取父类观察者对象
            try {
                Field field = getDeclaredField(RecyclerView.class, "mObserver");
                if (field != null) {
                    internalDataObserver = (AdapterDataObserver) field.get(SmartRecyclerView.this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Field getDeclaredField(Class clz, String fieldName) {
            Field field;
            for (; clz != Object.class; clz = clz.getSuperclass()) {
                try {
                    field = clz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field;
                } catch (Exception e) {
                }
            }
            return null;
        }

        @Override
        public void onChanged() {
            if (internalDataObserver != null) {
                internalDataObserver.onChanged();
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            if (internalDataObserver != null) {
                internalDataObserver.onItemRangeChanged(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            if (internalDataObserver != null) {
                internalDataObserver.onItemRangeChanged(positionStart, itemCount, payload);
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (internalDataObserver != null) {
                internalDataObserver.onItemRangeInserted(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            if (internalDataObserver != null) {
                internalDataObserver.onItemRangeRemoved(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (internalDataObserver != null) {
                internalDataObserver.onItemRangeMoved(fromPosition, toPosition, itemCount);
            }
        }
    }

    /**
     * Interface definition for a callback to be invoked when an item in this
     * AdapterView has been clicked.
     */
    public interface OnItemClickListener {
        /**
         * Callback method to be invoked when an item in this AdapterView has
         * been clicked.
         * <p>
         * Implementers can call getItemAtPosition(position) if they need
         * to access the data associated with the selected item.
         *
         * @param parent   The RecyclerView where the click happened.
         * @param view     The view within the AdapterView that was clicked (this
         *                 will be a view provided by the adapter)
         * @param position The position of the view in the adapter.
         * @param id       The row id of the item that was clicked.
         */
        void onItemClick(ViewGroup parent, View view, int position, long id);
    }

    public SmartRecyclerView(Context context) {
        this(context, null);
    }

    public SmartRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        headerViews = new ArrayList<>();
        footerViews = new ArrayList<>();
        layoutInflater = LayoutInflater.from(context);
        // 设置默认的布局管理器，默认为垂直方向排列
        setLayoutManager(new LinearLayoutManager(getContext()));

        int defStyleRes = 0;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SmartRecyclerView, defStyle, defStyleRes);

        CharSequence[] entries = typedArray.getTextArray(R.styleable.SmartRecyclerView_android_entries);
        if (entries == null) {
            entries = typedArray.getTextArray(R.styleable.SmartRecyclerView_app_entries);
        }
        if (entries != null) {
            setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, entries));
        }

//        addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
//
//        // Set item divider
//        Drawable divider = typedArray.getDrawable(R.styleable.SmartRecyclerView_android_divider);
//        if (divider == null) {
//            divider = typedArray.getDrawable(R.styleable.SmartRecyclerView_app_divider);
//        }
//        if (divider != null) {
//            // Use an implicit divider height which may be explicitly
//            // overridden by android:dividerHeight further down.
//            setDivider(divider);
//        }
        typedArray.recycle();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (internalAdapter != null && dataSetObserver != null) {
            internalAdapter.unregisterAdapterDataObserver(dataSetObserver);
        }
        if (headerViews.size() > 0 || footerViews.size() > 0) {
            internalAdapter = wrapHeaderListAdapterInternal(headerViews, footerViews, adapter);
        } else {
            internalAdapter = adapter;
        }
        if (internalAdapter != null) {
            dataSetObserver = new InternalAdapterDataObserver();
            internalAdapter.registerAdapterDataObserver(dataSetObserver);
        }

        super.swapAdapter(internalAdapter, true);
    }

    /**
     * Sets the drawable that will be drawn between each item in the list.
     * <p>
     * <strong>Note:</strong> If the drawable does not have an intrinsic
     * height, you should also call {@link #setDividerHeight(int)}.
     *
     * @param divider the drawable to use
     * @attr ref R.styleable#ListView_divider
     */
    public void setDivider(@Nullable Drawable divider) {
        if (divider != null) {
            dividerHeight = divider.getIntrinsicHeight();
        } else {
            dividerHeight = 0;
        }
        this.divider = divider;
//        mDividerIsOpaque = divider == null || divider.getOpacity() == PixelFormat.OPAQUE;
//        requestLayout();
//        invalidate();

        addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    /**
     * Sets the height of the divider that will be drawn between each item in the list. Calling
     * this will override the intrinsic height as set by {@link #setDivider(Drawable)}
     *
     * @param height The new height of the divider in pixels.
     */
    public void setDividerHeight(int height) {
        dividerHeight = height;
//        requestLayout();
//        invalidate();
    }

    /**
     * 设置item点击事件
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public void addHeaderView(@LayoutRes int resId) {
        addHeaderView(layoutInflater.inflate(resId, this, false));
    }

    /**
     * 添加一个view出现在列表的底部
     * <p>注意：此方法需要在{@link #setAdapter(Adapter)}之前调用</p>
     *
     * @param view 要添加的view
     */
    public void addHeaderView(View view) {
        headerViews.add(view);
        // Wrap the adapter if it wasn't already wrapped.
        if (internalAdapter != null) {
            if (!(internalAdapter instanceof HeaderAndFooterWrapperAdapter)) {
                wrapHeaderListAdapterInternal();
            }

            // In the case of re-adding a header view, or adding one later on,
            // we need to notify the observer.
            if (dataSetObserver != null) {
                dataSetObserver.onChanged();
            }
        }
    }

    public void addFooterView(@LayoutRes int resId) {
        addFooterView(layoutInflater.inflate(resId, this, false));
    }

    /**
     * 添加一个view出现在列表的底部
     *
     * @param view 要添加的view
     */
    public void addFooterView(View view) {
        footerViews.add(view);
        // Wrap the adapter if it wasn't already wrapped.
        if (internalAdapter != null) {
            if (!(internalAdapter instanceof HeaderAndFooterWrapperAdapter)) {
                wrapHeaderListAdapterInternal();
            }

            // In the case of re-adding a footer view, or adding one later on,
            // we need to notify the observer.
            if (dataSetObserver != null) {
                dataSetObserver.onChanged();
            }
        }
    }

    protected WrapperAdapter wrapHeaderListAdapterInternal(List<View> headerViews, List<View> footerViews, Adapter adapter) {
        return new HeaderAndFooterWrapperAdapter(getContext(), headerViews, footerViews, adapter);
    }

    protected void wrapHeaderListAdapterInternal() {
        internalAdapter = wrapHeaderListAdapterInternal(headerViews, footerViews, internalAdapter);
    }
}