/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 下午3:05
 * ********************************************************
 */

package com.zcolin.recyclerdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zcolin.gui.pullrecyclerview.BaseRecyclerAdapter;
import com.zcolin.gui.pullrecyclerview.PullRecyclerView;
import com.zcolin.gui.pullrecyclerview.PullSwipeMenuRecyclerView;
import com.zcolin.gui.pullrecyclerview.RecycleViewDivider;
import com.zcolin.recyclerdemo.adapter.SwipeMenuRecyclerAdapter;

import java.util.ArrayList;

public class SwipeMenuLayoutActivity extends AppCompatActivity {

    private PullSwipeMenuRecyclerView recyclerView;
    private SwipeMenuRecyclerAdapter  recyclerAdapter;
    private int                       mPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipemenu);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setOnPullLoadMoreListener(new PullLoadMoreListener());
        recyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayout.HORIZONTAL, 1, Color.GREEN));
        recyclerView.getLoadMoreFooterView().setBackgroundColor(Color.BLUE);
        recyclerView.setOnItemClickListener((BaseRecyclerAdapter.OnItemClickListener<String>) (covertView, position,
                data) -> Toast
                .makeText(SwipeMenuLayoutActivity.this, data, Toast.LENGTH_SHORT)
                .show());

        recyclerView.refreshWithPull();
    }

    /**
     * 设置数据Adapter
     */
    public void notifyData(ArrayList<String> list, boolean isClear) {
        if (recyclerAdapter == null) {
            recyclerAdapter = new SwipeMenuRecyclerAdapter();
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
            dataList.add(String.format("第%d条数据", i));
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
