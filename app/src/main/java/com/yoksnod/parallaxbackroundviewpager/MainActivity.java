package com.yoksnod.parallaxbackroundviewpager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import com.yoksnod.parallaxbackgroundviewpager.ParallaxBackgroundPageListener;
import com.yoksnod.parallaxbackgroundviewpager.ParallaxBackgroundViewPager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParallaxBackgroundViewPager pager = findViewById(R.id.pager);

        final List<Integer> items = new ArrayList<>();
        items.add(R.id.img3);
        items.add(R.id.img2);
        items.add(R.id.img1);

        FragmentPagerAdapter adapter = new ParallaxFragmentPagerAdapter(getSupportFragmentManager(), items);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ParallaxBackgroundPageListener(this, pager, adapter, items));
//        pager.startAutoScroll();
    }

    private class ParallaxFragmentPagerAdapter extends FragmentPagerAdapter {

        private final List<Integer> items;

        public ParallaxFragmentPagerAdapter(FragmentManager fragmentManager, List<Integer> items) {
            super(fragmentManager);
            this.items = items;
        }

        @Override
        public Fragment getItem(int i) {
            return new PictureFragment();
        }


        @Override
        public int getCount() {
            return items.size();
        }
    }
}
