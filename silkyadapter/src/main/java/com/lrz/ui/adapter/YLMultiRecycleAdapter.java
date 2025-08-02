package com.lrz.ui.adapter;

import android.annotation.SuppressLint;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Author And Date: liurongzhi on 2019/11/26.
 * Description: 封装adapter
 */
public class YLMultiRecycleAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private List dataList;
    private RecyclerView recyclerView;
    private final SparseArray<YLRecycleAdapter<?>> adapters = new SparseArray<>();
    //将itemAdapter顺序存下
    private LinkedList<YLRecycleAdapter<?>> orderAdapter = new LinkedList<>();
    private SparseIntArray itemTypes = new SparseIntArray();
    private ViewAttachedToWindowListener viewAttachedToWindowListener;
    private OnPreLoadListener preLoadListener;
    private SparseArray<IViewHolderCreator> footCreators = new SparseArray<>();
    private SparseArray<IViewHolderCreator> headCreators = new SparseArray<>();
    private int head = -0x100000 + this.hashCode();
    private int foot = -0x000001 + this.hashCode();
    private int preLoadNumber = 4;


    public YLMultiRecycleAdapter itemAdapter(YLRecycleAdapter<?>... itemAdapter) {
        for (YLRecycleAdapter<?> adapter : itemAdapter) {
            adapters.put(adapter.getTypeClass().hashCode(), adapter);
            orderAdapter.addLast(adapter);
            adapter.setMultiAdapter(this);

        }
        return this;
    }

    public <H> YLMultiRecycleAdapter headCreator(IViewHolderCreator<H> headCreator) {
        if (headCreator != null)
            this.headCreators.put(getHeaderCount() + head, headCreator);
        return this;
    }

    public <F> YLMultiRecycleAdapter footCreator(IViewHolderCreator<F> footCreator) {
        if (footCreator != null)
            this.footCreators.put(getFooterCount() + foot, footCreator);
        return this;
    }

    public YLMultiRecycleAdapter viewAttachListener(ViewAttachedToWindowListener<?> listener) {
        this.viewAttachedToWindowListener = listener;
        return this;
    }

    public YLMultiRecycleAdapter preLoadNumber(int preLoadNumber) {
        this.preLoadNumber = preLoadNumber;
        return this;
    }

    public YLMultiRecycleAdapter preLoadListener(OnPreLoadListener onPreLoadListener) {
        this.preLoadListener = onPreLoadListener;
        return this;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseViewHolder holder = null;
        if (headCreators.get(viewType) != null) {
            holder = headCreators.get(viewType).createViewHolder(parent.getContext(), parent, viewType);
        } else if (footCreators.get(viewType) != null) {
            holder = footCreators.get(viewType).createViewHolder(parent.getContext(), parent, viewType);
        } else {
            // 先获取 adapter
            YLRecycleAdapter<?> adapter = adapters.get(itemTypes.get(viewType));
            if (adapter == null) {
                //如果没有注册该类型怎么办
                throw new IllegalArgumentException("this type not found");
            }
            holder = adapter.onCreateViewHolder(parent, viewType);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.viewHolderPosition = position;
        int index = position;
        if (showPreLoad(position)) {
            if (hasMoreData()) {
                preLoadListener.onLoadMore();
            }
        }
        if (isHeaderPosition(position)) {
            holder.onBindViewHolder(null);
        } else if (isFooterPosition(position)) {
            holder.onBindViewHolder(null);
        } else {
            index -= getHeaderCount();
            List list = getDataList();
            if (list != null && index < list.size()) {
                holder.setData(list.get(index));
                holder.onBindViewHolder(list.get(index));
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            holder.viewHolderPosition = position;
            int index = position;
            index -= getHeaderCount();
            List list = getDataList();
            if (list != null && index < list.size()) {
                holder.setData(list.get(index));
                holder.onBindViewHolder(list.get(index), payloads);
            }
        }
    }

    /**
     * 是否在预加载的位置
     *
     * @param position
     * @return
     */
    private boolean showPreLoad(int position) {
        return position > 2 && (position >= (getItemCount() - preLoadNumber));
    }

    private boolean hasMoreData() {
        return preLoadListener != null && preLoadListener.hasMore();
    }

    public YLMultiRecycleAdapter setDataList(List dataList) {
        this.dataList = dataList;
        if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyDataSetChange();
            } else {
                recyclerView.post(() -> notifyDataSetChange());
            }
        }
        return this;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }


    @Override
    public int getItemCount() {
        int count = 0;
        if (headCreators != null) count += getHeaderCount();
        if (footCreators != null) count += getFooterCount();
        List list = getDataList();
        if (list != null) {
            count += list.size();
        }
        return count;
    }

    public int getHeaderCount() {
        return headCreators != null ? headCreators.size() : 0;
    }

    private boolean isHeaderPosition(int position) {
        return position < getHeaderCount();
    }

    public int getFooterCount() {
        return footCreators != null ? footCreators.size() : 0;
    }

    private boolean isFooterPosition(int position) {
        return position >= getHeaderCount() + getDataList().size();
    }

    @Override
    public int getItemViewType(int position) {
        List list = getDataList();
        if (isHeaderPosition(position)) {
            return headCreators.keyAt(position);
        } else if (isFooterPosition(position)) {
            return footCreators.keyAt(position - getHeaderCount() - list.size());
        } else {
            position -= getHeaderCount();
            if (list != null) {
                Object item = list.get(position);
                Class clzz = item.getClass();
                int hash = clzz.hashCode();
                YLRecycleAdapter adapter = adapters.get(hash);
                if (adapter == null) {
                    for (int i = 0; i < adapters.size(); i++) {
                        Class type = adapters.valueAt(i).getTypeClass();
                        if (type.isAssignableFrom(clzz)) {
                            clzz = type;
                            hash = clzz.hashCode();
                            adapter = adapters.get(hash);
                            break;
                        }
                    }
                }
                if (adapter != null) {
                    int itemType = adapter.getItemType(list.get(position), position);
                    itemTypes.put(itemType, hash);
                    return itemType;
                }
            }
        }
        return -1;
    }


    /**
     * 全局刷新
     */
    public final void notifyDataSetChange() {
        if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyDataSetChanged();
            } else {
                recyclerView.post(() -> notifyDataSetChanged());
            }
        }
    }

    /**
     * 通过被改变的itemObj刷新
     *
     * @param itemObj 发生变化的bean
     */
    public final void notifyItemChange(Object itemObj) {
        int position = getIndexOfData(itemObj);
        if (position > -1) {
            notifyItemChange(position);
        }
    }

    public final void notifyItemChange(final int position) {
        if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyItemChanged(position);
            } else {
                recyclerView.post(() -> notifyItemChanged(position));
            }
        }
    }

    /**
     * 通过itemObj 和 payload局部刷新
     */
    public final void notifyItemChange(Object itemObj, @Nullable Object payload) {
        int position = getIndexOfData(itemObj);
        if (position > -1) {
            notifyItemChange(position, payload);
        }
    }

    public final void notifyItemChange(final int position, final @Nullable Object payload) {
        if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyItemChanged(position, payload);
            } else {
                recyclerView.post(() -> notifyItemChanged(position, payload));
            }
        }
    }

    /**
     * 通过起始位置的obj 和数量，批量刷新
     *
     * @param startObj  起始位置的 bean
     * @param itemCount 自起始位置开始一共有多少item发生变化
     */
    public final void notifyItemRangeChange(Object startObj, int itemCount) {
        int position = getIndexOfData(startObj);
        if (position > -1) {
            notifyItemRangeChange(position, itemCount);
        }
    }

    public final void notifyItemRangeChange(final int positionStart, final int itemCount) {
        if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyItemRangeChanged(positionStart, itemCount);
            } else {
                recyclerView.post(() -> notifyItemRangeChanged(positionStart, itemCount));
            }
        }
    }

    /**
     * 通过起始位置的obj 和数量，批量刷新
     *
     * @param startObj  起始位置的 bean
     * @param itemCount 自起始位置开始一共有多少item发生变化
     * @param payload   局部刷新的id等标记
     */
    public final void notifyItemRangeChange(Object startObj, int itemCount, @Nullable Object payload) {
        int position = getIndexOfData(startObj);
        if (position > -1) {
            notifyItemRangeChange(position, itemCount, payload);
        }
    }

    public final void notifyItemRangeChange(final int positionStart, final int itemCount,
                                            final @Nullable Object payload) {
        if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyItemRangeChanged(positionStart, itemCount, payload);
            } else {
                recyclerView.post(() -> notifyItemRangeChanged(positionStart, itemCount, payload));
            }
        }
    }

    /**
     * 插入刷新
     *
     * @param itemObj 被插入的数据
     */
    public final void notifyItemInsert(Object itemObj) {
        int position = getIndexOfData(itemObj);
        if (position > -1) {
            notifyItemInsert(position);
        }
    }

    public final void notifyItemInsert(final int position) {
        if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyItemInserted(position);
                updatePosition(position);
            } else {
                recyclerView.post(() -> {
                    notifyItemInserted(position);
                    updatePosition(position);
                });
            }
        }
    }

    public final void notifyItemMove(final int fromPosition, final int toPosition) {
        if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyItemMoved(fromPosition, toPosition);
                updatePosition(Math.min(fromPosition, toPosition));
            } else {
                recyclerView.post(() -> {
                    notifyItemMoved(fromPosition, toPosition);
                    updatePosition(Math.min(fromPosition, toPosition));
                });
            }
        }
    }

    /**
     * 批量插入刷新
     *
     * @param startObj 被插入的第一个数据
     */
    public final void notifyItemRangeInsert(Object startObj, int count) {
        List list = getDataList();
        int position;
        if ((position = list.indexOf(startObj)) > -1) {
            notifyItemRangeInsert(position, count);
        }
    }

    public final void notifyItemRangeInsert(final int positionStart, final int itemCount) {
        if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyItemRangeInserted(positionStart, itemCount);
                updatePosition(positionStart + itemCount);
            } else {
                recyclerView.post(() -> {
                    notifyItemRangeInserted(positionStart, itemCount);
                    updatePosition(positionStart + itemCount);
                });
            }
        }
    }

    public final void notifyItemRemove(final int position) {
        if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyItemRemoved(position);
                updatePosition(position);
            } else {
                recyclerView.post(() -> {
                    notifyItemRemoved(position);
                    updatePosition(position);
                });
            }
        }
    }

    public final void notifyItemRangeRemove(final int positionStart, final int itemCount) {
        if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyItemRangeRemoved(positionStart, itemCount);
                updatePosition(positionStart);
            } else {
                recyclerView.post(() -> {
                    notifyItemRangeRemoved(positionStart, itemCount);
                    updatePosition(positionStart);
                });
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(BaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        YLRecycleAdapter<?> adapter = findAdapter(holder);
        if (adapter != null) {
            adapter.onViewAttachedToWindow(holder);
        }
        if (viewAttachedToWindowListener != null) {
            viewAttachedToWindowListener.onViewAttachedToWindow(holder);
        }
    }

    @Override
    public void onViewDetachedFromWindow(BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        YLRecycleAdapter<?> adapter = findAdapter(holder);
        if (adapter != null) {
            adapter.onViewDetachedFromWindow(holder);
        }
        if (viewAttachedToWindowListener != null) {
            viewAttachedToWindowListener.onViewDetachedFromWindow(holder);
        }
    }

    private YLRecycleAdapter<?> findAdapter(BaseViewHolder holder) {
        YLRecycleAdapter<?> adapter = adapters.get(holder.getTypeClass().hashCode());
        if (adapter == null) {
            Class clazz = holder.getTypeClass();
            for (int i = 0; i < adapters.size(); i++) {
                Class type = adapters.valueAt(i).getTypeClass();
                if (type.isAssignableFrom(clazz)) {
                    clazz = type;
                    adapter = adapters.get(clazz.hashCode());
                    break;
                }
            }
        }
        return adapter;
    }

    private void updatePosition(int start) {
        while (start > -1 && start < getItemCount()) {
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(start);
            if (holder instanceof BaseViewHolder) {
                ((BaseViewHolder<?>) holder).setViewHolderPosition(holder.getAdapterPosition());
            } else if (holder == null) {
                break;
            }
            start += 1;
        }
    }

    List getDataList() {
        if (dataList == null || dataList.isEmpty()) {
            if (dataList == null) {
                dataList = new ArrayList();
            }
            for (YLRecycleAdapter<?> adapter : orderAdapter) {
                if (adapter.getDataList() != null && !adapter.getDataList().isEmpty())
                    dataList.addAll(adapter.getDataList());
            }
        }
        return dataList;
    }

    public LinkedList<YLRecycleAdapter<?>> getOrderAdapter() {
        return orderAdapter;
    }

    /**
     * 当itemAdapter 数据发生变化，调用此方法清空数据，重新生成
     */
    void clearDataList() {
        if (dataList != null) {
            dataList.clear();
        }
    }

    /**
     * 获取当前bean数据体 在recycleView 中的渲染位置
     *
     * @param obj bean 数据
     * @return
     */
    public int getIndexOfData(Object obj) {
        return getDataList().indexOf(obj);
    }
}
