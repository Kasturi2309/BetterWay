package com.android.betterway.showscheduleactivity.view;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.android.betterway.R;
import com.android.betterway.data.MyDate;
import com.android.betterway.data.NewPlan;
import com.android.betterway.showscheduleactivity.Impel.ShowScheduleImpel;
import com.android.betterway.showscheduleactivity.daggerneed.DaggerShowScheduleImpelCompont;
import com.android.betterway.showscheduleactivity.daggerneed.ShowScheduleImpelCompont;
import com.android.betterway.showscheduleactivity.daggerneed.ShowScheduleImpelModule;
import com.android.betterway.utils.LogUtil;
import com.android.betterway.utils.TimeUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.WeakHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowScheduleActivity extends AppCompatActivity implements ShowScheduleView,
        ViewPager.OnPageChangeListener, TabLayout.OnTabSelectedListener{

    @BindView(R.id.toolbar_show_schedule)
    Toolbar mToolbarShowSchedule;
    @BindView(R.id.tabs_layout)
    TabLayout mTabsLayout;
    @BindView(R.id.appbar)
    AppBarLayout mAppbar;
    @BindView(R.id.viewpager)
    ViewPager mViewpager;
    private ShowScheduleImpel mShowScheduleImpel;
    int weatherStore;
    private WeakHashMap<String, WeakReference<Application>> mStringWeakReferenceWeakHashMap =
            new WeakHashMap<>();
    private ScheduleDetailFragment mScheduleDetailFragment;
    private ScheduleMapFragment mScheduleMapFragment;
    private long key;
    public static int NEW = 101, OLD = 102;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_schedule);
        ButterKnife.bind(this);
        init();
    }

    /**
     * 初始化控件
     */
    private void init() {
        setSupportActionBar(mToolbarShowSchedule);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        ArrayList<NewPlan> newPlans = intent.getParcelableArrayListExtra("list");
        long date = intent.getLongExtra("datelong", 20171111);
        weatherStore = intent.getIntExtra("weatherStore", 101);
        if (weatherStore == 102) {
            key = intent.getLongExtra("key", 0);
        }
        MyDate myDate = TimeUtil.intToMyDate((int)date);
        String city = intent.getStringExtra("city");
        mToolbarShowSchedule.setSubtitle(myDate.toSingleString() + "(" + city + ")");
        ShowScheduleImpelCompont showScheduleImpelCompont = DaggerShowScheduleImpelCompont.builder()
                .showScheduleImpelModule(new ShowScheduleImpelModule(this, city))
                .build();
        showScheduleImpelCompont.inject(this);
        mShowScheduleImpel = showScheduleImpelCompont.getShowScheduleImpel();
        mShowScheduleImpel.initData(newPlans, weatherStore);
        Bundle bundleDetail = new Bundle();
        bundleDetail.putParcelableArrayList("NewPlanList", mShowScheduleImpel.getNewPlanList());
        mScheduleDetailFragment = new ScheduleDetailFragment();
        mScheduleDetailFragment.setArguments(bundleDetail);
        Bundle bundleMap = new Bundle();
        bundleMap.putParcelableArrayList("latlnglist", mShowScheduleImpel.getLatLngList());
        mScheduleMapFragment = new ScheduleMapFragment();
        mScheduleMapFragment.setArguments(bundleMap);
        mViewpager.addOnPageChangeListener(this);
        mTabsLayout.addOnTabSelectedListener(this);
        mViewpager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return mScheduleDetailFragment;
                    case 1:
                        return mScheduleMapFragment;
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_schedule_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.delete:
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage("确定要删除此路书吗？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (weatherStore == NEW) {
                                    mShowScheduleImpel.deleteSchedule();
                                } else {
                                    mShowScheduleImpel.delteScheduleByKey(key);
                                }
                                finish();
                            }
                        })
                        .create();
                dialog.show();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public Bitmap getScreenShot() {
        return null;
    }

    @Override
    public Context returnApplicationContext() {
        return getApplicationContext();
    }

    @Override
    public Context returnContext() {
        return this;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mTabsLayout.getTabAt(position);
    }
    @Override
    public void onPageSelected(int position) {
        mTabsLayout.getTabAt(position).select();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewpager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public Application returnApplicatin() {
        if (mStringWeakReferenceWeakHashMap.get("application") == null) {
            WeakReference<Application> weakReference = new WeakReference<Application>(getApplication());
            mStringWeakReferenceWeakHashMap.put("application", weakReference);
            return weakReference.get();
        } else {
            return mStringWeakReferenceWeakHashMap.get("application").get();
        }
    }
}
