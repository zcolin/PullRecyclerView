/*
 * *********************************************************
 *   author   colin
 *   company  telchina
 *   email    wanglin2046@126.com
 *   date     18-1-9 下午3:05
 * ********************************************************
 */

package com.zcolin.gui.pullrecyclerview;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

public class CommonHolder extends RecyclerView.ViewHolder {
    public SparseArray<View> spHolder = new SparseArray<>();
    public RecyclerView      viewParent;

    public CommonHolder(RecyclerView viewParent, View itemView) {
        super(itemView);
        this.viewParent = viewParent;
    }
}