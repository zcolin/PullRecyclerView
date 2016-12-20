/*
 * *********************************************************
 *   author   colin
 *   company  fosung
 *   email    wanglin2046@126.com
 *   date     16-12-20 下午1:06
 * ********************************************************
 */
package com.zcolin.recyclerdemo.adapter;


import android.widget.ImageView;
import android.widget.TextView;

import com.zcolin.gui.pullrecyclerview.BaseRecyclerAdapter;
import com.zcolin.recyclerdemo.R;


public class RecyclerAdapter extends BaseRecyclerAdapter<String> {

    public static final int TYPE_CARDVIEW = 0;
    public static final int TYPE_LISTVIEW = 1;

    public int showType = TYPE_CARDVIEW;

    public RecyclerAdapter() {
    }


    public RecyclerAdapter(int showType) {
        this.showType = showType;
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return showType == TYPE_CARDVIEW ? R.layout.view_recycler_item : R.layout.view_recycler_item_1;
    }

    @Override
    public void setUpData(CommonHolder holder, int position, int viewType, String data) {
        TextView textView = getView(holder, R.id.textView);
        ImageView imageView = getView(holder, R.id.imageView);
        imageView.setImageResource(R.drawable.ic_launcher);
        textView.setText(data);
    }
}