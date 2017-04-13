package edu.wust.lynn.classcontacts.tab;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.browse.DormitoryFragment;
import edu.wust.lynn.classcontacts.browse.StudentFragment;
import edu.wust.lynn.classcontacts.browse.CourseFragment;

public class ContactsActivity extends FragmentActivity implements ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private View mCustomView;
    private ImageView mScrollStudent;
    private ImageView mScrollDormitory;
    private ImageView mScrollCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setPageMargin(8);
        mViewPager.setPageMarginDrawable(android.R.drawable.divider_horizontal_bright);

        mCustomView = getLayoutInflater().inflate(R.layout.custom_tab_view, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ActionBar actionBar = getParent().getActionBar();
        actionBar.setCustomView(mCustomView);
        mScrollStudent = (ImageView) mCustomView.findViewById(R.id.scroll_1);
        mScrollDormitory = (ImageView) mCustomView.findViewById(R.id.scroll_2);
        mScrollCourse = (ImageView) mCustomView.findViewById(R.id.scroll_3);

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        setCurrentScroll(mViewPager.getCurrentItem());
    }

    public void onTabClick(View view) {
        switch (view.getId()) {
            case R.id.tab_student:
                mViewPager.setCurrentItem(0, false);
                break;
            case R.id.tab_dormitory:
                mViewPager.setCurrentItem(1, false);
                break;
            case R.id.tab_course:
                mViewPager.setCurrentItem(2, false);
                break;
        }
    }

    private void setCurrentScroll(int selection) {
        if (mScrollStudent != null && mScrollDormitory != null && mScrollCourse != null) {
            mScrollStudent.setVisibility(selection == 0 ? View.VISIBLE : View.INVISIBLE);
            mScrollDormitory.setVisibility(selection == 1 ? View.VISIBLE : View.INVISIBLE);
            mScrollCourse.setVisibility(selection == 2 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        setCurrentScroll(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch(position) {
                case 0:
                    fragment = new StudentFragment();
                    break;
                case 1:
                    fragment = new DormitoryFragment();
                    break;
                case 2:
                    fragment = new CourseFragment();
                    break;
                default:
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}