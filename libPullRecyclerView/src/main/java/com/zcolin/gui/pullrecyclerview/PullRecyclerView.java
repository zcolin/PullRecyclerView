/*
 * **********************************************************
 *   author   colin
 *   company  fosung
 *   email    wanglin2046@126.com
 *   date     16-10-11 下午5:31
 * *********************************************************
 */

package com.zcolin.gui.pullrecyclerview;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.zcolin.gui.pullrecyclerview.hfooter.DefLoadMoreFooter;
import com.zcolin.gui.pullrecyclerview.hfooter.DefRefreshHeader;
import com.zcolin.gui.pullrecyclerview.hfooter.ILoadMoreFooter;
import com.zcolin.gui.pullrecyclerview.hfooter.IRefreshHeader;


/**
 * 下拉刷新_到底加载 组件
 * <p/>
 * 可以传入{@link android.support.v7.widget.RecyclerView.Adapter}及其子类，使用装饰者模式将用户传入的apapter进行包装，
 * 所以用户的adapter可以保持原有样式的操作
 */
public class PullRecyclerView extends android.support.v7.widget.RecyclerView {
    private WrapperRecyclerAdapter                mWrapAdapter;
    private PullRecyclerView.PullLoadMoreListener mLoadingListener;
    private RelativeLayout                        mEmptyViewContainer;
    private View                                  headerView;

    private View            footerView;
    private IRefreshHeader  refreshHeader;
    private ILoadMoreFooter loadMoreFooter;
    private boolean isShowNoMore      = true;   //是否显示 加载全部
    private boolean isRefreshEnabled  = true;    //设置下拉刷新是否可用
    private boolean isLoadMoreEnabled = true;    //设置到底加载是否可用
    private float   dragRate          = 2;//下拉刷新滑动阻力系数，越大需要手指下拉的距离越大才能刷新
    private BaseRecyclerAdapter.OnItemClickListener itemClickListener;

    private boolean isNoMore      = false;   //是否已没有更多
    private boolean isLoadingData = false;   //是否正在加载数据
    private boolean isRefreshing;//是否正在刷新
    private float mLastY = -1;      //上次触摸的的Y值
    private float sumOffSet;

    private final AdapterDataObserver             mEmptyDataObserver = new DataObserver();
    private       AppBarStateChangeListener.State appbarState        = AppBarStateChangeListener.State.EXPANDED;
    private boolean hasRegisterEmptyObserver;

    public PullRecyclerView(Context context) {
        this(context, null);
    }

    public PullRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (isRefreshEnabled) {
            refreshHeader = new DefRefreshHeader(getContext());
        }

        if (isLoadMoreEnabled) {
            loadMoreFooter = new DefLoadMoreFooter(getContext());
        }

        setLinearLayout(false);
    }

    /**
     * 设置下拉刷新上拉加载回调
     */
    public void setOnPullLoadMoreListener(PullLoadMoreListener listener) {
        mLoadingListener = listener;
    }

    /**
     * 此处设置OnItemClickListener是调用的{@link BaseRecyclerAdapter#setOnItemClickListener(BaseRecyclerAdapter.OnItemClickListener)}，
     * 此处的泛型类型必须和{@link BaseRecyclerAdapter}的相同
     */
    public <T> void setOnItemClickListener(BaseRecyclerAdapter.OnItemClickListener<T> li) {
        itemClickListener = li;
        if (mWrapAdapter != null) {
            if (mWrapAdapter.getAdapter() instanceof BaseRecyclerAdapter) {
                ((BaseRecyclerAdapter) mWrapAdapter.getAdapter()).setOnItemClickListener(li);
            } else {
                throw new IllegalArgumentException("adapter 必须继承BaseRecyclerAdapter 才能使用setOnItemClickListener");
            }
        }
    }

    /**
     * LinearLayoutManager
     *
     * @param isForce 如果已经设置了，是否强制设置
     */
    public void setLinearLayout(boolean isForce) {
        if (isForce || getLayoutManager() != null || !(getLayoutManager() instanceof LinearLayoutManager)) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            setLayoutManager(linearLayoutManager);
        }
    }

    /**
     * GridLayoutManager
     *
     * @param isForce 如果已经设置了，是否强制设置
     */
    public void setGridLayout(boolean isForce, int spanCount) {
        if (isForce || getLayoutManager() != null || !(getLayoutManager() instanceof GridLayoutManager)) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount);
            gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            setLayoutManager(gridLayoutManager);
        }
    }

    /**
     * StaggeredGridLayoutManager
     *
     * @param isForce 如果已经设置了，是否强制设置
     */
    public void setStaggeredGridLayout(boolean isForce, int spanCount) {
        if (isForce || getLayoutManager() != null || !(getLayoutManager() instanceof StaggeredGridLayoutManager)) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(spanCount, LinearLayoutManager.VERTICAL);
            setLayoutManager(staggeredGridLayoutManager);
        }
    }

    public View getRefreshHeaderView() {
        return refreshHeader.getHeaderView();
    }

    public View getLoadMoreFooterView() {
        return loadMoreFooter.getFootView();
    }

    public View getHeaderView() {
        return headerView;
    }

    public View getFooterView() {
        return footerView;
    }

    public PullRecyclerView setHeaderView(View headerView) {
        this.headerView = headerView;
        return this;
    }

    public PullRecyclerView setHeaderView(Context context, int headerViewLayoutId) {
        this.headerView = LayoutInflater.from(context)
                                        .inflate(headerViewLayoutId, null);
        return this;
    }

    public PullRecyclerView setFooterView(View footerView) {
        this.footerView = footerView;
        return this;

    }

    public PullRecyclerView setFooterView(Context context, int footerViewLayoutId) {
        this.footerView = LayoutInflater.from(context)
                                        .inflate(footerViewLayoutId, null);
        return this;
    }

    /**
     * 设置自定义的FooterView
     */
    public PullRecyclerView setLoadMoreFooter(ILoadMoreFooter loadMoreFooter) {
        this.loadMoreFooter = loadMoreFooter;
        return this;
    }

    /**
     * 设置自定义的header
     */
    public PullRecyclerView setRefreshHeader(IRefreshHeader refreshHeader) {
        this.refreshHeader = refreshHeader;
        return this;
    }

    /**
     * 下拉刷新是否可用
     */
    public PullRecyclerView setIsRefreshEnabled(boolean enabled) {
        isRefreshEnabled = enabled;
        return this;
    }

    /**
     * 到底加载是否可用
     */
    public PullRecyclerView setIsLoadMoreEnabled(boolean enabled) {
        isLoadMoreEnabled = enabled;
        if (!enabled && loadMoreFooter != null) {
            loadMoreFooter.onReset();
        }
        return this;
    }

    /**
     * 设置是否显示NoMoreView
     */
    public PullRecyclerView setIsShowNoMore(boolean isShowNoMore) {
        this.isShowNoMore = isShowNoMore;
        return this;
    }

    /**
     * 下拉刷新滑动阻力系数，越大需要手指下拉的距离越大才能刷新
     */
    public PullRecyclerView setDragRate(int dragRate) {
        this.dragRate = dragRate;
        return this;
    }

    /**
     * 设置下拉刷新的进度条风格
     */
    public PullRecyclerView setRefreshProgressStyle(String style) {
        if (refreshHeader != null && refreshHeader instanceof DefRefreshHeader) {
            ((DefRefreshHeader) refreshHeader).setProgressStyle(style);
        }
        return this;
    }


    /**
     * 设置加载更多的进度条风格
     */
    public PullRecyclerView setLoadMoreProgressStyle(String style) {
        if (loadMoreFooter != null && loadMoreFooter instanceof DefLoadMoreFooter) {
            ((DefLoadMoreFooter) loadMoreFooter).setProgressStyle(style);
        }
        return this;
    }

    /**
     * 设置加载更多的进度条风格
     */
    public PullRecyclerView setRefreshHeaderText(String str1, String str2, String str3, String str4) {
        if (refreshHeader != null && refreshHeader instanceof DefRefreshHeader) {
            ((DefRefreshHeader) refreshHeader).setInfoText(str1, str2, str3, str4);
        }
        return this;
    }

    /**
     * 设置下拉刷新的箭头图标
     */
    public PullRecyclerView setArrowImage(int resId) {
        if (refreshHeader != null && refreshHeader instanceof DefRefreshHeader) {
            ((DefRefreshHeader) refreshHeader).setArrowImageView(resId);
        }
        return this;
    }

    /**
     * 设置没有数据的EmptyView
     */
    public PullRecyclerView setEmptyView(Context context, int layoutId) {
        setEmptyView(LayoutInflater.from(context)
                                   .inflate(layoutId, null));
        return this;
    }

    /**
     * 设置没有数据的EmptyView
     * <p/>
     * <p>注意：如果调用此函数，会将RecyclerView从原来的布局中移除添加到一个RelativeLayout中，然后将RelativeLayout放置到原来的布局中，
     * 也就是说，在RecyclerView和其父布局中间添加了一层RelitiveLayout，用来盛放RecyclerView和emptyView<p/>
     */
    public void setEmptyView(View emptyView) {
        ViewGroup group = (ViewGroup) getParent();
        RelativeLayout container = new RelativeLayout(getContext());
        int index = group.indexOfChild(this);
        group.removeView(this);
        group.addView(container, index, getLayoutParams());
        container.addView(this, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        RelativeLayout.LayoutParams emptyViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        emptyViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mEmptyViewContainer = new RelativeLayout(getContext());
        mEmptyViewContainer.addView(emptyView, emptyViewParams);
        container.addView(mEmptyViewContainer, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        this.mEmptyViewContainer.setVisibility(View.GONE);
    }

    /**
     * 返回的放置emptyView的RelativeLayout
     */
    public View getEmptyViewContainer() {
        return mEmptyViewContainer;
    }

    /**
     * 返回的放置emptyView的RelativeLayout
     */
    public View getEmptyView() {
        return mEmptyViewContainer.getChildCount() > 0 ? mEmptyViewContainer.getChildAt(0) : null;
    }

    /**
     * 使RecyclerView滚动到顶部
     */
    public void scrollToTop() {
        scrollToPosition(0);
    }

    /**
     * 使RecyclerView滚动到顶部
     */
    public void scrollToBottom() {
        scrollToPosition(getHeight());
    }

    /**
     * 手动调用直接刷新，无下拉效果
     */
    public void refresh() {
        if (mLoadingListener != null) {
            isRefreshing = true;
            mLoadingListener.onRefresh();
        }
    }

    /**
     * 手动调用下拉刷新，有下拉效果
     */
    public void refreshWithPull() {
        setRefreshing(true);
        refresh();
    }

    /**
     * 手动调用加载状态，此函数不会调用 {@link PullLoadMoreListener#onRefresh()}加载数据
     * 如果需要加载数据和状态显示调用 {@link #refreshWithPull()}
     */
    public void setRefreshing(final boolean refreshing) {
        if (refreshing && isRefreshEnabled) {
            isRefreshing = true;
            refreshHeader.onRefreshing();

            int offSet = refreshHeader.getHeaderView()
                                      .getMeasuredHeight();
            refreshHeader.onMove(offSet, offSet);
        }
    }

    /**
     * 手动调用直接加载更多
     */
    public void loadMore() {
        if (mLoadingListener != null && !isNoMore) {
            mLoadingListener.onLoadMore();
        }
    }

    /**
     * 下拉刷新和到底加载完成
     */
    public void setPullLoadMoreCompleted() {
        if (isRefreshing) {
            isRefreshing = false;
            refreshHeader.onComplete();
            setNoMore(false);
        }

        isLoadingData = false;
        loadMoreFooter.onComplete();
    }

    /**
     * 设置是否已加载全部<br>
     * 设置之后到底{@link PullLoadMoreListener#onLoadMore()}不会再调用除非再次调用{@link #setNoMore(boolean)}为true;
     * 一般做法是在{@link PullLoadMoreListener#onRefresh()}中设置{@link #setNoMore(boolean)}为true;
     */
    public void setNoMore(boolean noMore) {
        isLoadingData = false;
        isNoMore = noMore;
        if (isNoMore) {
            loadMoreFooter.onNoMore();
        } else {
            loadMoreFooter.onComplete();
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mWrapAdapter = new WrapperRecyclerAdapter(adapter);
        mWrapAdapter.setHeaderView(headerView)
                    .setFooterView(footerView)
                    .setRefreshHeader(refreshHeader)
                    .setLoadMoreFooter(loadMoreFooter)
                    .setIsShowNoMore(isShowNoMore)
                    .setIsLoadMoreEnabled(isLoadMoreEnabled);
        super.setAdapter(mWrapAdapter);

        setOnItemClickListener(itemClickListener);
        if (!hasRegisterEmptyObserver) {
            mWrapAdapter.registerAdapterDataObserver(mEmptyDataObserver);
            hasRegisterEmptyObserver = true;
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE && mLoadingListener != null
                && !isLoadingData && isLoadMoreEnabled) {
            LayoutManager layoutManager = getLayoutManager();

            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                /*瀑布流不同的列的高度不一样，所以函数返回的是每一列的最后一个值*/
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }

            if (!isNoMore && layoutManager.getChildCount() > 0 && lastVisibleItemPosition >= layoutManager.getItemCount() - 1
                    && layoutManager.getItemCount() > layoutManager.getChildCount()
                    && !isRefreshing) {
                isLoadingData = true;
                loadMoreFooter.onLoading();
                mLoadingListener.onLoadMore();
            }
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                sumOffSet = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = (ev.getRawY() - mLastY) / dragRate;
                mLastY = ev.getRawY();
                sumOffSet += deltaY;
                if (isOnTop() && isRefreshEnabled && appbarState == AppBarStateChangeListener.State.EXPANDED) {
                    refreshHeader.onMove(deltaY, sumOffSet);
                    if (refreshHeader.getVisibleHeight() > 0 && !isRefreshing) {
                        return false;
                    }
                }
                break;
            default:
                mLastY = -1; // reset
                if (isOnTop() && isRefreshEnabled && appbarState == AppBarStateChangeListener.State.EXPANDED) {
                    if (refreshHeader.onRelease()) {
                        if (mLoadingListener != null) {
                            isRefreshing = true;
                            mLoadingListener.onRefresh();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }


    /**
     * 如果在HeaderView已经被添加到布局中，说明已经到顶部
     */
    private boolean isOnTop() {
        return refreshHeader.getHeaderView()
                            .getParent() != null;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        //解决和AppBarLayout冲突的问题
        ViewParent p = getParent();
        while (p != null) {
            if (p instanceof CoordinatorLayout) {
                break;
            }
            p = p.getParent();
        }

        if (p != null) {
            AppBarLayout appBarLayout = null;
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) p;
            final int childCount = coordinatorLayout.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View child = coordinatorLayout.getChildAt(i);
                if (child instanceof AppBarLayout) {
                    appBarLayout = (AppBarLayout) child;
                    break;
                }
            }

            if (appBarLayout != null) {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, State state) {
                        appbarState = state;
                    }
                });
            }
        }

        /*设置emptyView的监听者*/
        Adapter<?> adapter = getAdapter();
        if (adapter != null && !hasRegisterEmptyObserver && mEmptyDataObserver != null) {
            adapter.registerAdapterDataObserver(mEmptyDataObserver);
            hasRegisterEmptyObserver = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        
        /*注销监听者*/
        Adapter<?> adapter = getAdapter();
        if (adapter != null && hasRegisterEmptyObserver) {
            adapter.unregisterAdapterDataObserver(mEmptyDataObserver);
            hasRegisterEmptyObserver = false;
        }
    }

    /**
     * emptyView的监听者类
     */
    private class DataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            if (mWrapAdapter != null && mEmptyViewContainer != null) {
                if (mWrapAdapter.getAdapter()
                                .getItemCount() == 0) {
                    mEmptyViewContainer.setVisibility(View.VISIBLE);

                    //使emptyview居中（除headerview之外）
                    if (headerView != null && mEmptyViewContainer.getLayoutParams() instanceof MarginLayoutParams) {
                        ((MarginLayoutParams) mEmptyViewContainer.getLayoutParams()).topMargin = headerView.getHeight();
                    }
                } else {
                    mEmptyViewContainer.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapAdapter.getAdapter()
                        .notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapAdapter.getAdapter()
                        .notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mWrapAdapter.getAdapter()
                        .notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapAdapter.getAdapter()
                        .notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mWrapAdapter.getAdapter()
                        .notifyItemMoved(fromPosition, toPosition);
        }
    }

    public interface PullLoadMoreListener {

        void onRefresh();

        void onLoadMore();
    }
}