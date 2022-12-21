package com.lrz.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * Author:  liurongzhi
 * CreateTime:  2022/12/20
 * Description:快速生成简单的viewHolder
 */
public abstract class QuickViewHolder<D> extends BaseViewHolder<D> {
    private SparseArray<View> views = new SparseArray<>();

    /**
     * 构造函数
     *
     * @param parent   parent view  RecycleView
     * @param layoutID 布局id
     */
    public QuickViewHolder(@NonNull ViewGroup parent, @LayoutRes int layoutID) {
        super(parent, layoutID);
    }

    @Override
    protected void initView() {
    }


    public void setText(@IdRes int viewId, CharSequence charSequence) {
        TextView textView = getView(viewId);
        textView.setText(charSequence);
    }

    public void setText(@IdRes int viewId, @StringRes int strRes) {
        TextView textView = getView(viewId);
        textView.setText(strRes);
    }

    public void setImageResource(@IdRes int viewId, @DrawableRes int strRes) {
        ImageView imageView = getView(viewId);
        imageView.setImageResource(strRes);
    }

    public void setImageDrawable(@IdRes int viewId, Drawable strRes) {
        ImageView imageView = getView(viewId);
        imageView.setImageDrawable(strRes);
    }

    public void setImageBitmap(@IdRes int viewId, Bitmap bitmap) {
        ImageView imageView = getView(viewId);
        imageView.setImageBitmap(bitmap);
    }

    protected <V extends View> V getView(@IdRes int viewId) {
        View view = views.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            views.put(viewId, view);
        }
        return (V) view;
    }


}
