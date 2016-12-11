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
 * 加载更多FooterView需要实现的接口
 */
interface BaseLoadMoreFooter {

    /**
     * 下拉状态
     */
    int STATE_LOADING  = 0;
    int STATE_COMPLETE = 1;
    int STATE_NOMORE   = 2;

    void initView();

    /**
     * 设置加载更多状态
     */
    void setState(int state);

    /**
     * 设置进度条状态
     * @param style
     */
    void setProgressStyle(int style);
    
    /**
     * 加载更多的View
     */
    View getFootView();
}
