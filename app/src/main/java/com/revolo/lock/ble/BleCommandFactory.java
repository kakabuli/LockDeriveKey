package com.revolo.lock.ble;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.EncryptUtils;

import timber.log.Timber;

import static com.revolo.lock.ble.BleProtocolState.CMD_DURESS_PWD_SWITCH;
import static com.revolo.lock.ble.BleProtocolState.CMD_GET_ALL_RECORD;
import static com.revolo.lock.ble.BleProtocolState.CMD_KEY_ADD;
import static com.revolo.lock.ble.BleProtocolState.CMD_KEY_ATTRIBUTES_READ;
import static com.revolo.lock.ble.BleProtocolState.CMD_KEY_ATTRIBUTES_SET;
import static com.revolo.lock.ble.BleProtocolState.CMD_KNOCK_DOOR_AND_UNLOCK_TIME;
import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_ALARM_RECORD_CHECK;
import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_INFO;
import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_NUM_CHECK;
import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_OPEN_COUNT_CHECK;
import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_PARAMETER_CHANGED;
import static com.revolo.lock.ble.BleProtocolState.CMD_LOCK_PARAMETER_CHECK;
import static com.revolo.lock.ble.BleProtocolState.CMD_SET_SENSITIVITY;
import static com.revolo.lock.ble.BleProtocolState.CMD_WIFI_LIST_CHECK;
import static com.revolo.lock.ble.BleProtocolState.CMD_WIFI_SWITCH;

/**
 * author : Jack
 * time   : 2021/1/5
 * E-mail : wengmaowei@kaadas.com
 * desc   : 蓝牙协议生成工具类
 */
// TODO: 2021/2/5 记得超过2个字节的都改成小端模式， 除了esn是字符串
public class BleCommandFactory {

    // Control(1byte)+TSN(1byte)+Check(1byte)+Cmd(1byte)+payload(16byte)

    // Control bit0：加密标志位
    //     =0：代表Payload没有进行加密处理。
    //     =1：代表Payload进行了加密处理。
    //     bit1-7：保留

    // TSN: 传输序号（不能为0），每次发送时加1，命令和确认的TSN相同。

    // Check: Payload区域累加和的低字节。

    // Cmd：命令

    // Payload：载荷
    // 不足16个字节补0x00;
    // 采用AES128-ECB加密(除确认帧和心跳帧外);

    private static final byte CONTROL_ENCRYPTION = 0x01;
    private static final byte CONTROL_NORMAL = 0x00;

    private static byte sCommandTSN = 0x01;

    public static byte commandTSN() {
        // -1是byte的最大值
        if(sCommandTSN == -1) {
            sCommandTSN = 0x01;
        }
        return sCommandTSN++;
    }

    public static byte getCommandTSN() {
        return sCommandTSN;
    }

    public static void test() {
        // 测试TSN数据的递增是否存在问题
        for (int i=0; i < 1000; i++) {
            Timber.d("sCommandTSN : %1s", String.valueOf(commandTSN()));
        }
    }

    public static byte[] getPwd(byte[] pwd1, byte[] pwd2) {
        byte[] pwd = new byte[16];
        for (int i=0; i < pwd.length; i++) {
            if(i <= 11) {
                pwd[i]=pwd1[i];
            } else {
                pwd[i]=pwd2[i-12];
            }
        }
        return pwd;
    }

    public static byte[] littleMode(byte[] bytes) {
        for (int i=0; i<bytes.length/2; i++) {
            byte tmp = bytes[i];
            bytes[i] = bytes[bytes.length-1-i];
            bytes[bytes.length-1-i] = tmp;
        }
        return bytes;
    }

    /**
     * 简单的校验和
     * @param payload 要校验和的数据
     * @return 校验和
     */
    private static byte checksum(byte[] payload) {
        //加密前把数据的校验和算出来
        byte  checkSum = 0;
        for (byte b : payload) {
            checkSum += b;
        }
        return checkSum;
    }

    /**
     * 只用pwd1进行加密
     * @param needEncryptData 需要加密的数据
     * @param pwd1            加密的key pwd1 不足16位需要补0
     * @param command         把加密后的数据载入到对应的指令
     * @return                发送的完整的指令
     */
    private static byte[] pwd1Encrypt(byte[] needEncryptData, byte[] pwd1, byte[] command) {
        if(pwd1 == null) {
            return new byte[20];
        }
        if(pwd1.length != 16) {
            Timber.e("pwd1Encrypt pwd1 key的长度错误，请检查输入的数据 size: %1d", pwd1.length);
            return new byte[20];
        }
        byte[] payload = EncryptUtils.encryptAES(needEncryptData, pwd1, "AES/ECB/NoPadding", null);
        Timber.d("pwd1Encrypt 加密之前的数据：%1s\n pwd1：%2s\n 加密后的数据：%3s",
                ConvertUtils.bytes2HexString(needEncryptData), ConvertUtils.bytes2HexString(pwd1), ConvertUtils.bytes2HexString(payload));
        System.arraycopy(payload, 0, command, 4, payload.length);
        return command;
    }

    /**
     * 需要用pwd1和pwd2进行加密
     * @param needEncryptData  需要加密的数据
     * @param pwd1             加密的key pwd1 不足16位需要补0
     * @param pwd2Or3          加密的key pwd2或者pwd3，与pwd1结合生成pwd
     * @param command          把加密后的数据载入到对应的指令
     * @return                 发送的完整的指令,出错返回全是00的数据
     */
    private static byte[] pwdEncrypt(byte[] needEncryptData, byte[] pwd1, byte[] pwd2Or3, byte[] command) {
        if(pwd1.length != 16) {
            Timber.e("pwdEncrypt pwd1 key的长度错误，请检查输入的数据 size: %1d", pwd1.length);
            return new byte[20];
        }
        if(pwd2Or3.length != 4) {
            Timber.e("pwdEncrypt pwd2 key的长度错误，请检查输入的数据 size: %1d", pwd2Or3.length);
            return new byte[20];
        }
        byte[] pwd = new byte[16];
        for (int i=0; i < pwd.length; i++) {
            if(i <= 11) {
                pwd[i]=pwd1[i];
            } else {
                pwd[i]=pwd2Or3[i-12];
            }
        }
        command[2] = checksum(needEncryptData);
        byte[] payload = EncryptUtils.encryptAES(needEncryptData, pwd, "AES/ECB/NoPadding", null);
        Timber.d("pwdEncrypt 加密之前的数据：%1s\n pwd1：%2s\n pwd2: %3s\n pwd: %4s\n 加密后的数据：%5s",
                ConvertUtils.bytes2HexString(needEncryptData), ConvertUtils.bytes2HexString(pwd1),
                ConvertUtils.bytes2HexString(pwd2Or3), ConvertUtils.bytes2HexString(pwd),
                ConvertUtils.bytes2HexString(payload));
        System.arraycopy(payload, 0, command, 4, payload.length);
        return command;
    }

    /**
     * 命令封装
     * @param isEncrypt  是否加密
     * @param cmd        指令
     * @param tsn        传输序号（不能为0），每次发送时加1，命令和确认的TSN相同
     * @param payload    数据
     * @param pwd1       密码1，isEncrypt==false时，可以输入null
     * @param pwd2Or3    密码2或3, 可以输入null
     * @return  满足20个字节单条指令
     */
    public static byte[] commandPackage(boolean isEncrypt, byte cmd, byte tsn, byte[] payload,
                                         byte[] pwd1, byte[] pwd2Or3) {
        byte[] command = new byte[20];
        command[0] = isEncrypt?CONTROL_ENCRYPTION:CONTROL_NORMAL;
        command[1] = tsn;
        command[2] = payload==null?0x00:checksum(payload);
        command[3] = cmd;
        Timber.d("是否加密: %1b, TSN: %2d, CMD: %3s", isEncrypt, tsn, ConvertUtils.int2HexString(cmd));
        // 数据必须是16位来用于加密
        byte[] data = new byte[16];
        // 如果payload传入的数据为null，代表没有载入数据，只是直接发送指令
        if(payload != null) {
            System.arraycopy(payload, 0, data, 0, payload.length);
        }
        if(isEncrypt) {
            return ((pwd2Or3==null?pwd1Encrypt(data, pwd1, command):pwdEncrypt(data, pwd1, pwd2Or3, command)));
        } else {
            System.arraycopy(data, 0, command, 4, data.length);
            return command;
        }
    }

    /**
     * 配对
     * @param pwd1       需要使用pwd1加密
     * @param esn        需要加密esn
     */
    public static byte[] pairCommand(byte[] pwd1, byte[] esn) {
        return commandPackage(true, (byte) 0x1b, commandTSN(), esn, pwd1, null);
    }

    /**
     *  鉴权
     * @param pwd1       需要使用pwd1加密
     * @param pwd2       需要使用pwd2加密，pwd1和pwd2拼接一起来加密
     * @param esn        需要加密esn
     */
    public static byte[] authCommand(byte[] pwd1, byte[] pwd2, byte[] esn) {
        return commandPackage(true, (byte) 0x01, commandTSN(), esn, pwd1, pwd2);
    }

    /**
     * 锁控制
     * @param action     动作
     *                   0x00：UnLock
     *                   0x01：Lock
     *                   0x02：Toggle
     *                   0x03: APP在线解绑门锁
     *                   0x04~0xFF：保留
     * @param codeType   密钥类型
     *                   0x00：保留
     *                   0x01：PIN密码
     *                   0x02：RFID卡片
     *                   0x04：APP开锁（锁不鉴权）
     * @param userId     用户编号
     *                   0x01	BLE自动开锁
     * @param pwdLength  密码长度
     *                   6~12  PIN Code
     *                   4~10  RFID Code
     * @param code      密钥
     *                  Set PIN Code时为ASCII码，如123456为0x313233343536
     */
    public static byte[] lockControlCommand(@BleCommandState.OpenOrClose int action, byte codeType, byte userId,
                                            byte pwdLength, byte[] code, byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[16];
        data[0] = (byte) action;
        data[1] = codeType;
        data[2] = userId;
        data[3] = pwdLength;
        System.arraycopy(code, 0, data, 4, code.length);
        return commandPackage(true, (byte) 0x02, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 锁控制
     * @param action     动作
     *                   0x00：UnLock
     *                   0x01：Lock
     *                   0x02：Toggle
     *                   0x03: APP在线解绑门锁
     *                   0x04~0xFF：保留
     * @param codeType   密钥类型
     *                   0x00：保留
     *                   0x01：PIN密码
     *                   0x02：RFID卡片
     *                   0x04：APP开锁（锁不鉴权）
     * @param userId     用户编号
     *                   0x01	BLE自动开锁
     */
    public static byte[] lockControlCommand(@BleCommandState.OpenOrClose int action, byte codeType, byte userId, byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[4];
        data[0] = (byte) action;
        data[1] = codeType;
        data[2] = userId;
        data[3] = 0x00;
        return commandPackage(true, (byte) 0x02, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 密钥管理
     * @param action       动作
     *                     0x00：保留
     *                     0x01：Set Code设置密钥
     *                     0x02：Get Code查询密钥
     *                     0x03：Clear Code删除密钥
     *                     0x04：Check Code验证密钥
     * @param codeType     密钥类型
     *                     0x00：保留
     *                     0x01：PIN密码（Set\Get\Clear）
     *                     0x02：指纹（Set\Get\Clear）
     *                     0x03：RFID卡片（Set\Get\Clear）
     *                     0x04：管理员密码（Set\Check）
     * @param userId       用户编号
     *                     0~9/19  Code Type为PIN时
     *                     0~99  Code Type为指纹时
     *                     0~99  Code Type为RFID时
     *                     0xff  Code Type 为管理员密码时
     *                     0xFF  Action为Clear时删除所有
     * @param keyLength    密钥长度
     *                     6~12/4~10  Set PIN Code
     *                     4~10  Set RFID Code
     *                     0     Get/Clear Code
     * @param code         密钥
     *                     Set PIN Code时为ASCII码，如123456为0x313233343536
     */
    public static byte[] keyManagementCommand(byte action, byte codeType, byte userId,
                                              byte keyLength, byte[] code, byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[16];
        data[0] = action;
        data[1] = codeType;
        data[2] = userId;
        data[3] = keyLength;
        System.arraycopy(code, 0, data, 4, code.length);
        return commandPackage(true, (byte) 0x03, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 锁开锁记录查询
     * LogIndex Start的值为查询日志起始序号。
     * LogIndex End必须大于等于LogIndex Start，当只读取一条记录时LogIndex start等于LogIndex End。
     * 如果LogIndex Start大于LogTotal则直接返回确认帧。
     * 注意：锁最多存储最近的20条/组*10组共200条记录，当前BLE锁记录查询只支持单条、单组和所有记录查询。
     * 单条：LogIndex Start = LogIndex End，取值：0~199；
     * 单组：LogIndexStart,LogIndex End取值：{[0,20],[20,40],……,[180,200]}；
     * 全部：LogIndexStart,LogIndex End取值：[1,200]。
     * @param logIndexStart  日志起始序号
     * @param logIndexEnd    日志结束序号
     */
    public static byte[] openLockRecordCheckCommand(byte logIndexStart, byte logIndexEnd,
                                                    byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[2];
        data[0] = logIndexStart;
        data[1] = logIndexEnd;
        return commandPackage(true, (byte) 0x04, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * ack确认回复帧
     * @param lockReportTSN 和锁记录上报帧TSN一致。
     * @param cmd           和锁记录上报帧cmd一致。
     * @param status        状态
     *                      0x00	成功
     *                      0x01	失败
     *                      0x7E    无授权
     *                      0x7F    保留区域没有置零
     *                      0x80    异常命令
     *                      0x81    不支持该命令，不支持的命令必须返回该值
     *                      0x82    不使用
     *                      0x83    不使用
     *                      0x84    不使用
     *                      0x85    某个字段错误
     *                      0x86    不支持的属性
     *                      0x87    超出范围或者设置为保留值，或者序号已存在
     *                      0x88    属性为只读
     *                      0x89    操作空间不够
     *                      0x8A    重复存在
     *                      0x8B    被请求的数据没有找到，不支持的设置项必须返回该值
     *                      0x8C    不使用
     *                      0x8D    数据类型错误
     *                      0x8E    不使用
     *                      0x8F    只写
     *                      0x90    不使用
     *                      0x91    不使用
     *                      0x92    不使用
     *                      0x93    权限不够
     *                      0x94    超时，模块发送命令给锁，锁没有接收
     *                      0x95    客户端或服务端退出升级程序
     *                      0x96    错误的Image文件
     *                      0x97    Server does not have data block available yet.
     *                      0x98    没有可用的OTA Image文件
     *                      0x99    客户端需要更多的OTA文件
     *                      0x9A    命令已经接收正在处理
     *                      0xC0    硬件原因造成错误
     *                      0xC1    软件原因造成错误
     *                      0xC2    校验过程出现错误
     *                      0xC3    不使用
     *                      0xC4    反锁
     *                      0xC5    安全模式
     *                      0xD1    子模块版本相同
     *                      0xD2    获取版本超时
     *                      0xD3    升级方式错误
     *                      0xD4    升级方式错误
     *                      0xD5    配置WIFI失败
     *                      0xFF    锁已经接收到命令，但超时时间内没有处理结果返回
     */
    public static byte[] ackCommand(byte lockReportTSN, byte status, byte cmd) {
        byte[] data = new byte[1];
        data[0] = status;
        return commandPackage(false, cmd, lockReportTSN, data, null, null);
    }

    public static byte[] ackCommand(int lockReportTSN, byte status, int cmd) {
        byte[] data = new byte[1];
        data[0] = status;
        return commandPackage(false, (byte) cmd, (byte) lockReportTSN, data, null, null);
    }

    /**
     * 锁参数修改
     * @param num        参数
     *                   0x01 语言 Language
     *                   0x02 音量 SoundVolume
     *                   0x03 时间 TimeSeconds
     *                   0x04 自动关门 AutoLock
     *                   0x05 反锁
     *                   0x06 离家模式/布防VacationSwitch
     *                   0x07 蓝牙开关 BluetoothSwitch
     *                   0x08 安全模式SecuritySwitch
     *                   0x09 红外模式（Alfred）Infrared
     *                   0x0A 过道模式
     *                   0x0B 设置门磁开关
     *                   0x0C 设置门磁状态
     *                   0x81 锁事件上报切换 LockEventNotificationSwitch
     * @param length     参数长度
     *                   语言                  2
     *                   音量                  1
     *                   时间                  4
     *                   自动关门              1
     *                   反锁                  1
     *                   离家模式/布防         1
     *                   蓝牙开关              1
     *                   安全模式              1
     *                   红外模式              1
     *                   过道模式              1
     *                   设置门磁开关          1
     *                   设置门磁状态          1
     *                   锁事件上报切换        1
     * @param value      参数值
     *                   语言
     *                      ISO 639-1标准
     *                      zh：中文
     *                      en：英语
     *                   音量
     *                      0x00：Silent Mode静音
     *                      0x01：Low Volume低音量
     *                      0x02：High Volume高音量
     *                      0x03~0xFF：保留
     *                   时间          时间秒计数。以2000-01-01 00:00:00（本地时间）为起点开始计数
     *                   自动关门
     *                      0x00：开启
     *                      0x01：关闭
     *                   反锁
     *                      0x00：关闭反锁
     *                      0x01：开启反锁
     *                   离家模式/布防
     *                      0x00：开启
     *                      0x01：关闭
     *                   蓝牙开关
     *                       0x00：开启
     *                       0x01：关闭
     *                   安全模式
     *                       0x00：关闭（正常模式）
     *                       0x01：开启
     *                   红外模式
     *                       0x00: 开启
     *                       0x01: 关闭
     *                   过道模式
     *                       0x00：关闭
     *                       0x01：开启
     *                   设置门磁开关
     *                        0x00：关闭
     *                        0x01：开启
     *                   设置门磁状态
     *                       0x00：关门
     *                       0x01：开门
     *                       0x02：虚掩
     *                   锁事件上报切换
     *                       0x00：锁事件上报命令采用0x05，锁报警上报命令采用0x07
     *                       0x01：锁事件上报命令采用0x1A（Alfred）
     */
    public static byte[] lockParameterModificationCommand(byte num, byte length, byte[] value,
                                                          byte[] pwd1, byte[] pwd3) {
        byte[] data = new byte[16];
        data[0] = num;
        data[1] = length;
        System.arraycopy(value, 0, data, 2, value.length);
        return commandPackage(true, (byte) CMD_LOCK_PARAMETER_CHANGED, commandTSN(), data, pwd1, pwd3);
    }

    /**
     * 用户类型设置
     * @param codeType 密钥类型
     *                 0x00：保留
     *                 0x01：PIN密码
     *                 0x02：指纹
     *                 0x03：RFID卡片
     * @param userId   用户编号
     *                 0~9/19   Code Type为PIN时
     *                 0~99  Code Type为指纹时
     *                 0~99  Code Type为RFID时
     * @param userType 用户类型
     *                 0x00 默认（永久）
     *                 0x01 时间表用户
     *                 0x02 胁迫
     *                 0x03 管理员
     *                 0x04 无权限用户（查询权限）
     *                 0xFD 访客密码
     *                 0xFE 一次性密码
     */
    public static byte[] userTypeSettingCommand(byte codeType, byte userId, byte userType,
                                                byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[3];
        data[0] = codeType;
        data[1] = userId;
        data[2] = userType;
        return commandPackage(true, (byte) 0x09, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 用户类型查询
     * @param codeType 密钥类型
     *                 0x00：保留
     *                 0x01：PIN密码
     *                 0x02：指纹
     *                 0x03：RFID卡片
     * @param userId   用户编号
     *                 0~9   Code Type为PIN时
     *                 0~99  Code Type为RFID时
     */
    public static byte[] userTypeCheckCommand(byte codeType, byte userId, byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[2];
        data[0] = codeType;
        data[1] = userId;
        return commandPackage(true, (byte) 0x0A, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 周计划设置
     * @param scheduleID 计划编号
     * @param codeType   密钥类型
     *                   0x00：保留
     *                   0x01：PIN密码
     *                   0x03：RFID卡片
     * @param userId     用户编号
     *                   0~9   Code Type为PIN时
     *                   0~99  Code Type为RFID时
     * @param daysMask   日掩码
     *                   BIT：  7      6    5    4    3    2    1    0
     *                   星期：保留六五四三二一日
     * @param startHour  起始小时
     *                   十进制格式，0x00-0x17（00到23时）
     * @param startMin   起始分钟
     *                   十进制格式，0x00-0x3B（00到59分）
     * @param endHour    结束小时
     *                   十进制格式，0x00-0x17（00到23时）。结束小时必须大于等于起始小时。
     * @param endMin     结束分钟
     *                   十进制格式，0x00-0x3B（00到59分）。在结束小时等于起始小时时，结束分钟必须大于起始分钟。
     */
    public static byte[] weeklyPlanSettingCommand(byte scheduleID, byte codeType, byte userId,
                                           byte daysMask, byte startHour, byte startMin,
                                           byte endHour, byte endMin, byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[8];
        data[0] = scheduleID;
        data[1] = codeType;
        data[2] = userId;
        data[3] = daysMask;
        data[4] = startHour;
        data[5] = startMin;
        data[6] = endHour;
        data[7] = endMin;
        return commandPackage(true, (byte) 0x0B, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 周计划查询
     * @param scheduleID 计划编号
     */
    public static byte[] weeklyPlanCheckCommand(byte scheduleID, byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[1];
        data[0] = scheduleID;
        return commandPackage(true, (byte) 0x0C, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 周计划删除
     * @param scheduleID 计划编号
     */
    public static byte[] weeklyPlanDeleteCommand(byte scheduleID, byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[1];
        data[0] = scheduleID;
        return commandPackage(true, (byte) 0x0D, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 年月日计划设置
     * @param scheduleID 计划编号
     * @param userId     用户编号
     * @param codeType   密钥类型
     *                   0x00：保留
     *                   0x01：PIN密码
     *                   0x03：RFID卡片
     * @param startTime  起始时间
     *                   以2000.1.1 00:00:00为起始时间的秒计数
     * @param endTime    结束时间
     *                   结束时间必须大于起始时间
     * @return 值可能是null
     */
    public static byte[] yearMonthDaySettingCommand(byte scheduleID, byte userId, byte codeType, byte[] startTime,
                                                    byte[] endTime,  byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[11];
        data[0] = scheduleID;
        data[1] = userId;
        data[2] = codeType;
        if(startTime.length != 4 || endTime.length != 4) {
            Timber.e("yearMonthDaySettingCommand 传入的时间字节流长度不对 startTime.length %1d,endTime.length %2d",
                    startTime.length, endTime.length);
            return null;
        }
        System.arraycopy(startTime, 0, data, 3, startTime.length);
        System.arraycopy(endTime, 0, data, 7, endTime.length);
        return commandPackage(true, (byte) 0x0E, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 年月日计划查询
     * @param scheduleID 计划编号
     */
    public static byte[] yearMonthDayCheckCommand(byte scheduleID, byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[1];
        data[0] = scheduleID;
        return commandPackage(true, (byte) 0x0F, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 年月日计划删除
     * @param scheduleID 计划编号
     */
    public static byte[] yearMonthDayDeleteCommand(byte scheduleID, byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[1];
        data[0] = scheduleID;
        return commandPackage(true, (byte) 0x10, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 同步门锁密钥状态
     * @param codeType 密钥类型
     *                 0x00：保留
     *                 0x01：PIN密码
     *                 0x02：指纹
     *                 0x03：RFID卡片
     *                 0x04：保留（管理员密码）
     */
    public static byte[] synchronizeLockKeyStatusCommand(byte codeType, byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[1];
        data[0] = codeType;
        return commandPackage(true, (byte) 0x11, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 查询门锁基本信息
     * @param mode 0x01：查询
     */
    public static byte[] checkLockBaseInfoCommand(byte mode, byte[] pwd1, byte[] pwd3) {
        byte[] data = new byte[1];
        data[0] = mode;
        return commandPackage(true, (byte) CMD_LOCK_INFO, commandTSN(), data, pwd1, pwd3);
    }

    public static byte[] checkLockBaseInfoCommand(byte[] pwd1, byte[] pwd3) {
        return checkLockBaseInfoCommand((byte) 0x01, pwd1, pwd3);
    }

    /**
     * App绑定请求帧
     * @param pwdLen    管理密码长度 1位
     * @param managePwd 管理密码  4~10位长度
     * @param random    随机数    11~5位长度
     * @return 值可能为null
     */
    public static byte[] appBindRequestCommand(byte pwdLen, byte[] managePwd, byte[] random,
                                               byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[16];
        data[0] = pwdLen;
        if(managePwd.length < 4 || managePwd.length > 10) {
            Timber.e("appBindRequestCommand managePwd 长度不对 length: %1d", managePwd.length);
            return null;
        }
        if(random.length > 11 || random.length < 5) {
            Timber.e("appBindRequestCommand random 长度不对 length: %1d", random.length);
            return null;
        }
        if(random.length+managePwd.length != 15) {
            Timber.e("appBindRequestCommand random.length+managePwd.length=%1d", random.length+managePwd.length);
            return null;
        }
        System.arraycopy(managePwd, 0, data, 1, managePwd.length);
        System.arraycopy(random, 0, data, managePwd.length+1, random.length);
        return commandPackage(true, (byte) 0x13, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 锁报警记录查询
     * LogIndex End必须大于等于LogIndex Start，当只读取一条记录时LogIndex start等于LogIndex End。
     * 如果LogIndex Start大于LogTotal则直接返回确认帧。
     * 注意：锁最多存储最近的20条/组*10组共200条记录，当前BLE锁记录查询只支持单条、单组和所有记录查询。
     * 单条：LogIndex Start = LogIndex End，取值：0~199；
     * 单组：LogIndexStart,LogIndex End取值：{[0,20],[20,40],……,[180,200]}；
     * 全部：LogIndexStart,LogIndex End取值：[1,200]
     * @param logIndexStart  查询的报警记录起始序号
     * @param logIndexEnd    查询的报警记录结束序号
     */
    public static byte[] checkLockAlertRecordCommand(byte logIndexStart, byte logIndexEnd,
                                                     byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[2];
        data[0] = logIndexStart;
        data[1] = logIndexEnd;
        return commandPackage(true, (byte) CMD_LOCK_ALARM_RECORD_CHECK, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 锁序列号查询
     */
    public static byte[] lockSnCheckCommand() {
        return commandPackage(true, (byte) CMD_LOCK_NUM_CHECK, commandTSN(), null, null, null);
    }

    /**
     * 锁开锁次数查询
     */
    public static byte[] lockOpenCountCheckCommand() {
        return commandPackage(true, (byte) CMD_LOCK_OPEN_COUNT_CHECK, commandTSN(), null, null, null);
    }

    /**
     * 锁参数主动查询
     * @param num 序号
     *            表2-5454.锁参数
     */
    public static byte[] lockParameterCheckCommand(byte num, byte[] pwd1, byte[] pwd2) {
        byte[] data = new byte[1];
        data[0] = num;
        return commandPackage(true, (byte) CMD_LOCK_PARAMETER_CHECK, commandTSN(), data, pwd1, pwd2);
    }

    /**
     * 锁操作记录
     * LogIndex End必须大于等于LogIndex Start，当只读取一条记录时LogIndex start等于LogIndex End。
     * 如果LogIndex Start大于LogTotal则直接返回确认帧。
     * 注意：该条指令与之前的锁开锁记录查询、锁报警记录查询不同，不支持组读取，所以Log Index Start与Log Index End的定义不同。
     * 定义[Log Index Start, Log Index End]为全闭区间，如下例子：
     * [1,1] 读取单条，返回第1条记录；
     * [1,2] 读取两条，返回第1和第2条记录；
     * [0,19] 读取20条，返回第0 1 2…..19条；
     * [20,40] 读取21条，返回第20 21 22…..40条；
     * @param option         选项
     *                       0x1 查询记录
     * @param logType       记录类型
     *                      0x0 开门记录
     *                      0x01 报警记录
     *                      0x02 秘钥操作记录
     *                      0x03 混合记录
     * @param logIndexStart 查询的记录起始序号
     * @param logIndexEnd   查询的记录结束序号
     */
    public static byte[] lockOperateRecordCommand(@BleCommandState.LockRecordOpOp int option,
                                                  @BleCommandState.LockRecordOpLogType int logType,
                                                  byte[] logIndexStart, byte[] logIndexEnd,
                                                  byte[] pwd1, byte[] pwd3) {
        byte[] data = new byte[6];
        data[0] = (byte) option;
        data[1] = (byte) logType;
        byte[] realLogIndexStart = littleMode(logIndexStart);
        byte[] realLogIndexEnd = littleMode(logIndexEnd);
        System.arraycopy(realLogIndexStart, 0, data, 2, realLogIndexStart.length);
        System.arraycopy(realLogIndexEnd, 0, data, 4, realLogIndexEnd.length);
        return commandPackage(true, (byte) 0x18, commandTSN(), data, pwd1, pwd3);
    }

    public static byte[] lockOperateRecordCommand(byte[] logIndexStart, byte[] logIndexEnd,
                                                  byte[] pwd1, byte[] pwd3) {
        return lockOperateRecordCommand(BleCommandState.LOCK_RECORD_OP_OP_CHECK,
                BleCommandState.LOCK_RECORD_OP_LOG_TYPE_ALL, logIndexStart, logIndexEnd, pwd1, pwd3);
    }

    private static byte sHeartBeatTSN = 0x01;

    private static byte heartBeatTSN()  {
        // -1是byte的最大值
        if(sHeartBeatTSN == -1) {
            sHeartBeatTSN = 0x01;
        }
        return sHeartBeatTSN++;
    }

    /**
     * 密钥属性设置
     * @param option         1：添加/修改  2：删除
     * @param keyType        0：密码  4：指纹  3：卡片  7：人脸
     * @param keyNum         密钥编号
     * @param attribute      永久密钥：00
     *                       时间策略密钥：01
     *                       胁迫密钥：02
     *                       管理员密钥：03
     *                       无权限密钥：04
     *                       周策略密钥：05
     *                       一次性密钥：FE
     *                       Option=2 (删除时这个字段无用)
     * @param week           周策略 BIT:    7   6   5   4   3   2   1   0
     *                       星期：保留    六  五  四  三  二  一  日
     *                       如果为时间计划策略，置0
     *                       Option=2 (删除时这个字段无用)
     * @param startTime      起始时间（UTC）
     *                       Option=2 (删除这个字段无用)
     * @param endTime        结束时间（UTC）
     *                       Option=2 (删除时这个字段无用)
     */
    public static byte[] keyAttributesSet(@BleCommandState.KeySetKeyOption int option,
                                          @BleCommandState.KeySetKeyType int keyType,
                                          byte keyNum,
                                          @BleCommandState.KeySetAttribute int attribute,
                                          byte week, long startTime, long endTime,
                                          byte[] pwd1, byte[] pwd3) {
        byte[] data = new byte[13];
        data[0] = (byte) option;
        data[1] = (byte) keyType;
        data[2] = keyNum;
        data[3] = (byte) attribute;
        data[4] = week;
        byte[] realStartTime = littleMode(BleByteUtil.longToUnsigned32Bytes(startTime));
        byte[] realEndTime = littleMode(BleByteUtil.longToUnsigned32Bytes(endTime));
        System.arraycopy(realStartTime, 0, data, 5, 4);
        System.arraycopy(realEndTime, 0, data, 9, 4);
        return commandPackage(true, (byte) CMD_KEY_ATTRIBUTES_SET, commandTSN(), data, pwd1, pwd3);
    }


    /**
     * 密钥属性读
     * @param keyType   0：密码  4：指纹  3：卡片  7：人脸
     * @param num       密钥编号
     */
    public static byte[] keyAttributesRead(@BleCommandState.KeySetKeyType int keyType,
                                           byte num, byte[] pwd1, byte[] pwd3) {
        byte[] data = new byte[2];
        data[0] = (byte) keyType;
        data[1] = num;
        return commandPackage(true, (byte) CMD_KEY_ATTRIBUTES_READ, commandTSN(), data, pwd1, pwd3);
    }

    /**
     * 添加密钥并返回密钥编号
     * @param keyType 0：密码  4：指纹  3：卡片  7：人脸
     * @param key     密钥内容
     */
    public static byte[] addKey(@BleCommandState.KeySetKeyType int keyType, byte[] key,
                                byte[] pwd1, byte[] pwd3) {

        byte[] data =  new byte[key.length+2];
        data[0] = (byte) keyType;
        data[1] = (byte) key.length;
        System.arraycopy(key, 0, data,  2, key.length);
        return commandPackage(true, (byte) CMD_KEY_ADD, commandTSN(), data, pwd1, pwd3);
    }

    /**
     * 门磁校准
     * @param doorState 1:门状态开 2：们状态关 3：们状态虚掩 4：启动门磁 5：禁用门磁
     */
    public static byte[] doorCalibration(@BleCommandState.DoorCalibrationState int doorState,
                                         byte[] pwd1, byte[] pwd3) {
        byte[] data = new byte[1];
        data[0] = (byte) doorState;
        return commandPackage(true, (byte) 0x1F, commandTSN(), data, pwd1, pwd3);
    }

    /**
     * 敲门开锁灵敏度
     * @param sensitivity 1:灵敏度低  2：灵敏度中  3：灵敏度高
     */
    public static byte[] setSensitivity(@BleCommandState.KnockDoorSensitivity int sensitivity, byte[] pwd1, byte[] pwd3) {
        byte[] data = new byte[1];
        data[0] = (byte) sensitivity;
        return commandPackage(true, (byte) CMD_SET_SENSITIVITY, commandTSN(), data, pwd1, pwd3);
    }


    /**
     *  设置关门自动上锁时间
     * @param time 自动上锁时间
     */
    public static byte[] setAutoLockTime(int time, byte[] pwd1, byte[] pwd3) {
        return commandPackage(true, (byte) 0x21, commandTSN(), littleMode(BleByteUtil.int2BytesArray(time)), pwd1, pwd3);
    }

    /**
     *   敲门开锁指令
     * @param option   1：敲门开锁
     * @param time    格林威治时间 秒，不要毫秒
     */
    public static byte[] setKnockDoorAndUnlockTime(int option, long time, byte[] pwd1, byte[] pwd3) {
        byte[] data = new byte[5];
        data[0] = (byte) option;
        byte[] timeBytes = littleMode(BleByteUtil.longToUnsigned32Bytes(time));
        System.arraycopy(timeBytes, 0, data, 1, 4);
        return commandPackage(true, (byte) CMD_KNOCK_DOOR_AND_UNLOCK_TIME, commandTSN(), data, pwd1, pwd3);
    }

    /**
     * 与锁同步时间
     * @param time 手机当前的时间
     */
    public static byte[] syLockTime(long time, byte[] pwd1, byte[] pwd3) {
        byte[] realTime = littleMode(BleByteUtil.longToUnsigned32Bytes(time));
        return commandPackage(true, (byte) 0x23, commandTSN(), realTime, pwd1, pwd3);
    }


    /**
     *  读取混合记录
     * @param logIndexStart   查询的记录起始序号 2个字节
     * @param logIndexEnd     结束编号（当start index =end index时 读取一条记录
     *                        一次最多读100条 如范围0-99    第一条记录的编号为0）
     *                        2个字节
     */
    public static byte[] readAllRecordFromBigEndian(byte[] logIndexStart, byte[] logIndexEnd,
                                                    byte[] pwd1, byte[] pwd3) {
        byte[] data = new byte[4];
        data[0] = logIndexStart[1];
        data[1] = logIndexStart[0];
        data[2] = logIndexEnd[1];
        data[3] = logIndexEnd[0];
        return commandPackage(true, (byte) CMD_GET_ALL_RECORD, commandTSN(), data, pwd1, pwd3);
    }

    /**
     *  读取混合记录
     * @param logIndexStart   查询的记录起始序号 2个字节
     * @param logIndexEnd     结束编号（当start index =end index时 读取一条记录
     *                        一次最多读100条 如范围0-99    第一条记录的编号为0）
     *                        2个字节
     */
    public static byte[] readAllRecordFromSmallEndian(byte[] logIndexStart, byte[] logIndexEnd,
                                                    byte[] pwd1, byte[] pwd3) {
        byte[] data = new byte[4];
        data[0] = logIndexStart[0];
        data[1] = logIndexStart[1];
        data[2] = logIndexEnd[0];
        data[3] = logIndexEnd[1];
        return commandPackage(true, (byte) CMD_GET_ALL_RECORD, commandTSN(), data, pwd1, pwd3);
    }

    /**
     * 胁迫密码开关
     * @param control 1:打开胁迫密码功能   0：关闭胁迫密码功能
     */
    public static byte[] duressPwdSwitch(@BleCommandState.DuressPwdControl int control, byte[] pwd1, byte[] pwd3) {
        byte[] data = new byte[1];
        data[0] = (byte) control;
        return commandPackage(true, (byte) CMD_DURESS_PWD_SWITCH, commandTSN(), data, pwd1, pwd3);
    }

    /**
     * wifi开关
     * @param control 1:打开WIFI功能   0：关闭WIFI功能
     */
    public static byte[] wifiSwitch(@BleCommandState.WifiControl int control, byte[] pwd1, byte[] pwd3) {
        byte[] data = new byte[1];
        data[0] = (byte) control;
        return commandPackage(true, (byte) CMD_WIFI_SWITCH, commandTSN(), data, pwd1, pwd3);
    }

    /**
     * 心跳包
     */
    public static byte[] heartBeat() {
        byte[] data = new byte[1];
        data[0] = (byte) 0xFF;
        return commandPackage(false, (byte) 0xAA, heartBeatTSN(), data, null, null);
    }

    /*---------------------------------- 配网相关指令 --------------------------------*/

    /**
     * APP下发ssID
     * @param ssIDLen     ssID总长度
     * @param ssIDIndex   包序号，第N包（从0开始）
     * @param ssID        原始码，ssID的数据
     */
    public static byte[] sendSSIDCommand(byte ssIDLen, byte ssIDIndex, byte[] ssID) {
        byte[] data = new byte[16];
        data[0] = ssIDLen;
        data[1] = ssIDIndex;
        System.arraycopy(ssID, 0, data, 2, ssID.length);
        return commandPackage(false, (byte) 0x90, commandTSN(), data, null, null);
    }

    /**
     * APP下发密码
     * @param pwdLen    wifi密码总长度
     * @param pwdIndex  包序号，第N包（从0开始）
     * @param pwd       原始码
     */
    public static byte[] sendSSIDPwdCommand(byte pwdLen, byte pwdIndex, byte[] pwd) {
        byte[] data = new byte[16];
        data[0] = pwdLen;
        data[1] = pwdIndex;
        System.arraycopy(pwd, 0, data, 2, pwd.length);
        return commandPackage(false, (byte) 0x91, commandTSN(), data, null, null);
    }

    /**
     * 配网因子响应
     * @param status 状态
     *               0x00 成功
     *               0x01 失败
     */
    public static byte[] pairFactorResponseCommand(byte status) {
        byte[] data = new byte[1];
        data[0] = status;
        return commandPackage(false, (byte) 0x92, commandTSN(), data, null, null);
    }

    /**
     * BLE配网状态上报响应
     * @param status 状态
     *               0x00 成功
     *               0x01 失败
     */
    public static byte[] blePairStatusResponseCommand(byte status) {
        byte[] data = new byte[1];
        data[0] = status;
        return commandPackage(false, (byte) 0x93, commandTSN(), data, null, null);
    }

    /**
     * APP下发秘钥因子校验结果
     * @param value 结果
     *               0x00 成功
     *               0x01 失败
     */
    public static byte[] sendKeyFactorVerifyResultCommand(byte value) {
        byte[] data = new byte[1];
        data[0] = value;
        return commandPackage(false, (byte) 0x94, commandTSN(), data, null, null);
    }

    /**
     * 上报剩余校验次数响应
     * @param value 结果
     *              0x00 成功
     *              0x01 失败
     */
    public static byte[] remainVerifyCountResponseCommand(byte value) {
        byte[] data = new byte[1];
        data[0] = value;
        return commandPackage(false, (byte) 0x95, commandTSN(), data, null, null);
    }

    /**
     * APP下发配网状态
     * @param status 状态
     *               0x00 开始配网
     *               0x01 停止配网
     *               0x02 继续配网
     */
    public static byte[] sendPairStatusCommand(@BleCommandState.PairNetworkStatus int status) {
        byte[] data = new byte[1];
        data[0] = (byte) status;
        return commandPackage(false, (byte) BleProtocolState.CMD_PAIR_NETWORK_ACK, commandTSN(),
                data, null, null);
    }

    /**
     * 上报联网状态响应
     * @param status 状态
     *               0x00 成功
     *               0x01 失败
     */
    public static byte[] sendConnectStatusResponseCommand(byte status) {
        byte[] data = new byte[1];
        data[0] = status;
        return commandPackage(false, (byte) BleProtocolState.CMD_BLE_UPLOAD_PAIR_NETWORK_STATE,
                commandTSN(), data, null, null);
    }

    /**
     * wifi列表查询
     */
    public static byte[] wifiListSearchCommand() {
        return commandPackage(false, (byte) CMD_WIFI_LIST_CHECK, commandTSN(), null, null, null);
    }

}
