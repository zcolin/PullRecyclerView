/*
 * *********************************************************
 *   author   colin
 *   company  fosung
 *   email    wanglin2046@126.com
 *   date     16-12-15 下午1:47
 * ********************************************************
 */

package com.zcolin.gui.zrecyclerview.hfooter;

import android.view.View;

/**
 * 下拉HeadrView需要实现的接口
 */
public interface IRefreshHeader {
    /**
     * 手势状态
     */
    int STATE_NORMAL     = 0;
    int STATE_PREPARED   = 1;
    int STATE_REFRESHING = 2;
    int STATE_COMPLETE   = 3;

    void onReset();

    /**
     * 处于可以刷新的状态，已经过了指定距离
     */
    void onPrepare();

    /**
     * 正在刷新
     */
    void onRefreshing();

    /**
     * 下拉移动
     */
    void onMove(float offSet, float sumOffSet);

    /**
     * 下拉松开
     */
    boolean onRelease();

    /**
     * 下拉刷新完成
     */
    void onComplete();

    /**
     * 获取HeaderView
     */
    View getHeaderView();

    /**
     * 获取Header的显示高度
     */
    int getVisibleHeight();
}