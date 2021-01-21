package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.bean.test.TestUserBean;

public class MineFragment extends Fragment {

    private MineViewModel mMineViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mMineViewModel =
                new ViewModelProvider(this).get(MineViewModel.class);
        View root = inflater.inflate(R.layout.fragment_mine, container, false);
        final TextView tvHiName = root.findViewById(R.id.tvHiName);
        mMineViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<TestUserBean>() {
            @Override
            public void onChanged(TestUserBean testUserBean) {
                tvHiName.setText(getString(R.string.hi_name, testUserBean.getUserName()));
            }
        });
        root.findViewById(R.id.clUserDetail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserPageActivity.class);
                intent.putExtra(Constant.USER_INFO, mMineViewModel.getUser().getValue());
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
}