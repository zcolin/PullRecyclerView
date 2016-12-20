/*
 * *********************************************************
 *   author   colin
 *   company  fosung
 *   email    wanglin2046@126.com
 *   date     16-12-15 下午1:47
 * ********************************************************
 */

package com.zcolin.gui.pullrecyclerview.hfooter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zcolin.gui.pullrecyclerview.R;
import com.zcolin.gui.pullrecyclerview.progressindicator.AVLoadingIndicatorView;
import com.zcolin.gui.pullrecyclerview.progressindicator.ProgressStyle;
import com.zcolin.gui.pullrecyclerview.progressindicator.SimpleViewSwitcher;

/**
 * 默认的下拉更多HeaderView，如需要简单的变换，可以直接在{@link com.zcolin.gui.pullrecyclerview.PullRecyclerView}中设置
 * 复杂的需要继承此类重写或者实现{@link IRefreshHeader} 接口
 */
public class DefRefreshHeader extends LinearLayout implements IRefreshHeader {
    private static final int ROTATE_ANIM_DURATION = 180;

    private int mState;

    private String strInfo1 = "下拉刷新";
    private String strInfo2 = "释放立即刷新";
    private String strInfo3 = "正在刷新";
    private String strInfo4 = "刷新完成";

    private LinearLayout       mContainer;
    private ImageView          mArrowImageView;
    private SimpleViewSwitcher mProgressBar;
    private TextView           mStatusTextView;
    private TextView           mHeaderTimeView;

    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;

    public int mMeasuredHeight;

    public DefRefreshHeader(Context context) {
        this(context, null);
    }

    public DefRefreshHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        // 初始情况，设置下拉刷新view高度为0
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        this.setLayoutParams(lp);
        this.setPadding(0, 0, 0, 0);

        mContainer = (LinearLayout) LayoutInflater.from(getContext())
                                                  .inflate(R.layout.gui_pullrecyclerview_header, null);
        addView(mContainer, new LayoutParams(LayoutParams.MATCH_PARENT, 0));
        setGravity(Gravity.BOTTOM);

        mArrowImageView = (ImageView) findViewById(R.id.listview_header_arrow);
        mStatusTextView = (TextView) findViewById(R.id.refresh_status_textview);

        mProgressBar = (SimpleViewSwitcher) findViewById(R.id.listview_header_progressbar);
        setProgressStyle(ProgressStyle.BallSpinFadeLoaderIndicator);

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);

        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);

        mHeaderTimeView = (TextView) findViewById(R.id.last_refresh_time);
        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMeasuredHeight = getMeasuredHeight();
    }

    /**
     * 设置加载进度框样式
     *
     * @param style {@link ProgressStyle}
     */
    public void setProgressStyle(String style) {
        if (ProgressStyle.SysProgress.equals(style)) {
            mProgressBar.setView(new ProgressBar(getContext(), null, android.R.attr.progressBarStyle));
        } else {
            AVLoadingIndicatorView progressView = new AVLoadingIndicatorView(this.getContext());
            progressView.setIndicatorColor(0xffB5B5B5);
            progressView.setIndicator(style);
            mProgressBar.setView(progressView);
        }
    }
    
    /**
     * 设置控件的不同状态的文字
     *
     * @param str1 下拉时显示的文字，       如‘下拉刷新’
     * @param str2 拉到可以刷新的距离显示   如‘释放立即刷新’
     * @param str3 刷新中的文字            如‘正在刷新’
     * @param str4 刷新完成的文字         如‘刷新完成’
     */
    public void setInfoText(String str1, String str2, String str3, String str4) {
        strInfo1 = str1;
        strInfo2 = str2;
        strInfo3 = str3;
        strInfo4 = str4;
    }

    /**
     * 设置下拉箭头的图标
     */
    public void setArrowImageView(int resid) {
        mArrowImageView.setImageResource(resid);
    }


    @Override
    public void onReset() {
        mArrowImageView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);

        if (mState == STATE_PREPARED) {
            mArrowImageView.startAnimation(mRotateDownAnim);
        }
        if (mState == STATE_REFRESHING) {
            mArrowImageView.clearAnimation();
        }
        mStatusTextView.setText(strInfo1);
        mState = STATE_NORMAL;
    }

    @Override
    public void onPrepare() {
        mArrowImageView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);

        if (mState != STATE_PREPARED) {
            mArrowImageView.clearAnimation();
            mArrowImageView.startAnimation(mRotateUpAnim);
            mStatusTextView.setText(strInfo2);
        }
        mState = STATE_PREPARED;
    }

    @Override
    public void onMove(float offSet, float sumOffSet) {
        if (getVisibleHeight() > 0 || offSet > 0) {
            setVisibleHeight((int) offSet + getVisibleHeight());
            if (mState <= STATE_PREPARED) { // 未处于刷新状态，更新箭头
                if (getVisibleHeight() > mMeasuredHeight) {
                    onPrepare();
                } else {
                    onReset();
                }
            }
        }
    }

    @Override
    public boolean onRelease() {
        int height = getVisibleHeight();
        if (height > mMeasuredHeight && mState < STATE_REFRESHING) {
            onRefreshing();
        }

        // refreshing and header isn't shown fully. do nothing.
        if (mState == STATE_REFRESHING && height <= mMeasuredHeight) {
            //return;
        }

        int destHeight = 0; // default: scroll back to dismiss header.
        // is refreshing, just scroll back to show all the header.
        if (mState == STATE_REFRESHING) {
            destHeight = mMeasuredHeight;
        }
        smoothScrollTo(destHeight);
        return mState == STATE_REFRESHING;
    }

    @Override
    public void onRefreshing() {
        mArrowImageView.clearAnimation();
        mArrowImageView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mStatusTextView.setText(strInfo3);
        mState = STATE_REFRESHING;
    }

    @Override
    public void onComplete() {
        mArrowImageView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);

        SharedPreferences pre = getContext().getApplicationContext()
                                            .getSharedPreferences("pullrecyclerview", Context.MODE_PRIVATE);
        mHeaderTimeView.setText(friendlyTime(pre.getLong("refresh_time", 0)));
        new Handler().postDelayed(new Runnable() {
            public void run() {
                reset();
            }
        }, 200);
        mStatusTextView.setText(strInfo4);

        //当前刷新时间存入到本地
        long timestamp = System.currentTimeMillis();
        pre.edit()
           .putLong("refresh_time", timestamp)
           .apply();
        mState = STATE_COMPLETE;
    }

    /**
     * 回归初始状态，刷新完成时调用
     */
    private void reset() {
        smoothScrollTo(0);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                onReset();
            }
        }, 500);
    }

    /**
     * 平滑的显示Header偏移
     */
    private void smoothScrollTo(int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), destHeight);
        animator.setDuration(300)
                .start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setVisibleHeight((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }


    /**
     * 设置下拉Header需要显示的高度
     */
    private void setVisibleHeight(int height) {
        if (height < 0)
            height = 0;
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }


    @Override
    public int getVisibleHeight() {
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        return lp.height;
    }

    @Override
    public View getHeaderView() {
        return this;
    }

    private String friendlyTime(long timestamp) {
        if (timestamp == 0) {
            return "";
        }

        int ct = (int) ((System.currentTimeMillis() - timestamp) / 1000);
        if (ct >= 0 && ct < 60) {
            return "刚刚";
        }
        if (ct >= 60 && ct < 3600) {
            return Math.max(ct / 60, 1) + "分钟前";
        }
        if (ct >= 3600 && ct < 86400)
            return ct / 3600 + "小时前";
        if (ct >= 86400 && ct < 2592000) { //86400 * 30
            int day = ct / 86400;
            return day + "天前";
        }
        if (ct >= 2592000 && ct < 31104000) { //86400 * 30
            return ct / 2592000 + "月前";
        }
        return ct / 31104000 + "年前";
    }
}