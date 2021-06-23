package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;

/**
 * author : Jack
 * time   : 2021/3/10
 * E-mail : wengmaowei@kaadas.com
 * desc   : 加载用户协议和隐私协议
 */
public class TermActivity extends BaseActivity {

    private String mWillLoadUrl = "";
    private String mTitle = "";
    private WebView mTermWebView;


    @Override
    public void initData(@Nullable Bundle bundle) {
        isShowNetState = false;
        Intent intent = getIntent();
        String termType = "";
        if(intent.hasExtra(Constant.TERM_TYPE)) {
            termType = intent.getStringExtra(Constant.TERM_TYPE);
        }
        if(TextUtils.isEmpty(termType)) {
            finish();
            return;
        }
        if(termType.equals(Constant.TERM_TYPE_USER)) {
            mWillLoadUrl = "file:///android_asset/revolo_user_term.html";
            mTitle = getString(R.string.title_user_term);
        } else if(termType.equals(Constant.TERM_TYPE_PRIVACY)) {
            mWillLoadUrl = "file:///android_asset/revolo_privacy_term.html";
            mTitle = getString(R.string.title_privacy_term);
        }
        if(TextUtils.isEmpty(mWillLoadUrl)) {
            finish();
        }
        
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
    public int bindLayout() {
        return R.layout.activity_term;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(mTitle);
        mTermWebView = findViewById(R.id.termWebView);
    }

    @Override
    public void doBusiness() {
        loadUrl();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void loadUrl() {
        WebSettings settings = mTermWebView.getSettings();
//        settings.setJavaScriptEnabled(true);
//        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);

        mTermWebView.setWebViewClient(new WebViewClient());
        mTermWebView.loadUrl(mWillLoadUrl);
    }

//    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onResume() {
        super.onResume();
//        mTermWebView.getSettings().setJavaScriptEnabled(true);
        mTermWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTermWebView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //挂在后台  资源释放
        mTermWebView.getSettings().setJavaScriptEnabled(false);
    }

    @Override
    protected void onDestroy() {
        mTermWebView.clearHistory();
        mTermWebView.setVisibility(View.GONE);
        mTermWebView.destroy();
        super.onDestroy();
    }

}
