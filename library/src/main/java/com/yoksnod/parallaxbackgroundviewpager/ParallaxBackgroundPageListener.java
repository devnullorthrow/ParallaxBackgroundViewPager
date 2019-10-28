package com.yoksnod.parallaxbackgroundviewpager;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

public class ParallaxBackgroundPageListener implements ViewPager.OnPageChangeListener {

    private static final String TAG = "ParallaxListener";
    private static final String ANDROID_SWITCHER_TAG_SEGMENT = "android:switcher:";
    private static final String SEPARATOR_TAG_SEGMENT = ":";
    private static final float POSITION_OFFSET_BASE = 0.5f;
    private static final int POSITION_OFFSET_PIXELS_BASE = 1;
    private static final float ALPHA_TRANSPARENT = 0.0F;
    private static final float ALPHA_OPAQUE = 1.0f;
    private static final int MINUS_INFINITY_EDGE = -1;
    private static final int PLUS_INFINITY_EDGE = 1;
    private static final float SETTLED_PAGE_POSITION = 0.0f;
    private final ViewPager pager;
    private final FragmentPagerAdapter adapter;
    private final AppCompatActivity activity;
    private final List<Integer> items;

    private int currentPageScrollState;
    private int currentPagePosition;
    private boolean isScrollToRight = true;
    private boolean isScrollStarted;
    private boolean shouldCalculateScrollDirection;

    public ParallaxBackgroundPageListener(AppCompatActivity activity,
                                          ViewPager pager,
                                          FragmentPagerAdapter adapter,
                                          List<Integer> items) {
        this.pager = pager;
        this.adapter = adapter;
        this.activity = activity;
        this.items = items;
    }

    private static String makePagerFragmentTag(int index, int pagerId) {
        return ANDROID_SWITCHER_TAG_SEGMENT + pagerId + SEPARATOR_TAG_SEGMENT + index;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Log.d(TAG, "onPageScrolled position = " + position + " currentPagePosition = " + currentPagePosition + " shouldCalculateScrollDirection = " + shouldCalculateScrollDirection);
        recalculateScrollDirection(positionOffset, positionOffsetPixels);

        int scrollX = pager.getScrollX();
        int animatedItemIndex = isScrollToRight
                ? Math.min(currentPagePosition, adapter.getCount() - 1)
                : Math.max(0, currentPagePosition - 1);
        Log.d(TAG, "onPageScrolled isRight = " + isScrollToRight + " anim index = " + animatedItemIndex);

        setAlpha(animatedItemIndex, scrollX);


        if (isLeftEdge(scrollX)) {
            restoreInitialAlphaValues();
        }
    }

    private void setAlpha(int animatedItemIndex, int scrollX) {
        View child = findFragmentViewByIndex(animatedItemIndex);
        if (child == null)
            return;
        ViewPager.LayoutParams lp = (ViewPager.LayoutParams) child.getLayoutParams();
        if (lp.isDecor) {
            return;
        }
        float transformPos = (float) (child.getLeft() - scrollX) / (float) getClientWidth();
        initCurrentAlpha(transformPos, animatedItemIndex);
    }

    @Nullable
    private View findFragmentViewByIndex(int index) {
        String tag = makePagerFragmentTag(index, pager.getId());
        Fragment page = activity.getSupportFragmentManager().findFragmentByTag(tag);
        if (page == null || page.getView() == null)
            return null;

        return page.getView();
    }

    private boolean isLeftEdge(int scrollX) {
        return scrollX == 0;
    }

    private void initCurrentAlpha(float transformPos, int itemIndex) {
        ImageView currentImage = activity.findViewById(items.get(itemIndex));
        if (transformPos <= MINUS_INFINITY_EDGE || transformPos >= PLUS_INFINITY_EDGE) {
            currentImage.setAlpha(ALPHA_TRANSPARENT);
        } else if (transformPos == SETTLED_PAGE_POSITION) {
            currentImage.setAlpha(ALPHA_OPAQUE);
        } else {
            currentImage.setAlpha(ALPHA_OPAQUE - Math.abs(transformPos));
        }
    }


    private void restoreInitialAlphaValues() {
        for (int j = items.size() - 1; j >= 0; j--) {
            View view = activity.findViewById(items.get(j));
            view.setAlpha(ALPHA_OPAQUE);
        }
    }

    private void recalculateScrollDirection(float positionOffset, int positionOffsetPixels) {
        if (shouldCalculateScrollDirection) {
            isScrollToRight = POSITION_OFFSET_BASE > positionOffset && positionOffsetPixels > POSITION_OFFSET_PIXELS_BASE;
            shouldCalculateScrollDirection = false;
        }
    }

    private int getClientWidth() {
        return pager.getMeasuredWidth() - pager.getPaddingLeft() - pager.getPaddingRight();
    }


    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            onPageScrollStateChanged(ViewPager.SCROLL_STATE_IDLE);
        }

        Log.d(TAG, "onPageSelected position = " + position + " currentPagePosition = " + currentPagePosition + " pager current item = " + pager.getCurrentItem());

        if (Math.abs(currentPagePosition - position) > 1) {
            currentPagePosition = isScrollToRight
                    ? Math.max(0, position - 1)
                    : Math.min(position + 1, adapter.getCount() - 1);
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {
        currentPageScrollState = state;
        Log.d(TAG, "onPageScrollStateChanged state = " + PagerState.values()[state] + " currentPagePosition = " + currentPagePosition + " pager current item = " + pager.getCurrentItem());


        if (state == ViewPager.SCROLL_STATE_IDLE) {
            currentPagePosition = pager.getCurrentItem();
        }

        boolean isIdle = isIdleScroll();
        isScrollStarted = isIdle;
        if (isIdle) {
            shouldCalculateScrollDirection = true;
        }
    }

    private boolean isIdleScroll() {
        return !isScrollStarted && currentPageScrollState == ViewPager.SCROLL_STATE_IDLE;
    }

    private enum PagerState {
        SCROLL_STATE_IDLE,
        SCROLL_STATE_DRAGGING,
        SCROLL_STATE_SETTLING
    }
}
