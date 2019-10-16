package com.yoksnod.parallaxbackgroundviewpager;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ParallaxBackgroundViewPager extends ViewPager {

    private static final int DEFAULT_AUTO_SCROLL_PERIOD = 3000;
    private static final int CAROUSEL_ITEMS_MIN_COUNT = 2;
    private final int autoScrollPeriod;
    private final boolean hasAutoScroll;
    private final Handler autoScrollHandler;
    private final Runnable autoScrollMessage;
    private final OnPageChangeListener autoScrollPageListener;
    private int currentPagePosition;

    public ParallaxBackgroundViewPager(Context context) {
        this(context, null);
    }

    public ParallaxBackgroundViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ParallaxBgViewPager, 0, 0);
        try {
            hasAutoScroll = a.getBoolean(R.styleable.ParallaxBgViewPager_autoScroll, false);
            autoScrollPeriod = a.getInt(R.styleable.ParallaxBgViewPager_scrollPeriod, DEFAULT_AUTO_SCROLL_PERIOD);
        } finally {
            a.recycle();
        }
        autoScrollHandler = new Handler();
        autoScrollMessage = new AutoScrollMessage();
        autoScrollPageListener = new AutoScrollPageListener();
        addOnPageChangeListener(autoScrollPageListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoScroll();
        clearOnPageChangeListeners();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);

        if (action == MotionEvent.ACTION_DOWN && hasAutoScroll) {
            stopAutoScroll();
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_OUTSIDE) {
            startAutoScroll();
        }
        return super.dispatchTouchEvent(ev);
    }

    public void startAutoScroll() {
        autoScrollHandler.postDelayed(autoScrollMessage, autoScrollPeriod);
    }

    public void stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollMessage);
    }

    private boolean isLastAutoScrollPosition() {
        PagerAdapter adapter = getAdapter();
        return adapter != null && adapter.getCount() - 1 == currentPagePosition;
    }

    private class AutoScrollPageListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            currentPagePosition = position;
            autoScrollHandler.removeCallbacks(autoScrollMessage);
            autoScrollHandler.postDelayed(autoScrollMessage, autoScrollPeriod);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    private class AutoScrollMessage implements Runnable {
        @Override
        public void run() {
            PagerAdapter adapter = getAdapter();
            if (adapter == null
                    || adapter.getCount() < CAROUSEL_ITEMS_MIN_COUNT
                    || !hasAutoScroll) {
                return;
            }

            if (isLastAutoScrollPosition()) {
                currentPagePosition = 0;
            } else {
                currentPagePosition++;
            }
            setCurrentItem(currentPagePosition, true);
        }

    }
}
