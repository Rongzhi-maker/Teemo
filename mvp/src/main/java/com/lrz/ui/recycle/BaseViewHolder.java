package com.lrz.ui.recycle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lrz.ui.recycle.inter.OnGestureViewListener;
import com.lrz.ui.recycle.inter.OnItemClickListener;
import com.lrz.ui.recycle.inter.OnRecycleGestureListener;
import com.lrz.ui.recycle.inter.OnTimeClickListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Author And Date: liurongzhi on 2019/11/27.
 * Description: com.yilan.sdk.common.ui.recycle
 */
public abstract class BaseViewHolder<D> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    protected boolean isAttached = false;

    private OnItemClickListener<D> clickListener;
    private OnItemClickListener<D> longClickListener;
    private OnRecycleGestureListener<D> multiClickListener;
    protected int viewHolderPosition;
    protected D data;
    private View.OnClickListener onTimeClick;

    Class getTypeClass() {
        if (data != null) return data.getClass();
        Type type = BaseViewHolder.this.getClass().getGenericSuperclass();
        while (type instanceof Class && type != BaseViewHolder.class) {
            type = ((Class<?>) type).getGenericSuperclass();
        }
        return (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
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

    public BaseViewHolder(Context context, ViewGroup parent, @LayoutRes int layoutID) {
        this(LayoutInflater.from(context).inflate(layoutID, parent, false));
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

    public void proxyClick(View... views) {
        for (View v : views) {
            if (v != null) {
                v.setOnClickListener(this);
            }
        }
    }

    public void proxyGestureDetector(GestureView... views) {
        for (GestureView v : views) {
            v.setOnGestureListener(getOnGestureViewListener());
        }
    }

    public void proxyLongClick(View view) {
        if (view != null) {
            view.setOnLongClickListener(this);
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

    public void setOnClick(OnItemClickListener<D> clickListener) {
        this.clickListener = clickListener;
    }

    public void setOnLongClick(OnItemClickListener<D> longClickListener) {
        this.longClickListener = longClickListener;
    }

    protected OnItemClickListener<D> getClickListener() {
        return clickListener;
    }

    public OnRecycleGestureListener<D> getMultiClickListener() {
        return multiClickListener;
    }

    public void setOnMultiClick(OnRecycleGestureListener<D> clickListener) {
        this.multiClickListener = clickListener;
    }

    public int getViewHolderPosition() {
        return viewHolderPosition;
    }

    public abstract void onBindViewHolder(D d);

    /**
     * 局部刷新，可重写
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
        if (longClickListener != null) {
            longClickListener.onClick(v, viewHolderPosition, data);
            return true;
        } else {
            return false;
        }

    }

    OnGestureViewListener onGestureViewListener;

    public OnGestureViewListener getOnGestureViewListener() {
        if (onGestureViewListener == null) {
            onGestureViewListener = new OnGestureViewListener() {
                @Override
                public void onCancel(View view, MotionEvent event) {
                    if (multiClickListener != null)
                        multiClickListener.onCancel(view, viewHolderPosition, data, event);
                }

                @Override
                public void onSingleClick(View view, MotionEvent event) {
                    if (multiClickListener != null)
                        multiClickListener.onSingleClick(view, viewHolderPosition, data, event);
                }

                @Override
                public void onDoubleClick(View view, MotionEvent event) {
                    if (multiClickListener != null)
                        multiClickListener.onDoubleClick(view, viewHolderPosition, data, event);
                }

                @Override
                public void onPress(View view, MotionEvent event) {
                    if (multiClickListener != null)
                        multiClickListener.onPress(view, viewHolderPosition, data, event);
                }

                @Override
                public void onLongPress(View view, MotionEvent event) {
                    if (multiClickListener != null)
                        multiClickListener.onLongPress(view, viewHolderPosition, data, event);
                }

                @Override
                public void onUp(View view, MotionEvent event) {
                    if (multiClickListener != null)
                        multiClickListener.onUp(view, viewHolderPosition, data, event);
                }

                @Override
                public void onLongClick(View view, MotionEvent event) {
                    if (multiClickListener != null)
                        multiClickListener.onLongClick(view, viewHolderPosition, data, event);
                }

                @Override
                public void onDown(View view, MotionEvent event) {
                    if (multiClickListener != null)
                        multiClickListener.onDown(view, viewHolderPosition, data, event);
                }
            };
        }
        return onGestureViewListener;
    }
}
