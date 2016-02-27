package com.example.lim.coolweather.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.lim.coolweather.R;
import com.example.lim.coolweather.adapter.WeatherAdapter;
import com.example.lim.coolweather.model.WeatherInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lim on 2016/1/15.
 */
public class RefreshableView extends LinearLayout implements View.OnTouchListener {
    private final int STATUS_REFRESH_FINISHED = 4;
    private final SharedPreferences preferences;
    private final View header;
    private final ProgressBar processBar;
    private final ImageView arrow;
    private final TextView description;
    private final TextView updateAt;
    private final int touchSlop;
    private final int STATUS_REFRESHING = 1;
    private final int STATUS_PULL_TO_REFRESH = 2;
    private final int STATUS_RELEASE_TO_REFRESH = 3;
    private ViewGroup.MarginLayoutParams layoutParams;
    private boolean loadOnce = false;
    private int hideHeaderHeight;
    private ListView listView;
    private boolean ableToPull;
    private float yDown;
    private int currentStatus;

    public WeatherAdapter adapter;
    public String cityName;
    public List<WeatherInfo> mList;
    public WeatherInfo weatherInfo;
    /**
     * 一分钟的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_MINUTE = 60 * 1000;

    /**
     * 一小时的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_HOUR = 60 * ONE_MINUTE;

    /**
     * 一天的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_DAY = 24 * ONE_HOUR;

    /**
     * 一月的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_MONTH = 30 * ONE_DAY;

    /**
     * 一年的毫秒值，用于判断上次的更新时间
     */
    public static final long ONE_YEAR = 12 * ONE_MONTH;

    /**
     * 上次更新时间的字符串常量，用于作为SharedPreferences的键值
     */
    private static final String UPDATED_AT = "updated_at";
    /**
     * 为了防止不同界面的下拉刷新在上次更新时间上互相有冲突，使用id来做区分
     */
    private int mId = -1;
    /**
     * 上次更新时间的毫秒值
     */
    private long lastUpdateTime;

    /**
     * 记录上一次的状态是什么，避免进行重复操作
     */
    private int lastStatus = currentStatus;
    /**
     * 下拉头部回滚的速度
     */
    public static final int SCROLL_SPEED = -20;
    private PullToRefreshListener mListener;

    public RefreshableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        header = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh, null, true);
        processBar = (ProgressBar)header.findViewById(R.id.process_bar);
        arrow = (ImageView) header.findViewById(R.id.arrow);
        description = (TextView) header.findViewById(R.id.description);
        updateAt = (TextView) header.findViewById(R.id.updated_at);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mList = new ArrayList<>();
        refreshUpdatedAtValue();
        setOrientation(VERTICAL);
        addView(header, 0);
    }

    private void refreshUpdatedAtValue() {
        lastUpdateTime = preferences.getLong(UPDATED_AT + mId, -1);
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastUpdateTime;
        long timeIntoFormat;
        String updateAtValue;
        if (lastUpdateTime == -1) {
            updateAtValue = getResources().getString(R.string.not_updated_yet);
        } else if (timePassed < 0) {
            updateAtValue = getResources().getString(R.string.time_error);
        } else if (timePassed < ONE_MINUTE) {
            updateAtValue = getResources().getString(R.string.updated_just_now);
        } else if (timePassed < ONE_HOUR) {
            timeIntoFormat = timePassed / ONE_MINUTE;
            String value = timeIntoFormat + "分钟";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        } else if (timePassed < ONE_DAY) {
            timeIntoFormat = timePassed / ONE_HOUR;
            String value = timeIntoFormat + "小时";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        } else if (timePassed < ONE_MONTH) {
            timeIntoFormat = timePassed / ONE_DAY;
            String value = timeIntoFormat + "天";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        } else if (timePassed < ONE_YEAR) {
            timeIntoFormat = timePassed / ONE_MONTH;
            String value = timeIntoFormat + "个月";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        } else {
            timeIntoFormat = timePassed / ONE_YEAR;
            String value = timeIntoFormat + "年";
            updateAtValue = String.format(getResources().getString(R.string.updated_at), value);
        }
        updateAt.setText(updateAtValue);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !loadOnce){
            hideHeaderHeight = -header.getHeight();
            layoutParams = (MarginLayoutParams)header.getLayoutParams();
            layoutParams.topMargin = hideHeaderHeight;
            listView = (ListView) getChildAt(1);
            listView.setOnTouchListener(this);
            loadOnce = true;
            header.setLayoutParams(layoutParams);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        setIsAleToPull(event);
        if (ableToPull){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    yDown = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float yMove = event.getRawY();
                    int distance = (int)(yMove - yDown);
                    if (distance <= 0 && layoutParams.topMargin <= hideHeaderHeight){
                        return  false;
                    }
                    if (distance < touchSlop){
                        return false;
                    }
                    if (currentStatus != STATUS_REFRESHING){
                        if (layoutParams.topMargin > 0){
                            currentStatus = STATUS_RELEASE_TO_REFRESH;
                        }else{
                            currentStatus = STATUS_PULL_TO_REFRESH;
                        }
                    }
                    layoutParams.topMargin = distance/2 + hideHeaderHeight;
                    header.setLayoutParams(layoutParams);
                    break;
                case MotionEvent.ACTION_UP:
                    if (currentStatus == STATUS_RELEASE_TO_REFRESH){
                        new RefreshTask().execute();
                    }else if(currentStatus == STATUS_PULL_TO_REFRESH){
                        new HideHeaderTask().execute();
                    }
            }
            if (currentStatus == STATUS_RELEASE_TO_REFRESH || currentStatus == STATUS_PULL_TO_REFRESH){
                updateHeaderView();
                listView.setPressed(false);
                listView.setFocusable(false);
                listView.setFocusableInTouchMode(false);
                lastStatus = currentStatus;
                return true;
            }

        }
        return false;
    }

    private void updateHeaderView() {
            if (lastStatus != currentStatus){
                if (currentStatus == STATUS_PULL_TO_REFRESH){
                    description.setText(R.string.pull_to_refresh);
                    processBar.setVisibility(GONE);
                    arrow.setVisibility(VISIBLE);
                    rotateArrow();
                }else if(currentStatus == STATUS_RELEASE_TO_REFRESH){
                    description.setText(R.string.release_to_refresh);
                    processBar.setVisibility(GONE);
                    arrow.setVisibility(VISIBLE);
                    rotateArrow();
                }else if (currentStatus == STATUS_REFRESHING){
                    description.setText(R.string.refreshing);
                    arrow.setVisibility(GONE);
                    processBar.setVisibility(VISIBLE);
                    arrow.clearAnimation();//??????????
                }
                refreshUpdatedAtValue();
            }
    }

    private void rotateArrow() {
        float pivotX = arrow.getWidth()/2;
        float pivotY = arrow.getHeight()/2;
        float fromDegree = 0;
        float toDeree = 0;
        if (currentStatus == STATUS_PULL_TO_REFRESH){
            fromDegree = 180;
            toDeree = 360;
        }else if(currentStatus == STATUS_RELEASE_TO_REFRESH){
            toDeree = 180;
            fromDegree = 360;
        }
        RotateAnimation animation = new RotateAnimation(fromDegree, toDeree, pivotX, pivotY);
        animation.setDuration(100);
        animation.setFillAfter(true);
        arrow.startAnimation(animation);
    }

    private void setIsAleToPull(MotionEvent event) {
        View firstChild = listView.getChildAt(0);
        if (firstChild != null){
            int firstVisiblePos = listView.getFirstVisiblePosition();
            if (firstVisiblePos == 0 && firstChild.getTop() == 0){
                if (!ableToPull){
                    yDown = event.getRawY();
                }
                ableToPull = true;
            }else{
                if (layoutParams.topMargin != hideHeaderHeight) {
                    layoutParams.topMargin = hideHeaderHeight;
                    header.setLayoutParams(layoutParams);
                }
                ableToPull = false;
            }
        }else{
            ableToPull = true;
        }
    }


    private class HideHeaderTask extends AsyncTask<Void, Integer, Integer>{
        public HideHeaderTask() {
            super();
        }

        @Override
        protected Integer doInBackground(Void[] params) {
            int marginTop = layoutParams.topMargin;
            //header隐藏
            while (true){
                marginTop =  marginTop+SCROLL_SPEED;
                if (marginTop <= hideHeaderHeight){
                    marginTop = hideHeaderHeight;
                    break;
                }
                publishProgress(marginTop);
            }
            currentStatus = STATUS_REFRESH_FINISHED;
            return marginTop;
        }

        @Override
        protected void onProgressUpdate(Integer[] values) {
            layoutParams.topMargin = values[0];
            header.setLayoutParams(layoutParams);
        }

        @Override
        protected void onPostExecute(Integer topMargin) {
            layoutParams.topMargin =  topMargin;
            header.setLayoutParams(layoutParams);
            currentStatus = STATUS_REFRESH_FINISHED;
        }

        private void sleep(int time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class RefreshTask  extends AsyncTask<Void, Integer, Integer>{
        public RefreshTask() {
            super();
        }

        @Override
        protected Integer doInBackground(Void[] params) {
            int marginTop = layoutParams.topMargin;
            //先把header回滚到顶部
            while (true){
                marginTop = marginTop+SCROLL_SPEED;
                if (marginTop <= 0){
                    marginTop = 0;
                    break;
                }
                publishProgress(marginTop);
            }
            publishProgress(0);
            currentStatus = STATUS_REFRESHING;
            if (mListener != null){
                mListener.onRefresh();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer[] values) {
            updateHeaderView();
            layoutParams.topMargin = values[0];
            header.setLayoutParams(layoutParams);
        }

        private void sleep(int time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void refreshFinish(){
        currentStatus = STATUS_REFRESH_FINISHED;
        preferences.edit().putLong(UPDATED_AT + mId, System.currentTimeMillis()).commit();
        new HideHeaderTask().execute();
    }

    public interface  PullToRefreshListener{
        public void onRefresh();
    }
    public void setOnRefreshListener(PullToRefreshListener listener, int id){
        mListener = listener;
        mId = id;
    }
}
