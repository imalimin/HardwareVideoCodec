package com.lmy.sample.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by lmy on 2017/2/1.
 */

public class OnRecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    protected OnItemClickListener mListener;
    protected RecyclerView recyclerView;

    public interface OnItemClickListener {
        void onItemClick(RecyclerView parent, View view, int position);
    }

    public interface OnItemLongClickListener extends OnItemClickListener {
        void onLongItemClick(RecyclerView parent, View view, int position);
    }

    public interface OnItemDoubleClickListener extends OnItemLongClickListener {
        void onItemDoubleClick(RecyclerView parent, View view, int position);
    }

    GestureDetector mGestureDetector;

    public OnRecyclerItemClickListener(Context context, OnItemClickListener listener) {
        this.mListener = listener;
        mGestureDetector = createGestureDetector(context);
    }

    private boolean hasChildViewOnClickListeners(View view, int x, int y) {
        if (!(view instanceof ViewGroup)) return view.hasOnClickListeners();
        ViewGroup viewGroup = (ViewGroup) view;
        for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
            View v = viewGroup.getChildAt(i);
            Rect rect = new Rect();
            v.getHitRect(rect);
            if (!rect.contains(x, y)) continue;
//            Log.v(OnRecyclerItemClickListener.class, v.getClass().getSimpleName(), v.hasOnClickListeners());
            if (v.hasOnClickListeners()) return true;
            if (v instanceof ViewGroup && hasChildViewOnClickListeners(v, x - v.getLeft(), y - v.getTop()))
                return true;
        }
        return false;
    }

    protected GestureDetector createGestureDetector(Context context) {
        return new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
//                Log.v(OnRecyclerItemClickListener.class, e.getX(), e.getY());
                if (childView != null && mListener != null && !hasChildViewOnClickListeners(childView,
                        (int) e.getX() - childView.getLeft(), (int) e.getY() - childView.getTop()))
                    mListener.onItemClick(recyclerView, childView, recyclerView.getChildAdapterPosition(childView));
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListener != null && !hasChildViewOnClickListeners(childView,
                        (int) e.getX() - childView.getLeft(), (int) e.getY() - childView.getTop()))
                    if (mListener instanceof OnItemLongClickListener)
                        ((OnItemLongClickListener) mListener).onLongItemClick(recyclerView, childView, recyclerView.getChildAdapterPosition(childView));
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null && mListener != null && !hasChildViewOnClickListeners(childView,
                        (int) e.getX() - childView.getLeft(), (int) e.getY() - childView.getTop()))
                    if (mListener instanceof OnItemDoubleClickListener)
                        ((OnItemDoubleClickListener) mListener).onItemDoubleClick(recyclerView, childView, recyclerView.getChildAdapterPosition(childView));
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        this.recyclerView = view;
        if (view.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) return false;
        return mGestureDetector.onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}
