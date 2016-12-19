/*
 * *********************************************************
 *   author   colin
 *   company  fosung
 *   email    wanglin2046@126.com
 *   date     16-12-12 下午5:35
 * ********************************************************
 */

package com.zcolin.gui.pullrecyclerview;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

public class CommonHolder extends RecyclerView.ViewHolder {
    public SparseArray<View> spHolder = new SparseArray<>();
    public RecyclerView viewParent;

    public CommonHolder(RecyclerView viewParent, View itemView) {
        super(itemView);
        this.viewParent = viewParent;
    }
}