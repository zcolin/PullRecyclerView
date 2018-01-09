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

import com.zcolin.gui.pullrecyclerview.PullRecyclerView;
import com.zcolin.recyclerdemo.adapter.RecyclerAdapter;

import java.util.ArrayList;

public class DecorationActivity extends AppCompatActivity {

    private PullRecyclerView recyclerView;
    private RecyclerAdapter  recyclerAdapter;
    private int mPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.addDefaultItemDecoration();
        //设置HeaderView和footerView
        recyclerView.addHeaderView(this, R.layout.view_recyclerheader);
        recyclerView.addFooterView(this, R.layout.view_recyclerfooter);

        recyclerView.setIsShowNoMore(false);
        recyclerView.setOnPullLoadMoreListener(new PullLoadMoreListener());
        notifyData(new ArrayList<>(), false);
        recyclerView.refreshWithPull();     //有下拉效果的数据刷新
    }

    /**
     * 设置数据Adapter
     */
    public void notifyData(ArrayList<String> list, boolean isClear) {
        if (recyclerAdapter == null) {
            recyclerAdapter = new RecyclerAdapter(RecyclerAdapter.TYPE_LISTVIEW);
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
