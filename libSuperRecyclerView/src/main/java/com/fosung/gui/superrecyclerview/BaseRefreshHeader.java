/*
 * **********************************************************
 *   author   colin
 *   company  fosung
 *   email    wanglin2046@126.com
 *   date     16-10-11 下午3:33
 * *********************************************************
 */

package com.fosung.gui.superrecyclerview;

import android.view.View;

/**
 * 下拉HeadrView需要实现的接口
 */
interface BaseRefreshHeader {

    /**
     * 手势状态
     */
    int STATE_NORMAL             = 0;
    int STATE_RELEASE_TO_REFRESH = 1;
    int STATE_REFRESHING         = 2;
    int STATE_DONE               = 3;

    /**
     * 下拉移动
     */
    void onMove(float delta);

    /**
     * 下拉松开
     */
    boolean releaseAction();

    /**
     * 下拉刷新完成
     */
    void refreshComplete();

    /**
     * 获取手势状态
     */
    int getState();

    /**
     * 设置手势状态
     */
    void setState(int state);

    /**
     * 设置下拉箭头
     */
    void setArrowImageView(int resid);

    /**
     * 设置进度条风格
     */
    void setProgressStyle(int style);

    /**
     * 获取HeaderView
     */
    View getHeaderView();

    /**
     * 获取Header的显示高度
     */
    int getVisibleHeight();

}