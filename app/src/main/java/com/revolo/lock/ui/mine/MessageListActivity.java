package com.revolo.lock.ui.mine;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.adapter.MessageListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.AcceptShareBeanReq;
import com.revolo.lock.bean.request.DeleteSystemMessageReq;
import com.revolo.lock.bean.request.SystemMessageListReq;
import com.revolo.lock.bean.respone.AcceptShareBeanRsp;
import com.revolo.lock.bean.respone.DelInvalidShareBeanRsp;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.bean.respone.SystemMessageListBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.ui.view.SmartClassicsHeaderView;
import com.revolo.lock.widget.SlideRecyclerView;
import com.revolo.lock.widget.SpacesItemDecoration;
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_message));

        mSmartRefreshLayout = findViewById(R.id.smartRefresh);
        mSmartRefreshLayout.setRefreshHeader(new SmartClassicsHeaderView(this));
        mIvNoMessage = findViewById(R.id.ivNoMessage);
        mTvNoMessage = findViewById(R.id.tvNoMessage);
        SlideRecyclerView rvMessage = findViewById(R.id.rvMessage);
        rvMessage.getItemAnimator().setChangeDuration(300);
        rvMessage.getItemAnimator().setMoveDuration(300);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvMessage.setLayoutManager(linearLayoutManager);
        SpacesItemDecoration dividerItemDecoration = new SpacesItemDecoration(10, getColor(R.color.cF2F2F2));
        rvMessage.addItemDecoration(dividerItemDecoration);
        mMessageListAdapter = new MessageListAdapter(R.layout.item_message_rv);
        mMessageListAdapter.setOnItemClickListener((adapter, view, position) -> {
            // TODO 不能删除
        });
        rvMessage.setAdapter(mMessageListAdapter);
        mMessageListAdapter.setOnDeleteListener(dataBean -> {
            if (dataBean != null) {
                deleteSystemMessage(dataBean.get_id());
            }
            rvMessage.closeMenu();
        });

        mMessageListAdapter.setOnAcceptingListener((position, dataBean) -> {
            if (dataBean != null) {
                acceptShare(dataBean.getShareKey());
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

        getSystemMessageList();
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void getSystemMessageList() {

        if (!checkNetConnectFail()) {
            return;
        }
        MailLoginBeanRsp.DataBean userBean = App.getInstance().getUserBean();
        if (userBean == null) {
            return;
        }
        String token = userBean.getToken();
        SystemMessageListReq messageListReq = new SystemMessageListReq();
        messageListReq.setPageNum(page);
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
                } else if (systemMessageListBeanRsp.getCode().equals("444")) {
                    App.getInstance().logout(true, MessageListActivity.this);
                }
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                Timber.e("onError()");
                mSmartRefreshLayout.finishRefresh(false);
                mSmartRefreshLayout.finishLoadMore(false);
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
        if (null != data) {
            if (page == 1) {
                mDataBeanList.clear();
            }
            mDataBeanList.addAll(data);
            if (mDataBeanList.isEmpty()) {
                mSmartRefreshLayout.setNoMoreData(true);
            }
        }
        if (mDataBeanList == null || mDataBeanList.isEmpty() || mDataBeanList.size() == 0) {
            mIvNoMessage.setVisibility(View.VISIBLE);
            mTvNoMessage.setVisibility(View.VISIBLE);
        } else {
            mIvNoMessage.setVisibility(View.GONE);
            mTvNoMessage.setVisibility(View.GONE);
        }
        mMessageListAdapter.setList(mDataBeanList);
    }

    private void deleteSystemMessage(String messageId) {
        if (!checkNetConnectFail()) {
            return;
        }
        MailLoginBeanRsp.DataBean userBean = App.getInstance().getUserBean();
        if (userBean == null) {
            return;
        }
        String token = userBean.getToken();
        DeleteSystemMessageReq deleteSystemMessageReq = new DeleteSystemMessageReq();
        deleteSystemMessageReq.setMid(messageId);
        Observable<DelInvalidShareBeanRsp> stringObservable = HttpRequest.getInstance().deleteSystemMessage(token, deleteSystemMessageReq);
        ObservableDecorator.decorate(stringObservable).safeSubscribe(new Observer<DelInvalidShareBeanRsp>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull DelInvalidShareBeanRsp beanRsp) {
                if (beanRsp.getCode().equals("200")) {
                    page = 1;
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

    private void acceptShare(String mShareKey) {
        if (!checkNetConnectFail()) {
            return;
        }
        if (TextUtils.isEmpty(mShareKey)) {
            Timber.e("mShareKey == null");
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("acceptShare App.getInstance().getUserBean() == null");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("acceptShare token is empty");
            return;
        }

        AcceptShareBeanReq req = new AcceptShareBeanReq();
        req.setShareKey(mShareKey);
        Observable<AcceptShareBeanRsp> observable = HttpRequest.getInstance().acceptShare(token, req);
        // TODO: 2021/3/12 暂时屏蔽
        showLoading();
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<AcceptShareBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull AcceptShareBeanRsp acceptShareBeanRsp) {
                dismissLoading();
                String code = acceptShareBeanRsp.getCode();
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, MessageListActivity.this);
                        return;
                    }
                    String msg = acceptShareBeanRsp.getMsg();
                    Timber.e("code: %1s, msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    page = 1;
                    getSystemMessageList();
                    return;
                }
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_success);
                page = 1;
                getSystemMessageList();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                dismissLoading();
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }
}
