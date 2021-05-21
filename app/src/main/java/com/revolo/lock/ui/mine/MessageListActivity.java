package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.MessageListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.test.TestMessageBean;
import com.revolo.lock.widget.SlideRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Jack
 * time   : 2021/1/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 信息列表
 */
public class MessageListActivity extends BaseActivity {

    private MessageListAdapter mMessageListAdapter;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_message_list;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_message));

        SlideRecyclerView rvMessage = findViewById(R.id.rvMessage);
        rvMessage.setLayoutManager(new LinearLayoutManager(this));
        mMessageListAdapter = new MessageListAdapter(R.layout.item_message_rv);
        mMessageListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (position >= 0 && adapter.getItemCount() >= position && adapter.getItem(position) != null) {
                TestMessageBean testMessageBean = (TestMessageBean) adapter.getItem(position);
                Intent intent = new Intent(MessageListActivity.this, MessageDetailActivity.class);
                intent.putExtra(Constant.MESSAGE_DETAIL, testMessageBean);
                startActivity(intent);
            }
        });
        rvMessage.setAdapter(mMessageListAdapter);
        mMessageListAdapter.setOnDeleteListener((v, position) -> {

        });
    }

    @Override
    public void doBusiness() {
        // initTestData();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void initTestData() {
        if (mMessageListAdapter != null) {
            List<TestMessageBean> beanList = new ArrayList<>();
            TestMessageBean bean1 = new TestMessageBean("New year's day news", "      This is a message about the event，This is a message about the event，This is a message about the event.", 1610777058000L);
            beanList.add(bean1);
            TestMessageBean bean2 = new TestMessageBean("System upgrade service notification", "      This is a message about the event，This is a message about the event，This is a message about the event.", 1610777058000L);
            beanList.add(bean2);
            TestMessageBean bean3 = new TestMessageBean("New year's day news", "      This is a message about the event，This is a message about the event，This is a message about the event.", 1610777058000L);
            beanList.add(bean3);
            mMessageListAdapter.setList(beanList);
        }
    }
}
