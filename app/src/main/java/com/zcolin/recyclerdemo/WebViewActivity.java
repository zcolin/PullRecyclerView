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
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zcolin.gui.pullrecyclerview.PullScrollView;
import com.zcolin.gui.pullrecyclerview.progressindicator.ProgressStyle;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);


        final PullScrollView refreshLayout = findViewById(R.id.refresh_layout);
        final WebView webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        // refreshLayout.setIsRefreshEnabled(false);
        // refreshLayout.setRefreshHeader(new DefRefreshHeader(this));
        // refreshLayout.getRefreshHeaderView().setBackgroundColor(Color.BLUE);
        refreshLayout.setRefreshProgressStyle(ProgressStyle.LineScaleIndicator);
        refreshLayout.setRefreshListener(() -> new Handler().postDelayed(() -> {
            webView.loadUrl("http://www.sina.com.cn/");
            refreshLayout.setRefreshCompleted();
        }, 1000));

        refreshLayout.refreshWithPull();
    }
}
