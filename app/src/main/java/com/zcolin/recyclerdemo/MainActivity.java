/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 下午3:05
 * ********************************************************
 */

package com.zcolin.recyclerdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zcolin.gui.pullrecyclerview.BaseRecyclerAdapter;
import com.zcolin.gui.pullrecyclerview.PullRecyclerView;
import com.zcolin.gui.pullrecyclerview.progressindicator.ProgressStyle;
import com.zcolin.recyclerdemo.adapter.RecyclerAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private PullRecyclerView recyclerView;
    private RecyclerAdapter  recyclerAdapter;
    private View             headerView2;
    private int              mPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);

        // recyclerView.setGridLayout(false);//默认为LinearLayoutManager
        recyclerView.setOnPullLoadMoreListener(new PullLoadMoreListener());

        //设置数据为空时的EmptyView，DataObserver是注册在adapter之上的，也就是setAdapter是注册上，notifyDataSetChanged的时候才会生效
        recyclerView.setEmptyView(this, R.layout.view_recycler_empty);

        //设置HeaderView和footerView
        recyclerView.addHeaderView(this, R.layout.view_recyclerheader);
        headerView2 = LayoutInflater.from(this).inflate(R.layout.view_recyclerheader, null);
        ((TextView) headerView2.findViewById(R.id.textView)).setText("HEDER 2");
        recyclerView.addHeaderView(headerView2);
        recyclerView.addFooterView(this, R.layout.view_recyclerfooter);

        //下拉和到底加载的进度条样式，默认为 ProgressStyle.BallSpinFadeLoaderIndicator
        recyclerView.setRefreshProgressStyle(ProgressStyle.LineScaleIndicator);
        recyclerView.setLoadMoreProgressStyle(ProgressStyle.LineScaleIndicator);

        recyclerView.setOnItemClickListener((BaseRecyclerAdapter.OnItemClickListener<String>) (covertView, position,
                data) -> {
            Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
            if (position == 0) {
                Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
                startActivity(intent);
            } else if (position == 1) {
                Intent intent = new Intent(MainActivity.this, ScrollViewLayoutActivity.class);
                startActivity(intent);
            } else if (position == 2) {
                Intent intent = new Intent(MainActivity.this, TextViewActivity.class);
                startActivity(intent);
            } else if (position == 3) {
                Intent intent = new Intent(MainActivity.this, RelativeLayoutActivity.class);
                startActivity(intent);
            } else if (position == 4) {
                Intent intent = new Intent(MainActivity.this, GridLayoutActivity.class);
                startActivity(intent);
            } else if (position == 5) {
                Intent intent = new Intent(MainActivity.this, StaggeredGridLayoutActivity.class);
                startActivity(intent);
            } else if (position == 6) {
                Intent intent = new Intent(MainActivity.this, MultiTypeLayoutActivity.class);
                startActivity(intent);
            } else if (position == 7) {
                Intent intent = new Intent(MainActivity.this, DecorationActivity.class);
                startActivity(intent);
            } else if (position == 8) {
                Intent intent = new Intent(MainActivity.this, SwipeMenuLayoutActivity.class);
                startActivity(intent);
            } else if (position == 9) {
                recyclerView.removeHeaderView(headerView2);
            }
        });
        recyclerView.setOnItemLongClickListener((BaseRecyclerAdapter.OnItemLongClickListener<String>) (covertView,
                position, data) -> {
            recyclerAdapter.getDatas().remove(position);
            recyclerAdapter.notifyItemRemoved(position);
            recyclerAdapter.notifyItemRangeChanged(position, recyclerAdapter.getDatas().size() - position);
            return true;
        });


        // recyclerView.setIsShowNoMore(false);//不显示《已加载全部》
        // recyclerView.setIsLoadMoreEnabled(false);//到底加载是否可用
        // recyclerView.setIsRefreshEnabled(false);//下拉刷新是否可用

        //下拉刷新的文字显示
        recyclerView.setRefreshHeaderText("下拉刷新", "释放立即刷新", "正在刷新", "刷新完成");

        //绑定Adapter
        notifyData(new ArrayList<>(), false);

        recyclerView.refreshWithPull();     //有下拉效果的数据刷新
        // recyclerView.refresh();          //没有下拉刷新效果，直接刷新数据
        // recyclerView.setRefreshing(true);//只有下拉刷新效果，不刷新数据
    }

    /**
     * 设置数据Adapter
     */
    public void notifyData(ArrayList<String> list, boolean isClear) {
        if (recyclerAdapter == null) {
            recyclerAdapter = new RecyclerAdapter();
            recyclerAdapter.addDatas(list);
            recyclerView.setAdapter(recyclerAdapter);
        } else {
            if (isClear) {
                recyclerAdapter.setDatas(list);
            } else {
                recyclerAdapter.addDatas(list);
            }
            recyclerAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 模仿从网络请求数据
     */
    public void requestData(final int page) {
        new Handler().postDelayed(() -> {
            notifyData(setList(page), page == 1);
            recyclerView.setPullLoadMoreCompleted();
            if (page == 2) {
                recyclerView.setNoMore(true);
            }
        }, 1000);
    }

    //制造假数据
    private ArrayList<String> setList(int page) {
        ArrayList<String> dataList = new ArrayList<>();
        int start = 15 * (page - 1);
        for (int i = start; i < 15 * page; i++) {
            if (i == 0) {
                dataList.add("WebView");
            } else if (i == 1) {
                dataList.add("ScrollView");
            } else if (i == 2) {
                dataList.add("TextView");
            } else if (i == 3) {
                dataList.add("RelativeLayout");
            } else if (i == 4) {
                dataList.add("GridLayout");
            } else if (i == 5) {
                dataList.add("StaggeredGridLayout");
            } else if (i == 6) {
                dataList.add("MultiTypeLayout");
            } else if (i == 7) {
                dataList.add("Decoration");
            } else if (i == 8) {
                dataList.add("SwipeMenuLayout");
            } else if (i == 9) {
                dataList.add("移除Header2");
            } else {
                dataList.add(String.format("第%d条数据", i));
            }
        }
        return dataList;
    }

    class PullLoadMoreListener implements PullRecyclerView.PullLoadMoreListener {
        @Override
        public void onRefresh() {
            mPage = 1;
            requestData(mPage);
            recyclerView.setNoMore(false);
        }

        @Override
        public void onLoadMore() {
            mPage = mPage + 1;
            requestData(mPage);
        }
    }
}
