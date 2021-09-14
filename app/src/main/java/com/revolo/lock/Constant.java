package com.revolo.lock;

/**
 * author : Jack
 * time   : 2021/1/4
 * E-mail : wengmaowei@kaadas.com
 * desc   : 常量
 */
public class Constant {

    private Constant() {
    }

    public static final long WILL_ADD_TIME = 946656000L;
    public static final int DEFAULT_TIMEOUT_SEC_VALUE = 10;

    public static final String REVOLO_SP = "RevoloSP";

    public static final String PRE_A = "PreA";

    public static final String INPUT_ESN_A = "inputESNA";
    public static final String QR_CODE_A = "QRCodeA";
    public static final String AUTH_USER_DETAIL_A = "AuthUserDetailA";
    public static final String USER_MANAGEMENT_A = "UserManagementA";
    public static final String WIFI_SETTING_A = "WifiSettingA";
    public static final String BLE_CONNECT_FAIL_A = "BleConnectFailA";
    public static final String DOOR_SENSOR_CHECK_A = "DoorSensorCheckA";

    public static final String ESN = "ESN";
    public static final String QR_RESULT = "QRResult";

    public static final String LOCK_DETAIL = "LockDetail";
    public static final String PWD_DETAIL = "PwdDetail";
    public static final String USER_NAME = "UserName";
    public static final String MESSAGE_DETAIL = "MessageDetail";
    public static final String USER_INFO = "UserInfo";
    public static final String WIFI_NAME = "WifiName";
    public static final String WIFI_PWD = "WifiPwd";
    public static final String USER_PWD = "UserPwd";
    public static final String REGISTER_DETAIL = "RegisterDetail";
    public static final String COMMAND = "Command";
    public static final String ADD_DEVICE = "AddDevice";


    public static final String KEY_PWD1 = "KeyPwd1";
    public static final String KEY_PWD2 = "KeyPwd2";
    public static final String BLE_MAC = "BleMac";
    public static final String LOCK_ESN = "LockEsn";
    public static final String BLE_DEVICE = "BleDevice";
    public static final String KEY_PWD_NUM = "keyPwdNum";
    public static final String UNBIND_REQ = "UnbindReq";
    public static final String START_TIME = "startTime";

    public static final String DEVICE_ID = "DeviceId";
    public static final String PWD_NUM = "PwdNum";

    public static final String IS_GO_TO_ADD_WIFI = "isGoToAddWifi";
    public static final String IS_NEED_TO_CLOSE_BLE = "isNeedToCloseBle";

    public static final String TERM_TYPE = "TermType";
    public static final String TERM_TYPE_USER = "TermTypeUser";
    public static final String TERM_TYPE_PRIVACY = "TermTypePrivacy";

    // SP
    public static final String USER_MAIL = "UserMail";
    public static final String USER_TOKEN = "UserToken";
    public static final String USER_LOGIN_INFO = "UserLoginInfo";
    public static final String FIRST_OPEN_APP = "firstOpenApp";

    //DuressPwdReceive  设置胁迫邮箱
    public static final String DURESS_PWD_RECEIVE = "DuressPwdReceive";
    //SignSelect
    public static final String SIGN_SELECT_MODE = "sign_select_mode";

    /**
     * 在login界面，是否显示提示对话框key
     */
    public static final String IS_SHOW_DIALOG = "isShowDialog";

    // TODO: 2021/2/10 后续要整理
    public static final String IS_USE_BLE = "isUseBle";

    public static boolean isShowDialog = false;

    public static final String RECEIVE_ACTION_NETWORKS = "com.revolo.lock.receive.networks";

    public static final String PING_RESULT = "pingResult";

    public static final String SHARE_USER_DATA = "SharedUserData";

    public static final String SHARE_USER_DEVICE_DATA = "SharedUserDeviceData";

    public static final String SHARE_USER_MAIL = "ShareUserMail";

    public static final String SHARE_USER_FIRST_NAME = "ShareUserFirstName";

    public static final String SHARE_USER_LAST_NAME = "ShareUserLastName";

    public static final String SHARE_USER_SN_LIST = "ShareUserSnList";

    /**
     * 网络是否能访问
     */
    public static boolean pingResult = true;

    /**
     * 验证码是否正在倒计时
     */
    public static boolean isVerificationCodeTime = false;

    /**
     * 验证码倒计时
     */
    public static int verificationCodeTimeCount = 60;

    public static int LOCK_GEO_CONNECT_BLE_INDEX = 25;

    /**
     * 是否有新版本更新
     */
    public static boolean isNewAppVersion = false;

    public static String registerEmail = "";

    public static final String SHOW_SHARE_DIALOG_TITLE = "showShareDialogTitle";

    /**
     * 是否进入地理围栏引导
     */
    public static final String SHOW_GEOFENCE_LOADING = "show_geofence_loading";

    /**
     * 连接的wifiName
     */
    public static final String CONNECT_WIFI_NAME = "WiFiName";

    /**
     * wifiSetting页面去设置wifi
     */
    public static final String WIFI_SETTING_TO_ADD_WIFI = "wifiSettingToAddWifi";

    /**
     * 修改锁的名称
     */
    public static final String CHANGE_LOCK_NAME = "tvName";

    public static final String CHANGE_PWD_NAME = "passwordName";

    public static final String CHANGE_GESTURE_HAND_PASSWORD = "changeHandGesturePassword";

    /**
     * 是否显示门磁配置开启提示弹框
     */
    public static final String IS_OPEN_DOOR = "IS_OPEN_DOOR";
}
