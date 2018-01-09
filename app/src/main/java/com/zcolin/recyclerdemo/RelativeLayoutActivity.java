/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 下午3:05
 * ********************************************************
 */

package com.zcolin.recyclerdemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.zcolin.gui.pullrecyclerview.PullScrollView;
import com.zcolin.gui.pullrecyclerview.progressindicator.ProgressStyle;

public class RelativeLayoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relativelayout);

        final PullScrollView refreshLayout = findViewById(R.id.refresh_layout);
        // refreshLayout.setIsRefreshEnabled(false);                            //下拉刷新是否可用
        // refreshLayout.setRefreshHeader(new DefRefreshHeader(this));          //设置默认或者自定义的刷新Header
        // refreshLayout.getRefreshHeaderView().setBackgroundColor(Color.BLUE);//加载Header的背景颜色
        refreshLayout.setRefreshProgressStyle(ProgressStyle.LineScaleIndicator);
        refreshLayout.setRefreshListener(() -> new Handler().postDelayed(() -> refreshLayout.setRefreshCompleted(), 1000));

        refreshLayout.refreshWithPull();
    }
}
