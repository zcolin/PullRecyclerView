/*
 * *********************************************************
 *   author   colin
 *   company  fosung
 *   email    wanglin2046@126.com
 *   date     16-12-16 下午4:51
 * ********************************************************
 */

package com.zcolin.recyclerdemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zcolin.gui.zrecyclerview.ZRefreshLayout;
import com.zcolin.gui.zrecyclerview.progressindicator.ProgressStyle;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);


        final ZRefreshLayout refreshLayout = (ZRefreshLayout) findViewById(R.id.refresh_layout);
        final WebView webView = (WebView) findViewById(R.id.webview);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        // refreshLayout.setIsRefreshEnabled(false);
        //        refreshLayout.setRefreshHeader(new DefRefreshHeader(this));
        //        refreshLayout.getRefreshHeaderView()
        //                     .setBackgroundColor(Color.BLUE);
        refreshLayout.setRefreshProgressStyle(ProgressStyle.LineScaleIndicator);
        refreshLayout.setRefreshListener(new ZRefreshLayout.RefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("http://www.sina.com.cn/");
                        refreshLayout.setRefreshCompleted();
                    }
                }, 1000);
            }
        });

        refreshLayout.refreshWithPull();
    }
}
