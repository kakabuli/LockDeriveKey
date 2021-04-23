package com.revolo.lock.ui.mine;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.FeedBackBeanReq;
import com.revolo.lock.bean.respone.FeedBackBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 反馈页面
 */
public class FeedbackActivity extends BaseActivity {

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_feedback;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_feedback));
        applyDebouncingClickListener(findViewById(R.id.btnSubmit));

        initLoading("Feedback...");
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnSubmit) {
            feedback();
        }
    }

    private void feedback() {
        if(!checkNetConnectFail()) {
            return;
        }
        String feedbackStr = ((TextView) findViewById(R.id.etFeedback)).getText().toString().trim();
        if(TextUtils.isEmpty(feedbackStr)) {
            ToastUtils.showShort(R.string.t_please_enter_feedback_content);
            return;
        }
        if(App.getInstance().getUserBean() == null) {
            Timber.e("feedback App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            Timber.e("feedback uid is empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("feedback token is empty");
            return;
        }
        showLoading();
        FeedBackBeanReq req = new FeedBackBeanReq();
        req.setSuggest(feedbackStr);
        req.setUid(uid);
        Observable<FeedBackBeanRsp> observable = HttpRequest.getInstance().feedback(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<FeedBackBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull FeedBackBeanRsp feedBackBeanRsp) {
                dismissLoading();
                String code = feedBackBeanRsp.getCode();
                if(TextUtils.isEmpty(code)) {
                    Timber.e("feedback code is empty");
                    return;
                }
                if(!code.equals("200")) {
                    if(code.equals("444")) {
                        App.getInstance().logout(true, FeedbackActivity.this);
                        return;
                    }
                    String msg = feedBackBeanRsp.getMsg();
                    Timber.e("feedback code %1s, msg %2s", code, msg);
                    if(TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                    }
                    return;
                }
                ToastUtils.showShort(R.string.t_success);
                finish();
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
