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
            CMD_LOCK_PARAMETER_CHECK, CMD_LOCK_OP_RECORD, CMD_PAIR_ACK, CMD_KEY_ATTRIBUTES_SET,
            CMD_KEY_ATTRIBUTES_READ, CMD_HEART_ACK, CMD_KEY_ADD, CMD_DOOR_SENSOR_CALIBRATION, CMD_WIFI_SWITCH,
            CMD_SET_SENSITIVITY, CMD_SET_AUTO_LOCK_TIME, CMD_KNOCK_DOOR_AND_UNLOCK_TIME,CMD_GET_ALL_RECORD,
            CMD_DURESS_PWD_SWITCH, CMD_AUTHENTICATION_ACK, CMD_LOCK_CONTROL_ACK, CMD_LOCK_KEY_MANAGER_ACK, CMD_SY_LOCK_TIME,
            CMD_LOCK_PARAMETER_CHANGED, CMD_USER_TYPE_SETTING_ACK, CMD_WEEKLY_PLAN_SETTING_ACK,
            CMD_WEEKLY_PLAN_DELETE_ACK, CMD_YEAR_MON_DAY_PLAN_SETTING_ACK,  CMD_YEAR_MON_DAY_PLAN_DELETE_ACK,
            CMD_SY_KEY_STATE, CMD_LOCK_INFO, CMD_REQUEST_BIND_ACK, CMD_SS_ID_ACK, CMD_PWD_ACK,
            CMD_UPLOAD_PAIR_NETWORK, CMD_UPLOAD_PAIR_NETWORK_STATE, CMD_KEY_VERIFY_RESULT_ACK,
            CMD_UPLOAD_REMAIN_COUNT, CMD_PAIR_NETWORK_ACK, CMD_BLE_UPLOAD_PAIR_NETWORK_STATE,
            CMD_WIFI_LIST_CHECK, CMD_NOTHING})
    public @interface CMD{ }

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
    public static final int CMD_KEY_ATTRIBUTES_SET = 0x1C;              // 密钥属性设置
    public static final int CMD_KEY_ATTRIBUTES_READ = 0x1D;             // 密钥属性读
    public static final int CMD_KEY_ADD = 0x1E;                         // 密钥添加
    public static final int CMD_DOOR_SENSOR_CALIBRATION = 0x1F;         // 门磁校准
    public static final int CMD_SET_SENSITIVITY = 0x20;                 // 敲门开锁灵敏度
    public static final int CMD_SET_AUTO_LOCK_TIME = 0x21;              // 设置关门自动上锁时间
    public static final int CMD_KNOCK_DOOR_AND_UNLOCK_TIME = 0x22;      // 敲门开锁指令
    public static final int CMD_SY_LOCK_TIME = 0x23;                    // 与锁同步时间
    public static final int CMD_GET_ALL_RECORD = 0x24;                  // 获取混合记录
    public static final int CMD_DURESS_PWD_SWITCH = 0x25;               // 胁迫密码开关
    public static final int CMD_WIFI_SWITCH = 0x26;                     // wifi功能开关
    public static final int CMD_HEART_ACK = 0x00;                       // 心跳包确认帧
    public static final int CMD_AUTHENTICATION_ACK = 0x01;              // 鉴权确认帧
    public static final int CMD_LOCK_CONTROL_ACK =  0x02;               // 锁控制确认帧
    public static final int CMD_LOCK_KEY_MANAGER_ACK = 0x03;            // 锁密钥管理确认帧
    public static final int CMD_LOCK_PARAMETER_CHANGED = 0x06;          // 锁参数修改
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

    @IntDef(value = {ACK_SUCCESS, ACK_FAILURE, ACK_NOT_AUTHORIZED, ACK_RESERVED_FIELD_NOT_ZERO,
            ACK_MALFORMED_COMMAND, ACK_UN_SUPPORT_COMMAND, ACK_UN_SUP_GENERAL_COMMAND,
            ACK_UN_SUP_MANUF_COMMAND, ACK_UN_SUP_MANUF_GENERAL_COMMAND, ACK_INVALID_FIELD,
            ACK_UNSUPPORTED_ATTRIBUTE, ACK_INVALID_VALUE, ACK_READ_ONLY, ACK_INSUFFICIENT_SPACE,
            ACK_DUPLICATE_EXISTS, ACK_NOT_FOUND, ACK_UN_REPORTABLE_ATTRIBUTE, ACK_INVALID_DATA_TYPE,
            ACK_INVALID_SELECTOR, ACK_WRITE_ONLY, ACK_INCONSISTENT_STARTUP_STATE, ACK_DEFINED_OUT_OF_BAND,
            ACK_INCONSISTENT, ACK_ACTION_DENIED, ACK_TIMEOUT, ACK_ABORT, ACK_INVALID_IMAGE, ACK_WAIT_FOR_DATA,
            ACK_NO_IMAGE_AVAILABLE, ACK_REQUIRE_MORE_IMAGE, ACK_NOTIFICATION_PENDING, ACK_HARDWARE_FAILURE,
            ACK_SOFTWARE_FAILURE, ACK_CALIBRATION_ERROR, ACK_UNSUPPORTED_CLUSTER, ACK_BACK_LOCK, ACK_SAFE_MODE,
            ACK_SUBMODULE_VERSION_SAME, ACK_GET_VERSION_TIMEOUT, ACK_OTA_TYPE_INVALID, ACK_OPEN_WIFI_TIMEOUT,
            ACK_SET_WIFI_FAILURE, ACK_NO_THING})
    public @interface ACK{}

    public static final int ACK_SUCCESS = 0x00;                            // 操作成功
    public static final int ACK_FAILURE = 0x01;                            // 操作失败
    public static final int ACK_NOT_AUTHORIZED = 0x7E;                     // 无授权
    public static final int ACK_RESERVED_FIELD_NOT_ZERO = 0x7F;            // 保留区域没有置零
    public static final int ACK_MALFORMED_COMMAND = 0x80;                  // 异常命令
    public static final int ACK_UN_SUPPORT_COMMAND = 0x81;                 // 不支持该命令，不支持的命令必须返回该值
    public static final int ACK_UN_SUP_GENERAL_COMMAND = 0x82;             // 不使用
    public static final int ACK_UN_SUP_MANUF_COMMAND = 0x83;               // 不使用
    public static final int ACK_UN_SUP_MANUF_GENERAL_COMMAND = 0x84;       // 不使用
    public static final int ACK_INVALID_FIELD = 0x85;                      // 某个字段错误
    public static final int ACK_UNSUPPORTED_ATTRIBUTE = 0x86;              // 不支持的属性
    public static final int ACK_INVALID_VALUE = 0x87;                      // 超出范围或者设置为保留值，或者序号已存在
    public static final int ACK_READ_ONLY = 0x88;                          // 属性为只读
    public static final int ACK_INSUFFICIENT_SPACE = 0x89;                 // 操作空间不够
    public static final int ACK_DUPLICATE_EXISTS = 0x8A;                   // 重复存在
    public static final int ACK_NOT_FOUND = 0x8B;                          // 被请求的数据没有找到，不支持的设置项必须返回该值
    public static final int ACK_UN_REPORTABLE_ATTRIBUTE = 0x8C;            // 不使用
    public static final int ACK_INVALID_DATA_TYPE = 0x8D;                  // 数据类型错误
    public static final int ACK_INVALID_SELECTOR = 0x8E;                   // 不使用
    public static final int ACK_WRITE_ONLY = 0x8F;                         // 只写
    public static final int ACK_INCONSISTENT_STARTUP_STATE = 0x90;         // 不使用
    public static final int ACK_DEFINED_OUT_OF_BAND = 0x91;                // 不使用
    public static final int ACK_INCONSISTENT = 0x92;                       // 不使用
    public static final int ACK_ACTION_DENIED = 0x93;                      // 权限不够
    public static final int ACK_TIMEOUT = 0x94;                            // 超时，模块发送命令给锁，锁没有接收
    public static final int ACK_ABORT = 0x95;                              // 客户端或服务端退出升级程序
    public static final int ACK_INVALID_IMAGE = 0x96;                      // 错误的Image文件
    public static final int ACK_WAIT_FOR_DATA = 0x97;                      // Server does not have data block available yet.
    public static final int ACK_NO_IMAGE_AVAILABLE = 0x98;                 // 没有可用的OTA Image文件
    public static final int ACK_REQUIRE_MORE_IMAGE = 0x99;                 // 客户端需要更多的OTA文件
    public static final int ACK_NOTIFICATION_PENDING = 0x9A;               // 命令已经接收正在处理
    public static final int ACK_HARDWARE_FAILURE = 0xC0;                   // 硬件原因造成错误
    public static final int ACK_SOFTWARE_FAILURE = 0xC1;                   // 软件原因造成错误
    public static final int ACK_CALIBRATION_ERROR = 0xC2;                  // 校验过程出现错误
    public static final int ACK_UNSUPPORTED_CLUSTER = 0xC3;                // 不使用
    public static final int ACK_BACK_LOCK = 0xC4;                          // 反锁
    public static final int ACK_SAFE_MODE = 0xC5;                          // 安全模式
    public static final int ACK_SUBMODULE_VERSION_SAME = 0xD1;             // 子模块版本相同
    public static final int ACK_GET_VERSION_TIMEOUT = 0xD2;                // 获取版本超时
    public static final int ACK_OTA_TYPE_INVALID = 0xD3;                   // 升级方式错误
    public static final int ACK_OPEN_WIFI_TIMEOUT = 0xD4;                  // 升级方式错误
    public static final int ACK_SET_WIFI_FAILURE = 0xD5;                   // 配置WIFI失败
    public static final int ACK_NO_THING = 0xFF;                           // 锁已经接收到命令，但超时时间内没有处理结果返回

    @IntDef(value = {LOCK_OPEN_RECORD_EVENT_TYPE_OP, LOCK_OPEN_RECORD_EVENT_TYPE_PROGRAM})
    public @interface LockOpenRecordEventType{}
    public static final int LOCK_OPEN_RECORD_EVENT_TYPE_OP = 0x01;
    public static final int LOCK_OPEN_RECORD_EVENT_TYPE_PROGRAM = 0x02;

    @IntDef(value = {LOCK_OPEN_RECORD_EVENT_SOURCE_KEYPAD, LOCK_OPEN_RECORD_EVENT_SOURCE_RF,
            LOCK_OPEN_RECORD_EVENT_SOURCE_MANUAL, LOCK_OPEN_RECORD_EVENT_SOURCE_RFID,
            LOCK_OPEN_RECORD_EVENT_SOURCE_FINGERPRINT, LOCK_OPEN_RECORD_EVENT_SOURCE_VOICE,
            LOCK_OPEN_RECORD_EVENT_SOURCE_FINGER_VEIN, LOCK_OPEN_RECORD_EVENT_SOURCE_FACE_RECOGNITION,
            LOCK_OPEN_RECORD_EVENT_SOURCE_UNSURE})
    public @interface LockOpenRecordEventSource{}
    public static final int LOCK_OPEN_RECORD_EVENT_SOURCE_KEYPAD = 0x00;
    public static final int LOCK_OPEN_RECORD_EVENT_SOURCE_RF = 0x01;
    public static final int LOCK_OPEN_RECORD_EVENT_SOURCE_MANUAL = 0x02;
    public static final int LOCK_OPEN_RECORD_EVENT_SOURCE_RFID = 0x03;
    public static final int LOCK_OPEN_RECORD_EVENT_SOURCE_FINGERPRINT = 0x04;
    public static final int LOCK_OPEN_RECORD_EVENT_SOURCE_VOICE = 0x05;
    public static final int LOCK_OPEN_RECORD_EVENT_SOURCE_FINGER_VEIN = 0x06;
    public static final int LOCK_OPEN_RECORD_EVENT_SOURCE_FACE_RECOGNITION = 0x07;
    public static final int LOCK_OPEN_RECORD_EVENT_SOURCE_UNSURE = 0xFF;

    @IntDef(value = {LOCK_OPEN_RECORD_EVENT_CODE_OP_LOCK, LOCK_OPEN_RECORD_EVENT_CODE_OP_UNLOCK,
            LOCK_OPEN_RECORD_EVENT_CODE_OP_LOCK_FAILURE_INVALID_PIN_OR_ID, LOCK_OPEN_RECORD_EVENT_CODE_OP_LOCK_FAILURE_INVALID_SCHEDULE,
            LOCK_OPEN_RECORD_EVENT_CODE_OP_UNLOCK_FAILURE_INVALID_PIN_OR_ID,  LOCK_OPEN_RECORD_EVENT_CODE_OP_UNLOCK_FAILURE_INVALID_SCHEDULE,
            LOCK_OPEN_RECORD_EVENT_CODE_OP_ONE_TOUCH_LOCK, LOCK_OPEN_RECORD_EVENT_CODE_OP_KEY_LOCK,
            LOCK_OPEN_RECORD_EVENT_CODE_OP_KEY_UNLOCK, LOCK_OPEN_RECORD_EVENT_CODE_OP_AUTO_LOCK,
            LOCK_OPEN_RECORD_EVENT_CODE_OP_MANUAL_LOCK, LOCK_OPEN_RECORD_EVENT_CODE_OP_MANUAL_UNLOCK,
            LOCK_OPEN_RECORD_EVENT_CODE_OP_NON_ACCESS_USER_OP})
    public @interface LockOpenRecordEventCodeOp{}
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_LOCK = 0x01;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_UNLOCK = 0x02;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_LOCK_FAILURE_INVALID_PIN_OR_ID = 0x03;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_LOCK_FAILURE_INVALID_SCHEDULE = 0x04;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_UNLOCK_FAILURE_INVALID_PIN_OR_ID = 0x05;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_UNLOCK_FAILURE_INVALID_SCHEDULE = 0x06;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_ONE_TOUCH_LOCK = 0x07;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_KEY_LOCK = 0x08;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_KEY_UNLOCK = 0x09;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_AUTO_LOCK = 0x0A;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_MANUAL_LOCK = 0x0D;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_MANUAL_UNLOCK = 0x0E;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_OP_NON_ACCESS_USER_OP = 0x0F;

    @IntDef(value = {
            LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_MASTER_CODE_CHANGED, LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_PIN_CODE_ADDED,
            LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_PIN_CODE_DELETED, LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_PIN_CODE_CHANGED,
            LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_RFID_CODE_ADDED, LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_RFID_CODE_DELETED,
            LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_FINGERPRINT_ADDED, LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_FINGERPRINT_DELETED
    })
    public @interface LockOpenRecordEventCodeProgram{}
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_MASTER_CODE_CHANGED = 0x01;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_PIN_CODE_ADDED = 0x02;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_PIN_CODE_DELETED = 0x03;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_PIN_CODE_CHANGED = 0x04;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_RFID_CODE_ADDED = 0x05;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_RFID_CODE_DELETED = 0x06;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_FINGERPRINT_ADDED = 0x07;
    public static final int LOCK_OPEN_RECORD_EVENT_CODE_PROGRAM_FINGERPRINT_DELETED = 0x08;

    @IntDef(value = {
            LOCK_OPEN_RECORD_USER_ID_MACHINE_KEY,  LOCK_OPEN_RECORD_USER_ID_REMOTE_CONTROL,
            LOCK_OPEN_RECORD_USER_ID_ONE_TOUCH_OPEN, LOCK_OPEN_RECORD_USER_ID_APP_COMMAND,
            LOCK_OPEN_RECORD_USER_ID_BLE_AUTO, LOCK_OPEN_RECORD_USER_ID_NOTHING
    })
    public @interface LockOpenRecordUserId{}
    public static final int LOCK_OPEN_RECORD_USER_ID_MACHINE_KEY = 100;
    public static final int LOCK_OPEN_RECORD_USER_ID_REMOTE_CONTROL = 101;
    public static final int LOCK_OPEN_RECORD_USER_ID_ONE_TOUCH_OPEN = 102;
    public static final int LOCK_OPEN_RECORD_USER_ID_APP_COMMAND = 103;
    public static final int LOCK_OPEN_RECORD_USER_ID_BLE_AUTO = 104;
    public static final int LOCK_OPEN_RECORD_USER_ID_NOTHING = 0xFF;

    @IntDef(value = {
            LOCK_RECORD_OP_CMD_CMD_TYPE_OP, LOCK_RECORD_OP_CMD_CMD_TYPE_PROGRAM,
            LOCK_RECORD_OP_CMD_ALARM, LOCK_RECORD_OP_CMD_ALL
    })
    public @interface LockRecordOpCmd{}
    public static final int LOCK_RECORD_OP_CMD_CMD_TYPE_OP = 0x01;
    public static final int LOCK_RECORD_OP_CMD_CMD_TYPE_PROGRAM = 0x02;
    public static final int LOCK_RECORD_OP_CMD_ALARM = 0x03;
    public static final int LOCK_RECORD_OP_CMD_ALL = 0x04;

    @IntDef(value = {
            LOCK_RECORD_OP_EVENT_TYPE_OP, LOCK_RECORD_OP_EVENT_TYPE_PROGRAM,
            LOCK_RECORD_OP_EVENT_TYPE_ALARM
    })
    public @interface LockRecordOpEventType{}
    public static final int LOCK_RECORD_OP_EVENT_TYPE_OP = 0x01;
    public static final int LOCK_RECORD_OP_EVENT_TYPE_PROGRAM = 0x02;
    public static final int LOCK_RECORD_OP_EVENT_TYPE_ALARM = 0x03;

    @IntDef(value = {
            LOCK_RECORD_OP_EVENT_SOURCE_KEYPAD, LOCK_RECORD_OP_EVENT_SOURCE_RF,
            LOCK_RECORD_OP_EVENT_SOURCE_MANUAL, LOCK_RECORD_OP_EVENT_SOURCE_RFID,
            LOCK_RECORD_OP_EVENT_SOURCE_FINGERPRINT, LOCK_RECORD_OP_EVENT_SOURCE_VOICE,
            LOCK_RECORD_OP_EVENT_SOURCE_FINGER_VEIN, LOCK_RECORD_OP_EVENT_SOURCE_FACE,
            LOCK_RECORD_OP_EVENT_SOURCE_APP, LOCK_RECORD_OP_EVENT_SOURCE_UNSURE
    })
    public @interface LockRecordOpEventSource{}
    public static final int LOCK_RECORD_OP_EVENT_SOURCE_KEYPAD = 0x00;
    public static final int LOCK_RECORD_OP_EVENT_SOURCE_RF = 0x01;
    public static final int LOCK_RECORD_OP_EVENT_SOURCE_MANUAL = 0x02;
    public static final int LOCK_RECORD_OP_EVENT_SOURCE_RFID = 0x03;
    public static final int LOCK_RECORD_OP_EVENT_SOURCE_FINGERPRINT = 0x04;
    public static final int LOCK_RECORD_OP_EVENT_SOURCE_VOICE = 0x05;
    public static final int LOCK_RECORD_OP_EVENT_SOURCE_FINGER_VEIN = 0x06;
    public static final int LOCK_RECORD_OP_EVENT_SOURCE_FACE = 0x07;
    public static final int LOCK_RECORD_OP_EVENT_SOURCE_APP = 0x08;
    public static final int LOCK_RECORD_OP_EVENT_SOURCE_UNSURE = 0xFF;

    @IntDef(value = {
            LOCK_RECORD_OP_EVENT_CODE_OP_LOCK, LOCK_RECORD_OP_EVENT_CODE_OP_UNLOCK,
            LOCK_RECORD_OP_EVENT_CODE_OP_LOCK_FAILURE_INVALID_PIN_OR_ID,
            LOCK_RECORD_OP_EVENT_CODE_OP_LOCK_FAILURE_INVALID_SCHEDULE,
            LOCK_RECORD_OP_EVENT_CODE_OP_UNLOCK_FAILURE_INVALID_PIN_OR_ID,
            LOCK_RECORD_OP_EVENT_CODE_OP_UNLOCK_FAILURE_INVALID_SCHEDULE,
            LOCK_RECORD_OP_EVENT_CODE_OP_ONE_TOUCH_LOCK, LOCK_RECORD_OP_EVENT_CODE_OP_KEY_LOCK,
            LOCK_RECORD_OP_EVENT_CODE_OP_KEY_UNLOCK, LOCK_RECORD_OP_EVENT_CODE_OP_AUTO_LOCK,
            LOCK_RECORD_OP_EVENT_CODE_OP_MANUAL_LOCK, LOCK_RECORD_OP_EVENT_CODE_OP_MANUAL_UNLOCK,
            LOCK_RECORD_OP_EVENT_CODE_OP_NON_ACCESS_USER_OP
    })
    public @interface LockRecordOpEventCodeOp{}
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_LOCK = 0x01;
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_UNLOCK = 0x02;
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_LOCK_FAILURE_INVALID_PIN_OR_ID = 0x03;
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_LOCK_FAILURE_INVALID_SCHEDULE = 0x04;
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_UNLOCK_FAILURE_INVALID_PIN_OR_ID = 0x05;
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_UNLOCK_FAILURE_INVALID_SCHEDULE = 0x06;
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_ONE_TOUCH_LOCK = 0x07;
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_KEY_LOCK = 0x08;
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_KEY_UNLOCK = 0x09;
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_AUTO_LOCK = 0x0A;
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_MANUAL_LOCK = 0x0D;
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_MANUAL_UNLOCK = 0x0E;
    public static final int LOCK_RECORD_OP_EVENT_CODE_OP_NON_ACCESS_USER_OP = 0x0F;

    @IntDef(value = {
            LOCK_RECORD_OP_EVENT_CODE_PROGRAM_MASTER_CODE_CHANGED,
            LOCK_RECORD_OP_EVENT_CODE_PROGRAM_PIN_CODE_ADDED,
            LOCK_RECORD_OP_EVENT_CODE_PROGRAM_PIN_CODE_DELETED,
            LOCK_RECORD_OP_EVENT_CODE_PROGRAM_PIN_CODE_CHANGED,
            LOCK_RECORD_OP_EVENT_CODE_PROGRAM_RFID_CODE_ADDED,
            LOCK_RECORD_OP_EVENT_CODE_PROGRAM_RFID_CODE_DELETED,
            LOCK_RECORD_OP_EVENT_CODE_PROGRAM_FINGERPRINT_ADDED,
            LOCK_RECORD_OP_EVENT_CODE_PROGRAM_FINGERPRINT_DELETED,
            LOCK_RECORD_OP_EVENT_CODE_PROGRAM_FACE_ADDED, LOCK_RECORD_OP_EVENT_CODE_PROGRAM_FACE_DELETED,
            LOCK_RECORD_OP_EVENT_CODE_PROGRAM_FINGER_VEIN_ADDED, LOCK_RECORD_OP_EVENT_CODE_PROGRAM_FINGER_VEIN_DELETED,
            LOCK_RECORD_OP_EVENT_CODE_PROGRAM_RESET_SETTING
    })
    public @interface LockRecordOpEventCodeProgram{}
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_MASTER_CODE_CHANGED = 0x01;
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_PIN_CODE_ADDED = 0x02;
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_PIN_CODE_DELETED = 0x03;
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_PIN_CODE_CHANGED = 0x04;
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_RFID_CODE_ADDED = 0x05;
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_RFID_CODE_DELETED = 0x06;
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_FINGERPRINT_ADDED = 0x07;
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_FINGERPRINT_DELETED = 0x08;
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_FACE_ADDED = 0x0B;
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_FACE_DELETED = 0x0C;
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_FINGER_VEIN_ADDED = 0x0D;
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_FINGER_VEIN_DELETED = 0x0E;
    public static final int LOCK_RECORD_OP_EVENT_CODE_PROGRAM_RESET_SETTING = 0x0F;            // 恢复出厂设置

}
