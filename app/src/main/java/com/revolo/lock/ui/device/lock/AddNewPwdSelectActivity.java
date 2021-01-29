package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.AdaptScreenUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.widget.iosloading.CustomerLoadingDialog;

import java.nio.charset.StandardCharsets;

import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_TYPE_PWD;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加密码选择时间页面
 */
public class AddNewPwdSelectActivity extends BaseActivity {

    private ImageView mIvPermanent, mIvSchedule, mIvTemporary;
    private RelativeLayout mRlPermanent, mRlSchedule, mRlTemporary;
    private ConstraintLayout mClSchedule, mClTemporary;
    private Button mBtnNext;
    private BleBean mBleBean;
    private CustomerLoadingDialog mLoadingDialog;
    private String mKey;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleBean = App.getInstance().getBleBean();
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.USER_PWD)) {
            mKey = intent.getStringExtra(Constant.USER_PWD);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_add_new_pwd_select;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_password));
        mIvPermanent = findViewById(R.id.ivPermanent);
        mIvSchedule = findViewById(R.id.ivSchedule);
        mIvTemporary = findViewById(R.id.ivTemporary);
        mRlPermanent = findViewById(R.id.rlPermanent);
        mRlSchedule = findViewById(R.id.rlSchedule);
        mRlTemporary = findViewById(R.id.rlTemporary);
        mBtnNext = findViewById(R.id.btnNext);
        mClSchedule = findViewById(R.id.clSchedule);
        mClTemporary = findViewById(R.id.clTemporary);
        applyDebouncingClickListener(mRlPermanent, mRlSchedule, mRlTemporary, mBtnNext);

        // TODO: 2021/1/29 抽离英文
        mLoadingDialog = new CustomerLoadingDialog.Builder(this)
                .setMessage("password generating")
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
    }

    @Override
    public void doBusiness() {
        initDevice();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.rlPermanent) {
            permanentSwitch();
            showPermanentState();
            return;
        }
        if(view.getId() == R.id.rlSchedule) {
            scheduleSwitch();
            showSchedule();
            return;
        }
        if(view.getId() == R.id.rlTemporary) {
            temporarySwitch();
            showTemporary();
            return;
        }
        if(view.getId() == R.id.btnNext) {
            if(mLoadingDialog != null) {
                mLoadingDialog.show();
                if(mKey == null) {
                    // TODO: 2021/1/29 处理密码为空的情况
                    return;
                }
                App.getInstance().writeControlMsg(BleCommandFactory.addKey(KEY_SET_KEY_TYPE_PWD,
                        mKey.getBytes(StandardCharsets.UTF_8), mBleBean.getPwd1(), mBleBean.getPwd3()));
                // TODO: 2021/1/29 需要做超时操作
            }
            startActivity(new Intent(this, AddNewPwdNameActivity.class));
        }
    }

    @Override
    protected void onDestroy() {
        App.getInstance().clearBleDeviceListener();
        super.onDestroy();
    }

    private void showPermanentState() {
        mClSchedule.setVisibility(View.GONE);
        mClTemporary.setVisibility(View.GONE);
        changeBtnNext(128);
    }

    private void showSchedule() {
        mClSchedule.setVisibility(View.VISIBLE);
        mClTemporary.setVisibility(View.GONE);
        changeBtnNext(34);
    }

    private void showTemporary() {
        mClSchedule.setVisibility(View.GONE);
        mClTemporary.setVisibility(View.VISIBLE);
        changeBtnNext(128);
    }

    private void changeBtnNext(int bottomMargin) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mBtnNext.getLayoutParams();
        params.bottomMargin = AdaptScreenUtils.pt2Px(bottomMargin);
        mBtnNext.setLayoutParams(params);
    }


    private void permanentSwitch() {
        mIvPermanent.setImageResource(R.drawable.ic_home_password_icon_selected);
        mIvSchedule.setImageResource(R.drawable.ic_home_password_icon_default);
        mIvTemporary.setImageResource(R.drawable.ic_home_password_icon_default);
    }

    private void scheduleSwitch() {
        mIvPermanent.setImageResource(R.drawable.ic_home_password_icon_default);
        mIvSchedule.setImageResource(R.drawable.ic_home_password_icon_selected);
        mIvTemporary.setImageResource(R.drawable.ic_home_password_icon_default);
    }

    private void temporarySwitch() {
        mIvPermanent.setImageResource(R.drawable.ic_home_password_icon_default);
        mIvSchedule.setImageResource(R.drawable.ic_home_password_icon_default);
        mIvTemporary.setImageResource(R.drawable.ic_home_password_icon_selected);
    }

    private final OnBleDeviceListener mOnBleDeviceListener = new OnBleDeviceListener() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onReceivedValue(String uuid, byte[] value) {
            if(value == null) {
                return;
            }
            BleResultProcess.setOnReceivedProcess(mOnReceivedProcess);
            BleResultProcess.processReceivedData(value, mBleBean.getPwd1(), mBleBean.getPwd3(),
                    mBleBean.getOKBLEDeviceImp().getBleScanResult());
        }

        @Override
        public void onWriteValue(String uuid, byte[] value, boolean success) {

        }
    };

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        if(bleResultBean.getCMD() == BleProtocolState.CMD_KEY_ADD) {
            // 添加密钥
            byte state = bleResultBean.getPayload()[0];
            if(state == 0x00) {
                byte num = bleResultBean.getPayload()[1];
                // TODO: 2021/1/29 拿到编号并做对应的处理
            } else {
                // TODO: 2021/1/29 添加失败后的UI操作
                Timber.e("添加密钥失败，state: %1s", BleByteUtil.byteToInt(state));
            }
            if(mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        }
    };

    private void initDevice() {
        if (mBleBean.getOKBLEDeviceImp() != null) {
            App.getInstance().openPairNotify();
            App.getInstance().setOnBleDeviceListener(mOnBleDeviceListener);
        }
    }

}
