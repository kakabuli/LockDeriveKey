package com.revolo.lock.ui.device.lock;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.adapter.AutoMeasureLinearLayoutManager;
import com.revolo.lock.adapter.OperationRecordsAdapter;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.bean.BleBean;
import com.revolo.lock.bean.test.TestOperationRecords;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.ble.BleProtocolState;
import com.revolo.lock.ble.BleResultProcess;
import com.revolo.lock.ble.OnBleDeviceListener;
import com.revolo.lock.ble.bean.BleResultBean;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_GET_ALL_RECORD;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 操作记录
 */
public class OperationRecordsActivity extends BaseActivity {

    private OperationRecordsAdapter mRecordsAdapter;
    private BleBean mBleBean;

    @Override
    public void initData(@Nullable Bundle bundle) {
        mBleBean = App.getInstance().getBleBean();
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_operation_records;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.title_operation_records))
                .setRight(ContextCompat.getDrawable(this, R.drawable.ic_icon_date), v -> {
                    // TODO: 2021/1/13 打开日历筛选
                });
        RecyclerView rvRecords = findViewById(R.id.rvOperationRecords);
        AutoMeasureLinearLayoutManager linearLayoutManager = new AutoMeasureLinearLayoutManager(this);
        rvRecords.setLayoutManager(linearLayoutManager);
        mRecordsAdapter = new OperationRecordsAdapter(R.layout.item_operation_record_list_rv);
        rvRecords.setAdapter(mRecordsAdapter);
    }

    @Override
    public void doBusiness() {
        initDevice();
        initDataFromLock();
//        initData();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        if(bleResultBean.getCMD() == CMD_GET_ALL_RECORD) {
            refreshRecordFormBle(bleResultBean);
        }
//        if(bleResultBean.getCMD() == BleProtocolState.CMD_LOCK_OP_RECORD) {
//            refreshDataFromBle(bleResultBean);
//            @BleProtocolState.LockRecordOpEventType int event = bleResultBean.getPayload()[5];
//            switch (event) {
//                case BleProtocolState.LOCK_RECORD_OP_EVENT_TYPE_ALARM:
//                    processAlarmRecord(bleResultBean);
//                    break;
//                case BleProtocolState.LOCK_RECORD_OP_EVENT_TYPE_OP:
//                    processOpRecord(bleResultBean);
//                    break;
//                case BleProtocolState.LOCK_RECORD_OP_EVENT_TYPE_PROGRAM:
//                    processProgramRecord(bleResultBean);
//                    break;
//                default:
//                    // TODO: 2021/1/27 类型错误，其实可以什么都不处理
//                    break;
//            }
//        }
    };

    private void processAlarmRecord(BleResultBean bean) {

    }

    private void processProgramRecord(BleResultBean bean) {

    }

    private void processOpRecord(BleResultBean bean) {

    }

    private void initDevice() {
        if (mBleBean.getOKBLEDeviceImp() != null) {
            App.getInstance().openPairNotify();
            App.getInstance().setOnBleDeviceListener(mOnBleDeviceListener);
        }
    }

    private void initDataFromLock() {
        if(mBleBean.getOKBLEDeviceImp() != null) {
            if (mBleBean.getOKBLEDeviceImp().isConnected()) {
                // 查询100条记录
                byte[] start = new byte[2];
                byte[] end = new byte[2];
                end[0] = 0x64;
                App.getInstance().writeControlMsg(BleCommandFactory
                        .readAllRecord(start, end, mBleBean.getPwd1(), mBleBean.getPwd3()));
            } else {
                // TODO: 2021/1/26 没有连接上，需要连接上才能发送指令
            }
        }
    }

    private void refreshRecordFormBle(BleResultBean bean) {
        byte[] total = new byte[2];
        System.arraycopy(bean.getPayload(), 0, total, 0, total.length);
        short totalShort = BleByteUtil.bytesToShort(total);
        byte[] index = new byte[2];
        System.arraycopy(bean.getPayload(), 2, index, 0, index.length);
        short indexShort = BleByteUtil.bytesToShort(index);
        int eventType = bean.getPayload()[4];
        int eventSource = bean.getPayload()[5];
        int eventCode = bean.getPayload()[6];
        int userId = bean.getPayload()[7];
        int appId = bean.getPayload()[8];
        byte[] time = new byte[4];
        System.arraycopy(bean.getPayload(), 9, time, 0, time.length);
        long realTime = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(time)) * 1000;
        Timber.d("total: %1d, index: %2d, eventType: %3d, eventSource: %4d, eventCode: %5d, userId: %6d, appId: %7d, time: %8d",
                totalShort, indexShort, eventType, eventSource, eventCode, userId, appId, realTime);

        if(eventType == 0x01) {
            // 开锁记录
            if(userId == 100) {
                // 机械方式开锁

            } else if(userId == 102) {
                // 一键开锁
            } else if(userId == 106) {
                // 感应把手开锁
            } else if(userId == 103) {
                // APP指令开锁
            } else if(userId == 254) {
                // 管理员用户开锁
            } else if(userId == 253) {
                // 访客密码开锁
            } else if(userId == 252) {
                // 一次性密码开锁
            } else if(userId == 250) {
                // 离线密码开锁
            } else {
                // TODO: 2021/2/10 标准密码，指纹编号，卡片编号
            }
        } else if(eventType == 0x02) {
            // Program程序
        } else if(eventType == 0x03) {
            // Alarm 报警记录
        }
        
        if(eventType == 0x01) {
            // Operation操作(动作类)（开锁记录）

            if(eventSource == 0x00) {
                // Keypad键盘（密码）

                if(eventCode == 0x01) {
                    // lock上锁
                    Timber.d("记录：Keypad键盘 lock上锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // Unlock开锁
                    Timber.d("记录：Keypad键盘 Unlock开锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 如果出错，输出对应日志
                }


            } else if(eventSource ==  0x03) {
                // RFID卡片

                if(eventCode == 0x01) {
                    // lock上锁
                    Timber.d("记录：RFID卡片 lock上锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // Unlock开锁
                    Timber.d("记录：RFID卡片 Unlock开锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 如果出错，输出对应日志
                }

            } else if(eventSource == 0x04) {
                // Fingerprint指纹

                if(eventCode == 0x01) {
                    // lock上锁
                    Timber.d("记录：Fingerprint指纹 lock上锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // Unlock开锁
                    Timber.d("记录：Fingerprint指纹 Unlock开锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 如果出错，输出对应日志
                }

            } else if(eventSource == 0x08) {
                // App

                if(eventCode == 0x01) {
                    // lock上锁
                    Timber.d("记录：App lock上锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // Unlock开锁
                    Timber.d("记录：App Unlock开锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 如果出错，输出对应日志
                }

            } else if(eventSource == 0x09) {
                // Key Unlock机械钥匙（室内机械方式开锁、室外机械钥匙开锁）

                if(eventCode == 0x01) {
                    // lock上锁
                    Timber.d("记录：Key lock上锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // Unlock开锁
                    Timber.d("记录：Key Unlock开锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 如果出错，输出对应日志
                }

            } else if(eventSource == 0x0A) {
                // 室内open键开锁


                if(eventCode == 0x01) {
                    // lock上锁
                    Timber.d("记录：室内open键开锁 lock上锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // Unlock开锁
                    Timber.d("记录：室内open键开锁 Unlock开锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 如果出错，输出对应日志
                }

            } else if(eventSource == 0x0B) {
                // 室内感应把手开锁


                if(eventCode == 0x01) {
                    // lock上锁
                    Timber.d("记录：室内感应把手开锁 lock上锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // Unlock开锁
                    Timber.d("记录：室内感应把手开锁 Unlock开锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 如果出错，输出对应日志
                }

            } else if(eventSource == -1) {
                // 不确定（无效值）


                if(eventCode == 0x01) {
                    // lock上锁
                    Timber.d("记录：不确定 lock上锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // Unlock开锁
                    Timber.d("记录：不确定 Unlock开锁 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 如果出错，输出对应日志
                }


            } else {
                // TODO: 2021/2/9 其他错误，打印日志
            }



        } else if(eventType == 0x02) {
            // Program程序(用户管理类)（增删改记录）



            if(eventSource == 0x00) {
                // Keypad键盘（密码）

                if(eventCode == 0x01) {
                    // 修改
                    Timber.d("记录：Keypad键盘 修改密码 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 添加
                    Timber.d("记录：Keypad键盘 添加密码 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 删除
                    Timber.d("记录：Keypad键盘 删除密码 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x0F) {
                    // 恢复出厂设置
                    Timber.d("记录：Keypad键盘 恢复出厂设置 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 错误操作日志
                }

            } else if(eventSource ==  0x03) {
                // RFID卡片


                if(eventCode == 0x01) {
                    // 修改
                    Timber.d("记录：RFID卡片 修改 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 添加
                    Timber.d("记录：RFID卡片 添加 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 删除
                    Timber.d("记录：RFID卡片 删除 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x0F) {
                    // 恢复出厂设置
                    Timber.d("记录：RFID卡片 恢复出厂设置 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 错误操作日志
                }

            } else if(eventSource == 0x04) {
                // Fingerprint指纹

                if(eventCode == 0x01) {
                    // 修改
                    Timber.d("记录：Fingerprint指纹 修改 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 添加
                    Timber.d("记录：Fingerprint指纹 添加 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 删除
                    Timber.d("记录：Fingerprint指纹 删除 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x0F) {
                    // 恢复出厂设置
                    Timber.d("记录：Fingerprint指纹 恢复出厂设置 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 错误操作日志
                }


            } else if(eventSource == 0x08) {
                // App


                if(eventCode == 0x01) {
                    // 修改
                    Timber.d("记录：App 修改 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 添加
                    Timber.d("记录：App 添加 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 删除
                    Timber.d("记录：App 删除 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x0F) {
                    // 恢复出厂设置
                    Timber.d("记录：App 恢复出厂设置 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 错误操作日志
                }

            } else if(eventSource == 0x09) {
                // Key Unlock机械钥匙（室内机械方式开锁、室外机械钥匙开锁）


                if(eventCode == 0x01) {
                    // 修改
                    Timber.d("记录：Key 修改 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 添加
                    Timber.d("记录：Key 添加 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 删除
                    Timber.d("记录：Key 删除 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x0F) {
                    // 恢复出厂设置
                    Timber.d("记录：Key 恢复出厂设置 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 错误操作日志
                }

            } else if(eventSource == 0x0A) {
                // 室内open键开锁

                if(eventCode == 0x01) {
                    // 修改
                    Timber.d("记录：室内open键开锁 修改 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 添加
                    Timber.d("记录：室内open键开锁 添加 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 删除
                    Timber.d("记录：室内open键开锁 删除 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x0F) {
                    // 恢复出厂设置
                    Timber.d("记录：室内open键开锁 恢复出厂设置 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 错误操作日志
                }


            } else if(eventSource == 0x0B) {
                // 室内感应把手开锁


                if(eventCode == 0x01) {
                    // 修改
                    Timber.d("记录：室内感应把手开锁 修改 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 添加
                    Timber.d("记录：室内感应把手开锁 添加 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 删除
                    Timber.d("记录：室内感应把手开锁 删除 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x0F) {
                    // 恢复出厂设置
                    Timber.d("记录：室内感应把手开锁 恢复出厂设置 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 错误操作日志
                }

            }  else if(eventSource == -1) {
                // 不确定（无效值）


                if(eventCode == 0x01) {
                    // 修改
                    Timber.d("记录：不确定 修改密码 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 添加
                    Timber.d("记录：不确定 添加密码 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 删除
                    Timber.d("记录：不确定 删除密码 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x0F) {
                    // 恢复出厂设置
                    Timber.d("记录：不确定 恢复出厂设置 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {
                    // TODO: 2021/2/9 错误操作日志
                }

            }  else {
                // TODO: 2021/2/9 其他错误，打印日志
            }

        } else if(eventType == 0x03) {
            // Alarm 报警记录


            if(eventSource == 0x00) {
                // Keypad键盘（密码）

                if(eventCode == 0x01) {
                    // 锁定报警（输入错误密码或指纹或卡片超过5次就会系统锁定报警）
                    Timber.d("记录：Keypad键盘 锁定报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 劫持报警（输入防劫持密码或防劫持指纹开锁就报警）
                    Timber.d("记录：Keypad键盘 劫持报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 三次错误，上报提醒
                    Timber.d("记录：Keypad键盘 三次错误，上报提醒 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x04) {
                    // 撬锁报警（锁被撬开）
                    Timber.d("记录：Keypad键盘 撬锁报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x08) {
                    // 机械钥匙报警（使用机械钥匙开锁）
                    Timber.d("记录：Keypad键盘 机械钥匙报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x10) {
                    // 低电压报警（电池电量不足）
                    Timber.d("记录：Keypad键盘 低电压报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x20) {
                    // 门锁异常报警
                    Timber.d("记录：Keypad键盘 门锁异常报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x40) {
                    // 门锁布防报警（门外布防后，从门内开锁了就会报警）
                    Timber.d("记录：Keypad键盘 门锁布防报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {

                }


            } else if(eventSource ==  0x03) {
                // RFID卡片


                if(eventCode == 0x01) {
                    // 锁定报警（输入错误密码或指纹或卡片超过5次就会系统锁定报警）
                    Timber.d("记录：RFID卡片 锁定报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 劫持报警（输入防劫持密码或防劫持指纹开锁就报警）
                    Timber.d("记录：RFID卡片 劫持报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 三次错误，上报提醒
                    Timber.d("记录：RFID卡片 三次错误，上报提醒 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x04) {
                    // 撬锁报警（锁被撬开）
                    Timber.d("记录：RFID卡片 撬锁报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x08) {
                    // 机械钥匙报警（使用机械钥匙开锁）
                    Timber.d("记录：RFID卡片 机械钥匙报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x10) {
                    // 低电压报警（电池电量不足）
                    Timber.d("记录：RFID卡片 低电压报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x20) {
                    // 门锁异常报警
                    Timber.d("记录：RFID卡片 门锁异常报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x40) {
                    // 门锁布防报警（门外布防后，从门内开锁了就会报警）
                    Timber.d("记录：RFID卡片 门锁布防报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {

                }

            } else if(eventSource == 0x04) {
                // Fingerprint指纹


                if(eventCode == 0x01) {
                    // 锁定报警（输入错误密码或指纹或卡片超过5次就会系统锁定报警）
                    Timber.d("记录：Fingerprint指纹 锁定报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 劫持报警（输入防劫持密码或防劫持指纹开锁就报警）
                    Timber.d("记录：Fingerprint指纹 劫持报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 三次错误，上报提醒
                    Timber.d("记录：Fingerprint指纹 三次错误，上报提醒 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x04) {
                    // 撬锁报警（锁被撬开）
                    Timber.d("记录：Fingerprint指纹 撬锁报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x08) {
                    // 机械钥匙报警（使用机械钥匙开锁）
                    Timber.d("记录：Fingerprint指纹 机械钥匙报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x10) {
                    // 低电压报警（电池电量不足）
                    Timber.d("记录：Fingerprint指纹 低电压报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x20) {
                    // 门锁异常报警
                    Timber.d("记录：Fingerprint指纹 门锁异常报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x40) {
                    // 门锁布防报警（门外布防后，从门内开锁了就会报警）
                    Timber.d("记录：Fingerprint指纹 门锁布防报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {

                }

            } else if(eventSource == 0x08) {
                // App


                if(eventCode == 0x01) {
                    // 锁定报警（输入错误密码或指纹或卡片超过5次就会系统锁定报警）
                    Timber.d("记录：App 锁定报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 劫持报警（输入防劫持密码或防劫持指纹开锁就报警）
                    Timber.d("记录：App 劫持报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 三次错误，上报提醒
                    Timber.d("记录：App 三次错误，上报提醒 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x04) {
                    // 撬锁报警（锁被撬开）
                    Timber.d("记录：App 撬锁报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x08) {
                    // 机械钥匙报警（使用机械钥匙开锁）
                    Timber.d("记录：App 机械钥匙报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x10) {
                    // 低电压报警（电池电量不足）
                    Timber.d("记录：App 低电压报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x20) {
                    // 门锁异常报警
                    Timber.d("记录：App 门锁异常报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x40) {
                    // 门锁布防报警（门外布防后，从门内开锁了就会报警）
                    Timber.d("记录：App 门锁布防报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {

                }

            } else if(eventSource == 0x09) {
                // Key Unlock机械钥匙（室内机械方式开锁、室外机械钥匙开锁）


                if(eventCode == 0x01) {
                    // 锁定报警（输入错误密码或指纹或卡片超过5次就会系统锁定报警）
                    Timber.d("记录：Key 锁定报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 劫持报警（输入防劫持密码或防劫持指纹开锁就报警）
                    Timber.d("记录：Key 劫持报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 三次错误，上报提醒
                    Timber.d("记录：Key 三次错误，上报提醒 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x04) {
                    // 撬锁报警（锁被撬开）
                    Timber.d("记录：Key 撬锁报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x08) {
                    // 机械钥匙报警（使用机械钥匙开锁）
                    Timber.d("记录：Key 机械钥匙报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x10) {
                    // 低电压报警（电池电量不足）
                    Timber.d("记录：Key 低电压报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x20) {
                    // 门锁异常报警
                    Timber.d("记录：Key 门锁异常报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x40) {
                    // 门锁布防报警（门外布防后，从门内开锁了就会报警）
                    Timber.d("记录：Key 门锁布防报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {

                }

            } else if(eventSource == 0x0A) {
                // 室内open键开锁


                if(eventCode == 0x01) {
                    // 锁定报警（输入错误密码或指纹或卡片超过5次就会系统锁定报警）
                    Timber.d("记录：室内open键开锁 锁定报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 劫持报警（输入防劫持密码或防劫持指纹开锁就报警）
                    Timber.d("记录：室内open键开锁 劫持报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 三次错误，上报提醒
                    Timber.d("记录：室内open键开锁 三次错误，上报提醒 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x04) {
                    // 撬锁报警（锁被撬开）
                    Timber.d("记录：室内open键开锁 撬锁报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x08) {
                    // 机械钥匙报警（使用机械钥匙开锁）
                    Timber.d("记录：室内open键开锁 机械钥匙报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x10) {
                    // 低电压报警（电池电量不足）
                    Timber.d("记录：室内open键开锁 低电压报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x20) {
                    // 门锁异常报警
                    Timber.d("记录：室内open键开锁 门锁异常报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x40) {
                    // 门锁布防报警（门外布防后，从门内开锁了就会报警）
                    Timber.d("记录：室内open键开锁 门锁布防报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {

                }

            } else if(eventSource == 0x0B) {
                // 室内感应把手开锁


                if(eventCode == 0x01) {
                    // 锁定报警（输入错误密码或指纹或卡片超过5次就会系统锁定报警）
                    Timber.d("记录：室内感应把手开锁 锁定报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x02) {
                    // 劫持报警（输入防劫持密码或防劫持指纹开锁就报警）
                    Timber.d("记录：室内感应把手开锁 劫持报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x03) {
                    // 三次错误，上报提醒
                    Timber.d("记录：室内感应把手开锁 三次错误，上报提醒 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x04) {
                    // 撬锁报警（锁被撬开）
                    Timber.d("记录：室内感应把手开锁 撬锁报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x08) {
                    // 机械钥匙报警（使用机械钥匙开锁）
                    Timber.d("记录：室内感应把手开锁 机械钥匙报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x10) {
                    // 低电压报警（电池电量不足）
                    Timber.d("记录：室内感应把手开锁 低电压报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x20) {
                    // 门锁异常报警
                    Timber.d("记录：室内感应把手开锁 门锁异常报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else if(eventCode == 0x40) {
                    // 门锁布防报警（门外布防后，从门内开锁了就会报警）
                    Timber.d("记录：室内感应把手开锁 门锁布防报警 userId: %1d, AppId: %2d, 时间：%3s", userId, appId, TimeUtils.millis2String(realTime));
                } else {

                }

            } else if(eventSource == 0x0F) {
                // 不确定（无效值）
            } else {
                // TODO: 2021/2/9 其他错误，打印日志
            }

        } else {
            // TODO: 2021/2/9 其他类型，是否指令错误
        }
    }
    

    ArrayList<TestOperationRecords.TestOperationRecord> mList = new ArrayList<>();

    private void refreshDataFromBle(BleResultBean bean) {
        int eventType = bean.getPayload()[5];
        if(eventType == 0x03) {
            byte[] alarmCodeBytes = new byte[4];
            System.arraycopy(bean.getPayload(), 6, alarmCodeBytes, 0, alarmCodeBytes.length);
            // 1~8bit 小端，反向取
            byte[] bit1_8 = BleByteUtil.byteToBit(alarmCodeBytes[3]);
            byte[] bit9_16 = BleByteUtil.byteToBit(alarmCodeBytes[2]);
            int reserved = bean.getPayload()[10];
            byte[] localTimeBytes = new byte[4];
            System.arraycopy(bean.getPayload(), 11, localTimeBytes, 0, localTimeBytes.length);
            int count = 0;
            for (int i=0; i<4; i++) {
                if(localTimeBytes[i] == 0xff) {
                    count++;
                }
            }
            long localTime;
            if(count == 4) {
                localTime = -1;
            } else {
                localTime = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(localTimeBytes))*1000;
            }
            Timber.d("AlarmCode 1~8: %1s, AlarmCode 9~16: %2s, reserved: %3d, localtime: %4d",
                    ConvertUtils.bytes2HexString(bit1_8), ConvertUtils.bytes2HexString(bit9_16), reserved, localTime);

        } else {
            int eventSource1 = bean.getPayload()[6];
            int eventCode = bean.getPayload()[7];
            int userID1 = bean.getPayload()[8];
            byte[] localTimeBytes = new byte[4];
            System.arraycopy(bean.getPayload(), 9, localTimeBytes, 0, localTimeBytes.length);
            long localTime = BleByteUtil.bytesToLong(BleCommandFactory.littleMode(localTimeBytes))*1000;
            int eventSource2 =  bean.getPayload()[13];
            int userID2 = bean.getPayload()[14];
            Timber.d("eventSource1: %1d, eventCode: %2d, userID1: %3d, localTime: %4d, eventSource2: %5d, userID2: %6d",
                    eventSource1, eventCode, userID1, localTime, eventSource2, userID2);
        }

        // TODO: 2021/2/7
//        case 1:
//        message = getContext().getString(R.string.log_uses_a_password_to_unlock, "Ming");
//        case 2:
//        message = getContext().getString(R.string.log_uses_geo_fence_to_unlock, "John");
//        case 3:
//        message = getContext().getString(R.string.log_uses_the_app_to_unlock, "Han");
//        case 4:
//        message = getContext().getString(R.string.log_uses_mechanical_key_to_unlock, "Jack");
//        case 5:
//        message = getContext().getString(R.string.log_locking_inside_the_door);
//        case 6:
//        message = getContext().getString(R.string.log_double_lock_inside_the_door);
//        case 7:
//        message = getContext().getString(R.string.log_multi_functional_button_locking);
//        case 8:
//        message = getContext().getString(R.string.log_one_touch_lock_outside_the_door);
//        case 9:
//        message = getContext().getString(R.string.log_locked_the_door_by_app, "Amy");
//        case 10:
//        message = getContext().getString(R.string.log_locked_the_door_by_mechanical_key, "Ming");
//        case 11:
//        message = getContext().getString(R.string.log_duress_password_unlock);
//        case 12:
//        message = getContext().getString(R.string.log_lock_down_alarm);
//        case 13:
//        message = getContext().getString(R.string.log_low_battery_alarm);
//        case 14:
//        message = getContext().getString(R.string.log_jam_alarm);
//        case 15:
//        message = getContext().getString(R.string.log_door_opened_detected);
//        case 16:
//        message = getContext().getString(R.string.log_door_closed_detected);
//        case 17:
//        message = getContext().getString(R.string.log_the_user_added_a_password);
//        case 18:
//        message = getContext().getString(R.string.log_the_user_deleted_a_password);
//        case 19:
//        message = getContext().getString(R.string.log_the_user_added_someone_in_family_group, "Ming");
//        case 20:
//        message = getContext().getString(R.string.log_the_user_removed_someone_from_family_group, "Ming");
//        case 21:
//        message = getContext().getString(R.string.log_user_added_aa_as_guest_user, "Ming");
//        case 22:
//        message = getContext().getString(R.string.log_user_removed_aa_from_guest_user, "Tai");
    }

//    private void initData() {
//        List<TestOperationRecords> testOperationRecordsList = new ArrayList<>();
//        List<TestOperationRecords.TestOperationRecord> records = new ArrayList<>();
//        TestOperationRecords.TestOperationRecord record13 =
//                new TestOperationRecords.TestOperationRecord(1611062222000L, "password unlock", 13);
//        records.add(record13);
//        TestOperationRecords.TestOperationRecord record14 =
//                new TestOperationRecords.TestOperationRecord(1611062222000L, "Geo-fence unlock",14);
//        records.add(record14);
//        TestOperationRecords.TestOperationRecord record15 =
//                new TestOperationRecords.TestOperationRecord(1611062222000L, "APP unlock",15);
//        records.add(record15);
//        TestOperationRecords.TestOperationRecord record16 =
//                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",16);
//        records.add(record16);
//        TestOperationRecords.TestOperationRecord record17 =
//                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",17);
//        records.add(record17);
//        TestOperationRecords.TestOperationRecord record18 =
//                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",18);
//        records.add(record18);
//        TestOperationRecords.TestOperationRecord record19 =
//                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",19);
//        records.add(record19);
//        TestOperationRecords.TestOperationRecord record20 =
//                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",20);
//        records.add(record20);
//        TestOperationRecords.TestOperationRecord record21 =
//                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",21);
//        records.add(record21);
//        TestOperationRecords.TestOperationRecord record22 =
//                new TestOperationRecords.TestOperationRecord(1611062222000L, "Manual unlock",22);
//        records.add(record22);
//        TestOperationRecords testOperationRecords = new TestOperationRecords(1611062222000L, records);
//        testOperationRecordsList.add(testOperationRecords);
//
//        List<TestOperationRecords.TestOperationRecord> records1 = new ArrayList<>();
//        TestOperationRecords.TestOperationRecord record1 =
//                new TestOperationRecords.TestOperationRecord(1610506800000L, "password unlock", 1);
//        records1.add(record1);
//        TestOperationRecords.TestOperationRecord record2 =
//                new TestOperationRecords.TestOperationRecord(1610505000000L, "Geo-fence unlock",2);
//        records1.add(record2);
//        TestOperationRecords.TestOperationRecord record3 =
//                new TestOperationRecords.TestOperationRecord(1610504880000L, "APP unlock",3);
//        records1.add(record3);
//        TestOperationRecords.TestOperationRecord record4 =
//                new TestOperationRecords.TestOperationRecord(1610504880000L, "Manual unlock",4);
//        records1.add(record4);
//        TestOperationRecords testOperationRecords1 = new TestOperationRecords(1610504880000L, records1);
//        testOperationRecordsList.add(testOperationRecords1);
//
//        List<TestOperationRecords.TestOperationRecord> records2 = new ArrayList<>();
//        TestOperationRecords.TestOperationRecord record5 =
//                new TestOperationRecords.TestOperationRecord(1610418480000L, "Locking inside the door",5);
//        records2.add(record5);
//        TestOperationRecords.TestOperationRecord record6 =
//                new TestOperationRecords.TestOperationRecord(1610418480000L, "Double lock inside the door",6);
//        records2.add(record6);
//        TestOperationRecords.TestOperationRecord record7 =
//                new TestOperationRecords.TestOperationRecord(1610418480000L, "Multi-functional button locking",7);
//        records2.add(record7);
//        TestOperationRecords.TestOperationRecord record8 =
//                new TestOperationRecords.TestOperationRecord(1610418480000L, "One-touch lock outside the door ",8);
//        records2.add(record8);
//        TestOperationRecords testOperationRecords2 = new TestOperationRecords(1610418480000L, records2);
//        testOperationRecordsList.add(testOperationRecords2);
//
//        List<TestOperationRecords.TestOperationRecord> records3 = new ArrayList<>();
//        TestOperationRecords.TestOperationRecord record9 =
//                new TestOperationRecords.TestOperationRecord(1608863280000L, "Duress password unlock",9);
//        records3.add(record9);
//        TestOperationRecords.TestOperationRecord record10 =
//                new TestOperationRecords.TestOperationRecord(1608863280000L, "lock down alarm",10);
//        records3.add(record10);
//        TestOperationRecords.TestOperationRecord record11 =
//                new TestOperationRecords.TestOperationRecord(1608863280000L, "Low battery alarm",11);
//        records3.add(record11);
//        TestOperationRecords.TestOperationRecord record12 =
//                new TestOperationRecords.TestOperationRecord(1608863280000L, "Jam alarm",12);
//        records3.add(record12);
//        TestOperationRecords testOperationRecords3 = new TestOperationRecords(1608863280000L, records3);
//        testOperationRecordsList.add(testOperationRecords3);
//
//        mRecordsAdapter.setList(testOperationRecordsList);
//    }

}
