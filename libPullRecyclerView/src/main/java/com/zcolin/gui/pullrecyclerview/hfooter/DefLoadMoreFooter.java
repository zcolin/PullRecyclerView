/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 下午3:05
 * ********************************************************
 */

package com.zcolin.gui.pullrecyclerview.hfooter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zcolin.gui.pullrecyclerview.progressindicator.AVLoadingIndicatorView;
import com.zcolin.gui.pullrecyclerview.progressindicator.ProgressStyle;
import com.zcolin.gui.pullrecyclerview.progressindicator.SimpleViewSwitcher;


/**
 * 默认的加载更多FooterView，如需要简单的变换，可以直接在{@link com.zcolin.gui.pullrecyclerview.PullRecyclerView}中设置
 * 复杂的需要继承此类重写或者实现{@link ILoadMoreFooter} 接口
 */
public class DefLoadMoreFooter extends LinearLayout implements ILoadMoreFooter {

    public static String STR_LOADING       = "正在加载";
    public static String STR_LOAD_COMPLETE = "正在加载";
    public static String STR_NOMORE        = "已加载全部";

    private SimpleViewSwitcher mProgressBar;
    private TextView           mText;
    private boolean isShowNoMore = true;
    private int mMeasuredHeight;

    public DefLoadMoreFooter(Context context) {
        this(context, null);
    }

    public DefLoadMoreFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void initView() {
        setGravity(Gravity.CENTER);
        setPadding(0, 25, 0, 25);
        setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setProgressStyle(ProgressStyle.BallSpinFadeLoaderIndicator);

        mText = new TextView(getContext());
        mText.setText(STR_LOADING);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(20, 0, 0, 0);
        addView(mText, layoutParams);

        onReset();//初始为隐藏状态

        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMeasuredHeight = getMeasuredHeight();
    }


    public void setProgressStyle(String style) {
        if (mProgressBar == null) {
            mProgressBar = new SimpleViewSwitcher(getContext());
            mProgressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            addView(mProgressBar);
        }

        if (ProgressStyle.SysProgress.equals(style)) {
            mProgressBar.setView(new ProgressBar(getContext(), null, android.R.attr.progressBarStyle));
        } else {
            AVLoadingIndicatorView progressView = new AVLoadingIndicatorView(this.getContext());
            progressView.setIndicatorColor(0xffB5B5B5);
            progressView.setIndicator(style);
            mProgressBar.setView(progressView);
        }
    }

    @Override
    public void setIsShowNoMore(boolean isShow) {
        isShowNoMore = isShow;
    }

    @Override
    public void onReset() {
        onComplete();
    }

    @Override
    public void onLoading() {
        mProgressBar.setVisibility(View.VISIBLE);
        mText.setText(STR_LOADING);
        this.getLayoutParams().height = mMeasuredHeight;//动态设置高度，否则在列表中会占位高度
        this.setVisibility(View.VISIBLE);
    }

    @Override
    public void onComplete() {
        mText.setText(STR_LOAD_COMPLETE);
        this.getLayoutParams().height = mMeasuredHeight;
        this.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onNoMore() {
        mText.setText(STR_NOMORE);
        mProgressBar.setVisibility(View.GONE);
        this.setVisibility(isShowNoMore ? View.VISIBLE : View.GONE);
        this.getLayoutParams().height = isShowNoMore ? mMeasuredHeight : 5;
    }

    @Override
    public View getFootView() {
        return this;
    }
}
