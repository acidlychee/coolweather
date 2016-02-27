package com.example.lim.coolweather.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by lim on 2016/1/26.
 */
public class LeftSlideDeleteListView extends ListView {

    private OnListViewItemDeleteClickListener itemDeleteLitener;
    private OnItemClickListener itemClickListener;
    private ListAdapter targetAdapter;
    private OnScrollListener scrollListener;

    public LeftSlideDeleteListView(Context context) {
        super(context);
        init();
    }

    public LeftSlideDeleteListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LeftSlideDeleteListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public interface OnListViewItemDeleteClickListener{
        public void OnListViewItemDeleteClick(int position);
    }

    public  void setOnListViewItemDeleteClickListener(OnListViewItemDeleteClickListener listner){
        this.itemDeleteLitener = listner;
    }

    public  void deleteClick(View v){
        int first = getFirstVisiblePosition();
        int last = getLastVisiblePosition();
        for (int i = 0; i <= last - first; i++ ){
            SlideContainer sc = (SlideContainer) getChildAt(i);
            ViewGroup deleteLinearLayout = (ViewGroup) sc.getChildAt(0);
            if (deleteLinearLayout.getChildAt(0).getTag() == v.getTag()){
                sc.getChildAt(1).setTranslationX(0);
                break;
            };
        }
        itemDeleteLitener.OnListViewItemDeleteClick((Integer) v.getTag());
    }

    public void setOnListItemClickListener(OnItemClickListener listener){
        this.itemClickListener = listener;
    }
    public void setScrollListener(OnScrollListener scrollListener){
        this.scrollListener = scrollListener;
    }

    public void init(){
        super.setOnScrollListener(
                new OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (scrollState == 1){
                            int first = getFirstVisiblePosition();
                            int last  = getLastVisiblePosition();
                            for (int i = 0; i <= last - first ; i++){
                                SlideContainer sc = (SlideContainer)getChildAt(i);
                                translate1(sc.getChildAt(1), sc.getChildAt(1).getTranslationX(), 0);
                                //sc.getChildAt(1).setTranslationX(0);
                            }
                        }
                        if (scrollListener != null){
                            scrollListener.onScrollStateChanged(view, scrollState);
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if (scrollListener != null){
                            scrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                        }
                    }
                }
        );
    }

    public void translate1(View v ,float start, float end){
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, "translationX", start, end);
        animator.setDuration(200);
        animator.start();
    }

    private class SlideContainer extends FrameLayout{
        private TextView deleteTV;
        private float dis;

        public SlideContainer(Context context, View userItemView) {
            super(context);
            dis = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            deleteTV = new TextView(context);
            deleteTV.setText("删除");
            deleteTV.setBackgroundColor(Color.RED);
            deleteTV.setTextColor(Color.WHITE);
            deleteTV.setGravity(Gravity.CENTER);
            deleteTV.setTextSize(TypedValue.COMPLEX_UNIT_SP,22);
            deleteTV.setPadding(dptopx(22, getContext()), 0, dptopx(22, getContext()), 0);

            deleteTV.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteClick(v);
                }
            });
            LinearLayout bottomLayout  = new LinearLayout(context);
            bottomLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            deleteTV.setLayoutParams(params);
            bottomLayout.addView(deleteTV);

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int first = getFirstVisiblePosition();
                    int last = getLastVisiblePosition();

                    for (int i = 0; i <= last - first; i++) {
                        SlideContainer sc = (SlideContainer) LeftSlideDeleteListView.this.getChildAt(i);
                        ViewGroup deleteLinearLayout = (ViewGroup) sc.getChildAt(0);
                        Integer position = (Integer) deleteLinearLayout.getChildAt(0).getTag();
                        if (position == v.getTag()) {
                            if (itemClickListener != null) {
                                itemClickListener.onItemClick(null, null, first + i, 0);
                            }
                            return;
                        }
                    }
                }
            });


            addView(bottomLayout);
            addView(userItemView);
        }

        private int dptopx(int dp, Context context){
            return (int)(dp*(context.getResources().getDisplayMetrics().density)+0.5f);
        }


        private  float downX;
        private  float downY;
        private float downTranslateX;
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {

            switch (ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    downX = ev.getX();
                    downY = ev.getY();
                    downTranslateX = getChildAt(1).getTranslationX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = Math.abs(ev.getX() - downX);
                    float dy = Math.abs(ev.getY() - downY);
                    if (dx > dis && dx > dy){
                        return  true;
                    }
                    break;
            }
            return super.onInterceptTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float moveX = event.getX();
            float moveY = event.getY();
            switch (event.getAction()){
                case MotionEvent.ACTION_MOVE:
                    float dx = Math.abs(event.getX() - downX);
                    float dy = Math.abs(event.getY() - downY);
                    if (dx > dis && dx > dy){
                        getParent().requestDisallowInterceptTouchEvent(true);
                        if (moveX - downX < 0){
                            float mdis = downTranslateX+(moveX - downX);
                            getChildAt(1).setTranslationX(mdis);//移动useritem,显示delete按钮
                        }else{
                            //translate(getChildAt(1), getChildAt(1).getTranslationX(), 0);
                            getChildAt(1).setTranslationX(downTranslateX+(moveX - downX));//当回到触摸开始的位置时，复位。
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dx = Math.abs(moveX - downX);
                    dy = Math.abs(moveY - downY);
                    if (dx < dis && dy < dis){
                        performClick();
                    }else if(dx > deleteTV.getWidth() && moveX - downX < 0 || (getChildAt(1).getTranslationX() + deleteTV.getWidth()) <= 0){
                        translate(getChildAt(1), getChildAt(1).getTranslationX(), -deleteTV.getWidth());
                    }else{
                        translate(getChildAt(1), getChildAt(1).getTranslationX(), 0);
                    }
                    break;

            }
            return true;
        }

        public void translate(View v ,float start, float end){
            ObjectAnimator animator = ObjectAnimator.ofFloat(v, "translationX", start, end);
            animator.setDuration(300);
            animator.start();
        }

        public void setPostionTag(int postionTag) {
            this.deleteTV.setTag(postionTag);
        }
    }


    @Override
    public void setAdapter(ListAdapter adapter) {
        targetAdapter = adapter;
        super.setAdapter(new ProxyAdapter());
    }


    private class ProxyAdapter implements ListAdapter{
        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            targetAdapter.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            targetAdapter.unregisterDataSetObserver(observer);
        }

        @Override
        public int getCount() {
            return targetAdapter.getCount();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return targetAdapter.getItemId(position);
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                View v = targetAdapter.getView(position, convertView, parent);
                SlideContainer sc = new SlideContainer(getContext(), v);
                sc.setTag(position);
                sc.setPostionTag(position);
                return sc;
            }else{
                ViewGroup vg = (ViewGroup) convertView;
                View v = targetAdapter.getView(position,vg.getChildAt(1),parent);
                SlideContainer sc;
                if (vg.getChildAt(1) != v ){
                    sc = new SlideContainer(getContext(), v);
                }else{
                    sc = (SlideContainer) convertView;
                }
                sc.setPostionTag(position);
                sc.setTag(position);
                return sc;
            }
        }

        @Override
        public int getItemViewType(int position) {
            return targetAdapter.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return targetAdapter.getViewTypeCount();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }


}
