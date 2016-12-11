/*
 * *********************************************************
 *   author   colin
 *   company  fosung
 *   email    wanglin2046@126.com
 *   date     16-12-9 下午5:20
 * ********************************************************
 */

package com.fosung.gui.superrecyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fosung.gui.superrecyclerview.progressindicator.AVLoadingIndicatorView;


/**
 * Created by Jack on 2015/10/19.
 * update by super南仔
 */
public class DefLoadMoreFooter extends LinearLayout implements BaseLoadMoreFooter {

    private SimpleViewSwitcher mProgressBar;
    private TextView           mText;

    public DefLoadMoreFooter(Context context) {
        super(context);
        initView();
    }

    /**
     * @param context
     * @param attrs
     */
    public DefLoadMoreFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    @Override
    public void initView() {
        setGravity(Gravity.CENTER);
        setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mProgressBar = new SimpleViewSwitcher(getContext());
        mProgressBar.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setProgressStyle(ProgressStyle.BallSpinFadeLoader);

        addView(mProgressBar);
        mText = new TextView(getContext());
        mText.setText("正在加载");

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 0, 0, 0);

        mText.setLayoutParams(layoutParams);
        addView(mText);
    }


    @Override
    public void setProgressStyle(int style) {
        if (style == ProgressStyle.SysProgress) {
            mProgressBar.setView(new ProgressBar(getContext(), null, android.R.attr.progressBarStyle));
        } else {
            AVLoadingIndicatorView progressView = new AVLoadingIndicatorView(this.getContext());
            progressView.setIndicatorColor(0xffB5B5B5);
            progressView.setIndicatorId(style);
            mProgressBar.setView(progressView);
        }
    }

    @Override
    public void setState(int state) {
        switch (state) {
            case STATE_LOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                mText.setText("正在加载");
                this.setVisibility(View.VISIBLE);
                break;
            case STATE_COMPLETE:
                mText.setText("正在加载");
                this.setVisibility(View.GONE);
                break;
            case STATE_NOMORE:
                mText.setText("没有更多了");
                mProgressBar.setVisibility(View.GONE);
                this.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public View getFootView() {
        return this;
    }
}
