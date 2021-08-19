package com.revolo.lock.ui.device.lock.setting.geofence;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.SPUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.adapter.AutoLockViewPagerAdatper;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.view.AutoLockLoadHintView;

import java.util.ArrayList;
import java.util.List;

public class AutoLockLoadActivity extends BaseActivity implements View.OnClickListener {

    private BleDeviceLocal mBleDeviceLocal;
    private int mCurrIndex = 0;
    private ViewPager viewPager;
    private AutoLockViewPagerAdatper pagerAdatper;
    private List<View> viewList;
    private View mItem1View, mItem2View, mItem3View, mItem4View, mItem5View, mItem6View;
    //1
    private TextView mItem1Text, mItem2Text, mItem22Text, mItem3Text, mItem4Text, mItem5Text, mItem6Text;
    //2
    private ImageView mItem1Image, mItem2Image, mItem3Image, mItem4Image, mItem5Image, mItem6Image;
    //3
    private Button mItem1But, mItem2But, mItem3But, mItem4But, mItem5But, mItem6But;
    //4
    private ImageView mItem1HintView, mItem2HintView, mItem3HintView, mItem4HintView, mItem5HintView, mItem6HintView;
    //5


    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_auto_unlock_load_activity;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_geo_fence_unlock));
        viewPager = findViewById(R.id.auto_lock_load_viewpager);
        viewList = new ArrayList<>();
        initView();
        pagerAdatper = new AutoLockViewPagerAdatper(viewList);
        viewPager.setAdapter(pagerAdatper);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mCurrIndex = 0;
        viewPager.setCurrentItem(0);
    }

    private void initView() {
        mItem1View = LayoutInflater.from(AutoLockLoadActivity.this).inflate(R.layout.auto_lock_load_viewpager_item_view, null);
        mItem2View = LayoutInflater.from(AutoLockLoadActivity.this).inflate(R.layout.auto_lock_load_viewpager_item_view, null);
        mItem3View = LayoutInflater.from(AutoLockLoadActivity.this).inflate(R.layout.auto_lock_load_viewpager_item_view, null);
        mItem4View = LayoutInflater.from(AutoLockLoadActivity.this).inflate(R.layout.auto_lock_load_viewpager_item_view, null);
        mItem5View = LayoutInflater.from(AutoLockLoadActivity.this).inflate(R.layout.auto_lock_load_viewpager_item_view, null);
        mItem6View = LayoutInflater.from(AutoLockLoadActivity.this).inflate(R.layout.auto_lock_load_viewpager_item_view, null);
        mItem1Text = mItem1View.findViewById(R.id.auto_lock_load_hint_text);
        mItem2Text = mItem2View.findViewById(R.id.auto_lock_load_hint_text);
        mItem22Text = mItem2View.findViewById(R.id.auto_lock_load_hint_2text);
        mItem22Text.setVisibility(View.VISIBLE);
        mItem3Text = mItem3View.findViewById(R.id.auto_lock_load_hint_text);
        mItem4Text = mItem4View.findViewById(R.id.auto_lock_load_hint_text);
        mItem5Text = mItem5View.findViewById(R.id.auto_lock_load_hint_text);
        mItem6Text = mItem6View.findViewById(R.id.auto_lock_load_hint_text);

        mItem1Image = mItem1View.findViewById(R.id.auto_lock_load_hint_icon);
        mItem2Image = mItem2View.findViewById(R.id.auto_lock_load_hint_icon);
        mItem3Image = mItem3View.findViewById(R.id.auto_lock_load_hint_icon);
        mItem4Image = mItem4View.findViewById(R.id.auto_lock_load_hint_icon);
        mItem5Image = mItem5View.findViewById(R.id.auto_lock_load_hint_icon);
        mItem6Image = mItem6View.findViewById(R.id.auto_lock_load_hint_icon);

        mItem1Image.setBackgroundResource(R.mipmap.auto_unlock_load_1_icon);
        mItem2Image.setBackgroundResource(R.mipmap.auto_unlock_load_2_icon);
        mItem3Image.setBackgroundResource(R.mipmap.auto_unlock_load_3_icon);
        mItem4Image.setBackgroundResource(R.mipmap.auto_unlock_load_4_icon);
        mItem5Image.setBackgroundResource(R.mipmap.auto_unlock_load_5_icon);
        mItem6Image.setBackgroundResource(R.mipmap.auto_unlock_load_6_icon);

        mItem1But = mItem1View.findViewById(R.id.auto_lock_load_btnNext);
        mItem2But = mItem2View.findViewById(R.id.auto_lock_load_btnNext);
        mItem3But = mItem3View.findViewById(R.id.auto_lock_load_btnNext);
        mItem4But = mItem4View.findViewById(R.id.auto_lock_load_btnNext);
        mItem5But = mItem5View.findViewById(R.id.auto_lock_load_btnNext);
        mItem6But = mItem6View.findViewById(R.id.auto_lock_load_btnNext);
        mItem1HintView = mItem1View.findViewById(R.id.auto_lock_load_hint_back);
        mItem2HintView = mItem2View.findViewById(R.id.auto_lock_load_hint_back);
        mItem3HintView = mItem3View.findViewById(R.id.auto_lock_load_hint_back);
        mItem4HintView = mItem4View.findViewById(R.id.auto_lock_load_hint_back);
        mItem5HintView = mItem5View.findViewById(R.id.auto_lock_load_hint_back);
        mItem6HintView = mItem6View.findViewById(R.id.auto_lock_load_hint_back);
        mItem1Text.setText(getString(R.string.auto_lock_load_hint_1_text));
        mItem2Text.setText(getString(R.string.auto_lock_load_hint_2_text));
        mItem3Text.setText(getString(R.string.auto_lock_load_hint_3_text));
        mItem4Text.setText(getString(R.string.auto_lock_load_hint_4_text));
        mItem5Text.setText(getString(R.string.auto_lock_load_hint_5_text));
        mItem6Text.setText(getString(R.string.auto_lock_load_hint_6_text));

        mItem1HintView.setBackgroundResource(R.mipmap.vector_drawable_geo_loading_1_icon);
        mItem2HintView.setBackgroundResource(R.mipmap.vector_drawable_geo_loading_2_icon);
        mItem3HintView.setBackgroundResource(R.mipmap.vector_drawable_geo_loading_3_icon);
        mItem4HintView.setBackgroundResource(R.mipmap.vector_drawable_geo_loading_4_icon);
        mItem5HintView.setBackgroundResource(R.mipmap.vector_drawable_geo_loading_5_icon);
        mItem6HintView.setBackgroundResource(R.mipmap.vector_drawable_geo_loading_6_icon);
        mItem1But.setOnClickListener(this);
        mItem2But.setOnClickListener(this);
        mItem3But.setOnClickListener(this);
        mItem4But.setOnClickListener(this);
        mItem5But.setOnClickListener(this);
        mItem6But.setOnClickListener(this);
        viewList.add(mItem1View);
        viewList.add(mItem2View);
        viewList.add(mItem3View);
        viewList.add(mItem4View);
        viewList.add(mItem5View);
        viewList.add(mItem6View);

    }

    @Override
    public void doBusiness() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e("dddddddddddd", "keycode:" + keyCode + ";" + event.getRepeatCount());
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v == mItem1But) {
            mCurrIndex = 1;
            viewPager.setCurrentItem(1);
        } else if (v == mItem2But) {
            mCurrIndex = 2;
            viewPager.setCurrentItem(2);
        } else if (v == mItem3But) {
            mCurrIndex = 3;
            viewPager.setCurrentItem(3);
        } else if (v == mItem4But) {
            mCurrIndex = 4;
            viewPager.setCurrentItem(4);
        } else if (v == mItem5But) {
            mCurrIndex = 5;
            viewPager.setCurrentItem(5);
        } else if (v == mItem6But) {
            SPUtils.getInstance().put("SHOW_GEOFENCE_LOADING", "true");
            Intent intent = new Intent(this, AutoUnlockActivity.class);
            startActivity(intent);
            finish();
        }
    }
}