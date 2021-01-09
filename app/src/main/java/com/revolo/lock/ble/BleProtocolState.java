package com.revolo.lock.ble;

import androidx.annotation.IntDef;

/**
 * author : Jack
 * time   : 2021/1/8
 * E-mail : wengmaowei@kaadas.com
 * desc   : 协议的状态
 */
public class BleProtocolState {

    @IntDef(value = {CMD_LOCK_OPEN_RECORD, CMD_LOCK_UPLOAD, CMD_LOCK_ALARM_UPLOAD,
            CMD_ENCRYPT_KEY_UPLOAD, CMD_USER_TYPE, CMD_WEEKLY_PLAN_CHECK, CMD_YEAR_MON_DAY_PLAN_CHECK,
            CMD_LOCK_ALARM_RECORD_CHECK, CMD_LOCK_NUM_CHECK, CMD_LOCK_OPEN_COUNT_CHECK,
            CMD_LOCK_PARAMETER_CHECK, CMD_LOCK_OP_RECORD, CMD_PAIR_ACK, CMD_HEART_ACK,
            CMD_AUTHENTICATION_ACK, CMD_LOCK_CONTROL_ACK, CMD_LOCK_KEY_MANAGER_ACK,
            CMD_LOCK_PARAMETER_CHANGED_ACK, CMD_USER_TYPE_SETTING_ACK, CMD_WEEKLY_PLAN_SETTING_ACK,
            CMD_WEEKLY_PLAN_DELETE_ACK, CMD_YEAR_MON_DAY_PLAN_SETTING_ACK,  CMD_YEAR_MON_DAY_PLAN_DELETE_ACK,
            CMD_SY_KEY_STATE, CMD_LOCK_INFO, CMD_REQUEST_BIND_ACK, CMD_SS_ID_ACK, CMD_PWD_ACK,
            CMD_UPLOAD_PAIR_NETWORK, CMD_UPLOAD_PAIR_NETWORK_STATE, CMD_KEY_VERIFY_RESULT_ACK,
            CMD_UPLOAD_REMAIN_COUNT, CMD_PAIR_NETWORK_ACK, CMD_BLE_UPLOAD_PAIR_NETWORK_STATE,
            CMD_WIFI_LIST_CHECK, CMD_NOTHING})
    public @interface Cmd{ }

    public static final int CMD_LOCK_OPEN_RECORD = 0x04;                // 锁开锁记录查询响应
    public static final int CMD_LOCK_UPLOAD = 0x05;                     // 锁操作上报
    public static final int CMD_LOCK_ALARM_UPLOAD = 0x07;               // 锁报警上报
    public static final int CMD_ENCRYPT_KEY_UPLOAD = 0x08;              // 加密密钥上报
    public static final int CMD_USER_TYPE = 0x0A;                       // 用户类型查询
    public static final int CMD_WEEKLY_PLAN_CHECK = 0x0C;               // 周计划查询
    public static final int CMD_YEAR_MON_DAY_PLAN_CHECK = 0x0F;         // 年月日计划查询
    public static final int CMD_LOCK_ALARM_RECORD_CHECK = 0x14;         // 锁报警记录查询
    public static final int CMD_LOCK_NUM_CHECK = 0x15;                  // 锁序列号查询
    public static final int CMD_LOCK_OPEN_COUNT_CHECK = 0x16;           // 锁开锁次数查询
    public static final int CMD_LOCK_PARAMETER_CHECK = 0x17;            // 锁参数主动查询
    public static final int CMD_LOCK_OP_RECORD = 0x18;                  // 锁操作记录查询
    public static final int CMD_PAIR_ACK = 0x1b;                        // 配对确认帧
    public static final int CMD_HEART_ACK = 0x00;                       // 心跳包确认帧
    public static final int CMD_AUTHENTICATION_ACK = 0x01;              // 鉴权确认帧
    public static final int CMD_LOCK_CONTROL_ACK =  0x02;               // 锁控制确认帧
    public static final int CMD_LOCK_KEY_MANAGER_ACK = 0x03;            // 锁密钥管理确认帧
    public static final int CMD_LOCK_PARAMETER_CHANGED_ACK = 0x06;      // 锁参数修改确认帧
    public static final int CMD_USER_TYPE_SETTING_ACK = 0x09;           // 用户类型设置确认帧
    public static final int CMD_WEEKLY_PLAN_SETTING_ACK = 0x0B;         // 周计划设置确认帧
    public static final int CMD_WEEKLY_PLAN_DELETE_ACK = 0x0D;          // 周计划删除确认帧
    public static final int CMD_YEAR_MON_DAY_PLAN_SETTING_ACK = 0x0E;   // 年月日计划设置确认帧
    public static final int CMD_YEAR_MON_DAY_PLAN_DELETE_ACK = 0x10;    // 年月日计划删除确认帧
    public static final int CMD_SY_KEY_STATE = 0x11;                    // 同步门锁密钥状态响应
    public static final int CMD_LOCK_INFO = 0x12;                       // 查询门锁基本信息
    public static final int CMD_REQUEST_BIND_ACK = 0x13;                // APP绑定请求帧

    public static final int CMD_SS_ID_ACK = 0x90;                       // SS ID响应
    public static final int CMD_PWD_ACK = 0x91;                         // PWD响应
    public static final int CMD_UPLOAD_PAIR_NETWORK = 0x92;             // 上报配网因子
    public static final int CMD_UPLOAD_PAIR_NETWORK_STATE = 0x93;       // BLE上报配网状态
    public static final int CMD_KEY_VERIFY_RESULT_ACK = 0x94;           // 秘钥因子校验结果
    public static final int CMD_UPLOAD_REMAIN_COUNT = 0x95;             // 上报剩余校验次数
    public static final int CMD_PAIR_NETWORK_ACK = 0x96;                // App下发配网状态响应
    public static final int CMD_BLE_UPLOAD_PAIR_NETWORK_STATE = 0x97;   // BLE上报联网状态
    public static final int CMD_WIFI_LIST_CHECK = 0x98;                 // WIFI热点列表查询响应

    public static final int CMD_NOTHING = 0xFF;                         // 无用

}
