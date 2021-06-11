package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.TimeUtils;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.test.TestMessageBean;

/**
 * author : Jack
 * time   : 2021/1/16
 * E-mail : wengmaowei@kaadas.com
 * desc   : 新闻信息详情
 */
public class MessageDetailActivity extends BaseActivity {

    private TestMessageBean mTestMessageBean;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.MESSAGE_DETAIL)) {
            mTestMessageBean = intent.getParcelableExtra(Constant.MESSAGE_DETAIL);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_message_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_message_detail));
    }

    @Override
    public void doBusiness() {
        initMessageData();
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
    public void onDebouncingClick(@NonNull View view) {

    }

    private void initMessageData() {
        if(mTestMessageBean != null) {
            ((TextView) findViewById(R.id.tvMessageTitle)).setText(mTestMessageBean.getTitle());
            ((TextView) findViewById(R.id.tvTime)).setText(TimeUtils.millis2String(mTestMessageBean.getCreateTime(), "yyyy.MM.dd"));
            ((TextView) findViewById(R.id.tvContent)).setText(mTestMessageBean.getContent());
        }
    }

}
