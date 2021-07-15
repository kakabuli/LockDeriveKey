package com.revolo.lock.ui.device.lock;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.AdaptScreenUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.DevicePwdBean;
import com.revolo.lock.bean.request.LockKeyAddBeanReq;
import com.revolo.lock.bean.respone.LockKeyAddBeanRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.AddPwdFailDialog;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockAddPwdAttrPublishBean;
import com.revolo.lock.mqtt.bean.publishbean.WifiLockAddPwdPublishBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockAddPwdAttrResponseBean;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockAddPwdRspBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.util.ZoneUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_TIME_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_WEEK_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_OPTION_ADD_OR_CHANGE;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_TYPE_PWD;
import static com.revolo.lock.ble.BleProtocolState.CMD_KEY_ATTRIBUTES_SET;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 添加密码选择时间页面
 */
public class AddNewPwdSelectActivity extends BaseActivity {

    private ImageView mIvPermanent, mIvSchedule, mIvTemporary;
    private ConstraintLayout mClSchedule, mClTemporary;
    private Button mBtnNext;
    private BleBean mBleBean;
    private String mKey;
    private TextView mTvStartTime, mTvEndTime;
    private TextView mTvStartDate, mTvStartDateTime, mTvEndDate, mTvEndDateTime;
    private View mVSun, mVMon, mVTues, mVWed, mVThur, mVFri, mVSat;

    private boolean isSelectedSun = true;
    private boolean isSelectedMon = true;
    private boolean isSelectedTues = true;
    private boolean isSelectedWed = true;
    private boolean isSelectedThur = true;
    private boolean isSelectedFri = true;
    private boolean isSelectedSat = true;

    @IntDef(value = {PERMANENT_STATE, SCHEDULE_STATE, TEMPORARY_STATE})
    private @interface AttributeState {
    }

    private static final int PERMANENT_STATE = 1;
    private static final int SCHEDULE_STATE = 2;
    private static final int TEMPORARY_STATE = 3;

    @AttributeState
    private int mSelectedPwdState = PERMANENT_STATE;

    private BleDeviceLocal mBleDeviceLocal;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if (intent.hasExtra(Constant.USER_PWD)) {
            mKey = intent.getStringExtra(Constant.USER_PWD);
        }
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            // TODO: 2021/2/21 或者有其他方法
            finish();
        }
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            mBleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
            if (mBleBean == null) {
                finish();
            }
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
        return R.layout.activity_add_new_pwd_select;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_add_password));
        initGlobalView();
        initApplyClick();
        initLoading(getString(R.string.t_load_content_password_generating));
        mAddPwdFailDialog = new AddPwdFailDialog(this);
        initSucMessageDialog();
        onRegisterEventBus();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_USER) {

        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            BleResultBean bleResultBean = lockMessage.getBleResultBea();
            if (bleResultBean == null) {
                Timber.e("mOnReceivedProcess bleResultBean == null");
                dismissLoadingAndShowAddFail();
                return;
            }
            if (bleResultBean.getCMD() == BleProtocolState.CMD_KEY_ADD) {
                addKey(bleResultBean);
            } else if (bleResultBean.getCMD() == CMD_KEY_ATTRIBUTES_SET) {
                byte state = bleResultBean.getPayload()[0];
                if (state == 0x00) {
                    savePwdToService(mDevicePwdBean);
                } else {
                    dismissLoadingAndShowAddFail();
                    Timber.e("设置密钥属性失败，state: %1s", BleByteUtil.byteToInt(state));
                }
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_CREATE_PWD:
                        processAddPwd((WifiLockAddPwdRspBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                    case LockMessageCode.MSG_LOCK_MESSAGE_ADD_PWD:
                        setPwdAttrCallback((WifiLockAddPwdAttrResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                }
            } else {
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_CREATE_PWD:
                    case LockMessageCode.MSG_LOCK_MESSAGE_ADD_PWD:
                        dismissLoading();
                        break;
                }
            }
        } else {

        }
    }

    private void initApplyClick() {
        TextView tvSun = findViewById(R.id.tvSun);
        TextView tvMon = findViewById(R.id.tvMon);
        TextView tvTues = findViewById(R.id.tvTues);
        TextView tvWed = findViewById(R.id.tvWed);
        TextView tvThur = findViewById(R.id.tvThur);
        TextView tvFri = findViewById(R.id.tvFri);
        TextView tvSat = findViewById(R.id.tvSat);
        RelativeLayout rlPermanent = findViewById(R.id.rlPermanent);
        RelativeLayout rlSchedule = findViewById(R.id.rlSchedule);
        RelativeLayout rlTemporary = findViewById(R.id.rlTemporary);
        tvSun.setOnClickListener(this::onDebouncingClick);
        tvMon.setOnClickListener(this::onDebouncingClick);
        tvTues.setOnClickListener(this::onDebouncingClick);
        tvWed.setOnClickListener(this::onDebouncingClick);
        tvThur.setOnClickListener(this::onDebouncingClick);
        tvFri.setOnClickListener(this::onDebouncingClick);
        tvSat.setOnClickListener(this::onDebouncingClick);
        rlPermanent.setOnClickListener(this::onDebouncingClick);
        rlSchedule.setOnClickListener(this::onDebouncingClick);
        rlTemporary.setOnClickListener(this::onDebouncingClick);
        mBtnNext.setOnClickListener(this::onDebouncingClick);
        mTvStartTime.setOnClickListener(this::onDebouncingClick);
        mTvEndTime.setOnClickListener(this::onDebouncingClick);
        mTvStartDate.setOnClickListener(this::onDebouncingClick);
        mTvStartDateTime.setOnClickListener(this::onDebouncingClick);
        mTvEndDate.setOnClickListener(this::onDebouncingClick);
        mTvEndDateTime.setOnClickListener(this::onDebouncingClick);

    }

    private void initGlobalView() {
        mIvPermanent = findViewById(R.id.ivPermanent);
        mIvSchedule = findViewById(R.id.ivSchedule);
        mIvTemporary = findViewById(R.id.ivTemporary);
        mBtnNext = findViewById(R.id.btnNext);
        mClSchedule = findViewById(R.id.clSchedule);
        mClTemporary = findViewById(R.id.clTemporary);
        mTvStartTime = findViewById(R.id.tvStartTime);
        mTvEndTime = findViewById(R.id.tvEndTime);
        mVSun = findViewById(R.id.vSun);
        mVMon = findViewById(R.id.vMon);
        mVTues = findViewById(R.id.vTues);
        mVWed = findViewById(R.id.vWed);
        mVThur = findViewById(R.id.vThur);
        mVFri = findViewById(R.id.vFri);
        mVSat = findViewById(R.id.vSat);
        mTvStartDate = findViewById(R.id.tvStartDate);
        mTvStartDateTime = findViewById(R.id.tvStartDateTime);
        mTvEndDate = findViewById(R.id.tvEndDate);
        mTvEndDateTime = findViewById(R.id.tvEndDateTime);

        mVSun.setVisibility(isSelectedSun ? View.VISIBLE : View.GONE);
        mVMon.setVisibility(isSelectedMon ? View.VISIBLE : View.GONE);
        mVTues.setVisibility(isSelectedTues ? View.VISIBLE : View.GONE);
        mVWed.setVisibility(isSelectedWed ? View.VISIBLE : View.GONE);
        mVThur.setVisibility(isSelectedThur ? View.VISIBLE : View.GONE);
        mVFri.setVisibility(isSelectedFri ? View.VISIBLE : View.GONE);
        mVSat.setVisibility(isSelectedSat ? View.VISIBLE : View.GONE);
    }

    @Override
    public void doBusiness() {
        initScheduleStartTimeMill();
        initScheduleEndTimeMill();
        initTemStartDateTimeMill();
        initTemEndDateTimeMill();
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            initBleListener();
        }
    }

    public void onDebouncingClick(@NonNull View view) {
        if (view.getId() == R.id.rlPermanent) {
            permanentSwitch();
            showPermanentState();
            return;
        }
        if (view.getId() == R.id.rlSchedule) {
            scheduleSwitch();
            showSchedule();
            return;
        }
        if (view.getId() == R.id.rlTemporary) {
            temporarySwitch();
            showTemporary();
            return;
        }
        if (view.getId() == R.id.btnNext) {
            if (mSelectedPwdState == SCHEDULE_STATE) {
                if (isSelectedFri || isSelectedMon || isSelectedSat || isSelectedSun || isSelectedThur || isSelectedTues || isSelectedWed) {
                    if (mScheduleEndTimeMill < mScheduleStartTimeMill) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_the_end_time_cannot_be_less_than_the_start_time);
                    } else {
                        nextStep();
                    }
                } else {
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("Repeat Not Choose");
                    dismissLoading();
                }
            } else if (mSelectedPwdState == TEMPORARY_STATE) {
                if (mTemEndDateTimeMill < new Date().getTime()) {
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_the_end_time_cannot_be_less_than_the_current_time);
                } else if (mTemEndDateTimeMill < mTemStartDateTimeMill) {
                    ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_the_end_time_cannot_be_less_than_the_start_time);
                } else {
                    nextStep();
                }
            } else {
                nextStep();
            }
            return;
        }
        if (view.getId() == R.id.tvSun) {
            isSelectedSun = !isSelectedSun;
            mVSun.setVisibility(isSelectedSun ? View.VISIBLE : View.GONE);
            return;
        }
        if (view.getId() == R.id.tvMon) {
            isSelectedMon = !isSelectedMon;
            mVMon.setVisibility(isSelectedMon ? View.VISIBLE : View.GONE);
            return;
        }
        if (view.getId() == R.id.tvTues) {
            isSelectedTues = !isSelectedTues;
            mVTues.setVisibility(isSelectedTues ? View.VISIBLE : View.GONE);
            return;
        }
        if (view.getId() == R.id.tvWed) {
            isSelectedWed = !isSelectedWed;
            mVWed.setVisibility(isSelectedWed ? View.VISIBLE : View.GONE);
            return;
        }
        if (view.getId() == R.id.tvThur) {
            isSelectedThur = !isSelectedThur;
            mVThur.setVisibility(isSelectedThur ? View.VISIBLE : View.GONE);
            return;
        }
        if (view.getId() == R.id.tvFri) {
            isSelectedFri = !isSelectedFri;
            mVFri.setVisibility(isSelectedFri ? View.VISIBLE : View.GONE);
            return;
        }
        if (view.getId() == R.id.tvSat) {
            isSelectedSat = !isSelectedSat;
            mVSat.setVisibility(isSelectedSat ? View.VISIBLE : View.GONE);
            return;
        }
        if (view.getId() == R.id.tvStartTime) {
            showTimePicker(R.id.tvStartTime);
            return;
        }
        if (view.getId() == R.id.tvEndTime) {
            showTimePicker(R.id.tvEndTime);
            return;
        }
        if (view.getId() == R.id.tvStartDate) {
            showDatePicker(R.id.tvStartDate);
            return;
        }
        if (view.getId() == R.id.tvStartDateTime) {
            showTimePicker(R.id.tvStartDateTime);
            return;
        }
        if (view.getId() == R.id.tvEndDate) {
            showDatePicker(R.id.tvEndDate);
            return;
        }
        if (view.getId() == R.id.tvEndDateTime) {
            showTimePicker(R.id.tvEndDateTime);
        }
    }

    @Override
    protected void onDestroy() {
        mSucMessageDialog = null;
        mAddPwdFailDialog = null;
        super.onDestroy();
    }

    private void nextStep() {
        if (mKey == null) {
            // TODO: 2021/1/29 处理密码为空的情况
            return;
        }
        showLoading();
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            publishAddPwd(mBleDeviceLocal.getEsn(), mKey);
        } else if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            if (mBleBean == null) {
                Timber.e("nextStep mBleBean == null");
                return;
            }
            if (mBleBean.getPwd1() == null) {
                Timber.e("nextStep mBleBean.getPwd1() == null");
                return;
            }
            if (mBleBean.getPwd3() == null) {
                Timber.e("nextStep mBleBean.getPwd3() == null");
                return;
            }
            LockMessage message = new LockMessage();
            message.setMessageType(3);
            message.setBytes(BleCommandFactory.addKey(KEY_SET_KEY_TYPE_PWD,
                    mKey.getBytes(StandardCharsets.UTF_8), mBleBean.getPwd1(), mBleBean.getPwd3()));
            message.setMac(mBleBean.getOKBLEDeviceImp().getMacAddress());
            EventBus.getDefault().post(message);
        } else {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(getString(R.string.t_text_content_offline_devices));
        }
    }

    private void showPermanentState() {
        mClSchedule.setVisibility(View.GONE);
        mClTemporary.setVisibility(View.GONE);
        mSelectedPwdState = PERMANENT_STATE;
        changeBtnNext(128);
    }

    private void showSchedule() {
        mClSchedule.setVisibility(View.VISIBLE);
        mClTemporary.setVisibility(View.GONE);
        mSelectedPwdState = SCHEDULE_STATE;
        changeBtnNext(34);
    }

    private void showTemporary() {
        mClSchedule.setVisibility(View.GONE);
        mClTemporary.setVisibility(View.VISIBLE);
        mSelectedPwdState = TEMPORARY_STATE;
        changeBtnNext(34);
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

    /**
     * 时间和日期的选择器
     **/

    private long mScheduleStartTimeMill;
    private long mScheduleEndTimeMill;
    private long mTemStartDateTimeMill;
    private long mTemEndDateTimeMill;
    private String mTemStartDateTimeStr = "10:00:00";
    private String mTemEndDateTimeStr = "14:00:00";
    private final String mNowDateStr = ZoneUtil.getDate(App.getInstance().getBleDeviceLocal().getTimeZone(), ZoneUtil.getTime(), "yyyy-MM-dd");

    private String mTemStartDateStr = mNowDateStr;
    private String mTemEndDateStr = mNowDateStr;

    private void initScheduleStartTimeMill() {
        String date = mNowDateStr + " 00:00:00";
        //mScheduleStartTimeMill = TimeUtils.string2Millis(date);
        mScheduleStartTimeMill = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), date);
    }

    private void initScheduleEndTimeMill() {
        String date = mNowDateStr + " 23:59:00";
        mScheduleEndTimeMill = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), date);

    }

    private void initTemStartDateTimeMill() {
        String nowDate = ZoneUtil.getDate(mBleDeviceLocal.getTimeZone(), ZoneUtil.getTime(), "dd-MM-yyyy");
        mTemStartDateStr = nowDate;
        mTvStartDate.setText(nowDate.replace("-", "."));
        String date = nowDate + " 10:00:00";
        mTemStartDateTimeMill = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), date);
    }

    private void initTemEndDateTimeMill() {
        String nowDate = ZoneUtil.getDate(mBleDeviceLocal.getTimeZone(), System.currentTimeMillis(), "dd-MM-yyyy");
        mTemEndDateStr = nowDate;
        mTvEndDate.setText(nowDate.replace("-", "."));
        String date = nowDate + " 14:00:00";
        mTemEndDateTimeMill = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), date);
    }

    private void showTimePicker(@IdRes int id) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.MyTimePickerDialogTheme,
                (view, hourOfDay, minute) -> {
                    String time = (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay) + ":" + (minute < 10 ? "0" + minute : minute);
                    if (id == R.id.tvStartTime) {
                        long scheduleStartTimeMill = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), mNowDateStr + " " + time + ":00");
                        mTvStartTime.setText(time);
                        mScheduleStartTimeMill = scheduleStartTimeMill;
                        Timber.d("startTime 选择的时间%1s, 时间流：%2d, 转换的时间：%3s", time, mScheduleStartTimeMill,
                                ZoneUtil.getDate(mBleDeviceLocal.getTimeZone(), mScheduleStartTimeMill));
                    } else if (id == R.id.tvEndTime) {
                        long scheduleEndTimeMill = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), mNowDateStr + " " + time + ":00");
                        mTvEndTime.setText(time);
                        mScheduleEndTimeMill = scheduleEndTimeMill;
                        Timber.d("endTime 选择的时间%1s, 时间流：%2d, 转换的时间：%3s", time, mScheduleEndTimeMill, ZoneUtil.getDate(mBleDeviceLocal.getTimeZone(), mScheduleEndTimeMill));
                    } else if (id == R.id.tvEndDateTime) {
                        long temEndDateTimeMill = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), mTemEndDateStr + " " + time + ":00");
                        mTvEndDateTime.setText(time);
                        mTemEndDateTimeStr = time;
                        mTemEndDateTimeMill = temEndDateTimeMill;
                        Timber.d("endDateTime 选择的时间%1s, 时间流：%2d", time, mTemEndDateTimeMill);
                    } else if (id == R.id.tvStartDateTime) {
                        long temStartDateTimeMill = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), mTemStartDateStr + " " + time + ":00");
                        mTvStartDateTime.setText(time);
                        mTemStartDateTimeStr = time;
                        mTemStartDateTimeMill = temStartDateTimeMill;
                        Timber.d("startDateTime 选择的时间%1s, 时间流：%2d", time, mTemStartDateTimeMill);
                    }
                }, 0, 0, true);
        timePickerDialog.show();
    }


    private void showDatePicker(@IdRes int id) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyTimePickerDialogTheme);
        datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            // 月份需要加1
            month += 1;
            String date = year + "-" + (month < 10 ? "0" + month : month) + "-" + (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);
            String showDate = (dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "." + (month < 10 ? "0" + month : month) + "." + year;
            if (id == R.id.tvStartDate) {
                if (null != mTemStartDateTimeStr && mTemStartDateTimeStr.length() < 8) {
                    mTemStartDateTimeStr = mTemStartDateTimeStr + ":00";
                }
                long temStartDateTimeMill = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), date + " " + mTemStartDateTimeStr);
                mTemStartDateStr = date;
                mTemStartDateTimeMill = temStartDateTimeMill;
                mTvStartDate.setText(showDate);
                Timber.d("startDate 选择的日期%1s, 时间流：%2d", date, mTemStartDateTimeMill);
            } else if (id == R.id.tvEndDate) {
                if (null != mTemEndDateTimeStr && mTemEndDateTimeStr.length() < 8) {
                    mTemEndDateTimeStr = mTemEndDateTimeStr + ":00";
                }
                long temEndDateTimeMill = ZoneUtil.getTime(mBleDeviceLocal.getTimeZone(), date + " " + mTemEndDateTimeStr);
                mTemEndDateStr = date;
                mTemEndDateTimeMill = temEndDateTimeMill;
                mTvEndDate.setText(showDate);
                Timber.d("startDate 选择的日期%1s, 时间流：%2d", date, mTemEndDateTimeMill);
            }
        });
        datePickerDialog.setCancelable(true);
        datePickerDialog.show();
    }

    /**
     * 蓝牙指令与处理
     **/
    private int mNum;
    private final DevicePwdBean mDevicePwdBean = new DevicePwdBean();

    private void addKey(BleResultBean bleResultBean) {
        // TODO: 2021/2/4 添加的时候需要判断后时间不能少于前时间
        // 添加密钥
        byte state = bleResultBean.getPayload()[0];
        if (state == 0x00) {
            savePwd(bleResultBean);
        } else {
            dismissLoadingAndShowAddFail();
            Timber.e("添加密钥失败，state: %1s", BleByteUtil.byteToInt(state));
        }
    }

    private void savePwd(BleResultBean bleResultBean) {
        mNum = bleResultBean.getPayload()[1];
        mDevicePwdBean.setPwdNum(mNum);
        // 使用秒存储，所以除以1000
        mDevicePwdBean.setCreateTime(ZoneUtil.getTime() / 1000);
        mDevicePwdBean.setDeviceId(mBleDeviceLocal.getId());
        mDevicePwdBean.setAttribute(BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS);
        if (mSelectedPwdState == PERMANENT_STATE) {
            savePwdToService(mDevicePwdBean);
        } else if (mSelectedPwdState == SCHEDULE_STATE) {
            setSchedulePwd();
        } else if (mSelectedPwdState == TEMPORARY_STATE) {
            setTimePwd();
        }
    }

    private AddPwdFailDialog mAddPwdFailDialog;

    private void dismissLoadingAndShowAddFail() {
        dismissLoading();
        runOnUiThread(() -> {
            ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_add_pwd_fail);
        });
    }

    private void setTimePwd() {
        Timber.d("num: %1s, startTime: %2d, endTime: %3d",
                mNum, mTemStartDateTimeMill / 1000, mTemEndDateTimeMill / 1000);
        mDevicePwdBean.setStartTime(mTemStartDateTimeMill / 1000);
        mDevicePwdBean.setEndTime(mTemEndDateTimeMill / 1000);
        mDevicePwdBean.setAttribute(KEY_SET_ATTRIBUTE_TIME_KEY);
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            publishAddPwdAttr(mBleDeviceLocal.getEsn(),
                    KEY_SET_ATTRIBUTE_TIME_KEY,
                    mNum,
                    mTemStartDateTimeMill / 1000,
                    mTemEndDateTimeMill / 1000,
                    0);
        } else {
            LockMessage message = new LockMessage();
            message.setMac(mBleBean.getOKBLEDeviceImp().getMacAddress());
            message.setBytes(BleCommandFactory
                    .keyAttributesSet(KEY_SET_KEY_OPTION_ADD_OR_CHANGE,
                            KEY_SET_KEY_TYPE_PWD,
                            (byte) mNum,
                            KEY_SET_ATTRIBUTE_TIME_KEY,
                            (byte) 0x00,
                            mTemStartDateTimeMill / 1000,
                            mTemEndDateTimeMill / 1000,
                            mBleBean.getPwd1(),
                            mBleBean.getPwd3()));
            message.setMessageType(3);
            EventBus.getDefault().post(message);
        }
    }

    private void setSchedulePwd() {
        // 周策略 BIT:   7   6   5   4   3   2   1   0
        // 星期：      保留  六  五  四  三  二  一  日
        byte[] weekBit = new byte[8];
        weekBit[0] = (byte) (isSelectedSun ? 0x01 : 0x00);
        weekBit[1] = (byte) (isSelectedMon ? 0x01 : 0x00);
        weekBit[2] = (byte) (isSelectedTues ? 0x01 : 0x00);
        weekBit[3] = (byte) (isSelectedWed ? 0x01 : 0x00);
        weekBit[4] = (byte) (isSelectedThur ? 0x01 : 0x00);
        weekBit[5] = (byte) (isSelectedFri ? 0x01 : 0x00);
        weekBit[6] = (byte) (isSelectedSat ? 0x01 : 0x00);
        byte week = BleByteUtil.bitToByte(weekBit);
        Timber.d("sun: %1b, mon: %2b, tues: %3b, wed: %4b, thur: %5b, fri: %6b, sat: %7b",
                isSelectedSun, isSelectedMon, isSelectedTues, isSelectedWed,
                isSelectedThur, isSelectedFri, isSelectedSat);
        Timber.d("num: %1s, week: %2s, weekBytes: %3s, startTime: %4d, endTime: %5d",
                mNum, ConvertUtils.int2HexString(week), ConvertUtils.bytes2HexString(weekBit),
                mScheduleStartTimeMill / 1000, mScheduleEndTimeMill / 1000);
        mDevicePwdBean.setWeekly(week);
        mDevicePwdBean.setStartTime(mScheduleStartTimeMill / 1000);
        mDevicePwdBean.setEndTime(mScheduleEndTimeMill / 1000);
        mDevicePwdBean.setAttribute(KEY_SET_ATTRIBUTE_WEEK_KEY);
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            publishAddPwdAttr(mBleDeviceLocal.getEsn(),
                    KEY_SET_ATTRIBUTE_WEEK_KEY,
                    mNum,
                    mScheduleStartTimeMill / 1000,
                    mScheduleEndTimeMill / 1000,
                    week);
        } else {
            LockMessage message = new LockMessage();
            message.setMessageType(3);
            message.setMac(mBleBean.getOKBLEDeviceImp().getMacAddress());
            message.setBytes(BleCommandFactory
                    .keyAttributesSet(KEY_SET_KEY_OPTION_ADD_OR_CHANGE,
                            KEY_SET_KEY_TYPE_PWD,
                            (byte) mNum,
                            KEY_SET_ATTRIBUTE_WEEK_KEY,
                            week,
                            mScheduleStartTimeMill / 1000,
                            mScheduleEndTimeMill / 1000,
                            mBleBean.getPwd1(),
                            mBleBean.getPwd3()));
            EventBus.getDefault().post(message);
        }
    }

    private MessageDialog mSucMessageDialog;

    private void initSucMessageDialog() {
        mSucMessageDialog = new MessageDialog(AddNewPwdSelectActivity.this);
        mSucMessageDialog.setMessage(getString(R.string.dialog_tip_password_added));
        mSucMessageDialog.setOnListener(v -> {
            // 不销毁会导致内存泄漏
            if (mSucMessageDialog != null) {
                mSucMessageDialog.dismiss();
            }
            Intent intent = new Intent(AddNewPwdSelectActivity.this, AddNewPwdNameActivity.class);
            intent.putExtra(Constant.PWD_NUM, mDevicePwdBean.getPwdNum());
            intent.putExtra(Constant.LOCK_ESN, mBleDeviceLocal.getEsn());
            startActivity(intent);
            finish();
        });
    }

    private void showSucMessage() {
        runOnUiThread(() -> {
            if (mSucMessageDialog != null) {
                mSucMessageDialog.show();
            }
        });
    }

    private void initBleListener() {
        if (mBleBean == null || mBleBean.getOKBLEDeviceImp() == null) {
            // TODO: 2021/1/30 做对应的处理
            Timber.e("initDevice mBleBean == null || mBleBean.getOKBLEDeviceImp() == null");
            return;
        }
        //替换
        //App.getInstance().openPairNotify(mBleBean.getOKBLEDeviceImp());
    }

    private void publishAddPwd(String wifiId, String key) {
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("publishAddPwd uid is empty");
            return;
        }
        showLoading();
        WifiLockAddPwdPublishBean.ParamsBean paramsBean = new WifiLockAddPwdPublishBean.ParamsBean();
        paramsBean.setKey(key);
        // TODO: 2021/3/17 后期修改密钥属性
        paramsBean.setKeyType(0);

        LockMessage message = new LockMessage();
        message.setMqtt_message_code(MQttConstant.CREATE_PWD);
        message.setMqtt_topic(MQttConstant.getCallTopic(uid));
        message.setMqttMessage(MqttCommandFactory.addPwd(
                wifiId,
                paramsBean,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2())
                )));
        message.setMessageType(2);
        EventBus.getDefault().post(message);
    }

    private void processAddPwd(WifiLockAddPwdRspBean bean) {
        // toDisposable(mAddPwdDisposable);
        dismissLoading();
        if (bean == null) {
            Timber.e("processAddPwd bean == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("processAddPwd code : %1d", bean.getCode());
            if (bean.getCode() == 201) {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(R.string.t_add_pwd_fail);
            }
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("processAddPwd bean.getParams() == null");
            return;
        }
        mNum = bean.getParams().getKeyNum();
        mDevicePwdBean.setPwdNum(mNum);
        // 使用秒存储，所以除以1000
        mDevicePwdBean.setCreateTime(ZoneUtil.getTime() / 1000);
        mDevicePwdBean.setDeviceId(mBleDeviceLocal.getId());
        mDevicePwdBean.setAttribute(BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS);
        if (mSelectedPwdState == PERMANENT_STATE) {
            savePwdToService(mDevicePwdBean);
        } else if (mSelectedPwdState == SCHEDULE_STATE) {
            setSchedulePwd();
        } else if (mSelectedPwdState == TEMPORARY_STATE) {
            setTimePwd();
        }
    }

    private void publishAddPwdAttr(String wifiId, int attribute, int keyNum, long startTime, long endTime, int week) {
        WifiLockAddPwdAttrPublishBean.ParamsBean paramsBean = new WifiLockAddPwdAttrPublishBean.ParamsBean();
        paramsBean.setAttribute(attribute);
        paramsBean.setEndTime(endTime);
        paramsBean.setKeyNum(keyNum);
        paramsBean.setStartTime(startTime);
        paramsBean.setWeek(week);
        // TODO: 2021/3/17 后期修改密钥属性
        paramsBean.setKeyType(0);
        //toDisposable(mSetPwdAttrDisposable);

        LockMessage message = new LockMessage();
        message.setMessageType(2);
        message.setMqtt_message_code(MQttConstant.ADD_PWD);
        message.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        message.setMqttMessage(MqttCommandFactory.addPwdAttr(
                wifiId,
                paramsBean,
                BleCommandFactory.getPwd(
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()),
                        ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2())
                )));
        EventBus.getDefault().post(message);
    }

    private void setPwdAttrCallback(WifiLockAddPwdAttrResponseBean bean) {
        dismissLoading();
        if (bean == null) {
            Timber.e("publishAddPwdAttr bean == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("publishAddPwdAttr code : %1d", bean.getCode());
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("publishAddPwdAttr bean.getParams() == null");
            return;
        }
        savePwdToService(mDevicePwdBean);
    }

    // TODO: 2021/2/4 要做后面时间不能超过前面时间的判断和逻辑处理
    /*--------------------------  把密码上传到服务器  ---------------------------*/
    // TODO: 2021/2/24 要做失败重新请求
    private void savePwdToService(DevicePwdBean devicePwdBean) {
        List<LockKeyAddBeanReq.PwdListBean> pwdListBeans = new ArrayList<>();
        LockKeyAddBeanReq.PwdListBean pwdListBean = new LockKeyAddBeanReq.PwdListBean();
        pwdListBean.setNum(devicePwdBean.getPwdNum());
        pwdListBean.setNickName(devicePwdBean.getPwdNum() + "");
        pwdListBean.setPwdType(1);
        if (devicePwdBean.getAttribute() == BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS) {
            pwdListBean.setType(BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS);
        } else if (devicePwdBean.getAttribute() == KEY_SET_ATTRIBUTE_TIME_KEY) {
            pwdListBean.setType(KEY_SET_ATTRIBUTE_TIME_KEY);
            pwdListBean.setStartTime(devicePwdBean.getStartTime());
            pwdListBean.setEndTime(devicePwdBean.getEndTime());
        } else if (devicePwdBean.getAttribute() == KEY_SET_ATTRIBUTE_WEEK_KEY) {
            pwdListBean.setType(KEY_SET_ATTRIBUTE_WEEK_KEY);
            pwdListBean.setStartTime(devicePwdBean.getStartTime());
            pwdListBean.setEndTime(devicePwdBean.getEndTime());
            pwdListBean.setItems(getWeekItems());
        }
        pwdListBeans.add(pwdListBean);
        LockKeyAddBeanReq req = new LockKeyAddBeanReq();
        req.setPwdList(pwdListBeans);
        req.setSn(mBleDeviceLocal.getEsn());
        if (App.getInstance().getUserBean() == null) {
            Timber.e("savePwdToService App.getInstance().getUserBean() is null");
            dismissLoadingAndShowAddFail();
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("savePwdToService uid is Empty");
            dismissLoadingAndShowAddFail();
            return;
        }
        req.setUid(uid);

        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("savePwdToService token is Empty");
            dismissLoadingAndShowAddFail();
            return;
        }
        dataRequestService(devicePwdBean, req, token);
    }

    private void dataRequestService(@NotNull DevicePwdBean devicePwdBean,
                                    @NotNull LockKeyAddBeanReq req,
                                    @NotNull String token) {
        if (!checkNetConnectFail()) {
            return;
        }
        Observable<LockKeyAddBeanRsp> observable = HttpRequest.getInstance().addLockKey(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<LockKeyAddBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull LockKeyAddBeanRsp lockKeyAddBeanRsp) {
                // TODO: 2021/2/24 处理异常情况
                String code = lockKeyAddBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("savePwdToService lockKeyAddBeanRsp.getCode() is Empty");
                    dismissLoadingAndShowAddFail();
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, AddNewPwdSelectActivity.this);
                        return;
                    }
                    if (!TextUtils.isEmpty(lockKeyAddBeanRsp.getMsg())) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(lockKeyAddBeanRsp.getMsg());
                    }
                    Timber.e("savePwdToService code: %1s, msg: %2s", lockKeyAddBeanRsp.getCode(), lockKeyAddBeanRsp.getMsg());
                    dismissLoadingAndShowAddFail();
                    return;
                }
                if (lockKeyAddBeanRsp.getData() == null) {
                    Timber.e("savePwdToService lockKeyAddBeanRsp.getData() == null");
                    dismissLoadingAndShowAddFail();
                    return;
                }
                dismissLoading();
                devicePwdBean.setCreateTime(lockKeyAddBeanRsp.getData().getCreateTime());
                showSucMessage();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.e(e);
                dismissLoading();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private List<String> getWeekItems() {
        List<String> list = new ArrayList<>();
        if (isSelectedSun) {
            list.add("0");
        }
        if (isSelectedMon) {
            list.add("1");
        }
        if (isSelectedTues) {
            list.add("2");
        }
        if (isSelectedWed) {
            list.add("3");
        }
        if (isSelectedThur) {
            list.add("4");
        }
        if (isSelectedFri) {
            list.add("5");
        }
        if (isSelectedSat) {
            list.add("6");
        }
        return list;
    }
}
