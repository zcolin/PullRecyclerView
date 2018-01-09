/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 下午3:05
 * ********************************************************
 */
package com.zcolin.recyclerdemo.adapter;

import android.widget.ImageView;
import android.widget.TextView;

import com.zcolin.gui.pullrecyclerview.BaseRecyclerAdapter;
import com.zcolin.recyclerdemo.R;


public class MultiTypeAdapter extends BaseRecyclerAdapter<String> {

    public static final int TYPE_0 = 0;
    public static final int TYPE_1 = 1;
    public static final int TYPE_2 = 2;

    @Override
    public int getItemLayoutId(int viewType) {
        if (viewType == TYPE_0) {
            return R.layout.view_recycler_item;
        } else {
            return R.layout.view_recycler_item_2;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 3 == 0) {
            return TYPE_2;
        } else if (position % 4 == 0) {
            return TYPE_0;
        } else {
            return TYPE_1;
        }
    }

    @Override
    public int getGridItemSpanCount(int position, int viewType) {
        if (viewType == TYPE_0) {
            return 0;
        } else if (viewType == TYPE_1) {
            return 2;
        } else {
            return 1;
        }
    }

    @Override
    public void setUpData(CommonHolder holder, int position, int viewType, String data) {
        TextView textView = getView(holder, R.id.textView);
        textView.setText(data);
        if (viewType == TYPE_0) {
            ImageView imageView = getView(holder, R.id.imageView);
            imageView.setImageResource(R.drawable.ic_launcher);
        }
    }
}