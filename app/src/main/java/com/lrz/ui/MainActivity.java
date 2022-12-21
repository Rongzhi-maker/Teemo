package com.lrz.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.lrz.ui.adapter.BaseViewHolder;
import com.lrz.ui.adapter.IViewHolderCreator;
import com.lrz.ui.adapter.OnPreLoadListener;
import com.lrz.ui.adapter.QuickViewHolder;
import com.lrz.ui.adapter.ViewAttachedToWindowListener;
import com.lrz.ui.adapter.YLMultiRecycleAdapter;
import com.lrz.ui.adapter.YLRecycleAdapter;
import com.lrz.ui.inter.OnItemClickListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    YLMultiRecycleAdapter multiRecycleAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 一种数据类型
        YLRecycleAdapter<String> adapter = new YLRecycleAdapter<String>() {
        }
                // viewhodler 构造器
                .itemCreator((context, parent, type) -> new QuickViewHolder<String>(recyclerView, R.layout.layout_view_holder) {
                    @Override
                    public void onBindViewHolder(String s) {
                        setText(R.id.text1, s);
                    }
                })
                .clickListener((view, position, data) -> { //点击监听


                }).longClickListener((view, position, data) -> false) // 长按监听
                .preLoadListener(new OnPreLoadListener() { //加载更多监听
                    @Override
                    public void onLoadMore() {

                    }

                    @Override
                    public boolean hasMore() {
                        return false;
                    }
                }).viewAttachListener(new ViewAttachedToWindowListener<BaseViewHolder>() { // viewholder attach监听
                    @Override
                    public void onViewAttachedToWindow(BaseViewHolder holder) {

                    }

                    @Override
                    public void onViewDetachedFromWindow(BaseViewHolder holder) {

                    }
                });

        ArrayList<String> data = new ArrayList<String>() {{
            add("1");
            add("2");
            add("3");
        }};
        adapter.setDataList(data);

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
                    Bean bean = new Bean("b-"+ SystemClock.uptimeMillis());
                    data2.add(0,bean);
                    notifyItemInsert(bean);
                });
                headCreator(new IViewHolderCreator<Object>() {
                    @Override
                    public BaseViewHolder<Object> createViewHolder(Context context, ViewGroup parent, int type) {
                        return new QuickViewHolder<Object>(recyclerView, R.layout.layout_view_holder) {
                            @Override
                            public void onBindViewHolder(Object o) {
                                setText(R.id.text1, "i am head");
                            }
                        };
                    }
                });
                footCreator(new IViewHolderCreator<Object>() {
                    @Override
                    public BaseViewHolder<Object> createViewHolder(Context context, ViewGroup parent, int type) {
                        return new QuickViewHolder<Object>(recyclerView, R.layout.layout_view_holder) {
                            @Override
                            public void onBindViewHolder(Object o) {
                                setText(R.id.text1, "i am head");
                            }
                        };
                    }
                });
            }
        };

        adapter2.setDataList(data2);


        // 多种数据类型
        multiRecycleAdapter = new YLMultiRecycleAdapter();
        //可以添加多个adapter；
        multiRecycleAdapter.itemAdapter(adapter2,adapter);
        recyclerView.setAdapter(multiRecycleAdapter);
    }

    public static class Bean {
        public final String str;

        public Bean(String str) {
            this.str = str;
        }
    }
}