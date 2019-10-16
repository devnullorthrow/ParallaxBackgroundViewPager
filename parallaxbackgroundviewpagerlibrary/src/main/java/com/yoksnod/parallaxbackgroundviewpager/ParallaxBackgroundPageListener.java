package com.yoksnod.parallaxbackgroundviewpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.util.List;
import java.util.NoSuchElementException;

import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;

public class ParallaxBackgroundPageListener implements ViewPager.OnPageChangeListener {

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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        recalculateScrollDirection(positionOffset, positionOffsetPixels);

        int scrollX = pager.getScrollX();
        if (canScrollToLeft(scrollX) || isRightEdge(scrollX)) {
            return;
        }

        int childrenCount = pager.getChildCount();
        int animatedItemIndex = isScrollToRight ? currentPagePosition : currentPagePosition - 1;
        for (int index = 0; index < childrenCount; index++) {
            View child = pager.getChildAt(index);
            ViewPager.LayoutParams lp = (ViewPager.LayoutParams) child.getLayoutParams();
            applyCurrentTransformPosition(lp, child, scrollX, animatedItemIndex);
        }

        if (isLeftEdge(scrollX)) {
            restoreInitialAlphaValues();
        }
    }

    private boolean canScrollToLeft(int scrollX) {
        return isLeftEdge(scrollX) && !isScrollToRight;
    }

    private void applyCurrentTransformPosition(ViewPager.LayoutParams lp,
                                               View child,
                                               int scrollX,
                                               int animatedItemIndex) {
        if (lp.isDecor) {
            return;
        }

        float transformPos = (float) (child.getLeft() - scrollX) / (float) getClientWidth();
        int fragIndex = findFragmentIndexByView(child);
        if (fragIndex == animatedItemIndex) {
            initCurrentAlpha(transformPos, fragIndex);
        }
    }


    private int findFragmentIndexByView(View currentView) {
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            String tag = ANDROID_SWITCHER_TAG_SEGMENT + R.id.pager + SEPARATOR_TAG_SEGMENT + i;
            Fragment page = activity.getSupportFragmentManager().findFragmentByTag(tag);
            if (page == null)
                throw new NoSuchElementException("no such element for tag  : " + tag);

            if (page.getView() == currentView) {
                return i;
            }

        }
        throw new IllegalArgumentException(String.valueOf(currentView));
    }


    private boolean isLeftEdge(int scrollX) {
        return scrollX == 0;
    }

    private void initCurrentAlpha(float transformPos, int itemIndex) {
        ImageView currentImage = activity.findViewById(items.get(itemIndex));
        if ((transformPos <= MINUS_INFINITY_EDGE || transformPos >= PLUS_INFINITY_EDGE)) {
            currentImage.setAlpha(ALPHA_TRANSPARENT);
        } else if (transformPos == SETTLED_PAGE_POSITION) {
            currentImage.setAlpha(ALPHA_OPAQUE);
        } else {
            currentImage.setAlpha(ALPHA_OPAQUE - Math.abs(transformPos));
        }
    }

    private boolean isRightEdge(int scrollX) {
        return scrollX == pager.getWidth() * adapter.getCount();
    }


    private void restoreInitialAlphaValues() {
        for (int j = items.size() - 1; j >= 0; j--) {
            activity.findViewById(items.get(j)).setAlpha(ALPHA_OPAQUE);
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
            onPageScrollStateChanged(SCROLL_STATE_IDLE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        currentPageScrollState = state;
        if (state == SCROLL_STATE_IDLE) {
            currentPagePosition = pager.getCurrentItem();
            isScrollToRight = true;
        }

        if (!isScrollStarted && currentPageScrollState == ViewPager.SCROLL_STATE_DRAGGING) {
            isScrollStarted = true;
            shouldCalculateScrollDirection = true;
        } else {
            isScrollStarted = false;
        }
    }
}
