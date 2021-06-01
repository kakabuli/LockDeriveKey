package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.MessageListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.SystemMessageListReq;
import com.revolo.lock.bean.respone.SystemMessageListBeanRsp;
import com.revolo.lock.bean.test.TestMessageBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.widget.SlideRecyclerView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 信息列表
 */
public class MessageListActivity extends BaseActivity {

    private MessageListAdapter mMessageListAdapter;
    private SmartRefreshLayout mSmartRefreshLayout;

    private int page = 1;

    private ImageView mIvNoMessage;
    private TextView mTvNoMessage;

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

        mSmartRefreshLayout = findViewById(R.id.smartRefresh);
        mIvNoMessage = findViewById(R.id.ivNoMessage);
        mTvNoMessage = findViewById(R.id.tvNoMessage);
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

        getSystemMessageList();
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

    private void getSystemMessageList() {

        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("updateLockInfoToService token is empty");
            return;
        }

        SystemMessageListReq messageListReq = new SystemMessageListReq();
        Observable<SystemMessageListBeanRsp> observable = HttpRequest.getInstance().systemMessageList("", messageListReq);
    }
}
