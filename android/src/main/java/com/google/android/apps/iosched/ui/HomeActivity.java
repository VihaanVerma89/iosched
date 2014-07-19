package com.google.android.apps.iosched.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.apps.iosched.R;

import static com.google.android.apps.iosched.util.LogUtils.makeLogTag;


public class HomeActivity extends BaseActivity implements ActionBar.TabListener,
        ViewPager.OnPageChangeListener
{
    private static final String TAG = makeLogTag(HomeActivity.class);


    private ViewPager mViewPager;
    private Menu mOptionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isFinishing = isFinishing();

        if(isFinishing)
        {
            return;
        }

        setContentView(R.layout.activity_home);

        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        mViewPager = (ViewPager) findViewById(R.id.pager);

        if(mViewPager != null)
        {

            mViewPager.setAdapter(new HomePagerAdapter(getSupportFragmentManager()));
            mViewPager.setOnPageChangeListener(this);
            mViewPager.setPageMarginDrawable(R.drawable.grey_border_inset_lr);
            mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.page_margin_width));

            final ActionBar actionBar = getSupportActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.addTab(actionBar.newTab().setText(R.string.title_my_schedule).setTabListener(this));
            actionBar.addTab(actionBar.newTab().setText(R.string.title_explore).setTabListener(this));
            actionBar.addTab(actionBar.newTab().setText(R.string.title_stream).setTabListener(this));


        }



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class HomePagerAdapter extends FragmentPagerAdapter
    {

        public HomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position)
            {
                case 0:
                    return new ScheduleFragment();
            }

            return null;
        }

        @Override
        public int getCount() {
            return 0;
        }
    }
}
