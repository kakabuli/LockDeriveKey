package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.adapter.PasswordListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.request.SearchKeyListBeanReq;
import com.revolo.lock.bean.respone.SearchKeyListBeanRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.DevicePwd;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_TIME_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_WEEK_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_TYPE_PWD;
import static com.revolo.lock.ble.BleProtocolState.CMD_KEY_ATTRIBUTES_READ;
import static com.revolo.lock.ble.BleProtocolState.CMD_SY_KEY_STATE;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 密码列表界面
 */
public class PasswordListActivity extends BaseActivity {

    private PasswordListAdapter mPasswordListAdapter;
    private long mDeviceId;
    private String mESN;

    @Override
    public void initData(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        if(intent.hasExtra(Constant.DEVICE_ID)) {
            mDeviceId = intent.getLongExtra(Constant.DEVICE_ID, -1L);
            Timber.d("initData Device Id: %1d", mDeviceId);
        }
        if(mDeviceId == -1) {
            // TODO: 2021/2/24 处理异常情况
            finish();
        }
        if(intent.hasExtra(Constant.LOCK_ESN)) {
            mESN = intent.getStringExtra(Constant.LOCK_ESN);
            Timber.d("initData Device Esn: %1s", mESN);
        }
        if(TextUtils.isEmpty(mESN)) {
            // TODO: 2021/2/24 无法获取esn来处理问题
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_password_list;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.password))
                .setRight(ContextCompat.getDrawable(this, R.drawable.ic_home_icon_add),
                        v -> {
                    if(mDeviceId == -1) {
                        return;
                    }
                    Intent intent = new Intent(this, AddInputNewPwdActivity.class);
                    intent.putExtra(Constant.DEVICE_ID, mDeviceId);
                    startActivity(intent);
                });
        RecyclerView rvPwdList = findViewById(R.id.rvPwdList);
        rvPwdList.setLayoutManager(new LinearLayoutManager(this));
        mPasswordListAdapter = new PasswordListAdapter(R.layout.item_pwd_list_rv);
        mPasswordListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if(position >= 0 && adapter.getItem(position) instanceof DevicePwd) {
                Intent intent = new Intent(PasswordListActivity.this, PasswordDetailActivity.class);
                DevicePwd item  = (DevicePwd) adapter.getItem(position);
                intent.putExtra(Constant.PWD_ID, item.getId());
                startActivity(intent);
            }
        });
        rvPwdList.setAdapter(mPasswordListAdapter);
    }

    @Override
    public void doBusiness() {
        initBleListener();
        searchPwdListFromLocal();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    // TODO: 2021/2/24 要做数据校对流程, 要做超时, 需要添加加载框
    /*-------------------------------- 密钥数据从服务器库获取 ---------------------------------*/

    private void searchPwdListFromNET() {
        // TODO: 2021/2/24 异常情况处理
        if(App.getInstance().getUserBean() == null) {
            Timber.e("searchPwdListFromNET App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if(TextUtils.isEmpty(uid)) {
            Timber.e("searchPwdListFromNET App.getInstance().getUserBean().getUid() is Empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if(TextUtils.isEmpty(token)) {
            Timber.e("searchPwdListFromNET App.getInstance().getUserBean().getToken()");
            return;
        }
        SearchKeyListBeanReq req = new SearchKeyListBeanReq();
        req.setPwdType(1);
        req.setSn(mESN);
        req.setUid(uid);
        Observable<SearchKeyListBeanRsp> observable = HttpRequest.getInstance().searchLockKey(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<SearchKeyListBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull SearchKeyListBeanRsp searchKeyListBeanRsp) {
                processKeyListFromNet(searchKeyListBeanRsp);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void processKeyListFromNet(@NonNull SearchKeyListBeanRsp searchKeyListBeanRsp) {
        if(TextUtils.isEmpty(searchKeyListBeanRsp.getCode())) {
            Timber.e("processKeyListFromNet searchKeyListBeanRsp.getCode() is Empty");
            return;
        }
        if(!searchKeyListBeanRsp.getCode().equals("200")) {
            // TODO: 2021/2/24 还得做其他处理
            ToastUtils.showShort(searchKeyListBeanRsp.getMsg());
            Timber.e("processKeyListFromNet code: %1s, msg: %2s",
                    searchKeyListBeanRsp.getCode(), searchKeyListBeanRsp.getMsg());
            return;
        }
        if(searchKeyListBeanRsp.getData() == null) {
            Timber.e("processKeyListFromNet searchKeyListBeanRsp.getData() == null");
            return;
        }
        if(searchKeyListBeanRsp.getData().getPwdList() == null) {
            Timber.e("processKeyListFromNet searchKeyListBeanRsp.getData().getPwdList() == null");
            return;
        }
        if(searchKeyListBeanRsp.getData().getPwdList().isEmpty()) {
            Timber.e("processKeyListFromNet searchKeyListBeanRsp.getData().getPwdList().isEmpty()");
            return;
        }
        List<DevicePwd> pwdList = new ArrayList<>();
        for (SearchKeyListBeanRsp.DataBean.PwdListBean bean : searchKeyListBeanRsp.getData().getPwdList()) {
            DevicePwd devicePwd = new DevicePwd();
            devicePwd.setPwdNum(bean.getNum());
            devicePwd.setDeviceId(mDeviceId);
            devicePwd.setCreateTime(bean.getCreateTime());
            devicePwd.setPwdName(bean.getNickName());
            devicePwd.setStartTime(bean.getStartTime());
            devicePwd.setEndTime(bean.getEndTime());
            @BleCommandState.KeySetAttribute int attribute = getPwdAttribute(bean.getType());
            devicePwd.setAttribute(attribute);
            setWeeklyFromNetData(bean, devicePwd, attribute);
            // 默认可用
            // TODO: 2021/2/24 后面需要修改通过策略和时间判断是否可用
            devicePwd.setPwdState(1);
            pwdList.add(devicePwd);
        }
        AppDatabase.getInstance(getApplicationContext()).devicePwdDao().insert(pwdList);
    }

    private void setWeeklyFromNetData(SearchKeyListBeanRsp.DataBean.PwdListBean bean, DevicePwd devicePwd, int attribute) {
        // TODO: 2021/2/24 后续需要考虑为空的情况如何处理
        // 周策略 BIT:   7   6   5   4   3   2   1   0
        // 星期：      保留  六  五  四  三  二  一  日
        if(attribute == KEY_SET_ATTRIBUTE_WEEK_KEY) {
            boolean isSaveWeekly = true;
            if(bean.getItems() == null) {
                Timber.e("processKeyListFromNet bean.getItems() == null");
                isSaveWeekly = false;
            }
            if(bean.getItems().isEmpty()) {
                Timber.e("processKeyListFromNet bean.getItems().isEmpty()");
                isSaveWeekly = false;
            }
            byte[] weekBit = new byte[8];
            for (String day : bean.getItems()) {
                for (int i=0; i<=6; i++) {
                    String tmpDay = i+"";
                    if(day.equals(tmpDay)) {
                        weekBit[i] = 0x01;
                        break;
                    }
                }
            }
            if(isSaveWeekly) {
                devicePwd.setWeekly(BleByteUtil.bitToByte(weekBit));
            }
        }
    }

    private int getPwdAttribute(int type) {
        if(type == 1) {
            return BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS;
        } else if(type == 2) {
            return BleCommandState.KEY_SET_ATTRIBUTE_TIME_KEY;
        } else if(type == 3) {
            return BleCommandState.KEY_SET_ATTRIBUTE_WEEK_KEY;
        } else {
            return BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS;
        }
    }

    /*-------------------------------- 密钥数据从APP本地数据库获取 ---------------------------------*/

    private List<DevicePwd> mDevicePwdList;

    private void searchPwdListFromLocal() {
        if(mDeviceId == -1) {
            // TODO: 2021/2/21 错误的数据如何处理
            return;
        }
        mDevicePwdList = AppDatabase.getInstance(this).devicePwdDao().findDevicePwdListFromDeviceId(mDeviceId);
        // TODO: 2021/2/24 后续需要使用数据校验
        if(mDevicePwdList == null) {
            searchPwdListFromNET();
            return;
        }
        if(mDevicePwdList.isEmpty()) {
            searchPwdListFromNET();
            return;
        }
        // TODO: 2021/2/21 取得所有密码数据并进行显示
        runOnUiThread(() -> mPasswordListAdapter.setList(mDevicePwdList));
    }

    /*-------------------------------- 密钥数据从锁端蓝牙获取 ---------------------------------*/

    private final BleResultProcess.OnReceivedProcess mOnReceivedProcess = bleResultBean -> {
        if(bleResultBean == null) {
            Timber.e("mOnReceivedProcess bleResultBean == null");
            return;
        }
        getPwdListFormBle(bleResultBean);
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

    private void checkHadPwdFromBle() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mWillSearchList.clear();
            mPasswordListAdapter.setList(mDevicePwdFormBle);
            App.getInstance().writeControlMsg(BleCommandFactory
                    .synchronizeLockKeyStatusCommand((byte) 0x01,
                            App.getInstance().getBleBean().getPwd1(),
                            App.getInstance().getBleBean().getPwd3()));
            Timber.d("发送了请求密钥列表指令");
        }, 100);
    }

    private final ArrayList<Byte> mWillSearchList = new ArrayList<>();
    private final ArrayList<DevicePwd> mDevicePwdFormBle = new ArrayList<>();

    private void getPwdListFormBle(BleResultBean bean) {
        // TODO: 2021/2/3 可能存在100条数据以上，后续需要做100条数据以上的测试
        // TODO: 2021/2/4 后续需要做去重操作
        if(bean.getCMD() == CMD_SY_KEY_STATE) {
            checkPwdIsExist(bean);
            // 查询到密钥存在后，开始读取对应密钥
            mHandler.postDelayed(mSearchPwdListRunnable, 20);
        } else if(bean.getCMD() == CMD_KEY_ATTRIBUTES_READ) {
            String name = App.getInstance().getCacheDiskUtils().getString("pwdName"+mCurrentSearchNum);
            byte attribute = bean.getPayload()[0];
            if(attribute == KEY_SET_ATTRIBUTE_ALWAYS) {
                addPermanentPwd(name);
            } else if(attribute == KEY_SET_ATTRIBUTE_TIME_KEY) {
                // TODO: 2021/2/7 时间高低位反回来取
                addTimePwd(bean, name);
            } else if(attribute == KEY_SET_ATTRIBUTE_WEEK_KEY) {
                // TODO: 2021/2/7 时间高低位反回来取
                addWeeklyPwd(bean, name);
            }
            runOnUiThread(() -> mPasswordListAdapter.setList(mDevicePwdFormBle));
            mHandler.postDelayed(mSearchPwdListRunnable, 20);
        }
    }

    private void addPermanentPwd(String name) {
        Timber.d("addPermanentPwd num: %1s", mCurrentSearchNum);
        DevicePwd devicePwd = new DevicePwd();
        devicePwd.setPwdNum(mCurrentSearchNum);
        // 使用秒存储，所以除以1000
        // TODO: 2021/2/24 后续需要改掉，存在问题，不可能使用这个创建时间
        devicePwd.setCreateTime(TimeUtils.getNowMills()/1000);
        devicePwd.setDeviceId(mDeviceId);
        devicePwd.setAttribute(BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS);
        devicePwd.setPwdName(name);
        AppDatabase.getInstance(this).devicePwdDao().insert(devicePwd);
        mDevicePwdFormBle.add(devicePwd);
    }

    private void addTimePwd(BleResultBean bean, String name) {
        byte[] startTimeBytes = new byte[4];
        byte[] endTimeBytes = new byte[4];
        System.arraycopy(bean.getPayload(), 2, startTimeBytes, 0, startTimeBytes.length);
        System.arraycopy(bean.getPayload(), 6, endTimeBytes, 0, endTimeBytes.length);
        long startTimeMill = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(startTimeBytes));
        long endTimeMill = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(endTimeBytes));
        DevicePwd devicePwd = new DevicePwd();
        devicePwd.setDeviceId(mDeviceId);
        devicePwd.setPwdName(name);
        devicePwd.setPwdNum(mCurrentSearchNum);
        devicePwd.setAttribute(KEY_SET_ATTRIBUTE_TIME_KEY);
        devicePwd.setStartTime(startTimeMill);
        devicePwd.setEndTime(endTimeMill);
        AppDatabase.getInstance(this).devicePwdDao().insert(devicePwd);
        mDevicePwdFormBle.add(devicePwd);
    }

    private void addWeeklyPwd(BleResultBean bean, String name) {
        byte[] weekBytes = BleByteUtil.byteToBit(bean.getPayload()[1]);
        Timber.d("addWeeklyPwd num: %1s week: %1s", mCurrentSearchNum, ConvertUtils.bytes2HexString(weekBytes));
        byte[] startTimeBytes = new byte[4];
        byte[] endTimeBytes = new byte[4];
        System.arraycopy(bean.getPayload(), 2, startTimeBytes, 0, startTimeBytes.length);
        System.arraycopy(bean.getPayload(), 6, endTimeBytes, 0, endTimeBytes.length);
        long startTimeMill = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(startTimeBytes));
        long endTimeMill = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(endTimeBytes));
        DevicePwd devicePwd = new DevicePwd();
        devicePwd.setDeviceId(mDeviceId);
        devicePwd.setPwdNum(mCurrentSearchNum);
        devicePwd.setPwdName(name);
        devicePwd.setWeekly(bean.getPayload()[1]);
        devicePwd.setStartTime(startTimeMill);
        devicePwd.setEndTime(endTimeMill);
        devicePwd.setAttribute(KEY_SET_ATTRIBUTE_WEEK_KEY);
        AppDatabase.getInstance(this).devicePwdDao().insert(devicePwd);
        mDevicePwdFormBle.add(devicePwd);
    }

    private void checkPwdIsExist(BleResultBean bean) {
        mWillSearchList.clear();
        byte[] value = bean.getPayload();
        int index = value[0] & 0xff;
        int codeType = value[1] & 0xff;
        int codeNumber = value[2] & 0xff;
        Timber.d("秘钥的帧数是  %1d, 秘钥类型是  %2d  秘钥总数是   %3d", index, codeType, codeNumber);
        // TODO: 2021/2/3 密钥列表的解析，有疑问，后续需要增加解析并显示
        // 暂时项目只有20条密码极限
        // 1-8 数据是倒着来计算的从byte[7]-byte[0]
        byte[] num1 = BleByteUtil.byteToBit(value[3]);
        // 9-16
        byte[] num2 = BleByteUtil.byteToBit(value[4]);
        // 17-20
        byte[] num3 = BleByteUtil.byteToBit(value[5]);
        Timber.d("1-8: %1s, 9-16: %2s, 17-20: %3s",
                ConvertUtils.bytes2HexString(num1), ConvertUtils.bytes2HexString(num2), ConvertUtils.bytes2HexString(num3));
        // 循环判断20个内有哪些编号是存在密码的
        for (int i=7; i>=0; i--) {
            if(num1[i] == 0x01) {
                mWillSearchList.add((byte) (7-i));
            }
        }
        for (int i=7; i>=0; i--) {
            if(num2[i] == 0x01) {
                mWillSearchList.add((byte) (15-i));
            }
        }
        for (int i=7; i>=4; i--) {
            if(num3[i] == 0x01) {
                mWillSearchList.add((byte) (23-i));
            }
        }
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mSearchPwdListRunnable = this::searchPwdList;
    private byte mCurrentSearchNum;

    private void searchPwdList() {
        if(mWillSearchList.isEmpty()) {
            Timber.d("searchPwdList 要搜索的密码列表是空");
            return;
        }
        mCurrentSearchNum = mWillSearchList.get(0);
        App.getInstance().writeControlMsg(BleCommandFactory
                .keyAttributesRead(KEY_SET_KEY_TYPE_PWD,
                        mCurrentSearchNum,
                        App.getInstance().getBleBean().getPwd1(),
                        App.getInstance().getBleBean().getPwd3()));
        mWillSearchList.remove(0);
    }

}
