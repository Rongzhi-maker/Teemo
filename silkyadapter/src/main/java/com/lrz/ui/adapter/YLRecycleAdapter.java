package com.lrz.ui.adapter;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.lrz.ui.inter.OnItemClickListener;
import com.lrz.ui.inter.OnItemLongClickListener;
import com.lrz.ui.inter.OnItemMultiClickListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

/**
 * Author And Date: liurongzhi on 2019/11/26.
 * Description: adapter 基类
 */
public abstract class YLRecycleAdapter<D> extends RecyclerView.Adapter<BaseViewHolder<D>> {
    private List<D> dataList;
    private RecyclerView recyclerView;
    private ViewAttachedToWindowListener viewAttachedToWindowListener;
    private IViewHolderCreator headCreator;
    private IViewHolderCreator<D> itemCreator;
    private IViewHolderCreator footCreator;
    private IRecycleViewItemType<D> itemType;
    private OnItemClickListener<D> clickListener;
    private OnItemLongClickListener<D> longClickListener;
    private OnItemMultiClickListener<D> multiClickListener;
    private OnPreLoadListener preLoadListener;
    private final int head = -0x000001 + this.hashCode();
    private final int normal = 0x000001 + this.hashCode();
    private final int foot = -0x000003 + this.hashCode();
    private int preLoadNumber = 4;
    private YLMultiRecycleAdapter multiRecycleAdapter;

    public Class<?> getTypeClass() {
        Type superClass = getClass().getGenericSuperclass();
        Type type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        Class<?> bClass;
        if (type instanceof ParameterizedType) {
            bClass = (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            bClass = (Class<?>) type;
        }
        return bClass;
    }

    public <H> YLRecycleAdapter<D> headCreator(IViewHolderCreator<H> headCreator) {
        this.headCreator = headCreator;
        return this;
    }

    public YLRecycleAdapter<D> itemCreator(IViewHolderCreator<D> itemCreator) {
        this.itemCreator = itemCreator;
        return this;
    }

    public <F> YLRecycleAdapter<D> footCreator(IViewHolderCreator<F> footCreator) {
        this.footCreator = footCreator;
        return this;
    }

    public YLRecycleAdapter<D> itemType(IRecycleViewItemType<D> itemType) {
        this.itemType = itemType;
        return this;
    }

    public YLRecycleAdapter<D> preLoadNumber(int preLoadNumber) {
        this.preLoadNumber = preLoadNumber;
        return this;
    }

    public YLRecycleAdapter<D> preLoadListener(OnPreLoadListener onPreLoadListener) {
        this.preLoadListener = onPreLoadListener;
        return this;
    }

    public YLRecycleAdapter<D> clickListener(OnItemClickListener<D> clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public YLRecycleAdapter<D> longClickListener(OnItemLongClickListener<D> longClickListener) {
        this.longClickListener = longClickListener;
        return this;
    }

    public YLRecycleAdapter<D> multiClickListener(OnItemMultiClickListener<D> clickListener) {
        this.multiClickListener = clickListener;
        return this;
    }

    public YLRecycleAdapter<D> viewAttachListener(ViewAttachedToWindowListener<? extends BaseViewHolder> listener) {
        this.viewAttachedToWindowListener = listener;
        return this;
    }


    @NonNull
    @Override
    public BaseViewHolder<D> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (headCreator == null && itemCreator == null && footCreator == null) {
            throw new IllegalArgumentException("所有item构造器不可以都是null");
        }
        BaseViewHolder<D> holder = null;
        if (viewType == head && headCreator != null) {
            holder = headCreator.createViewHolder(parent.getContext(), parent, viewType);
        } else if (viewType == foot && footCreator != null) {
            holder = footCreator.createViewHolder(parent.getContext(), parent, viewType);
        } else if (itemCreator != null) {
            holder = itemCreator.createViewHolder(parent.getContext(), parent, viewType);
        }
        if (holder == null) {
            throw new IllegalArgumentException();
        }
        if (clickListener != null) {
            holder.setOnClick(clickListener);
        }

        if (longClickListener != null) {
            holder.setOnLongClickListener(longClickListener);
        }

        if (multiClickListener != null) {
            holder.setOnMultiClick(multiClickListener);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<D> holder, @SuppressLint("RecyclerView") int position) {
        if (showPreLoad(position)) {
            if (hasMoreData()) {
                preLoadListener.onLoadMore();
            }
        }
        holder.viewHolderPosition = position;
        int index = position;
        if (getItemViewType(position) == head && headCreator != null) {
            holder.onBindViewHolder(null);
        } else if (getItemViewType(position) == foot && footCreator != null) {
            holder.onBindViewHolder(null);
        } else {
            if (headCreator != null) {
                index -= 1;
            }
            if (dataList != null && index < dataList.size()) {
                holder.setData(dataList.get(index));
                holder.onBindViewHolder(dataList.get(index));
            }
        }
    }

    private boolean hasMoreData() {
        return preLoadListener != null && preLoadListener.hasMore();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<D> holder, @SuppressLint("RecyclerView") int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            holder.viewHolderPosition = position;
            int index = position;
            if (headCreator != null) {
                index -= 1;
            }
            if (dataList != null && index < dataList.size()) {
                holder.setData(dataList.get(index));
                holder.onBindViewHolder(dataList.get(index), payloads);
            }
        }
    }

    public YLRecycleAdapter<D> setDataList(List<D> dataList) {
        this.dataList = dataList;
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.clearDataList();
        }
        if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyDataSetChange();
            } else {
                recyclerView.post(() -> notifyDataSetChange());
            }
        }
        return this;
    }

    public List<D> getDataList() {
        return dataList;
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
        if (headCreator != null) count += 1;
        if (footCreator != null) count += 1;
        if (dataList != null) {
            count += dataList.size();
        }
        return count;
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

    @Override
    public int getItemViewType(int position) {
        int max = 0;
        if (dataList != null) max = dataList.size();
        if (position == 0 && headCreator != null) return head;
        else if (footCreator != null && position == max + (headCreator == null ? 0 : 1))
            return foot;
        else {
            int dataPosition = position;
            if (headCreator != null) dataPosition -= 1;

            if (dataList != null) {
                return getItemType(dataList.get(dataPosition), dataPosition);
            }
            return normal;
        }
    }

    int getItemType(D data, int dataPosition) {
        if (itemType != null) {
            return itemType.getItemTypeForDataPosition(data, dataPosition);
        }
        return normal;
    }

    /**
     * 全局刷新
     */
    public final void notifyDataSetChange() {
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.notifyDataSetChanged();
        } else if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyDataSetChanged();
            } else {
                recyclerView.post(() -> notifyDataSetChanged());
            }
        }
    }

    /**
     * 更新数据
     *
     * @param itemObj 数据
     */
    public final void notifyItemChange(Object itemObj) {
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.clearDataList();
            multiRecycleAdapter.notifyItemChange(itemObj);
        } else {
            int position = getIndexOfData(itemObj);
            if (position > -1) {
                notifyItemChange(position);
            }
        }
    }

    public final void notifyItemChange(final int position) {
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.notifyItemChange(getIndexOfData(getDataList().get(position)));
        } else if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyItemChanged(position);
            } else {
                recyclerView.post(() -> notifyItemChanged(position));
            }
        }
    }


    /**
     * 更新刷新
     *
     * @param itemObj 数据
     */
    public final void notifyItemChange(Object itemObj, @Nullable Object payload) {
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.clearDataList();
            multiRecycleAdapter.notifyItemChange(itemObj, payload);
        } else {
            int position = getIndexOfData(itemObj);
            if (position > -1) {
                notifyItemChange(position, payload);
            }
        }
    }

    public final void notifyItemChange(final int position, final @Nullable Object payload) {
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.notifyItemChange(getIndexOfData(getDataList().get(position)), payload);
        } else if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyItemChanged(position, payload);
            } else {
                recyclerView.post(() -> notifyItemChanged(position, payload));
            }
        }
    }

    /**
     * 更新刷新
     *
     * @param startObj 数据
     */
    public final void notifyItemRangeChange(Object startObj, int itemCount) {
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.clearDataList();
            multiRecycleAdapter.notifyItemRangeChange(startObj, itemCount);
        } else {
            int position = getIndexOfData(startObj);
            if (position > -1) {
                notifyItemRangeChange(position, itemCount);
            }
        }
    }

    public final void notifyItemRangeChange(final int positionStart, final int itemCount) {
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.notifyItemRangeChange(getIndexOfData(getDataList().get(positionStart)), itemCount);
        } else if (recyclerView != null) {
            if (!recyclerView.isComputingLayout()) {
                notifyItemRangeChanged(positionStart, itemCount);
            } else {
                recyclerView.post(() -> notifyItemRangeChanged(positionStart, itemCount));
            }
        }
    }

    /**
     * 更新刷新
     *
     * @param startObj 数据
     */
    public final void notifyItemRangeChange(Object startObj, int itemCount, @Nullable Object payload) {
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.clearDataList();
            multiRecycleAdapter.notifyItemRangeChange(startObj, itemCount, payload);
        } else {
            int position = getIndexOfData(startObj);
            if (position > -1) {
                notifyItemRangeChange(position, itemCount, payload);
            }
        }
    }

    public final void notifyItemRangeChange(final int positionStart, final int itemCount,
                                            final @Nullable Object payload) {
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.notifyItemRangeChange(getIndexOfData(getDataList().get(positionStart)), itemCount, payload);
        } else if (recyclerView != null) {
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
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.clearDataList();
            multiRecycleAdapter.notifyItemInsert(itemObj);
        } else {
            int position = getIndexOfData(itemObj);
            if (position > -1) {
                notifyItemInsert(position);
            }
        }
    }

    public final void notifyItemInsert(final int position) {
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.notifyItemInsert(getIndexOfData(getDataList().get(position)));
        } else if (recyclerView != null) {
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
        if (multiRecycleAdapter != null) {
            int offset = getDataListOffset();
            multiRecycleAdapter.notifyItemMove(fromPosition + offset, toPosition + offset);
        } else if (recyclerView != null) {
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
     * 插入刷新
     *
     * @param startObj 被插入的数据
     */
    public final void notifyItemRangeInsert(Object startObj, int count) {
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.clearDataList();
            multiRecycleAdapter.notifyItemRangeInsert(startObj, count);
        } else {
            int position = getIndexOfData(startObj);
            if (position > -1) {
                notifyItemRangeInsert(position, count);
            }
        }
    }

    public final void notifyItemRangeInsert(final int positionStart, final int itemCount) {
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.notifyItemRangeInsert(getIndexOfData(getDataList().get(positionStart)), itemCount);
        } else if (recyclerView != null) {
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
        if (multiRecycleAdapter != null) {
            multiRecycleAdapter.notifyItemRemove(position + getDataListOffset());
        } else if (recyclerView != null) {
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
        if (multiRecycleAdapter != null) {
            int offset = getDataListOffset();
            multiRecycleAdapter.notifyItemRangeRemove(positionStart + offset, itemCount);
        } else if (recyclerView != null) {
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
        holder.onViewAttachedToWindow();
        if (viewAttachedToWindowListener != null) {
            viewAttachedToWindowListener.onViewAttachedToWindow(holder);
        }
    }

    @Override
    public void onViewDetachedFromWindow(BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onViewDetachedFromWindow();
        if (viewAttachedToWindowListener != null) {
            viewAttachedToWindowListener.onViewDetachedFromWindow(holder);
        }
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

    void setMultiAdapter(YLMultiRecycleAdapter ylMultiRecycleAdapter) {
        this.multiRecycleAdapter = ylMultiRecycleAdapter;
    }

    /**
     * 获取当前bean数据体 在recycleView 中的渲染位置
     *
     * @param obj bean 数据
     * @return
     */
    public int getIndexOfData(Object obj) {
        if (multiRecycleAdapter != null) {
            return multiRecycleAdapter.getIndexOfData(obj);
        }
        List list = getDataList();
        if (list == null) return -1;
        return getDataList().indexOf(obj);
    }

    int getDataListOffset() {
        int offset = 0;
        if (multiRecycleAdapter != null) {
            LinkedList<YLRecycleAdapter<?>> orderAdapter = multiRecycleAdapter.getOrderAdapter();
            for (YLRecycleAdapter<?> adapter : orderAdapter) {
                if (adapter == this) {
                    break;
                } else if (adapter.getDataList() != null) {
                    offset += adapter.getDataList().size();
                }
            }
        }
        return offset;
    }
}
