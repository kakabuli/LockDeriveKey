package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.GainKeyBeanReq;
import com.revolo.lock.bean.respone.GainKeyBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * author : Jack
 * time   : 2021/1/14
 * E-mail : wengmaowei@kaadas.com
 * desc   : 用户权限选择
 */
public class AuthorizationManagementActivity extends BaseActivity {

    private ImageView mIvGuest, mIvFamily;

    // TODO: 2021/3/8 后续写成enum
    private int mCurrentUserType = 1;                // 1 Family  2 Guest

    private String mEsn;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.LOCK_ESN)) {
            mEsn = intent.getStringExtra(Constant.LOCK_ESN);
        }
        // TODO: 2021/3/8 处理
        if(TextUtils.isEmpty(mEsn)) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_authorization_management;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_authorization_management));
        TextView tvUserTip = findViewById(R.id.tvUserTip);
        tvUserTip.setText(getString(R.string.tip_invite_user));
        mIvGuest = findViewById(R.id.ivGuest);
        mIvFamily = findViewById(R.id.ivFamily);
        applyDebouncingClickListener(findViewById(R.id.clFamily), findViewById(R.id.clGuest), findViewById(R.id.btnShare));
        initLoading("Creating...");
    }

    @Override
    public void doBusiness() {

    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnShare) {
            share();
            return;
        }
        if(view.getId() == R.id.clFamily) {
            mCurrentUserType = 1;
            mIvGuest.setImageResource(R.drawable.ic_home_password_icon_default);
            mIvFamily.setImageResource(R.drawable.ic_home_password_icon_selected);
            return;
        }
        if(view.getId() == R.id.clGuest) {
            mCurrentUserType = 2;
            mIvGuest.setImageResource(R.drawable.ic_home_password_icon_selected);
            mIvFamily.setImageResource(R.drawable.ic_home_password_icon_default);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void share() {
        if(App.getInstance().getUserBean() == null) {
            Timber.e("share App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            Timber.e("share uid is empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("share token is empty");
            return;
        }
        GainKeyBeanReq req = new GainKeyBeanReq();
        req.setDeviceSN(mEsn);
        req.setShareUserType(mCurrentUserType);
        req.setUid(uid);
        showLoading();
        Observable<GainKeyBeanRsp> observable = HttpRequest.getInstance().gainKey(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<GainKeyBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull GainKeyBeanRsp gainKeyBeanRsp) {
                dismissLoading();
                String code = gainKeyBeanRsp.getCode();
                if(TextUtils.isEmpty(code)) {
                    Timber.e("share code empty");
                    return;
                }
                if(!code.equals("200")) {
                    Timber.e("share code: %1s, msg: %2s", code, gainKeyBeanRsp.getMsg());
                    return;
                }
                if(gainKeyBeanRsp.getData() == null) {
                    Timber.e("share gainKeyBeanRsp.getData() == null");
                    return;
                }
                String url = gainKeyBeanRsp.getData().getUrl();
                if(TextUtils.isEmpty(url)) {
                    Timber.e("share url is empty");
                    return;
                }
                shareUrlToOtherApp(url);
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

    private void shareUrlToOtherApp(@NotNull String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        // TODO: 2021/3/8 标题后续是否需要修改
        intent.putExtra(Intent.EXTRA_SUBJECT, "Share To");
        intent.putExtra(Intent.EXTRA_TEXT, url);//extraText为文本的内容
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//为Activity新建一个任务栈
        startActivity(intent);
    }

}
