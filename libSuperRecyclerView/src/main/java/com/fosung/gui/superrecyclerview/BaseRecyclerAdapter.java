/*
 * *********************************************************
 *   author   colin
 *   company  fosung
 *   email    wanglin2046@126.com
 *   date     16-12-8 下午5:18
 * ********************************************************
 */
package com.fosung.gui.superrecyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fosung.gui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 可以设置Header并且封装了基本方法的的Adapter
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseRecyclerAdapter.CommonHolder> {
    public static final int TYPE_NORMAL        = 100;
    public static final int TYPE_HEADER        = 101;
    public static final int TYPE_FOOTER        = 102;
    public static final int TYPE_NOMORE_FOOTER = 103;

    private ArrayList<T> listData = new ArrayList<>();
    private View headerView;
    private View footerView;
    private View noMoreView;
    private boolean isNoMoreVisible = false;
    private boolean isShowNoMore = true;
    private OnItemClickListener<T> itemClickListener;

    public void setOnItemClickListener(OnItemClickListener<T> li) {
        itemClickListener = li;
    }

    public View getHeaderView() {
        return headerView;
    }

    public View getFooterView() {
        return footerView;
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
        notifyItemInserted(0);
    }

    public void setHeaderView(Context context, int headerViewLayoutId) {
        this.headerView = LayoutInflater.from(context)
                                        .inflate(headerViewLayoutId, null);
        notifyItemInserted(0);
    }

    public void setFooterView(View footerView) {
        this.footerView = footerView;
        this.notifyDataSetChanged();
    }

    public void setFooterView(Context context, int footerViewLayoutId) {
        this.headerView = LayoutInflater.from(context)
                                        .inflate(footerViewLayoutId, null);
        this.notifyDataSetChanged();
    }

    /**
     * 设置“已加载全部”为其他文字
     */
    public void setNoMoreText(Context context, String text) {
        if (isShowNoMore) {
            if (noMoreView == null) {
                this.noMoreView = LayoutInflater.from(context)
                                                .inflate(R.layout.gui_view_superrecycler_nomore, null);
                this.notifyDataSetChanged();
            }

            if (noMoreView instanceof TextView) {
                ((TextView) noMoreView).setText(text);
            }
        }
    }

    /**
     * 设置“已加载全部”为的字体颜色
     */
    public void setNoMoreTextColor(Context context, int color) {
        if (isShowNoMore) {
            if (noMoreView == null) {
                this.noMoreView = LayoutInflater.from(context)
                                                .inflate(R.layout.gui_view_superrecycler_nomore, null);
                this.notifyDataSetChanged();
            }

            if (noMoreView instanceof TextView) {
                ((TextView) noMoreView).setTextColor(color);
            }
        }
    }

    public void setNoMoreVisibility(Context context, boolean isVisible) {
        if (isShowNoMore && getRealItemCount() > 0) {
            if (noMoreView == null) {
                this.noMoreView = LayoutInflater.from(context)
                                                .inflate(R.layout.gui_view_superrecycler_nomore, null);
                this.notifyDataSetChanged();
            }
            noMoreView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            isNoMoreVisible = isVisible;
        }
    }

    public void setNoMoreView(@NonNull View view) {
        this.noMoreView = view;
    }

    public void setIsShowNoMore(boolean isShowNoMore) {
        this.isShowNoMore = isShowNoMore;
    }

    /**
     * 追加条目数据
     */
    public void addDatas(ArrayList<T> datas) {
        if (datas != null) {
            listData.addAll(datas);
        }
        notifyDataSetChanged();
    }

    /**
     * 将数据替换为传入的数据集
     */
    public void setDatas(List<T> datas) {
        listData.clear();
        if (datas != null) {
            listData.addAll(datas);
        }
        notifyDataSetChanged();
    }

    /**
     * 追加单条数据
     */
    public void addData(T data) {
        listData.add(data);
        notifyDataSetChanged();
    }

    /**
     * 清空数据集
     */
    public void clearDatas() {
        listData.clear();
        notifyDataSetChanged();
    }

    public T getItem(int position) {
        return listData.get(getRealPosition(position));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && headerView != null) {
            return TYPE_HEADER;
        } else if (((isShowNoMore && position == getItemCount() - 2) || (!isShowNoMore && position == getItemCount() - 1))
                && footerView != null) {
            return TYPE_FOOTER;
        } else if (position == getItemCount() - 1 && isShowNoMore) {
            return TYPE_NOMORE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    public int getRealPosition(int position) {
        return headerView == null ? position : position - 1;
    }

    @Override
    public int getItemCount() {
        int count = listData.size();
        count += headerView == null ? 0 : 1;
        count += footerView == null ? 0 : 1;
        count += isShowNoMore ? 1 : 0;
        return count;
    }

    /**
     * @return 返回真实条目数据，不包含Header和Footer
     */
    public int getRealItemCount() {
        return listData.size();
    }

    @Override
    public CommonHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        if (headerView != null && viewType == TYPE_HEADER) {
            return new CommonHolder((RecyclerView) parent, headerView);
        } else if (footerView != null && viewType == TYPE_FOOTER) {
            return new CommonHolder((RecyclerView) parent, footerView);
        } else if (viewType == TYPE_NOMORE_FOOTER) {
            if (noMoreView == null) {
                this.noMoreView = LayoutInflater.from(parent.getContext())
                                                .inflate(R.layout.gui_view_superrecycler_nomore, null);
            }
            noMoreView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            noMoreView.setVisibility(isShowNoMore && isNoMoreVisible ? View.VISIBLE : View.GONE);
            return new CommonHolder((RecyclerView) parent, noMoreView);
        }

        View v = LayoutInflater.from(parent.getContext())
                               .inflate(getItemLayoutId(viewType), parent, false);
        return new CommonHolder((RecyclerView) parent, v);
    }

    @Override
    public void onBindViewHolder(final CommonHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_HEADER) {
            return;
        } else if (viewType == TYPE_FOOTER) {
            return;
        } else if (viewType == TYPE_NOMORE_FOOTER) {
            return;
        }

        final int pos = getRealPosition(position);
        final T data = listData.get(pos);
        if (itemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(viewHolder.itemView, pos, data);
                }
            });
        }

        setUpData(viewHolder, pos, viewType, data);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    return (type == TYPE_HEADER || type == TYPE_FOOTER) ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(CommonHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams && ((holder.getLayoutPosition() == 0 && headerView != null) ||
                (holder.getLayoutPosition() == getItemCount() - 1 && footerView != null))) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }
    }

    /**
     * 获取布局ID
     *
     * @return 布局Id,  ex:R.layout.listitem_***
     */
    public abstract int getItemLayoutId(int viewType);

    /**
     * 设置显示数据,替代getView，在此函数中进行赋值操作
     * <p>
     * ex:
     * TextView tvNumb = get(view, R.id.tv);
     * tvNumb.setText(String.valueOf(position + 1));
     */
    public abstract void setUpData(CommonHolder holder, int position, int viewType, T data);

    /**
     * @return 返回<E extends View>
     */
    protected <E extends View> E getView(CommonHolder holder, int id) {
        SparseArray<View> spHolder = holder.spHolder;
        View childView = spHolder.get(id);
        if (null == childView) {
            childView = holder.itemView.findViewById(id);
            spHolder.put(id, childView);
        }
        return (E) childView;
    }

    /**
     * @return 返回<E extends View>
     *
     * @deprecated use {@link #getView(CommonHolder, int)}
     */
    protected <E extends View> E get(CommonHolder holder, int id) {
        return getView(holder, id);
    }

    private class HeaderHolder extends RecyclerView.ViewHolder {
        public HeaderHolder(View itemView) {
            super(itemView);
        }
    }

    public static class CommonHolder extends RecyclerView.ViewHolder {
        public SparseArray<View> spHolder = new SparseArray<>();
        public RecyclerView viewParent;

        public CommonHolder(RecyclerView viewParent, View itemView) {
            super(itemView);
            this.viewParent = viewParent;
        }
    }

    public interface OnItemClickListener<T> {
        void onItemClick(View covertView, int position, T data);
    }
}