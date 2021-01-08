/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 下午3:05
 * ********************************************************
 */

package com.zcolin.gui.pullrecyclerview;

import android.content.Context;
import android.os.Build;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.zcolin.gui.pullrecyclerview.hfooter.DefLoadMoreFooter;
import com.zcolin.gui.pullrecyclerview.hfooter.DefRefreshHeader;
import com.zcolin.gui.pullrecyclerview.hfooter.ILoadMoreFooter;
import com.zcolin.gui.pullrecyclerview.hfooter.IRefreshHeader;

import java.util.ArrayList;
import java.util.List;


/**
 * 下拉刷新_到底加载 组件
 * <p>
 * 可以传入{@link RecyclerView.Adapter}及其子类，使用装饰者模式将用户传入的apapter进行包装，
 * 所以用户的adapter可以保持原有样式的操作
 */
public class PullRecyclerView extends RecyclerView {
    private WrapperRecyclerAdapter                mWrapAdapter;
    private PullRecyclerView.PullLoadMoreListener mLoadingListener;
    private RelativeLayout                        mEmptyViewContainer;

    private ArrayList<View>                             listHeaderView;
    private ArrayList<View>                             listFooterView;
    private IRefreshHeader                              refreshHeader;
    private ILoadMoreFooter                             loadMoreFooter;
    private boolean                                     isShowNoMore         = true;   //是否显示 加载全部
    private boolean                                     isRefreshEnabled     = true;    //设置下拉刷新是否可用
    private boolean                                     isLoadMoreEnabled    = true;    //设置到底加载是否可用
    private float                                       dragRate             = 2;//下拉刷新滑动阻力系数，越大需要手指下拉的距离越大才能刷新
    private BaseRecyclerAdapter.OnItemClickListener     itemClickListener;
    private BaseRecyclerAdapter.OnItemLongClickListener itemLongClickListener;
    private long                                        minClickIntervaltime = 100; //ITEM点击的最小间隔

    private boolean isAddHeader;//如果在设置adapter之前设置,此变量为false,之后设置,则为true
    private boolean isAddFooter;//如果在设置adapter之前设置,此变量为false,之后设置,则为true
    private boolean isNoMore      = false;   //是否已没有更多
    private boolean isLoadingData = false;   //是否正在加载数据
    private boolean isRefreshing;//是否正在刷新
    private float   mLastY        = -1;      //上次触摸的的Y值
    private float   sumOffSet;

    private final AdapterDataObserver             mEmptyDataObserver = new DataObserver();
    private       AppBarStateChangeListener.State appbarState        = AppBarStateChangeListener.State.EXPANDED;
    private       boolean                         hasRegisterEmptyObserver;

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
            setRefreshHeader(new DefRefreshHeader(getContext()));
        }

        if (isLoadMoreEnabled) {
            setLoadMoreFooter(new DefLoadMoreFooter(getContext()));
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
     * 此处设置OnItemClickListener
     * 是调用的{@link BaseRecyclerAdapter#setOnItemClickListener(BaseRecyclerAdapter.OnItemClickListener)}，
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
     * 此处设置OnItemLongClickListener
     * 是调用的{@link BaseRecyclerAdapter#setOnItemLongClickListener(BaseRecyclerAdapter.OnItemLongClickListener)}，
     * 此处的泛型类型必须和{@link BaseRecyclerAdapter}的相同
     */
    public <T> void setOnItemLongClickListener(BaseRecyclerAdapter.OnItemLongClickListener<T> li) {
        itemLongClickListener = li;
        if (mWrapAdapter != null) {
            if (mWrapAdapter.getAdapter() instanceof BaseRecyclerAdapter) {
                ((BaseRecyclerAdapter) mWrapAdapter.getAdapter()).setOnItemLongClickListener(li);
            } else {
                throw new IllegalArgumentException("adapter 必须继承BaseRecyclerAdapter setOnItemLongClickListener");
            }
        }
    }

    /**
     * 设置Item点击的最小间隔
     *
     * @param minClickIntervaltime millionSeconds
     */
    public PullRecyclerView setItemMinClickIntervalTime(long minClickIntervaltime) {
        this.minClickIntervaltime = minClickIntervaltime;
        if (mWrapAdapter != null) {
            if (mWrapAdapter.getAdapter() instanceof BaseRecyclerAdapter) {
                ((BaseRecyclerAdapter) mWrapAdapter.getAdapter()).setItemMinClickIntervalTime(minClickIntervaltime);
            } else {
                throw new IllegalArgumentException("adapter 必须继承BaseRecyclerAdapter 才能使用setItemMinClickIntervalTime");
            }
        }
        return this;
    }

    /**
     * LinearLayoutManager
     *
     * @param isForce 如果已经设置了，是否强制设置
     */
    public void setLinearLayout(boolean isForce) {
        if (isForce || getLayoutManager() == null || !(getLayoutManager() instanceof LinearLayoutManager)) {
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
        if (isForce || getLayoutManager() == null || !(getLayoutManager() instanceof GridLayoutManager)) {
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
        if (isForce || getLayoutManager() == null || !(getLayoutManager() instanceof StaggeredGridLayoutManager)) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(spanCount,
                                                                                                   LinearLayoutManager.VERTICAL);
            setLayoutManager(staggeredGridLayoutManager);
        }
    }

    /**
     * 增加默认分割线
     */
    public PullRecyclerView addDefaultItemDecoration() {
        addItemDecoration(new RecycleViewDivider(getContext(), LinearLayout.HORIZONTAL));
        return this;
    }


    public View getRefreshHeaderView() {
        return refreshHeader.getHeaderView();
    }

    public View getLoadMoreFooterView() {
        return loadMoreFooter.getFootView();
    }

    /**
     * 获取设置的HeaderView的外层LinearLayout
     */
    public View getHeaderLayout() {
        if (mWrapAdapter == null) {
            return null;
        } else {
            return mWrapAdapter.getHeaderLayout();
        }
    }

    /**
     * 获取设置的FooterView的外层LinearLayout
     */
    public View getFooterLayout() {
        if (mWrapAdapter == null) {
            return null;
        } else {
            return mWrapAdapter.getFooterLayout();
        }
    }

    /**
     * 设置自定义的HeaderView
     */
    public PullRecyclerView addHeaderView(View headerView) {
        return addHeaderView(headerView, -1);
    }

    /**
     * 设置自定义的HeaderView
     */
    public PullRecyclerView addHeaderView(Context context, int headerViewLayoutId) {
        return addHeaderView(context, headerViewLayoutId, -1);
    }

    /**
     * 设置自定义的HeaderView
     */
    public PullRecyclerView addHeaderView(Context context, int headerViewLayoutId, int index) {
        return addHeaderView(LayoutInflater.from(context).inflate(headerViewLayoutId, null), index);
    }

    /**
     * 设置自定义的HeaderView
     */
    public PullRecyclerView addHeaderView(View headerView, int index) {
        if (headerView != null) {
            if (listHeaderView == null) {
                listHeaderView = new ArrayList<>();
            }

            index = index < 0 ? listHeaderView.size() : index;
            index = index > listHeaderView.size() ? listHeaderView.size() : index;
            headerView.setTag(R.id.srv_reserved_ivew, "reservedView");
            listHeaderView.add(index, headerView);

            if (mWrapAdapter != null) {
                mWrapAdapter.addHeaderView(headerView, index);
                isAddHeader = true;
            }
        }
        return this;
    }

    /**
     * 设置自定义的FooterView
     */
    public PullRecyclerView addFooterView(View footerView) {
        return addFooterView(footerView, -1);
    }

    /**
     * 设置自定义的FooterView
     */
    public PullRecyclerView addFooterView(Context context, int footerViewLayoutId) {
        return addFooterView(context, footerViewLayoutId, -1);
    }

    /**
     * 设置自定义的FooterView
     */
    public PullRecyclerView addFooterView(Context context, int footerViewLayoutId, int index) {
        return addFooterView(LayoutInflater.from(context).inflate(footerViewLayoutId, null), index);
    }

    /**
     * 设置自定义的FooterView
     */
    public PullRecyclerView addFooterView(View footerView, int index) {
        if (footerView != null) {
            if (listFooterView == null) {
                listFooterView = new ArrayList<>();
            }

            index = index < 0 ? listFooterView.size() : index;
            index = index > listFooterView.size() ? listFooterView.size() : index;
            footerView.setTag(R.id.srv_reserved_ivew, "reservedView");
            listFooterView.add(index, footerView);

            if (mWrapAdapter != null) {
                mWrapAdapter.addFooterView(footerView, index);
                isAddFooter = true;
            }
        }
        return this;
    }

    public PullRecyclerView removeHeaderView(View header) {
        if (listHeaderView != null) {
            listHeaderView.remove(header);
            if (listHeaderView.size() == 0) {
                listHeaderView = null;
            }
        }

        if (mWrapAdapter != null) {
            mWrapAdapter.removeHeaderView(header);
        }
        return this;
    }

    public PullRecyclerView removeAllHeaderView() {
        if (listHeaderView != null) {
            listHeaderView.clear();
            listHeaderView = null;
        }

        if (mWrapAdapter != null) {
            mWrapAdapter.removeAllHeaderView();
        }
        return this;
    }

    public PullRecyclerView removeFooterView(View footer) {
        if (listFooterView != null) {
            listFooterView.remove(footer);
            if (listFooterView.size() == 0) {
                listFooterView = null;
            }
        }

        if (mWrapAdapter != null) {
            mWrapAdapter.removeFooterView(footer);
        }
        return this;
    }

    public PullRecyclerView removeAllFooterView() {
        if (listFooterView != null) {
            listFooterView.clear();
            listFooterView = null;
        }

        if (mWrapAdapter != null) {
            mWrapAdapter.removeAllFooterView();
        }
        return this;
    }

    /**
     * 设置自定义的FooterView
     */
    public PullRecyclerView setLoadMoreFooter(ILoadMoreFooter loadMoreFooter) {
        this.loadMoreFooter = loadMoreFooter;
        this.loadMoreFooter.getFootView().setTag("reservedView");
        return this;
    }

    /**
     * 设置自定义的header
     */
    public PullRecyclerView setRefreshHeader(IRefreshHeader refreshHeader) {
        this.refreshHeader = refreshHeader;
        this.refreshHeader.getHeaderView().setTag("reservedView");
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
        setEmptyView(LayoutInflater.from(context).inflate(layoutId, null));
        return this;
    }

    /**
     * 设置没有数据的EmptyView
     * <p>
     * <p>注意：如果调用此函数，会将RecyclerView从原来的布局中移除添加到一个RelativeLayout中，然后将RelativeLayout放置到原来的布局中，
     * 也就是说，在RecyclerView和其父布局中间添加了一层RelitiveLayout，用来盛放RecyclerView和emptyView<p/>
     */
    public ViewGroup setEmptyView(View emptyView) {
        ViewGroup group = (ViewGroup) getParent();
        RelativeLayout container = new RelativeLayout(getContext());
        if (group == null) {
            group = new RelativeLayout(getContext());
            group.addView(container,
                          new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                          ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            int index = group.indexOfChild(this);
            group.removeView(this);
            group.addView(container, index, getLayoutParams());
        }

        container.addView(this,
                          new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                          ViewGroup.LayoutParams.MATCH_PARENT));

        RelativeLayout.LayoutParams emptyViewParams =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                                      ViewGroup.LayoutParams.WRAP_CONTENT);
        emptyViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mEmptyViewContainer = new RelativeLayout(getContext());
        mEmptyViewContainer.addView(emptyView, emptyViewParams);
        container.addView(mEmptyViewContainer,
                          new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                          ViewGroup.LayoutParams.MATCH_PARENT));

        this.mEmptyViewContainer.setVisibility(View.GONE);
        return group;
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

            int offSet = refreshHeader.getHeaderView().getMeasuredHeight();
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
        }

        isLoadingData = false;
        if (!isNoMore) {
            loadMoreFooter.onComplete();
        }
    }

    /**
     * 设置是否已加载全部<br>
     * 设置之后到底{@link PullLoadMoreListener#onLoadMore()}不会再调用除非再次调用{@link #setNoMore(boolean)}为true;
     * 一般做法是在{@link PullLoadMoreListener#onRefresh()}中设置{@link #setNoMore(boolean)}为true;
     */
    public void setNoMore(boolean noMore) {
        setNoMore(noMore, 0, 0);
    }

    /**
     * 设置是否已加载全部, <br>
     * 设置之后到底{@link PullLoadMoreListener#onLoadMore()}不会再调用除非再次调用{@link #setNoMore(boolean)}为true;
     * 一般做法是在{@link PullLoadMoreListener#onRefresh()}中设置{@link #setNoMore(boolean)}为true;
     */
    public void setNoMore(boolean noMore, int minShowItem, List<?> data) {
        setNoMore(noMore, minShowItem, data == null ? 0 : data.size());
    }

    /**
     * 设置是否已加载全部, <br>
     * 设置之后到底{@link PullLoadMoreListener#onLoadMore()}不会再调用除非再次调用{@link #setNoMore(boolean)}为true;
     * 一般做法是在{@link PullLoadMoreListener#onRefresh()}中设置{@link #setNoMore(boolean)}为true;
     */
    public void setNoMore(boolean noMore, int minShowItem, int dataSize) {
        isLoadingData = false;
        isNoMore = noMore;

        if (isNoMore && dataSize >= minShowItem) {
            loadMoreFooter.onNoMore();
        } else {
            loadMoreFooter.onComplete();
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (mWrapAdapter != null && mWrapAdapter.getAdapter() != null && hasRegisterEmptyObserver) {
            mWrapAdapter.getAdapter().unregisterAdapterDataObserver(mEmptyDataObserver);
            hasRegisterEmptyObserver = false;
        }

        mWrapAdapter = new WrapperRecyclerAdapter(adapter);
        mWrapAdapter.setRefreshHeader(refreshHeader)
                    .setLoadMoreFooter(loadMoreFooter)
                    .setIsShowNoMore(isShowNoMore)
                    .setIsLoadMoreEnabled(isLoadMoreEnabled);

        if (!isAddHeader) {
            mWrapAdapter.setHeaderViews(listHeaderView);
        }

        if (!isAddFooter) {
            mWrapAdapter.setFooterViews(listFooterView);
        }
        super.setAdapter(mWrapAdapter);

        setOnItemClickListener(itemClickListener);
        setOnItemLongClickListener(itemLongClickListener);
        if (!hasRegisterEmptyObserver) {
            adapter.registerAdapterDataObserver(mEmptyDataObserver);
            hasRegisterEmptyObserver = true;
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE && mLoadingListener != null && !isLoadingData && isLoadMoreEnabled) {
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

            if (!isNoMore && layoutManager.getChildCount() > 0 && lastVisibleItemPosition >= layoutManager.getItemCount() - 1 && layoutManager
                    .getItemCount() > layoutManager.getChildCount() && !isRefreshing) {
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
        return refreshHeader.getHeaderView().getParent() != null;
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
        if (mWrapAdapter != null && mWrapAdapter.getAdapter() != null && !hasRegisterEmptyObserver && mEmptyDataObserver != null) {
            mWrapAdapter.getAdapter().registerAdapterDataObserver(mEmptyDataObserver);
            hasRegisterEmptyObserver = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        /*注销监听者*/
        if (mWrapAdapter != null && mWrapAdapter.getAdapter() != null && hasRegisterEmptyObserver) {
            mWrapAdapter.getAdapter().unregisterAdapterDataObserver(mEmptyDataObserver);
            hasRegisterEmptyObserver = false;
        }
    }

    /**
     * emptyView的监听者类
     */
    private class DataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            checkEmptyView();
            mWrapAdapter.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeInserted(mWrapAdapter.getHeaderLayout() == null ?
                                                 positionStart + 1 :
                                                 positionStart + 2, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeRemoved(mWrapAdapter.getHeaderLayout() == null ?
                                                positionStart + 1 :
                                                positionStart + 2, itemCount);
            checkEmptyView();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            int from = mWrapAdapter.getHeaderLayout() == null ? fromPosition + 1 : fromPosition + 2;
            int to = mWrapAdapter.getHeaderLayout() == null ? toPosition + 1 : toPosition + 2;
            mWrapAdapter.notifyItemMoved(from, to);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeChanged(mWrapAdapter.getHeaderLayout() == null ?
                                                positionStart + 1 :
                                                positionStart + 2, itemCount);
            checkEmptyView();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mWrapAdapter.notifyItemRangeChanged(mWrapAdapter.getHeaderLayout() == null ?
                                                positionStart + 1 :
                                                positionStart + 2, itemCount, payload);
            checkEmptyView();
        }

        private void checkEmptyView() {
            if (mEmptyViewContainer != null) {
                if (mWrapAdapter.getAdapter().getItemCount() == 0) {
                    mEmptyViewContainer.setVisibility(View.VISIBLE);

                    //使emptyview居中（除headerview之外）
                    if (mWrapAdapter.getHeaderLayout() != null && mEmptyViewContainer.getLayoutParams() instanceof MarginLayoutParams) {
                        if (mWrapAdapter.getHeaderLayout()
                                        .getHeight() == 0 && Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            mWrapAdapter.getHeaderLayout().measure(0, 0);
                            ((MarginLayoutParams) mEmptyViewContainer.getLayoutParams()).topMargin =
                                    mWrapAdapter.getHeaderLayout()
                                                                                                                 .getMeasuredHeight();
                        } else {
                            ((MarginLayoutParams) mEmptyViewContainer.getLayoutParams()).topMargin =
                                    mWrapAdapter.getHeaderLayout()
                                                                                                                 .getHeight();
                        }
                    }
                } else {
                    mEmptyViewContainer.setVisibility(View.GONE);
                }
            }
        }
    }

    public interface PullLoadMoreListener {

        void onRefresh();

        void onLoadMore();
    }
}
