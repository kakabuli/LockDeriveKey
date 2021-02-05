package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.test.TestPwdBean;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.widget.iosloading.CustomerLoadingDialog;

import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_WEEK_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_OPTION_DEL;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_TYPE_PWD;
import static com.revolo.lock.ble.BleProtocolState.CMD_KEY_ATTRIBUTES_SET;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 密码详情页面
 */
public class PasswordDetailActivity extends BaseActivity {

    private TestPwdBean mTestPwdBean;
    private TextView mTvPwdName, mTvPwd, mTvPwdCharacteristic, mTvCreationDate;
    private CustomerLoadingDialog mLoadingDialog;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.PWD_DETAIL)) {
            mTestPwdBean = intent.getParcelableExtra(Constant.PWD_DETAIL);
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_password_detail;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_pwd_details));
        applyDebouncingClickListener(findViewById(R.id.ivEditPwdName), findViewById(R.id.btnDeletePwd));
        mTvPwdName = findViewById(R.id.tvPwdName);
        mTvPwd = findViewById(R.id.tvPwd);
        mTvCreationDate = findViewById(R.id.tvCreationDate);
        mTvPwdCharacteristic  = findViewById(R.id.tvPwdCharacteristic);
        // TODO: 2021/1/29 抽离英文
        mLoadingDialog = new CustomerLoadingDialog.Builder(this)
                .setMessage("Deleting")
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
    }

    @Override
    public void doBusiness() {
        initDetail();
        initBleListener();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.ivEditPwdName) {
            startActivity(new Intent(this, ChangePwdNameActivity.class));
            return;
        }
        if(view.getId() == R.id.btnDeletePwd) {
            showDelDialog();
        }
    }

    private void initDetail() {
        if(mTestPwdBean != null) {
            mTvPwdName.setText(mTestPwdBean.getPwdName());
            mTvPwd.setText(mTestPwdBean.getPwd());
            mTvPwdCharacteristic.setText(mTestPwdBean.getPwdCharacteristic());
            mTvCreationDate.setText(mTestPwdBean.getCreateDate());
        }
    }

    private void showDelDialog() {
        SelectDialog dialog = new SelectDialog(this);
        dialog.setMessage(getString(R.string.dialog_tip_please_approach_the_door_lock_to_delete_password));
        dialog.setOnCancelClickListener(v -> dialog.dismiss());
        dialog.setOnConfirmListener(v -> {
            dialog.dismiss();
            delPwd();
        });
        dialog.show();
    }

    private void delPwd() {
        if(mLoadingDialog != null) {
            mLoadingDialog.show();
        }
        App.getInstance().writeControlMsg(BleCommandFactory
                        .keyAttributesSet(KEY_SET_KEY_OPTION_DEL,
                                KEY_SET_KEY_TYPE_PWD,
                                (byte)mTestPwdBean.getNum(),
                                KEY_SET_ATTRIBUTE_WEEK_KEY,
                                (byte)0x00,
                                (byte)0x00,
                                (byte)0x00,
                                App.getInstance().getBleBean().getPwd1(),
                                App.getInstance().getBleBean().getPwd3()));
    }

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        if(bleResultBean.getCMD() == CMD_KEY_ATTRIBUTES_SET) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
                    MessageDialog messageDialog = new MessageDialog(PasswordDetailActivity.this);
                    if(bleResultBean.getPayload()[0] == 0x00) {
                        // 删除成功
                        messageDialog.setMessage(getString(R.string.dialog_tip_password_deleted));
                        messageDialog.setOnListener(v -> {
                            messageDialog.dismiss();
                            finish();
                        });
                    } else {
                        // 删除失败
                        // TODO: 2021/2/5 还有其他原因导致删除失败，不是蓝牙断开
                        messageDialog.setMessage(getString(R.string.dialog_tip_deletion_failed_door_lock_bluetooth_is_not_found));
                        messageDialog.setOnListener(v -> messageDialog.dismiss());
                    }
                    messageDialog.show();
                }
            });

        }
    };

    private void initBleListener() {
        App.getInstance().setOnBleDeviceListener(new OnBleDeviceListener() {
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
                BleResultProcess.processReceivedData(value,
                        App.getInstance().getBleBean().getPwd1(),
                        App.getInstance().getBleBean().getPwd3(),
                        App.getInstance().getBleBean().getOKBLEDeviceImp().getBleScanResult());
            }

            @Override
            public void onWriteValue(String uuid, byte[] value, boolean success) {

            }
        });
    }
    
}
