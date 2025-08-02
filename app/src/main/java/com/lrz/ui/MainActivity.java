package com.lrz.ui;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;

import com.lrz.coroutine.LLog;
import com.lrz.ui.recycle.BaseViewHolder;
import com.lrz.ui.recycle.OnPreLoadListener;
import com.lrz.ui.recycle.QuickViewHolder;
import com.lrz.ui.recycle.ViewAttachedToWindowListener;
import com.lrz.ui.recycle.YLMultiRecycleAdapter;
import com.lrz.ui.recycle.YLRecycleAdapter;

import java.util.ArrayList;

public class MainActivity extends BaseActivity<MainPresenter> {
    RecyclerView recyclerView;
    YLMultiRecycleAdapter multiRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateContentView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.activity_main,null);
    }

    @Override
    public void initView(View viewRoot) {
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 一种数据类型
        ArrayList<String> data1 = new ArrayList<String>() {{
            add("1");
            add("2");
            add("3");
        }};
        YLRecycleAdapter<String> adapter = new YLRecycleAdapter<String>() {
            {
                clickListener((view, position, data) -> {
                    String newD = data1.size() + 1 + "";
                    data1.add(data1.size(), newD);
                    notifyItemInsert(newD);
                });
            }
        }
                // viewhodler 构造器
                .itemCreator((context, parent, type) -> new QuickViewHolder<String>(recyclerView, R.layout.layout_view_holder) {
                    @Override
                    public void onBindViewHolder(String s) {
                        setText(R.id.text1, s);
                    }
                }).longClickListener((view, position, data) -> {

                }) // 长按监听
                .preLoadListener(new OnPreLoadListener() { //加载更多监听
                    @Override
                    public void onLoadMore() {

                    }

                    @Override
                    public boolean hasMore() {
                        return false;
                    }
                }).viewAttachListener(new ViewAttachedToWindowListener<String>() { // viewholder attach监听
                    @Override
                    public void onViewAttachedToWindow(BaseViewHolder holder) {

                    }

                    @Override
                    public void onViewDetachedFromWindow(BaseViewHolder holder) {

                    }
                });

        adapter.setDataList(data1);

        ArrayList<Bean> data2 = new ArrayList<Bean>() {{
            add(new Bean("b-1"));
            add(new Bean("b-2"));
            add(new Bean("b-3"));
            add(new Bean("b-4"));
            add(new Bean("b-5"));

        }};
        YLRecycleAdapter<Bean> adapter2 = new YLRecycleAdapter<Bean>() {
            {
                itemCreator((context, parent, type) -> new QuickViewHolder<Bean>(recyclerView, R.layout.layout_view_holder) {
                    @Override
                    public void onBindViewHolder(Bean bean) {
                        setText(R.id.text1, bean.str);
                    }
                });
                clickListener((view, position, data1) -> {
                    Bean bean = new Bean("b-" + SystemClock.uptimeMillis());
                    data2.add(0, bean);
                    notifyItemInsert(bean);
                });
            }
        };

        adapter2.setDataList(data2);


        // 多种数据类型
        multiRecycleAdapter = new YLMultiRecycleAdapter();
        //可以添加多个adapter；
        multiRecycleAdapter.itemAdapter(adapter2, adapter);
        LLog.e("adapter", "setAdapter");
        recyclerView.setAdapter(multiRecycleAdapter);
        multiRecycleAdapter.notifyDataSetChange();
    }

    public static class Bean {
        public final String str;

        public Bean(String str) {
            this.str = str;
        }
    }
}