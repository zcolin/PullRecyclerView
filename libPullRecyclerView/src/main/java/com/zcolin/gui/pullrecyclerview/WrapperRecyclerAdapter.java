/*
 * *********************************************************
 *   author   colin
 *   company  fosung
 *   email    wanglin2046@126.com
 *   date     16-12-12 下午5:07
 * ********************************************************
 */
package com.zcolin.gui.pullrecyclerview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.zcolin.gui.pullrecyclerview.hfooter.DefLoadMoreFooter;
import com.zcolin.gui.pullrecyclerview.hfooter.DefRefreshHeader;
import com.zcolin.gui.pullrecyclerview.hfooter.ILoadMoreFooter;
import com.zcolin.gui.pullrecyclerview.hfooter.IRefreshHeader;

/**
 * 可以设置Header并且封装了基本方法的的Adapter
 */
class WrapperRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_REFRESH_HEADER  = 100001;
    private static final int TYPE_LOADMORE_FOOTER = 100002;
    public static final  int TYPE_HEADER          = 100003;
    public static final  int TYPE_FOOTER          = 100004;

    private View            headerView;
    private View            footerView;
    private IRefreshHeader  refreshHeader;
    private ILoadMoreFooter loadMoreFooter;
    private boolean isLoadMoreEnabled = true;//设置到底加载是否可用

    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

    public WrapperRecyclerAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        this.adapter = adapter;
        if (adapter == null) {
            throw new IllegalArgumentException("adapter cant be null");
        }
    }

    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }

    public WrapperRecyclerAdapter setHeaderView(View headerView) {
        this.headerView = headerView;
        return this;
    }

    public WrapperRecyclerAdapter setFooterView(View footerView) {
        this.footerView = footerView;
        return this;

    }

    /**
     * 设置自定义的FooterView
     */
    public WrapperRecyclerAdapter setLoadMoreFooter(ILoadMoreFooter loadMoreFooter) {
        this.loadMoreFooter = loadMoreFooter;
        return this;
    }

    /**
     * 设置自定义的header
     */
    public WrapperRecyclerAdapter setRefreshHeader(IRefreshHeader refreshHeader) {
        this.refreshHeader = refreshHeader;
        return this;
    }

    /**
     * 到底加载是否可用
     */
    public WrapperRecyclerAdapter setIsLoadMoreEnabled(boolean enabled) {
        isLoadMoreEnabled = enabled;
        if (!enabled && loadMoreFooter != null) {
            loadMoreFooter.onComplete();
        }
        return this;
    }

    /**
     * 到底加载是否可用
     */
    public WrapperRecyclerAdapter setIsShowNoMore(boolean isShow) {
        if (loadMoreFooter != null) {
            loadMoreFooter.setIsShowNoMore(isShow);
        }
        return this;
    }

    /**
     * 设置下拉刷新的进度条风格
     */
    public WrapperRecyclerAdapter setRefreshProgressStyle(String style) {
        if (refreshHeader != null && refreshHeader instanceof DefRefreshHeader) {
            ((DefRefreshHeader) refreshHeader).setProgressStyle(style);
        }
        return this;
    }

    /**
     * 设置加载更多的进度条风格
     */
    public WrapperRecyclerAdapter setLoadMoreProgressStyle(String style) {
        if (loadMoreFooter != null && refreshHeader instanceof DefRefreshHeader) {
            ((DefLoadMoreFooter) loadMoreFooter).setProgressStyle(style);
        }
        return this;
    }

    /**
     * 设置下拉刷新的箭头图标
     */
    public WrapperRecyclerAdapter setArrowImage(int resId) {
        if (refreshHeader != null && refreshHeader instanceof DefRefreshHeader) {
            ((DefRefreshHeader) refreshHeader).setArrowImageView(resId);
        }
        return this;
    }

    /**
     * 获取在数据集中的真实位置，而不是在Recycle中包含Header和Footer的位置
     */
    public int getRealPosition(int position) {
        return headerView == null ? position - 1 : position - 2;
    }

    @Override
    public int getItemCount() {
        int count = adapter.getItemCount();
        count += isLoadMoreEnabled ? 2 : 1;
        count += headerView == null ? 0 : 1;
        count += footerView == null ? 0 : 1;
        return count;
    }

    /**
     * @return 返回真实条目数据，不包含Header和Footer
     */
    public int getRealItemCount() {
        return adapter.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        int realPosition = getRealPosition(position);
        if (realPosition >= 0 && realPosition < adapter.getItemCount()) {
            return adapter.getItemId(realPosition);
        }
        return -1;
    }

    private boolean isReservedItemType(int itemViewType) {
        return itemViewType == TYPE_REFRESH_HEADER || itemViewType == TYPE_LOADMORE_FOOTER
                || itemViewType == TYPE_HEADER || itemViewType == TYPE_FOOTER;
    }

    @Override
    public int getItemViewType(int position) {
        if (isRefreshHeader(position)) {
            return TYPE_REFRESH_HEADER;
        } else if (isLoadMoreFooter(position)) {
            return TYPE_LOADMORE_FOOTER;
        } else if (isHeaderView(position)) {
            return TYPE_HEADER;
        } else if (isFooterView(position)) {
            return TYPE_FOOTER;
        }

        int realPosition = getRealPosition(position);
        if (realPosition >= 0 && realPosition < adapter.getItemCount()) {
            if (isReservedItemType(adapter.getItemViewType(realPosition))) {
                throw new IllegalStateException("XRecyclerView require itemViewType in adapter should be less than 10000 ");
            }

            return adapter.getItemViewType(realPosition);
        }
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        if (viewType == TYPE_REFRESH_HEADER) {
            return new SimpleViewHolder(refreshHeader.getHeaderView());
        } else if (viewType == TYPE_LOADMORE_FOOTER) {
            return new SimpleViewHolder(loadMoreFooter.getFootView());
        } else if (headerView != null && viewType == TYPE_HEADER) {
            return new SimpleViewHolder(headerView);
        } else if (footerView != null && viewType == TYPE_FOOTER) {
            return new SimpleViewHolder(footerView);
        }

        return adapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        if (isReservedItemType(getItemViewType(position)))
            return;

        int realPosition = getRealPosition(position);
        if (realPosition >= 0 && realPosition < adapter.getItemCount()) {
            adapter.onBindViewHolder(viewHolder, realPosition);
        }
    }


    public boolean isLoadMoreFooter(int position) {
        return isLoadMoreEnabled && position == getItemCount() - 1;
    }


    public boolean isRefreshHeader(int position) {
        return position == 0;
    }

    public boolean isHeaderView(int position) {
        return position == 1 && headerView != null;
    }

    public boolean isFooterView(int position) {
        return footerView != null &&
                ((isLoadMoreEnabled && position == getItemCount() - 2) || (!isLoadMoreEnabled && position == getItemCount() - 1));
    }

    @Override
    public void onAttachedToRecyclerView(android.support.v7.widget.RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return isReservedItemType(position) ? gridManager.getSpanCount() : 1;
                }
            });
        }
        adapter.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(android.support.v7.widget.RecyclerView recyclerView) {
        adapter.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        int position = holder.getLayoutPosition();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams
                && (isRefreshHeader(position) || isLoadMoreFooter(position) || isHeaderView(position) || isFooterView(position))) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }
        adapter.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        adapter.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        adapter.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        return adapter.onFailedToRecycleView(holder);
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        adapter.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        adapter.registerAdapterDataObserver(observer);
    }

    private class SimpleViewHolder extends RecyclerView.ViewHolder {
        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }
}