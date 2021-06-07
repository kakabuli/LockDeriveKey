package com.revolo.lock.ui.mine;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.adapter.MessageListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.DeleteSystemMessageReq;
import com.revolo.lock.bean.request.SystemMessageListReq;
import com.revolo.lock.bean.respone.SystemMessageListBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.widget.SlideRecyclerView;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
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
    private List<SystemMessageListBeanRsp.DataBean> mDataBeanList = new ArrayList<>();

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
        rvMessage.getItemAnimator().setChangeDuration(300);
        rvMessage.getItemAnimator().setMoveDuration(300);
        rvMessage.setLayoutManager(new LinearLayoutManager(this));
        mMessageListAdapter = new MessageListAdapter(R.layout.item_message_rv);
        mMessageListAdapter.setOnItemClickListener((adapter, view, position) -> {
            ConstraintLayout constraintLayout = view.findViewById(R.id.cl_message);
            if (constraintLayout != null) {
                if (constraintLayout.getVisibility() == View.GONE) {
                    constraintLayout.setVisibility(View.VISIBLE);
                } else {
                    constraintLayout.setVisibility(View.GONE);
                }
            }
        });
        rvMessage.setAdapter(mMessageListAdapter);
        mMessageListAdapter.setOnDeleteListener(dataBean -> {
            if (dataBean != null) {
                deleteSystemMessage(dataBean.get_id());
            }
        });

        mSmartRefreshLayout.setOnLoadMoreListener(refreshLayout -> {
            page++;
            getSystemMessageList();
        });

        mSmartRefreshLayout.setOnRefreshListener(refreshLayout -> {
            page = 1;
            getSystemMessageList();
        });
    }

    @Override
    public void doBusiness() {

        getSystemMessageList();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void getSystemMessageList() {

        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("updateLockInfoToService token is empty");
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            return;
        }

        SystemMessageListReq messageListReq = new SystemMessageListReq();
        messageListReq.setPage(page);
        messageListReq.setUid(App.getInstance().getUserBean().getUid());
        Observable<SystemMessageListBeanRsp> observable = HttpRequest.getInstance().systemMessageList(token, messageListReq);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<SystemMessageListBeanRsp>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull SystemMessageListBeanRsp systemMessageListBeanRsp) {
                if (systemMessageListBeanRsp.getCode().equals("200")) {
                    systemMessageList(systemMessageListBeanRsp);
                } else if (systemMessageListBeanRsp.getCode().equals("400")) {
                    App.getInstance().logout(true, MessageListActivity.this);
                }
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void systemMessageList(SystemMessageListBeanRsp systemMessageListBeanRsp) {

        mSmartRefreshLayout.finishRefresh(true);
        mSmartRefreshLayout.finishLoadMore(true);
        List<SystemMessageListBeanRsp.DataBean> data = systemMessageListBeanRsp.getData();
        if (page == 1) {
            mDataBeanList.clear();
        }
        mDataBeanList.addAll(data);
        if (mDataBeanList.isEmpty()) {
            mSmartRefreshLayout.setNoMoreData(true);
        }
        mMessageListAdapter.setList(data);
    }

    private void deleteSystemMessage(String messageId) {
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("updateLockInfoToService token is empty");
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            return;
        }

        DeleteSystemMessageReq deleteSystemMessageReq = new DeleteSystemMessageReq();
        deleteSystemMessageReq.setMid(messageId);
        deleteSystemMessageReq.setUid(App.getInstance().getUserBean().getUid());
        Observable<SystemMessageListBeanRsp> stringObservable = HttpRequest.getInstance().deleteSystemMessage(token, deleteSystemMessageReq);
        ObservableDecorator.decorate(stringObservable).safeSubscribe(new Observer<SystemMessageListBeanRsp>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull SystemMessageListBeanRsp beanRsp) {
                if (beanRsp != null && beanRsp.getCode().equals("200")) {
                    getSystemMessageList();
                }
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
