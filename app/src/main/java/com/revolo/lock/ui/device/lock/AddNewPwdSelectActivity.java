package com.revolo.lock.ui.device.lock;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.blankj.utilcode.util.TimeUtils;
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
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.widget.iosloading.CustomerLoadingDialog;

import java.nio.charset.StandardCharsets;

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
    private CustomerLoadingDialog mLoadingDialog;
    private String mKey;
    private TextView mTvStartTime, mTvEndTime;
    private TextView mTvStartDate, mTvStartDateTime, mTvEndDate, mTvEndDateTime;
    private View mVSun, mVMon, mVTues, mVWed, mVThur, mVFri, mVSat;

    private boolean isSelectedSun = false;
    private boolean isSelectedMon = false;
    private boolean isSelectedTues = false;
    private boolean isSelectedWed = false;
    private boolean isSelectedThur = false;
    private boolean isSelectedFri = false;
    private boolean isSelectedSat = false;

    @IntDef(value = {PERMANENT_STATE, SCHEDULE_STATE, TEMPORARY_STATE})
    private @interface AttributeState{}

    private static final int PERMANENT_STATE = 1;
    private static final int SCHEDULE_STATE = 2;
    private static final int TEMPORARY_STATE = 3;

    @AttributeState
    private int mSelectedPwdState = PERMANENT_STATE;

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
        initGlobalView();
        initApplyClick();

        // TODO: 2021/1/29 抽离英文
        mLoadingDialog = new CustomerLoadingDialog.Builder(this)
                .setMessage("password generating")
                .setCancelable(true)
                .setCancelOutside(false)
                .create();
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
        applyDebouncingClickListener(rlPermanent, rlSchedule, rlTemporary, mBtnNext,
                tvSun, tvMon, tvTues, tvWed, tvThur, tvFri, tvSat, mTvStartTime,
                mTvEndTime, mTvStartDate, mTvStartDateTime, mTvEndDate, mTvEndDateTime);
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
    }

    @Override
    public void doBusiness() {
        initScheduleStartTimeMill();
        initScheduleEndTimeMill();
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
            nextStep();
            return;
        }
        if(view.getId() == R.id.tvSun) {
            isSelectedSun = !isSelectedSun;
            mVSun.setVisibility(isSelectedSun?View.VISIBLE:View.GONE);
            return;
        }
        if(view.getId() == R.id.tvMon) {
            isSelectedMon = !isSelectedMon;
            mVMon.setVisibility(isSelectedMon?View.VISIBLE:View.GONE);
            return;
        }
        if(view.getId() == R.id.tvTues) {
            isSelectedTues = !isSelectedTues;
            mVTues.setVisibility(isSelectedTues?View.VISIBLE:View.GONE);
            return;
        }
        if(view.getId() == R.id.tvWed) {
            isSelectedWed = !isSelectedWed;
            mVWed.setVisibility(isSelectedWed?View.VISIBLE:View.GONE);
            return;
        }
        if(view.getId() == R.id.tvThur) {
            isSelectedThur = !isSelectedThur;
            mVThur.setVisibility(isSelectedThur?View.VISIBLE:View.GONE);
            return;
        }
        if(view.getId() == R.id.tvFri) {
            isSelectedFri = !isSelectedFri;
            mVFri.setVisibility(isSelectedFri?View.VISIBLE:View.GONE);
            return;
        }
        if(view.getId() == R.id.tvSat) {
            isSelectedSat = !isSelectedSat;
            mVSat.setVisibility(isSelectedSat?View.VISIBLE:View.GONE);
            return;
        }
        if(view.getId() == R.id.tvStartTime) {
            showTimePicker(R.id.tvStartTime);
            return;
        }
        if(view.getId() == R.id.tvEndTime) {
            showTimePicker(R.id.tvEndTime);
            return;
        }
        if(view.getId() == R.id.tvStartDate) {
            showDatePicker(R.id.tvStartDate);
            return;
        }
        if(view.getId() == R.id.tvStartDateTime) {
            showTimePicker(R.id.tvStartDateTime);
            return;
        }
        if(view.getId() == R.id.tvEndDate) {
            showDatePicker(R.id.tvEndDate);
            return;
        }
        if(view.getId() == R.id.tvEndDateTime) {
            showTimePicker(R.id.tvEndDateTime);
        }
    }

    private void nextStep() {
        if(mLoadingDialog != null) {
            if(mKey == null) {
                // TODO: 2021/1/29 处理密码为空的情况
                return;
            }
            if(mBleBean == null) {
                Timber.e("mBleBean == null");
                return;
            }
            if(mBleBean.getPwd1() == null) {
                Timber.e("mBleBean.getPwd1() == null");
                return;
            }
            if(mBleBean.getPwd3() == null) {
                Timber.e("mBleBean.getPwd3() == null");
                return;
            }
            mLoadingDialog.show();
            App.getInstance().writeControlMsg(BleCommandFactory.addKey(KEY_SET_KEY_TYPE_PWD,
                    mKey.getBytes(StandardCharsets.UTF_8), mBleBean.getPwd1(), mBleBean.getPwd3()));
            // TODO: 2021/1/29 需要做超时操作
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

    /**                     时间和日期的选择器                         **/

    private long mScheduleStartTimeMill;
    private long mScheduleEndTimeMill;
    private long mTemStartDateTimeMill = TimeUtils.string2Millis("2020-12-28 10:00:00");
    private long mTemEndDateTimeMill = TimeUtils.string2Millis("2020-12-28 14:00:00");
    private String mTemStartDateTimeStr = "10:00:00";
    private String mTemEndDateTimeStr = "14:00:00";

    private void initScheduleStartTimeMill() {
        String nowDate = TimeUtils.millis2String(TimeUtils.getNowMills(), TimeUtils.getSafeDateFormat("yyyy-MM-dd"));
        String date = nowDate + " 00:00:00";
        mScheduleStartTimeMill = TimeUtils.string2Millis(date);
    }

    private void initScheduleEndTimeMill() {
        String nowDate = TimeUtils.millis2String(TimeUtils.getNowMills(), TimeUtils.getSafeDateFormat("yyyy-MM-dd"));
        String date = nowDate + " 23:59:00";
        mScheduleEndTimeMill = TimeUtils.string2Millis(date);
    }

    private void showTimePicker(@IdRes int id) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.MyTimePickerDialogTheme,
                (view, hourOfDay, minute) -> {
            String time = (hourOfDay<10?"0"+hourOfDay:hourOfDay)+":"+(minute<10?"0"+minute:minute);
            if(id == R.id.tvStartTime) {
                mTvStartTime.setText(time);
                mScheduleStartTimeMill = TimeUtils.string2Millis("2000-01-01 " + time + ":00");
                Timber.d("startTime 选择的时间%1s, 时间流：%2d",time, mScheduleStartTimeMill);
            } else if(id == R.id.tvEndTime) {
                mTvEndTime.setText(time);
                mScheduleEndTimeMill = TimeUtils.string2Millis("2000-01-01 " + time + ":00");
                Timber.d("endTime 选择的时间%1s, 时间流：%2d",time, mScheduleEndTimeMill);
            } else if(id == R.id.tvEndDateTime) {
                mTvEndDateTime.setText(time);
                mTemEndDateTimeStr = time;
                mTemEndDateTimeMill = TimeUtils.string2Millis(mTemEndDateStr + " " + mTemEndDateTimeStr + ":00");
                Timber.d("endDateTime 选择的时间%1s, 时间流：%2d",time, mTemEndDateTimeMill);
            } else if(id == R.id.tvStartDateTime) {
                mTvStartDateTime.setText(time);
                mTemStartDateTimeStr = time;
                mTemStartDateTimeMill = TimeUtils.string2Millis(mTemStartDateStr + " " + mTemStartDateTimeStr + ":00");
                Timber.d("startDateTime 选择的时间%1s, 时间流：%2d",time, mTemStartDateTimeMill);
            }
        }, 0,0, true);
        timePickerDialog.show();
    }

    private String mTemEndDateStr = "2020-12-28";
    private String mTemStartDateStr = "2020-12-28";
    private void showDatePicker(@IdRes int id) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, R.style.MyTimePickerDialogTheme);
        datePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            // TODO: 2021/2/4 date
            month+=1;
            String date = year+"-"+(month<10?"0"+month:month)+"-"+(dayOfMonth<10?"0"+dayOfMonth:dayOfMonth);
            if(id == R.id.tvStartDate) {
                mTemStartDateStr = date;
                mTemStartDateTimeMill = TimeUtils.string2Millis(mTemStartDateStr + " " + mTemStartDateTimeStr);
                mTvStartDate.setText(mTemStartDateStr);
                Timber.d("startDate 选择的日期%1s, 时间流：%2d",date, mTemStartDateTimeMill);
            } else if(id == R.id.tvEndDate) {
                mTemEndDateStr = date;
                mTemEndDateTimeMill = TimeUtils.string2Millis(mTemEndDateStr + " " + mTemEndDateTimeStr);
                mTvEndDate.setText(mTemEndDateStr);
                Timber.d("startDate 选择的日期%1s, 时间流：%2d",date, mTemStartDateTimeMill);
            }
        });
        datePickerDialog.setCancelable(true);
        datePickerDialog.show();
    }

    /**                  蓝牙指令与处理               **/
    private byte mNum;

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
        if(mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            // TODO: 2021/1/30 提示失败
            return;
        }
        if(bleResultBean.getCMD() == BleProtocolState.CMD_KEY_ADD) {
            // TODO: 2021/2/4 添加的时候需要判断后时间不能少于前时间
            // 添加密钥
            byte state = bleResultBean.getPayload()[0];
            if(state == 0x00) {
                mNum = bleResultBean.getPayload()[1];
                App.getInstance().getCacheDiskUtils().put("createTime"+mNum, TimeUtils.getNowMills());
                if(mSelectedPwdState == PERMANENT_STATE) {
                    showSucAndGotoAnotherPage();
                } else if(mSelectedPwdState == SCHEDULE_STATE) {
                    // 周策略 BIT:   7   6   5   4   3   2   1   0
                    // 星期：      保留  六  五  四  三  二  一  日
                    byte[] weekBit = new byte[8];
                    weekBit[0] = (byte) (isSelectedSun?0x01:0x00);
                    weekBit[1] = (byte) (isSelectedMon?0x01:0x00);
                    weekBit[2] = (byte) (isSelectedTues?0x01:0x00);
                    weekBit[3] = (byte) (isSelectedWed?0x01:0x00);
                    weekBit[4] = (byte) (isSelectedThur?0x01:0x00);
                    weekBit[5] = (byte) (isSelectedFri?0x01:0x00);
                    weekBit[6] = (byte) (isSelectedSat?0x01:0x00);
                    byte week = BleByteUtil.bitToByte(weekBit);
                    Timber.d("sun: %1b, mon: %2b, tues: %3b, wed: %4b, thur: %5b, fri: %6b, sat: %7b",
                            isSelectedSun, isSelectedMon, isSelectedTues, isSelectedWed,
                            isSelectedThur, isSelectedFri, isSelectedSat);
                    Timber.d("num: %1s, week: %2s, weekBytes: %3s, startTime: %4d, endTime: %5d",
                            mNum, ConvertUtils.int2HexString(week), ConvertUtils.bytes2HexString(weekBit),
                            mScheduleStartTimeMill/1000, mScheduleEndTimeMill/1000);
                    App.getInstance()
                            .writeControlMsg(BleCommandFactory
                                    .keyAttributesSet(KEY_SET_KEY_OPTION_ADD_OR_CHANGE,
                                            KEY_SET_KEY_TYPE_PWD,
                                            mNum,
                                            KEY_SET_ATTRIBUTE_WEEK_KEY,
                                            week,
                                            mScheduleStartTimeMill/1000,
                                            mScheduleEndTimeMill/1000,
                                            mBleBean.getPwd1(),
                                            mBleBean.getPwd3()));
                } else if(mSelectedPwdState == TEMPORARY_STATE) {
                    Timber.d("num: %1s, startTime: %2d, endTime: %3d",
                            mNum, mTemStartDateTimeMill/1000, mTemEndDateTimeMill/1000);
                    App.getInstance()
                            .writeControlMsg(BleCommandFactory
                                    .keyAttributesSet(KEY_SET_KEY_OPTION_ADD_OR_CHANGE,
                                            KEY_SET_KEY_TYPE_PWD, 
                                            mNum,
                                            KEY_SET_ATTRIBUTE_TIME_KEY,
                                            (byte) 0x00,
                                            mTemStartDateTimeMill/1000,
                                            mTemEndDateTimeMill/1000,
                                            mBleBean.getPwd1(),
                                            mBleBean.getPwd3()));
                }

            } else {
                // TODO: 2021/1/29 添加失败后的UI操作
                Timber.e("添加密钥失败，state: %1s", BleByteUtil.byteToInt(state));
            }
        } else if(bleResultBean.getCMD() == CMD_KEY_ATTRIBUTES_SET) {
            byte state = bleResultBean.getPayload()[0];
            if(state == 0x00) {
                showSucAndGotoAnotherPage();
            } else {
                // TODO: 2021/2/4  设置密钥属性失败要如何处理
                Timber.e("设置密钥属性失败，state: %1s", BleByteUtil.byteToInt(state));
            }
        }
    };

    private void showSucAndGotoAnotherPage() {
        runOnUiThread(() -> {
            MessageDialog dialog = new MessageDialog(AddNewPwdSelectActivity.this);
            dialog.setMessage(getString(R.string.dialog_tip_password_added));
            dialog.setOnListener(v -> {
                // 不销毁会导致内存泄漏
                dialog.dismiss();
                App.getInstance().addWillFinishAct(this);
                Intent intent = new Intent(AddNewPwdSelectActivity.this, AddNewPwdNameActivity.class);
                intent.putExtra(Constant.KEY_PWD_NUM, mNum);
                startActivity(intent);
            });
            dialog.show();
        });
    }

    private void initDevice() {
        if(mBleBean == null || mBleBean.getOKBLEDeviceImp() == null) {
            // TODO: 2021/1/30 做对应的处理
            Timber.e("initDevice mBleBean == null || mBleBean.getOKBLEDeviceImp() == null");
            return;
        }
        App.getInstance().openPairNotify();
        App.getInstance().setOnBleDeviceListener(mOnBleDeviceListener);
    }


    // TODO: 2021/2/4 要做后面时间不能超过前面时间的判断和逻辑处理

}
