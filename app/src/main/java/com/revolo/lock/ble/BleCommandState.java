package com.revolo.lock.ble;

import androidx.annotation.IntDef;

/**
 * author : Jack
 * time   : 2021/1/18
 * E-mail : wengmaowei@kaadas.com
 * desc   : 锁发送指令参数
 */
public class BleCommandState {

    @IntDef(value = {LOCK_CONTROL_ACTION_CONTROL_UNLOCK, LOCK_CONTROL_ACTION_LOCK, LOCK_CONTROL_ACTION_TOGGLE, LOCK_CONTROL_ACTION_APP})
    public @interface LockControlAction{}

    public static final int LOCK_CONTROL_ACTION_CONTROL_UNLOCK = 0x00;
    public static final int LOCK_CONTROL_ACTION_LOCK = 0x01;
    public static final int LOCK_CONTROL_ACTION_TOGGLE = 0x02;
    public static final int LOCK_CONTROL_ACTION_APP = 0x03;

    @IntDef(value = {LOCK_CONTROL_CODE_TYPE_PIN, LOCK_CONTROL_CODE_TYPE_RFID_CARD, LOCK_CONTROL_CODE_TYPE_APP})
    public @interface LockControlCodeType{}
    public static final int LOCK_CONTROL_CODE_TYPE_PIN = 0x01;
    public static final int LOCK_CONTROL_CODE_TYPE_RFID_CARD = 0x02;
    public static final int LOCK_CONTROL_CODE_TYPE_APP = 0x04;

    @IntDef(value = {LOCK_CONTROL_USER_ID_BLE_AUTO})
    public @interface LockControlUserId{}
    public static final int LOCK_CONTROL_USER_ID_BLE_AUTO = 0x01;

    @IntDef(value = {KEY_MANAGE_ACTION_SET_CODE, KEY_MANAGE_ACTION_GET_CODE, KEY_MANAGE_ACTION_CLEAR_CODE, KEY_MANAGE_ACTION_CHECK_CODE})
    public @interface KeyManageAction{}
    public static final int KEY_MANAGE_ACTION_SET_CODE = 0x01;
    public static final int KEY_MANAGE_ACTION_GET_CODE = 0x02;
    public static final int KEY_MANAGE_ACTION_CLEAR_CODE = 0x03;
    public static final int KEY_MANAGE_ACTION_CHECK_CODE = 0x04;

    @IntDef(value = {KEY_MANAGE_CODE_TYPE_PIN, KEY_MANAGE_CODE_TYPE_FINGERPRINT, KEY_MANAGE_CODE_TYPE_RFID_CARD, KEY_MANAGE_CODE_TYPE_MANAGER_PWD})
    public @interface KeyManageCodeType{}
    public static final int KEY_MANAGE_CODE_TYPE_PIN = 0x01;
    public static final int KEY_MANAGE_CODE_TYPE_FINGERPRINT = 0x02;
    public static final int KEY_MANAGE_CODE_TYPE_RFID_CARD = 0x03;
    public static final int KEY_MANAGE_CODE_TYPE_MANAGER_PWD = 0x04;

    @IntDef(value = {LOCK_SETTING_OPEN, LOCK_SETTING_CLOSE})
    public @interface OpenOrClose{}
    public static final int LOCK_SETTING_OPEN = 0x00;
    public static final int LOCK_SETTING_CLOSE = 0x01;

    @IntDef(value = {USER_TYPE_SETTING_CODE_TYPE_PIN, USER_TYPE_SETTING_CODE_TYPE_FINGERPRINT, USER_TYPE_SETTING_CODE_TYPE_RFID})
    public @interface UserTypeSettingCodeType{}
    public static final int USER_TYPE_SETTING_CODE_TYPE_PIN = 0x01;
    public static final int USER_TYPE_SETTING_CODE_TYPE_FINGERPRINT = 0x02;
    public static final int USER_TYPE_SETTING_CODE_TYPE_RFID = 0x03;

    @IntDef(value = {USER_TYPE_SETTING_USER_TYPE_DEFAULT, USER_TYPE_SETTING_USER_TYPE_TIME_TABLE_USER,
            USER_TYPE_SETTING_USER_TYPE_DURESS, USER_TYPE_SETTING_USER_TYPE_MANAGER,
            USER_TYPE_SETTING_USER_TYPE_UNAUTHORIZED_USER, USER_TYPE_SETTING_USER_TYPE_GUEST_PWD,
            USER_TYPE_SETTING_USER_TYPE_ONCE_PWD})
    public @interface UserTypeSettingUserType{}
    public static final int USER_TYPE_SETTING_USER_TYPE_DEFAULT = 0x00;
    public static final int USER_TYPE_SETTING_USER_TYPE_TIME_TABLE_USER = 0x01;
    public static final int USER_TYPE_SETTING_USER_TYPE_DURESS = 0x02;
    public static final int USER_TYPE_SETTING_USER_TYPE_MANAGER = 0x03;
    public static final int USER_TYPE_SETTING_USER_TYPE_UNAUTHORIZED_USER = 0x04;
    public static final int USER_TYPE_SETTING_USER_TYPE_GUEST_PWD = 0xFD;
    public static final int USER_TYPE_SETTING_USER_TYPE_ONCE_PWD = 0xFE;

    @IntDef(value = {USER_TYPE_SEARCH_CODE_TYPE_PIN, USER_TYPE_SEARCH_CODE_TYPE_FINGERPRINT, USER_TYPE_SEARCH_CODE_TYPE_RFID})
    public @interface UserTypeSearchCodeType{}
    public static final int USER_TYPE_SEARCH_CODE_TYPE_PIN = 0x01;
    public static final int USER_TYPE_SEARCH_CODE_TYPE_FINGERPRINT = 0x02;
    public static final int USER_TYPE_SEARCH_CODE_TYPE_RFID = 0x03;

    @IntDef(value = {LOCK_OP_RECORD_OP_SEARCH})
    public @interface LockOpRecordOp{}
    public static final int LOCK_OP_RECORD_OP_SEARCH = 0x01;

    @IntDef(value = {LOCK_OP_RECORD_LOG_TYPE_OPEN, LOCK_OP_RECORD_LOG_TYPE_ALARM,
            LOCK_OP_RECORD_LOG_TYPE_KEY_OP, LOCK_OP_RECORD_LOG_TYPE_ALL})
    public @interface LockOpRecordLogType{}
    public static final int LOCK_OP_RECORD_LOG_TYPE_OPEN = 0x00;
    public static final int LOCK_OP_RECORD_LOG_TYPE_ALARM = 0x01;
    public static final int LOCK_OP_RECORD_LOG_TYPE_KEY_OP = 0x02;
    public static final int LOCK_OP_RECORD_LOG_TYPE_ALL = 0x03;

    @IntDef(value = {KEY_SET_KEY_OPTION_ADD_OR_CHANGE, KEY_SET_KEY_OPTION_DEL})
    public @interface KeySetKeyOption{}
    public static final int KEY_SET_KEY_OPTION_ADD_OR_CHANGE = 0x01;
    public static final int KEY_SET_KEY_OPTION_DEL = 0x02;

    @IntDef(value = {KEY_SET_KEY_TYPE_PWD, KEY_SET_KEY_TYPE_FINGERPRINT,
            KEY_SET_KEY_TYPE_CARD, KEY_SET_KEY_TYPE_FACE})
    public @interface KeySetKeyType{}
    public static final int KEY_SET_KEY_TYPE_PWD = 0x00;
    public static final int KEY_SET_KEY_TYPE_FINGERPRINT = 0x04;
    public static final int KEY_SET_KEY_TYPE_CARD = 0x03;
    public static final int KEY_SET_KEY_TYPE_FACE = 0x07;

    @IntDef(value = {KEY_SET_ATTRIBUTE_ALWAYS, KEY_SET_ATTRIBUTE_TIME_KEY,
            KEY_SET_ATTRIBUTE_DURESS_PWD_KEY, KEY_SET_ATTRIBUTE_ADMIN_PWD_KEY,
            KEY_SET_ATTRIBUTE_NO_AUTHORITY_KEY, KEY_SET_ATTRIBUTE_WEEK_KEY, KEY_SET_ATTRIBUTE_ONCE_PWD_KEY})
    public @interface KeySetAttribute{}
    public static final int KEY_SET_ATTRIBUTE_ALWAYS = 0x00;
    public static final int KEY_SET_ATTRIBUTE_TIME_KEY = 0x01;
    public static final int KEY_SET_ATTRIBUTE_DURESS_PWD_KEY = 0x02;
    public static final int KEY_SET_ATTRIBUTE_ADMIN_PWD_KEY = 0x03;
    public static final int KEY_SET_ATTRIBUTE_NO_AUTHORITY_KEY = 0x04;
    public static final int KEY_SET_ATTRIBUTE_WEEK_KEY = 0x05;
    public static final int KEY_SET_ATTRIBUTE_ONCE_PWD_KEY = 0xFE;

    @IntDef(value = {PAIR_NETWORK_STATUS_START, PAIR_NETWORK_STATUS_STOP, PAIR_NETWORK_STATUS_CONTINUE})
    public @interface PairNetworkStatus{}
    public static final int PAIR_NETWORK_STATUS_START = 0x00;
    public static final int PAIR_NETWORK_STATUS_STOP = 0x01;
    public static final int PAIR_NETWORK_STATUS_CONTINUE = 0x02;

    @IntDef(value = {LOCK_RECORD_OP_OP_CHECK})
    public @interface LockRecordOpOp{}
    public static final int LOCK_RECORD_OP_OP_CHECK = 0x01;

    @IntDef(value = {LOCK_RECORD_OP_LOG_TYPE_OPEN, LOCK_RECORD_OP_LOG_TYPE_ALARM,
            LOCK_RECORD_OP_LOG_TYPE_KEY, LOCK_RECORD_OP_LOG_TYPE_ALL})
    public @interface LockRecordOpLogType{}
    public static final int LOCK_RECORD_OP_LOG_TYPE_OPEN = 0x00;
    public static final int LOCK_RECORD_OP_LOG_TYPE_ALARM = 0x01;
    public static final int LOCK_RECORD_OP_LOG_TYPE_KEY = 0x02;
    public static final int LOCK_RECORD_OP_LOG_TYPE_ALL = 0x03;

}
