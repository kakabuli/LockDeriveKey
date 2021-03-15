package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.TimeUtils;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.bean.test.TestUserBean;
import com.revolo.lock.room.entity.User;

public class MineFragment extends Fragment {

    private MineViewModel mMineViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mMineViewModel =
                new ViewModelProvider(this).get(MineViewModel.class);
        View root = inflater.inflate(R.layout.fragment_mine, container, false);
        final TextView tvHiName = root.findViewById(R.id.tvHiName);
        final TextView tvDayDetail = root.findViewById(R.id.tvDayDetail);
        mMineViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                String userName = user.getFirstName();
                // TODO: 2021/3/7 名字后面需要更改其他显示
                tvHiName.setText(getString(R.string.hi_name, TextUtils.isEmpty(userName)?"":userName));
                long registerTime = user.getRegisterTime();
                tvDayDetail.setText(getString(R.string.day_detail, daysBetween(TimeUtils.getNowMills(), registerTime)));
            }
        });
        root.findViewById(R.id.clUserDetail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserPageActivity.class);
                startActivity(intent);
            }
        });
        root.findViewById(R.id.clMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MessageListActivity.class));
            }
        });
        root.findViewById(R.id.clSetting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), SettingActivity.class));
            }
        });
        root.findViewById(R.id.clAbout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AboutActivity.class));
            }
        });
        root.findViewById(R.id.clFeedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), FeedbackActivity.class));
            }
        });
        return root;
    }

    //计算间隔日，比较两个时间是否同一天，如果两个时间都是同一天的话，返回0。
    // 两个比较的时间都不是同一天的话，根据传参位置 可返回正数/负数。两个比较的时间都是同一天的话，返回0。
    private static int daysBetween(long now, long createTime) {
        return (int) ((now - createTime)/(1000*3600*24));
    }

}