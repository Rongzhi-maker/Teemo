package com.lrz.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lrz.ui.inter.OnItemClickListener;
import com.lrz.ui.inter.OnItemLongClickListener;
import com.lrz.ui.inter.OnItemMultiClickListener;
import com.lrz.ui.inter.OnTimeClickListener;

import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * Author And Date: liurongzhi on 2019/11/27.
 * Description: viewHolder超类
 */
public abstract class BaseViewHolder<D> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    protected boolean isAttached = false;

    private OnItemClickListener<D> clickListener;
    private OnItemLongClickListener<D> onLongClickListener;
    private OnItemMultiClickListener<D> multiClickListener;
    protected int viewHolderPosition = 0;
    protected D data;
    private View.OnClickListener onTimeClick;

    Class<D> getTypeClass() {
        return (Class<D>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public BaseViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        initView();
    }

    public D getData() {
        return data;
    }

    void setData(D data) {
        D old = this.data;
        this.data = data;
        onDataChanged(this.data, old);
    }

    protected void onDataChanged(D data, D old) {

    }

    void setViewHolderPosition(int viewHolderPosition) {
        this.viewHolderPosition = viewHolderPosition;
    }

    public BaseViewHolder(Context context, @LayoutRes int layoutID) {
        this(View.inflate(context, layoutID, null));
    }

    public BaseViewHolder(@NonNull ViewGroup parent, @LayoutRes int layoutID) {
        this(LayoutInflater.from(parent.getContext()).inflate(layoutID, parent, false));
    }

    protected abstract void initView();

    public boolean isAttachedToWindow() {
        return isAttached;
    }

    public void onViewAttachedToWindow() {
        isAttached = true;
    }

    public void onViewDetachedFromWindow() {
        isAttached = false;
    }

    public void proxyClick(View view) {
        if (view != null) {
            view.setOnClickListener(this);
        }
    }

    public void proxyTimeClick(View view) {
        if (view != null) {
            if (onTimeClick == null) {
                onTimeClick = new OnTimeClickListener() {
                    @Override
                    public void onTimeClick(View v) {
                        if (clickListener != null) {
                            clickListener.onClick(v, viewHolderPosition, data);
                        }
                    }
                };
            }
            view.setOnClickListener(onTimeClick);
        }
    }

    public void proxyLongClick(View view) {
        if (view != null) {
            view.setOnLongClickListener(this);
        }
    }

    void setOnClick(OnItemClickListener<D> clickListener) {
        this.clickListener = clickListener;
    }

    void setOnLongClickListener(OnItemLongClickListener<D> onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    protected OnItemClickListener<D> getClickListener() {
        return clickListener;
    }

    public OnItemMultiClickListener<D> getMultiClickListener() {
        return multiClickListener;
    }

    public void setOnMultiClick(OnItemMultiClickListener<D> clickListener) {
        this.multiClickListener = clickListener;
    }

    public int getViewHolderPosition() {
        return viewHolderPosition;
    }

    public abstract void onBindViewHolder(D d);

    /**
     * 局部刷新，可重写
     *
     * @param d
     * @param payloads
     */
    public void onBindViewHolder(D d, List<Object> payloads) {

    }


    @Override
    public void onClick(View v) {
        if (clickListener != null) {
            clickListener.onClick(v, viewHolderPosition, data);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (onLongClickListener != null) {
            return onLongClickListener.onLongClick(v, viewHolderPosition, data);
        }
        return false;
    }

    public void onSingleClick(View view) {
        if (multiClickListener != null) {
            multiClickListener.onSingleClick(view, viewHolderPosition, data);
        }
    }

    public void onDoubleClick(View view) {
        if (multiClickListener != null) {
            multiClickListener.onDoubleClick(view, viewHolderPosition, data);
        }
    }
}
