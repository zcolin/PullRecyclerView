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
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.zcolin.gui.pullrecyclerview.hfooter.DefRefreshHeader;
import com.zcolin.gui.pullrecyclerview.hfooter.IRefreshHeader;


/**
 * 下拉刷新的ScrollView
 * <p/>
 */
public class PullScrollView extends ScrollView {
    private RefreshListener mRefreshListener;

    private IRefreshHeader refreshHeader;
    private boolean        isRefreshEnabled = true;    //设置下拉刷新是否可用
    private float          dragRate         = 2;       //下拉刷新滑动阻力系数，越大需要手指下拉的距离越大才能刷新

    private boolean isRefreshing;   //是否正在刷新
    private float   mLastY = -1;      //上次触摸的的Y值
    private int     topY;
    private float   sumOffSet;
    private boolean isAdded;

    private AppBarStateChangeListener.State appbarState = AppBarStateChangeListener.State.EXPANDED;

    public PullScrollView(Context context) {
        this(context, null);
    }

    public PullScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (isRefreshEnabled) {
            refreshHeader = new DefRefreshHeader(getContext());
        }
    }

    private void setLayout() {
        if (!isAdded) {
            isAdded = true;

            ViewGroup group = (ViewGroup) getParent();
            LinearLayout container = new LinearLayout(getContext());
            container.setOrientation(LinearLayout.VERTICAL);
            int index = group.indexOfChild(this);
            group.removeView(this);
            group.addView(container, index, getLayoutParams());
            container.addView(refreshHeader.getHeaderView(),
                              new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                            ViewGroup.LayoutParams.WRAP_CONTENT));
            container.addView(this,
                              new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                            ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    public View getRefreshHeaderView() {
        return refreshHeader.getHeaderView();
    }

    /**
     * 设置下拉刷新上拉加载回调
     */
    public void setRefreshListener(RefreshListener listener) {
        mRefreshListener = listener;
    }

    /**
     * 设置自定义的header
     */
    public PullScrollView setRefreshHeader(IRefreshHeader refreshHeader) {
        this.refreshHeader = refreshHeader;
        return this;
    }

    /**
     * 下拉刷新是否可用
     */
    public PullScrollView setIsRefreshEnabled(boolean enabled) {
        isRefreshEnabled = enabled;
        return this;
    }

    /**
     * 下拉刷新滑动阻力系数，越大需要手指下拉的距离越大才能刷新
     */
    public PullScrollView setDragRate(int dragRate) {
        this.dragRate = dragRate;
        return this;
    }

    /**
     * 设置下拉刷新的进度条风格
     */
    public PullScrollView setRefreshProgressStyle(String style) {
        if (refreshHeader != null && refreshHeader instanceof DefRefreshHeader) {
            ((DefRefreshHeader) refreshHeader).setProgressStyle(style);
        }
        return this;
    }

    /**
     * 设置加载更多的进度条风格
     */
    public PullScrollView setRefreshHeaderText(String str1, String str2, String str3, String str4) {
        if (refreshHeader != null && refreshHeader instanceof DefRefreshHeader) {
            ((DefRefreshHeader) refreshHeader).setInfoText(str1, str2, str3, str4);
        }
        return this;
    }

    /**
     * 设置下拉刷新的箭头图标
     */
    public PullScrollView setArrowImage(int resId) {
        if (refreshHeader != null && refreshHeader instanceof DefRefreshHeader) {
            ((DefRefreshHeader) refreshHeader).setArrowImageView(resId);
        }
        return this;
    }


    /**
     * 手动调用直接刷新，无下拉效果
     */
    public void refresh() {
        if (mRefreshListener != null) {
            isRefreshing = true;
            mRefreshListener.onRefresh();
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
     * 下拉刷新和到底加载完成
     */
    public void setRefreshCompleted() {
        if (isRefreshing) {
            isRefreshing = false;
            refreshHeader.onComplete();
        }
    }

    /**
     * 手动调用加载状态，此函数不会调用 {@link RefreshListener#onRefresh()}加载数据
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
                        if (mRefreshListener != null) {
                            isRefreshing = true;
                            mRefreshListener.onRefresh();
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        topY = t;
    }

    /**
     * 如果在HeaderView已经被添加到布局中，说明已经到顶部
     */
    private boolean isOnTop() {
        return topY == 0;
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
        setLayout();
    }

    public interface RefreshListener {
        void onRefresh();
    }
}