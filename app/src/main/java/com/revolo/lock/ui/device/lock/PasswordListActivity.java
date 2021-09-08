package com.revolo.lock.ui.device.lock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.LocalState;
import com.revolo.lock.R;
import com.revolo.lock.adapter.PasswordListAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.bean.DevicePwdBean;
import com.revolo.lock.bean.request.DelKeyBeanReq;
import com.revolo.lock.bean.request.SearchKeyListBeanReq;
import com.revolo.lock.bean.respone.DelKeyBeanRsp;
import com.revolo.lock.bean.respone.SearchKeyListBeanRsp;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleCommandState;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.ble.bean.BleResultBean;
import com.revolo.lock.dialog.MessageDialog;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.manager.LockMessageCode;
import com.revolo.lock.manager.LockMessageRes;
import com.revolo.lock.mqtt.MQttConstant;
import com.revolo.lock.mqtt.MqttCommandFactory;
import com.revolo.lock.mqtt.bean.publishresultbean.WifiLockRemovePasswordResponseBean;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.BleDeviceLocal;
import com.revolo.lock.ui.view.SmartClassicsHeaderView;
import com.revolo.lock.util.ZoneUtil;
import com.revolo.lock.widget.SlideRecyclerView;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_TIME_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_WEEK_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_OPTION_DEL;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_KEY_TYPE_PWD;
import static com.revolo.lock.ble.BleProtocolState.CMD_KEY_ATTRIBUTES_READ;
import static com.revolo.lock.ble.BleProtocolState.CMD_KEY_ATTRIBUTES_SET;
import static com.revolo.lock.ble.BleProtocolState.CMD_SY_KEY_STATE;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 密码列表界面
 */
public class PasswordListActivity extends BaseActivity {
    private PasswordListAdapter mPasswordListAdapter;
    private BleDeviceLocal mBleDeviceLocal;
    private RefreshLayout mRefreshLayout;
    private MessageDialog mPasswordFull;
    private DevicePwdBean devicePwdBean;
    private SlideRecyclerView rvPwdList;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        if (mBleDeviceLocal == null) {
            finish();
        }
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_password_list;
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
        onRegisterEventBus();
        mPasswordFull = new MessageDialog(this);
        mPasswordFull.setMessage(getString(R.string.t_add_input_new_pwd_full));
        mPasswordFull.setOnListener(v -> {
            if (mPasswordFull != null) {
                mPasswordFull.dismiss();
            }
        });
        useCommonTitleBar(getString(R.string.password))
                .setRight(R.drawable.ic_home_icon_add,
                        v -> {
                            if (mPasswordListAdapter != null && mPasswordListAdapter.getItemCount() < 20) {
                                Intent intent = new Intent(this, AddInputNewPwdActivity.class);
                                startActivity(intent);
                            } else {
                                if (mPasswordFull != null) {
                                    mPasswordFull.show();
                                }
                            }
                        });
        rvPwdList = findViewById(R.id.rvPwdList);
        rvPwdList.setLayoutManager(new LinearLayoutManager(this));
        mPasswordListAdapter = new PasswordListAdapter(R.layout.item_pwd_list_rv);
        mBleDeviceLocal = App.getInstance().getBleDeviceLocal();
        String zone = mBleDeviceLocal.getTimeZone();
        Timber.e("zone:" + zone);
        //设置时区
        mPasswordListAdapter.setTimeZone(zone);
        mPasswordListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (position >= 0 && adapter.getItem(position) instanceof DevicePwdBean) {
                Intent intent = new Intent(PasswordListActivity.this, PasswordDetailActivity.class);
                DevicePwdBean item = (DevicePwdBean) adapter.getItem(position);
                intent.putExtra(Constant.PWD_DETAIL, item);
                intent.putExtra(Constant.LOCK_ESN, mBleDeviceLocal.getEsn());
                startActivity(intent);
            }
        });
        rvPwdList.setAdapter(mPasswordListAdapter);
        mPasswordListAdapter.setEmptyView(R.layout.empty_view_password_list);
        mPasswordListAdapter.setOnDeletePassWordListener((view, position) -> {

            if (mPasswordListAdapter != null) {
                DevicePwdBean item = mPasswordListAdapter.getItem(position);
                if (item != null) {
                    showDelDialog(item);
                }
            }
        });
        initLoading(getString(R.string.t_load_content_loading));

        mRefreshLayout = findViewById(R.id.refreshLayout);
        mRefreshLayout.setEnableLoadMore(false);
        mRefreshLayout.setRefreshHeader(new SmartClassicsHeaderView(this));
        mRefreshLayout.setOnRefreshListener(refreshLayout -> initData());

        initSucMessageDialog();
        initFailMessageDialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void getEventBus(LockMessageRes lockMessage) {
        if (lockMessage == null) {
            return;
        }
        if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_USER) {

        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_BLE) {
            //蓝牙消息
            if (null != lockMessage.getBleResultBea()) {
                //密钥数据从锁端蓝牙获取
                getPwdListFormBle(lockMessage.getBleResultBea());
            }
        } else if (lockMessage.getMessgaeType() == LockMessageCode.MSG_LOCK_MESSAGE_MQTT) {
            //MQTT
            if (lockMessage.getResultCode() == LockMessageCode.MSG_LOCK_MESSAGE_CODE_SUCCESS) {
                switch (lockMessage.getMessageCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_REMOVE_PWD:
                        processDelPwd((WifiLockRemovePasswordResponseBean) lockMessage.getWifiLockBaseResponseBean());
                        break;
                }
            } else {
                switch (lockMessage.getResultCode()) {
                    case LockMessageCode.MSG_LOCK_MESSAGE_REMOVE_PWD:
                        break;
                }
            }
        } else {

        }
    }

    @Override
    public void doBusiness() {
        showLoading();
        initData();
    }

    private void initData() {
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
            BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
            if (bleBean != null) {
                new Handler(Looper.getMainLooper()).postDelayed(this::checkHadPwdFromBle, 20);
                //          checkHadPwdFromBle();
            }
        } else {
            searchPwdListFromNET();
        }
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    private void showDelDialog(DevicePwdBean devicePwdBean) {
        SelectDialog dialog = new SelectDialog(this);
        dialog.setMessage(getString(R.string.dialog_tip_password_deleted_message));
        dialog.setOnCancelClickListener(v -> dialog.dismiss());
        dialog.setOnConfirmListener(v -> {
            dialog.dismiss();
            delPwd(devicePwdBean);
            if (rvPwdList != null) {
                rvPwdList.closeMenu();
            }
        });
        dialog.show();
    }

    // TODO: 2021/2/24 要做数据校对流程, 要做超时, 需要添加加载框
    /*-------------------------------- 密钥数据从服务器库获取 ---------------------------------*/

    private void searchPwdListFromNET() {
        if (!checkNetConnectFail()) {
            return;
        }
        // TODO: 2021/2/24 异常情况处理
        if (App.getInstance().getUserBean() == null) {
            Timber.e("searchPwdListFromNET App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("searchPwdListFromNET App.getInstance().getUserBean().getUid() is Empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("searchPwdListFromNET App.getInstance().getUserBean().getToken()");
            return;
        }
        SearchKeyListBeanReq req = new SearchKeyListBeanReq();
        req.setPwdType(1);
        req.setSn(mBleDeviceLocal.getEsn());
        req.setUid(uid);
        Observable<SearchKeyListBeanRsp> observable = HttpRequest.getInstance().searchLockKey(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<SearchKeyListBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull SearchKeyListBeanRsp searchKeyListBeanRsp) {
                if (mRefreshLayout != null) {
                    mRefreshLayout.finishRefresh();
                }
                processPwdListFromNet(searchKeyListBeanRsp);
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

    private void processPwdListFromNet(@NonNull SearchKeyListBeanRsp searchKeyListBeanRsp) {
        String code = searchKeyListBeanRsp.getCode();
        if (TextUtils.isEmpty(code)) {
            Timber.e("processKeyListFromNet searchKeyListBeanRsp.getCode() is Empty");
            dismissLoading();
            return;
        }
        if (!code.equals("200")) {
            // TODO: 2021/2/24 还得做其他处理
            dismissLoading();
            if (code.equals("444")) {
                App.getInstance().logout(true, PasswordListActivity.this);
                return;
            }
            String msg = searchKeyListBeanRsp.getMsg();
            if (!TextUtils.isEmpty(msg)) {
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
            }
            Timber.e("processKeyListFromNet code: %1s, msg: %2s", code, msg);
            return;
        }
        if (searchKeyListBeanRsp.getData() == null) {
            dismissLoading();
            Timber.e("processKeyListFromNet searchKeyListBeanRsp.getData() == null");
            return;
        }
        if (searchKeyListBeanRsp.getData().getPwdList() == null) {
            dismissLoading();
            Timber.e("processKeyListFromNet searchKeyListBeanRsp.getData().getPwdList() == null");
            // 清空数据
            mPasswordListAdapter.setList(new ArrayList<>());
            return;
        }
        if (searchKeyListBeanRsp.getData().getPwdList().isEmpty()) {
            dismissLoading();
            Timber.e("processKeyListFromNet searchKeyListBeanRsp.getData().getPwdList().isEmpty()");
            // 清空数据
            mPasswordListAdapter.setList(new ArrayList<>());
            return;
        }
        List<DevicePwdBean> pwdList = new ArrayList<>();
        for (SearchKeyListBeanRsp.DataBean.PwdListBean bean : searchKeyListBeanRsp.getData().getPwdList()) {
            DevicePwdBean devicePwdBean = new DevicePwdBean();
            devicePwdBean.setPwdNum(bean.getNum());
            devicePwdBean.setDeviceId(mBleDeviceLocal.getId());
            devicePwdBean.setCreateTime(bean.getCreateTime());
            devicePwdBean.setPwdName(bean.getNickName());
            devicePwdBean.setStartTime(bean.getStartTime());
            devicePwdBean.setEndTime(bean.getEndTime());
            @BleCommandState.KeySetAttribute int attribute = bean.getType();
            devicePwdBean.setAttribute(attribute);
            setWeeklyFromNetData(bean, devicePwdBean, attribute);
            // 默认可用
            // TODO: 2021/2/24 后面需要修改通过策略和时间判断是否可用
            devicePwdBean.setPwdState(1);
            if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) {
                Timber.d("bean num: %1d", bean.getNum());
                if (!mWillSearchList.contains(BleByteUtil.intToByte(bean.getNum()))) {
                    mWillDelPwd.add(bean.getNum());
                    continue;
                }
            }
            pwdList.add(devicePwdBean);
        }
        deletePwdToService(mWillDelPwd);
        // deleteCantFindPwd(pwdList);

        showPwdList(pwdList);
    }

    private void showPwdList(List<DevicePwdBean> pwdList) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mPasswordListAdapter.setList(pwdList);
            dismissLoading();
        }, 200);
    }

    private final List<Integer> mWillDelPwd = new ArrayList<>();

   /* private void deleteCantFindPwd(List<DevicePwdBean> pwdList) {
        if (!checkNetConnectFail()) {
            showPwdList(pwdList);
            return;
        }
        if (mWillDelPwd.isEmpty()) {
            showPwdList(pwdList);
            return;
        }
        List<DelKeyBeanReq.PwdListBean> listBeans = new ArrayList<>();
        for (int num : mWillDelPwd) {
            DelKeyBeanReq.PwdListBean pwdListBean = new DelKeyBeanReq.PwdListBean();
            pwdListBean.setNum(num);
            pwdListBean.setPwdType(1);
            listBeans.add(pwdListBean);
        }
        // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除，下面所有
        if (mBleDeviceLocal == null) {
            showPwdList(pwdList);
            Timber.e("delKeyFromService bleDeviceLocal == null");
            return;
        }
        String esn = mBleDeviceLocal.getEsn();
        if (TextUtils.isEmpty(esn)) {
            showPwdList(pwdList);
            Timber.e("delKeyFromService esn is Empty");
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            showPwdList(pwdList);
            Timber.e("delKeyFromService App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            showPwdList(pwdList);
            Timber.e("delKeyFromService uid is Empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            showPwdList(pwdList);
            Timber.e("delKeyFromService token is Empty");
            return;
        }
        DelKeyBeanReq req = new DelKeyBeanReq();
        req.setPwdList(listBeans);
        req.setSn(esn);
        req.setUid(uid);
        Observable<DelKeyBeanRsp> observable = HttpRequest.getInstance().delKey(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<DelKeyBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull DelKeyBeanRsp delKeyBeanRsp) {
                String code = delKeyBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除
                    showPwdList(pwdList);
                    Timber.e("delKeyFromService delKeyBeanRsp.getCode() is Empty");
                    return;
                }
                if (!code.equals("200")) {
                    // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除
                    if (code.equals("444")) {
                        dismissLoading();
                        App.getInstance().logout(true, PasswordListActivity.this);
                        return;
                    }
                    showPwdList(pwdList);
                    String msg = delKeyBeanRsp.getMsg();
                    Timber.e("delKeyFromService code: %1s msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                showPwdList(pwdList);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                showPwdList(pwdList);
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
        mWillDelPwd.clear();
    }*/

    private void setWeeklyFromNetData(SearchKeyListBeanRsp.DataBean.PwdListBean bean, DevicePwdBean devicePwdBean, int attribute) {
        // TODO: 2021/2/24 后续需要考虑为空的情况如何处理
        // 周策略 BIT:   7   6   5   4   3   2   1   0
        // 星期：      保留  六  五  四  三  二  一  日
        if (attribute == KEY_SET_ATTRIBUTE_WEEK_KEY) {
            boolean isSaveWeekly = true;
            if (bean.getItems() == null || bean.getItems().isEmpty()) {
                Timber.e("processKeyListFromNet bean.getItems() == null");
                isSaveWeekly = false;
                return;
            }
            byte[] weekBit = new byte[8];
            for (String day : bean.getItems()) {
                for (int i = 0; i <= 6; i++) {
                    String tmpDay = i + "";
                    if (day.equals(tmpDay)) {
                        weekBit[i] = 0x01;
                        break;
                    }
                }
            }
            if (isSaveWeekly) {
                devicePwdBean.setWeekly(BleByteUtil.bitToByte(weekBit));
            }
        }
    }

    private void checkHadPwdFromBle() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            //   mHandler.removeMessages(MSG_BLE_GET_PWD_LIST_TIME);
            //   mHandler.sendEmptyMessageDelayed(MSG_BLE_GET_PWD_LIST_TIME, 4000);
            //  mDevicePwdBeanCopyList.clear();
            // if (null != mPasswordListAdapter) {
            //     mDevicePwdBeanCopyList.addAll(mPasswordListAdapter.getData());
            //  }
            mWillSearchList.clear();
            mDevicePwdBeanFormBle.clear();
            mPasswordListAdapter.setList(mDevicePwdBeanFormBle);
            BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
            if (bleBean == null) {
                Timber.e("checkHadPwdFromBle bleBean == null");
                return;
            }
            if (bleBean.getOKBLEDeviceImp() == null) {
                Timber.e("checkHadPwdFromBle bleBean.getOKBLEDeviceImp() == null");
                return;
            }
            if (bleBean.getPwd1() == null) {
                Timber.e("checkHadPwdFromBle bleBean.getPwd1() == null");
                return;
            }
            if (bleBean.getPwd3() == null) {
                Timber.e("checkHadPwdFromBle bleBean.getPwd3() == null");
                return;
            }
            LockMessage lockMessage = new LockMessage();
            lockMessage.setBytes(BleCommandFactory
                    .synchronizeLockKeyStatusCommand((byte) 0x01, bleBean.getPwd1(), bleBean.getPwd3()));
            lockMessage.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
            lockMessage.setMessageType(3);
            EventBus.getDefault().post(lockMessage);

           /* App.getInstance().writeControlMsg(BleCommandFactory
                    .synchronizeLockKeyStatusCommand((byte) 0x01, bleBean.getPwd1(), bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());*/
            Timber.d("发送了请求密钥列表指令");
        }, 100);
    }

    private final ArrayList<Byte> mWillSearchList = new ArrayList<>();
    //private final ArrayList<DevicePwdBean> mDevicePwdBeanCopyList = new ArrayList<>();//蓝牙模式 加载数据之前的缓存列表
    private final ArrayList<DevicePwdBean> mDevicePwdBeanFormBle = new ArrayList<>();

    private void getPwdListFormBle(BleResultBean bean) {
        dismissLoading();
        // TODO: 2021/2/3 可能存在100条数据以上，后续需要做100条数据以上的测试
        // TODO: 2021/2/4 后续需要做去重操作
        if (bean.getCMD() == CMD_SY_KEY_STATE) {
            checkPwdIsExist(bean);
            runOnUiThread(() -> {
                if (mRefreshLayout != null) {
                    mRefreshLayout.finishRefresh();
                }
            });
        } else if (bean.getCMD() == CMD_KEY_ATTRIBUTES_READ) {
            byte attribute = bean.getPayload()[0];
            if (attribute == KEY_SET_ATTRIBUTE_ALWAYS) {
                addPermanentPwd();
            } else if (attribute == KEY_SET_ATTRIBUTE_TIME_KEY) {
                // TODO: 2021/2/7 时间高低位反回来取
                addTimePwd(bean);
            } else if (attribute == KEY_SET_ATTRIBUTE_WEEK_KEY) {
                // TODO: 2021/2/7 时间高低位反回来取
                addWeeklyPwd(bean);
            }
            runOnUiThread(() -> mPasswordListAdapter.setList(mDevicePwdBeanFormBle));
            mHandler.postDelayed(mSearchPwdListRunnable, 20);
        } else if (bean.getCMD() == CMD_KEY_ATTRIBUTES_SET) {
            // mHandler.sendEmptyMessageDelayed(MSG_UPDATE_PWD_LIST, 1000);
            Timber.e("操作成功");
            new Handler(Looper.getMainLooper()).postDelayed(this::checkHadPwdFromBle, 20);
        }
    }

    private void addPermanentPwd() {
        Timber.d("addPermanentPwd num: %1s", mCurrentSearchNum);
        DevicePwdBean devicePwdBean = new DevicePwdBean();
        devicePwdBean.setPwdNum(mCurrentSearchNum);
        // 使用秒存储，所以除以1000
        // TODO: 2021/2/24 后续需要改掉，存在问题，不可能使用这个创建时间
        devicePwdBean.setCreateTime(ZoneUtil.getTime() / 1000);
        devicePwdBean.setDeviceId(mBleDeviceLocal.getId());
        devicePwdBean.setAttribute(BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS);
        devicePwdBean.setPwdName("" + mCurrentSearchNum);
        mDevicePwdBeanFormBle.add(devicePwdBean);
    }

    private void addTimePwd(BleResultBean bean) {
        byte[] startTimeBytes = new byte[4];
        byte[] endTimeBytes = new byte[4];
        System.arraycopy(bean.getPayload(), 2, startTimeBytes, 0, startTimeBytes.length);
        System.arraycopy(bean.getPayload(), 6, endTimeBytes, 0, endTimeBytes.length);
        long startTimeMill = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(startTimeBytes));
        long endTimeMill = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(endTimeBytes));
        DevicePwdBean devicePwdBean = new DevicePwdBean();
        devicePwdBean.setDeviceId(mBleDeviceLocal.getId());
        devicePwdBean.setPwdName("" + mCurrentSearchNum);
        devicePwdBean.setPwdNum(mCurrentSearchNum);
        devicePwdBean.setAttribute(KEY_SET_ATTRIBUTE_TIME_KEY);
        devicePwdBean.setStartTime(startTimeMill);
        devicePwdBean.setEndTime(endTimeMill);
        mDevicePwdBeanFormBle.add(devicePwdBean);
    }

    private void addWeeklyPwd(BleResultBean bean) {
        byte[] weekBytes = BleByteUtil.byteToBit(bean.getPayload()[1]);
        Timber.d("addWeeklyPwd num: %1s week: %1s", mCurrentSearchNum, ConvertUtils.bytes2HexString(weekBytes));
        byte[] startTimeBytes = new byte[4];
        byte[] endTimeBytes = new byte[4];
        System.arraycopy(bean.getPayload(), 2, startTimeBytes, 0, startTimeBytes.length);
        System.arraycopy(bean.getPayload(), 6, endTimeBytes, 0, endTimeBytes.length);
        long startTimeMill = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(startTimeBytes));
        long endTimeMill = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(endTimeBytes));
        DevicePwdBean devicePwdBean = new DevicePwdBean();
        devicePwdBean.setDeviceId(mBleDeviceLocal.getId());
        devicePwdBean.setPwdNum(mCurrentSearchNum);
        devicePwdBean.setPwdName("" + mCurrentSearchNum);
        devicePwdBean.setWeekly(bean.getPayload()[1]);
        devicePwdBean.setStartTime(startTimeMill);
        devicePwdBean.setEndTime(endTimeMill);
        devicePwdBean.setAttribute(KEY_SET_ATTRIBUTE_WEEK_KEY);
        mDevicePwdBeanFormBle.add(devicePwdBean);
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
        Timber.d("7-0: %1s, 15-8: %2s, 19-16: %3s",
                ConvertUtils.bytes2HexString(num1), ConvertUtils.bytes2HexString(num2), ConvertUtils.bytes2HexString(num3));
        // 循环判断20个内有哪些编号是存在密码的
        for (int i = 7; i >= 0; i--) {
            if (num1[i] == 0x01) {
                mWillSearchList.add((byte) (7 - i));
            }
        }
        for (int i = 7; i >= 0; i--) {
            if (num2[i] == 0x01) {
                mWillSearchList.add((byte) (15 - i));
            }
        }
        for (int i = 7; i >= 4; i--) {
            if (num3[i] == 0x01) {
                mWillSearchList.add((byte) (23 - i));
            }
        }

        if (!mWillSearchList.isEmpty()) {
            StringBuilder logStr = new StringBuilder();
            for (byte b : mWillSearchList) {
                logStr.append(", ").append(BleByteUtil.byteToInt(b));
            }
            Timber.d("存在的密钥编号：%1s", logStr.toString());
        }
       /* searchPwdListFromNET();
        // TODO: 2021/4/20 后续再校对数据是否存在遗漏或者重合
        // 查询到密钥存在后，开始读取对应密钥
//        mHandler.postDelayed(mSearchPwdListRunnable, 20);*/
        if ((null != mBleDeviceLocal && mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_BLE) && !getNetError()) {
            mHandler.postDelayed(mSearchPwdListRunnable, 20);
        } else {
            searchPwdListFromNET();
        }
    }

    private static final int MSG_UPDATE_PWD_LIST = 854;
    //private static final int MSG_BLE_GET_PWD_LIST_TIME = 896;//蓝牙模式 获取列表对比
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_UPDATE_PWD_LIST) {
                mWillSearchList.clear();
                mPasswordListAdapter.notifyDataSetChanged();
                searchPwdListFromNET();
            } //else if (msg.what == MSG_BLE_GET_PWD_LIST_TIME) {
          /*      List<DevicePwdBean> devicePwds = null;
                if (null != mPasswordListAdapter) {
                    devicePwds = mPasswordListAdapter.getData();
                }
                if (null != devicePwds) {
                    Timber.e("list len:" + devicePwds.size());
                }
                if (null != mDevicePwdBeanCopyList) {
                    Timber.e("copy list len:" + mDevicePwdBeanCopyList.size());
                }
                if (null == devicePwds && null != mDevicePwdBeanCopyList) {
                    if (mDevicePwdBeanCopyList.size() > 0) {
                        deletePwdToService(mDevicePwdBeanCopyList);
                    }
                } else {
                    if (null != devicePwds && null != mDevicePwdBeanCopyList) {
                        if (mDevicePwdBeanCopyList.size() > devicePwds.size()) {
                            Timber.e("当前本地密码个数大于锁端实际密码");
                            List<DevicePwdBean> deleteDevics = new ArrayList<>();
                            for (int b = 0; b < mDevicePwdBeanCopyList.size(); b++) {
                                boolean isDele = true;
                                for (int c = 0; c < devicePwds.size(); c++) {
                                    if (devicePwds.get(c).getPwdNum() == mDevicePwdBeanCopyList.get(b).getPwdNum()) {
                                        isDele = false;
                                    }
                                }
                                Timber.e("是否被删除：" + isDele);
                                if (isDele) {
                                    deleteDevics.add(mDevicePwdBeanCopyList.get(b));
                                }
                            }
                            deletePwdToService(deleteDevics);
                        }
                    }
                }*/
            //}
        }
    };

    /**
     * 蓝牙模式下同步密码
     *
     * @param pwdList
     */
    private void deletePwdToService(List<Integer> pwdList) {
        Timber.e("蓝牙模式下将多余的密码删除于服务器");
        if (null == pwdList || pwdList.size() == 0) {
            return;
        }
        List<DelKeyBeanReq.PwdListBean> listBeans = new ArrayList<>();
        for (int num : pwdList) {
            DelKeyBeanReq.PwdListBean pwdListBean = new DelKeyBeanReq.PwdListBean();
            pwdListBean.setNum(num);
            pwdListBean.setPwdType(1);
            Timber.e("蓝牙模式下将多余的密码删除于服务器:" + pwdListBean.toString());
            listBeans.add(pwdListBean);
        }
        mWillDelPwd.clear();
        // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除，下面所有
        if (mBleDeviceLocal == null) {
            Timber.e("delKeyFromService bleDeviceLocal == null");
            return;
        }
        String esn = mBleDeviceLocal.getEsn();
        if (TextUtils.isEmpty(esn)) {
            Timber.e("delKeyFromService esn is Empty");
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            Timber.e("delKeyFromService App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            Timber.e("delKeyFromService uid is Empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("delKeyFromService token is Empty");
            return;
        }
        DelKeyBeanReq req = new DelKeyBeanReq();
        req.setPwdList(listBeans);
        req.setSn(esn);
        req.setUid(uid);
        Observable<DelKeyBeanRsp> observable = HttpRequest.getInstance().delKey(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<DelKeyBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull DelKeyBeanRsp delKeyBeanRsp) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private final Runnable mSearchPwdListRunnable = this::searchPwdList;
    private byte mCurrentSearchNum;

    private void searchPwdList() {
        if (mWillSearchList.isEmpty()) {
            Timber.d("searchPwdList 要搜索的密码列表是空");
            return;
        }
        mCurrentSearchNum = mWillSearchList.get(0);
        BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
        if (bleBean == null) {
            Timber.e("checkHadPwdFromBle bleBean == null");
            return;
        }
        if (bleBean.getOKBLEDeviceImp() == null) {
            Timber.e("checkHadPwdFromBle bleBean.getOKBLEDeviceImp() == null");
            return;
        }
        if (bleBean.getPwd1() == null) {
            Timber.e("checkHadPwdFromBle bleBean.getPwd1() == null");
            return;
        }
        if (bleBean.getPwd3() == null) {
            Timber.e("checkHadPwdFromBle bleBean.getPwd3() == null");
            return;
        }
        LockMessage message = new LockMessage();
        message.setBytes(BleCommandFactory
                .keyAttributesRead(KEY_SET_KEY_TYPE_PWD, mCurrentSearchNum, bleBean.getPwd1(),
                        bleBean.getPwd3()));
        message.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
        message.setMessageType(3);
        EventBus.getDefault().post(message);
      /*  App.getInstance().writeControlMsg(BleCommandFactory
                .keyAttributesRead(KEY_SET_KEY_TYPE_PWD, mCurrentSearchNum, bleBean.getPwd1(),
                        bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());*/
        mWillSearchList.remove(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // mHandler.removeMessages(MSG_BLE_GET_PWD_LIST_TIME);
        mPasswordFull = null;
    }

    private void delPwd(DevicePwdBean devicePwdBean) {
        showLoading();
        if (mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI || mBleDeviceLocal.getConnectedType() == LocalState.DEVICE_CONNECT_TYPE_WIFI_BLE) {
            publishDelPwd(mBleDeviceLocal.getEsn(), devicePwdBean);
        } else {
            BleBean bleBean = App.getInstance().getUserBleBean(mBleDeviceLocal.getMac());
            if (bleBean == null || bleBean.getOKBLEDeviceImp() == null || bleBean.getPwd1() == null || bleBean.getPwd3() == null) {
                Timber.e("delPwd bleBean == null");
                ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show("Delete failed, Bluetooth connection failed");
                return;
            }
            LockMessage lockMessage = new LockMessage();
            lockMessage.setMessageType(3);
            lockMessage.setBytes(BleCommandFactory
                    .keyAttributesSet(KEY_SET_KEY_OPTION_DEL,
                            KEY_SET_KEY_TYPE_PWD,
                            (byte) devicePwdBean.getPwdNum(),
                            KEY_SET_ATTRIBUTE_WEEK_KEY,
                            (byte) 0x00,
                            (byte) 0x00,
                            (byte) 0x00,
                            bleBean.getPwd1(),
                            bleBean.getPwd3()));
            lockMessage.setMac(bleBean.getOKBLEDeviceImp().getMacAddress());
            EventBus.getDefault().post(lockMessage);
           /* App.getInstance().writeControlMsg(BleCommandFactory
                    .keyAttributesSet(KEY_SET_KEY_OPTION_DEL,
                            KEY_SET_KEY_TYPE_PWD,
                            (byte) devicePwdBean.getPwdNum(),
                            KEY_SET_ATTRIBUTE_WEEK_KEY,
                            (byte) 0x00,
                            (byte) 0x00,
                            (byte) 0x00,
                            bleBean.getPwd1(),
                            bleBean.getPwd3()), bleBean.getOKBLEDeviceImp());*/
        }
    }

    private void publishDelPwd(String wifiId, DevicePwdBean devicePwdBean) {
        this.devicePwdBean = devicePwdBean;
        LockMessage message = new LockMessage();
        message.setMqtt_message_code(MQttConstant.REMOVE_PWD);
        message.setMqttMessage(MqttCommandFactory.removePwd(
                wifiId,
                0,
                devicePwdBean.getPwdNum(),
                BleCommandFactory.getPwd(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()), ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))));
        message.setMqtt_topic(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()));
        message.setMessageType(2);
        EventBus.getDefault().post(message);


    /*    if (mMQttService == null) {
            Timber.e("publishDelPwd mMQttService == null");
            return;
        }
        toDisposable(mDelPwdDisposable);
        mDelPwdDisposable = mMQttService
                .mqttPublish(MQttConstant.getCallTopic(App.getInstance().getUserBean().getUid()),
                        MqttCommandFactory.removePwd(
                                wifiId,
                                0,
                                devicePwdBean.getPwdNum(),
                                BleCommandFactory.getPwd(ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd1()), ConvertUtils.hexString2Bytes(mBleDeviceLocal.getPwd2()))))
                .timeout(DEFAULT_TIMEOUT_SEC_VALUE, TimeUnit.SECONDS)
                .filter(mqttData -> mqttData.getFunc().equals(MQttConstant.REMOVE_PWD)).subscribe(mqttData -> processDelPwd(mqttData, devicePwdBean), e -> {
                    dismissLoading();
                    Timber.e(e);
                });
        mCompositeDisposable.add(mDelPwdDisposable);*/
    }

    private void processDelPwd(WifiLockRemovePasswordResponseBean bean) {
        /*toDisposable(mDelPwdDisposable);
        if (TextUtils.isEmpty(mqttData.getFunc())) {
            return;
        }*/
        // if (mqttData.getFunc().equals(MQttConstant.REMOVE_PWD)) {
        dismissLoading();
       /* Timber.d("删除密码信息: %1s", mqttData);
        WifiLockRemovePasswordResponseBean bean;
        try {
            bean = GsonUtils.fromJson(mqttData.getPayload(), WifiLockRemovePasswordResponseBean.class);
        } catch (JsonSyntaxException e) {
            Timber.e(e);
            return;
        }*/
        if (bean == null) {
            Timber.e("publishDelPwd bean == null");
            return;
        }
        if (bean.getParams() == null) {
            Timber.e("publishDelPwd bean.getParams() == null");
            return;
        }
        if (bean.getCode() != 200) {
            Timber.e("publishDelPwd code : %1d", bean.getCode());
            return;
        }
        delKeyFromService(this.devicePwdBean);
        //}
        // Timber.d("publishDelPwd %1s", mqttData.toString());
    }

    private void delKeyFromService(DevicePwdBean devicePwdBean) {
        if (!checkNetConnectFail()) {
            return;
        }
        List<DelKeyBeanReq.PwdListBean> listBeans = new ArrayList<>();
        DelKeyBeanReq.PwdListBean pwdListBean = new DelKeyBeanReq.PwdListBean();
        pwdListBean.setNum(devicePwdBean.getPwdNum());
        pwdListBean.setPwdType(1);
        listBeans.add(pwdListBean);
        // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除，下面所有
        BleDeviceLocal bleDeviceLocal = AppDatabase.getInstance(this).bleDeviceDao().findBleDeviceFromId(devicePwdBean.getDeviceId());
        if (bleDeviceLocal == null) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService bleDeviceLocal == null");
            return;
        }
        String esn = bleDeviceLocal.getEsn();
        if (TextUtils.isEmpty(esn)) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService esn is Empty");
            return;
        }
        if (App.getInstance().getUserBean() == null) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService App.getInstance().getUserBean() == null");
            return;
        }
        String uid = App.getInstance().getUserBean().getUid();
        if (TextUtils.isEmpty(uid)) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService uid is Empty");
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            dismissLoadingAndShowFailMessage();
            Timber.e("delKeyFromService token is Empty");
            return;
        }
        DelKeyBeanReq req = new DelKeyBeanReq();
        req.setPwdList(listBeans);
        req.setSn(esn);
        req.setUid(uid);
        Observable<DelKeyBeanRsp> observable = HttpRequest.getInstance().delKey(token, req);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<DelKeyBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull DelKeyBeanRsp delKeyBeanRsp) {
                String code = delKeyBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除
                    dismissLoadingAndShowFailMessage();
                    Timber.e("delKeyFromService delKeyBeanRsp.getCode() is Empty");
                    return;
                }
                if (!code.equals("200")) {
                    // TODO: 2021/2/24 服务器删除失败，需要检查如何通过服务器再删除
                    if (code.equals("444")) {
                        dismissLoading();
                        App.getInstance().logout(true, PasswordListActivity.this);
                        return;
                    }
                    dismissLoadingAndShowFailMessage();
                    String msg = delKeyBeanRsp.getMsg();
                    Timber.e("delKeyFromService code: %1s msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(msg);
                    }
                    return;
                }
                searchPwdListFromNET();
                dismissLoadingAndShowSucMessage();
            }

            @Override
            public void onError(@NonNull Throwable e) {
                dismissLoadingAndShowFailMessage();
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void dismissLoadingAndShowFailMessage() {
        dismissLoading();
        showFailMessage();
    }

    private void dismissLoadingAndShowSucMessage() {
        dismissLoading();
        showSucMessage();
    }

    private MessageDialog mFailMessageDialog;
    private MessageDialog mSucMessageDialog;

    private void initSucMessageDialog() {
        mSucMessageDialog = new MessageDialog(PasswordListActivity.this);
        mSucMessageDialog.setMessage(getString(R.string.dialog_tip_password_deleted));
        mSucMessageDialog.setOnListener(v -> {
            if (mSucMessageDialog != null) {
                mSucMessageDialog.dismiss();
            }
        });
    }

    private void showSucMessage() {
        runOnUiThread(() -> {
            if (mSucMessageDialog != null) {
                mSucMessageDialog.show();
            }
        });
    }

    private void initFailMessageDialog() {
        mFailMessageDialog = new MessageDialog(this);
        mFailMessageDialog.setMessage(getString(R.string.dialog_tip_deletion_failed_door_lock_bluetooth_is_not_found));
        mFailMessageDialog.setOnListener(v -> {
            if (mFailMessageDialog != null) {
                mFailMessageDialog.dismiss();
            }
        });
    }

    private void showFailMessage() {
        runOnUiThread(() -> {
            if (mFailMessageDialog != null) {
                mFailMessageDialog.show();
            }
        });
    }
}
