package com.revolo.lock.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 自定义自适应item高度
 */
public class AutoMeasureLinearLayoutManager extends LinearLayoutManager {
    public AutoMeasureLinearLayoutManager(Context context) {
        super(context);
    }

    public AutoMeasureLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public AutoMeasureLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
        try {
            View view = recycler.getViewForPosition(0);
            measureChild(view, widthSpec, heightSpec);
            //int measuredWidth = View.MeasureSpec.getSize(widthSpec);
            int measuredHeight = view.getMeasuredHeight();
            int showHeight = measuredHeight * state.getItemCount();
            if(state.getItemCount() >= 5){
                showHeight = measuredHeight * 5;
            }
            setMeasuredDimension(widthSpec, showHeight);
        } catch (Exception e) {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            e.printStackTrace();
        }

    }
}
