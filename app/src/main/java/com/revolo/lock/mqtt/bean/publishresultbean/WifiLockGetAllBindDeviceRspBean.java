package com.revolo.lock.mqtt.bean.publishresultbean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * author :
 * time   : 2021/2/6
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class WifiLockGetAllBindDeviceRspBean {


    /**
     * msgId : 123456
     * msgtype : response
     * userId :
     * gwId :
     * deviceId :
     * func : getAllBindDevice
     * code : 200
     * timestamp : 1556172487814
     * data : {"gwList":[{"deviceSN":"GW01182510163","relayType":0,"deviceNickName":"GW01182510163","adminuid":"5c4fe492dc93897aa7d8600b","adminName":"8618954359822","adminNickname":"8618954359822","isAdmin":1,"shareFlag":1,"meUsername":"17c830b7267a4d208029c217fbb6b7c5","mePwd":"1456dfc75ba34d5191399bcc6473b85a","meBindState":1,"deviceType":"wifi","wifiPwdNum":"05","wifiPwdNickname":"nickName","wifiPwdkey":"523121","deviceList":[{"ipaddr":"192.168.168.235","macaddr":"0C:9A:42:B7:8C:F5","SW":"orangecat-1.3.4","event_str":"offline","device_type":"kdscateye","deviceId":"CH01183910242","shareFlag":1,"pushSwitch":1,"lockversion":"20190716","time":"2019-04-02 09:13:57.481"}]}],"devList":[{"_id":"5c8f5563dc938989e2f5429d","lockName":"BT123456","lockNickName":"BT123456","macLock":"123456","open_purview":"3","is_admin":"1","center_latitude":"0","center_longitude":"0","circle_radius":"0","auto_lock":"0","password1":"123456","password2":"654321","deviceSN":"ZG01191810001","peripheralId":"5c70ac053c554639ea931111","bleVersion":"1","softwareVersion":"1.1.0","model":"","functionSet":"01","keyString":"dfsadgfdsagfdsgsd","localName":"KDS0CB2B73FA663"}],"wifiList":[{"_id":"5de4c32a33cc1949441265ca","wifiSN":"WF132231004","isAdmin":1,"adminUid":"5c4fe492dc93897aa7d8600b","adminName":"8618954359822","productSN":"s10001192910010","productModel":"k8","appId":1,"lockNickname":"wode","lockSoftwareVersion":"22222","functionSet":"00","uid":"5c4fe492dc93897aa7d8600b","uname":"8618954359822","pushSwitch":1,"amMode":1,"safeMode":1,"defences":0,"language":"zh","operatingMode":0,"volume":1,"faceStatus":1,"powerSave":1,"bleVersion":"33333","wifiVersion":"44444","mqttVersion":"55555","faceVersion":"66666","lockFirmwareVersion":"11111","randomCode":"randomCode666","distributionNetwork":1,"wifiName":"wodewifi","power":55,"updateTime":1577176575,"createTime":1577176575,"openStatus":2,"openStatusTime":1541468973,"switch":{"createTime":"154148973342","mac":"112233445566","switchEn":1,"total":1,"switchArray":[{"type":1,"timeEn":0,"startTime":0,"stopTime":0,"week":0,"nickname":"第一个开关昵称"}]}}],"productInfoList":[{"_id":"5d66619c497ecf326f25469b","developmentModel":"1","productModel":"k8","snHead":"WF1","adminUrl":"47.106.94.189/deviceModelFiles/1566990166590/android_admin_xxx.png","deviceListUrl":"47.106.94.189/deviceModelFiles/1566990488685/android_device_list_xxx.png","authUrl":"47.106.94.189/deviceModelFiles/1566990492106/android_auth_xxx.png","adminUrl@1x":"47.106.94.189/deviceModelFiles/1566990495762/ios_admin_xxx@1x.png","deviceListUrl@1x":"47.106.94.189/deviceModelFiles/1566990500347/ios_device_list_xxx@1x.png","authUrl@1x":"47.106.94.189/deviceModelFiles/1566990503571/ios_auth_xxx@1x.png","adminUrl@2x":"47.106.94.189/deviceModelFiles/1566990506856/ios_admin_xxx@2x.png","deviceListUrl@2x":"47.106.94.189/deviceModelFiles/1566990510246/ios_device_list_xxx@2x.png","authUrl@2x":"47.106.94.189/deviceModelFiles/1566990513628/ios_auth_xxx@2x.png","adminUrl@3x":"47.106.94.189/deviceModelFiles/1566990518026/ios_admin_xxx@3x.png","deviceListUrl@3x":"47.106.94.189/deviceModelFiles/1566990522179/ios_device_list_xxx@3x.png","authUrl@3x":"47.106.94.189/deviceModelFiles/1566990530690/ios_auth_xxx@3x.png","createTime":"2019-08-28 19:12:28.060"}]}
     */

    private int msgId;
    private String msgtype;
    private String userId;
    private String gwId;
    private String deviceId;
    private String func;
    private String code;
    private String timestamp;
    private DataBean data;

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGwId() {
        return gwId;
    }

    public void setGwId(String gwId) {
        this.gwId = gwId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFunc() {
        return func;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        private List<GwListBean> gwList;
        private List<DevListBean> devList;
        private List<WifiListBean> wifiList;
        private List<ProductInfoListBean> productInfoList;

        public List<GwListBean> getGwList() {
            return gwList;
        }

        public void setGwList(List<GwListBean> gwList) {
            this.gwList = gwList;
        }

        public List<DevListBean> getDevList() {
            return devList;
        }

        public void setDevList(List<DevListBean> devList) {
            this.devList = devList;
        }

        public List<WifiListBean> getWifiList() {
            return wifiList;
        }

        public void setWifiList(List<WifiListBean> wifiList) {
            this.wifiList = wifiList;
        }

        public List<ProductInfoListBean> getProductInfoList() {
            return productInfoList;
        }

        public void setProductInfoList(List<ProductInfoListBean> productInfoList) {
            this.productInfoList = productInfoList;
        }

        public static class GwListBean {
            /**
             * deviceSN : GW01182510163
             * relayType : 0
             * deviceNickName : GW01182510163
             * adminuid : 5c4fe492dc93897aa7d8600b
             * adminName : 8618954359822
             * adminNickname : 8618954359822
             * isAdmin : 1
             * shareFlag : 1
             * meUsername : 17c830b7267a4d208029c217fbb6b7c5
             * mePwd : 1456dfc75ba34d5191399bcc6473b85a
             * meBindState : 1
             * deviceType : wifi
             * wifiPwdNum : 05
             * wifiPwdNickname : nickName
             * wifiPwdkey : 523121
             * deviceList : [{"ipaddr":"192.168.168.235","macaddr":"0C:9A:42:B7:8C:F5","SW":"orangecat-1.3.4","event_str":"offline","device_type":"kdscateye","deviceId":"CH01183910242","shareFlag":1,"pushSwitch":1,"lockversion":"20190716","time":"2019-04-02 09:13:57.481"}]
             */

            private String deviceSN;
            private int relayType;
            private String deviceNickName;
            private String adminuid;
            private String adminName;
            private String adminNickname;
            private int isAdmin;
            private int shareFlag;
            private String meUsername;
            private String mePwd;
            private int meBindState;
            private String deviceType;
            private String wifiPwdNum;
            private String wifiPwdNickname;
            private String wifiPwdkey;
            private List<DeviceListBean> deviceList;

            public String getDeviceSN() {
                return deviceSN;
            }

            public void setDeviceSN(String deviceSN) {
                this.deviceSN = deviceSN;
            }

            public int getRelayType() {
                return relayType;
            }

            public void setRelayType(int relayType) {
                this.relayType = relayType;
            }

            public String getDeviceNickName() {
                return deviceNickName;
            }

            public void setDeviceNickName(String deviceNickName) {
                this.deviceNickName = deviceNickName;
            }

            public String getAdminuid() {
                return adminuid;
            }

            public void setAdminuid(String adminuid) {
                this.adminuid = adminuid;
            }

            public String getAdminName() {
                return adminName;
            }

            public void setAdminName(String adminName) {
                this.adminName = adminName;
            }

            public String getAdminNickname() {
                return adminNickname;
            }

            public void setAdminNickname(String adminNickname) {
                this.adminNickname = adminNickname;
            }

            public int getIsAdmin() {
                return isAdmin;
            }

            public void setIsAdmin(int isAdmin) {
                this.isAdmin = isAdmin;
            }

            public int getShareFlag() {
                return shareFlag;
            }

            public void setShareFlag(int shareFlag) {
                this.shareFlag = shareFlag;
            }

            public String getMeUsername() {
                return meUsername;
            }

            public void setMeUsername(String meUsername) {
                this.meUsername = meUsername;
            }

            public String getMePwd() {
                return mePwd;
            }

            public void setMePwd(String mePwd) {
                this.mePwd = mePwd;
            }

            public int getMeBindState() {
                return meBindState;
            }

            public void setMeBindState(int meBindState) {
                this.meBindState = meBindState;
            }

            public String getDeviceType() {
                return deviceType;
            }

            public void setDeviceType(String deviceType) {
                this.deviceType = deviceType;
            }

            public String getWifiPwdNum() {
                return wifiPwdNum;
            }

            public void setWifiPwdNum(String wifiPwdNum) {
                this.wifiPwdNum = wifiPwdNum;
            }

            public String getWifiPwdNickname() {
                return wifiPwdNickname;
            }

            public void setWifiPwdNickname(String wifiPwdNickname) {
                this.wifiPwdNickname = wifiPwdNickname;
            }

            public String getWifiPwdkey() {
                return wifiPwdkey;
            }

            public void setWifiPwdkey(String wifiPwdkey) {
                this.wifiPwdkey = wifiPwdkey;
            }

            public List<DeviceListBean> getDeviceList() {
                return deviceList;
            }

            public void setDeviceList(List<DeviceListBean> deviceList) {
                this.deviceList = deviceList;
            }

            public static class DeviceListBean {
                /**
                 * ipaddr : 192.168.168.235
                 * macaddr : 0C:9A:42:B7:8C:F5
                 * SW : orangecat-1.3.4
                 * event_str : offline
                 * device_type : kdscateye
                 * deviceId : CH01183910242
                 * shareFlag : 1
                 * pushSwitch : 1
                 * lockversion : 20190716
                 * time : 2019-04-02 09:13:57.481
                 */

                private String ipaddr;
                private String macaddr;
                private String SW;
                private String event_str;
                private String device_type;
                private String deviceId;
                private int shareFlag;
                private int pushSwitch;
                private String lockversion;
                private String time;

                public String getIpaddr() {
                    return ipaddr;
                }

                public void setIpaddr(String ipaddr) {
                    this.ipaddr = ipaddr;
                }

                public String getMacaddr() {
                    return macaddr;
                }

                public void setMacaddr(String macaddr) {
                    this.macaddr = macaddr;
                }

                public String getSW() {
                    return SW;
                }

                public void setSW(String SW) {
                    this.SW = SW;
                }

                public String getEvent_str() {
                    return event_str;
                }

                public void setEvent_str(String event_str) {
                    this.event_str = event_str;
                }

                public String getDevice_type() {
                    return device_type;
                }

                public void setDevice_type(String device_type) {
                    this.device_type = device_type;
                }

                public String getDeviceId() {
                    return deviceId;
                }

                public void setDeviceId(String deviceId) {
                    this.deviceId = deviceId;
                }

                public int getShareFlag() {
                    return shareFlag;
                }

                public void setShareFlag(int shareFlag) {
                    this.shareFlag = shareFlag;
                }

                public int getPushSwitch() {
                    return pushSwitch;
                }

                public void setPushSwitch(int pushSwitch) {
                    this.pushSwitch = pushSwitch;
                }

                public String getLockversion() {
                    return lockversion;
                }

                public void setLockversion(String lockversion) {
                    this.lockversion = lockversion;
                }

                public String getTime() {
                    return time;
                }

                public void setTime(String time) {
                    this.time = time;
                }
            }
        }

        public static class DevListBean {
            /**
             * _id : 5c8f5563dc938989e2f5429d
             * lockName : BT123456
             * lockNickName : BT123456
             * macLock : 123456
             * open_purview : 3
             * is_admin : 1
             * center_latitude : 0
             * center_longitude : 0
             * circle_radius : 0
             * auto_lock : 0
             * password1 : 123456
             * password2 : 654321
             * deviceSN : ZG01191810001
             * peripheralId : 5c70ac053c554639ea931111
             * bleVersion : 1
             * softwareVersion : 1.1.0
             * model :
             * functionSet : 01
             * keyString : dfsadgfdsagfdsgsd
             * localName : KDS0CB2B73FA663
             */

            private String _id;
            private String lockName;
            private String lockNickName;
            private String macLock;
            private String open_purview;
            private String is_admin;
            private String center_latitude;
            private String center_longitude;
            private String circle_radius;
            private String auto_lock;
            private String password1;
            private String password2;
            private String deviceSN;
            private String peripheralId;
            private String bleVersion;
            private String softwareVersion;
            private String model;
            private String functionSet;
            private String keyString;
            private String localName;

            public String get_id() {
                return _id;
            }

            public void set_id(String _id) {
                this._id = _id;
            }

            public String getLockName() {
                return lockName;
            }

            public void setLockName(String lockName) {
                this.lockName = lockName;
            }

            public String getLockNickName() {
                return lockNickName;
            }

            public void setLockNickName(String lockNickName) {
                this.lockNickName = lockNickName;
            }

            public String getMacLock() {
                return macLock;
            }

            public void setMacLock(String macLock) {
                this.macLock = macLock;
            }

            public String getOpen_purview() {
                return open_purview;
            }

            public void setOpen_purview(String open_purview) {
                this.open_purview = open_purview;
            }

            public String getIs_admin() {
                return is_admin;
            }

            public void setIs_admin(String is_admin) {
                this.is_admin = is_admin;
            }

            public String getCenter_latitude() {
                return center_latitude;
            }

            public void setCenter_latitude(String center_latitude) {
                this.center_latitude = center_latitude;
            }

            public String getCenter_longitude() {
                return center_longitude;
            }

            public void setCenter_longitude(String center_longitude) {
                this.center_longitude = center_longitude;
            }

            public String getCircle_radius() {
                return circle_radius;
            }

            public void setCircle_radius(String circle_radius) {
                this.circle_radius = circle_radius;
            }

            public String getAuto_lock() {
                return auto_lock;
            }

            public void setAuto_lock(String auto_lock) {
                this.auto_lock = auto_lock;
            }

            public String getPassword1() {
                return password1;
            }

            public void setPassword1(String password1) {
                this.password1 = password1;
            }

            public String getPassword2() {
                return password2;
            }

            public void setPassword2(String password2) {
                this.password2 = password2;
            }

            public String getDeviceSN() {
                return deviceSN;
            }

            public void setDeviceSN(String deviceSN) {
                this.deviceSN = deviceSN;
            }

            public String getPeripheralId() {
                return peripheralId;
            }

            public void setPeripheralId(String peripheralId) {
                this.peripheralId = peripheralId;
            }

            public String getBleVersion() {
                return bleVersion;
            }

            public void setBleVersion(String bleVersion) {
                this.bleVersion = bleVersion;
            }

            public String getSoftwareVersion() {
                return softwareVersion;
            }

            public void setSoftwareVersion(String softwareVersion) {
                this.softwareVersion = softwareVersion;
            }

            public String getModel() {
                return model;
            }

            public void setModel(String model) {
                this.model = model;
            }

            public String getFunctionSet() {
                return functionSet;
            }

            public void setFunctionSet(String functionSet) {
                this.functionSet = functionSet;
            }

            public String getKeyString() {
                return keyString;
            }

            public void setKeyString(String keyString) {
                this.keyString = keyString;
            }

            public String getLocalName() {
                return localName;
            }

            public void setLocalName(String localName) {
                this.localName = localName;
            }
        }

        public static class WifiListBean implements Parcelable {
            /**
             * _id : 5de4c32a33cc1949441265ca
             * wifiSN : WF132231004
             * isAdmin : 1
             * adminUid : 5c4fe492dc93897aa7d8600b
             * adminName : 8618954359822
             * productSN : s10001192910010
             * productModel : k8
             * appId : 1
             * lockNickname : wode
             * lockSoftwareVersion : 22222
             * functionSet : 00
             * uid : 5c4fe492dc93897aa7d8600b
             * uname : 8618954359822
             * pushSwitch : 1
             * amMode : 1
             * safeMode : 1
             * defences : 0
             * language : zh
             * operatingMode : 0
             * volume : 1
             * faceStatus : 1
             * powerSave : 1
             * bleVersion : 33333
             * wifiVersion : 44444
             * mqttVersion : 55555
             * faceVersion : 66666
             * lockFirmwareVersion : 11111
             * randomCode : randomCode666
             * distributionNetwork : 1
             * wifiName : wodewifi
             * power : 55
             * updateTime : 1577176575
             * createTime : 1577176575
             * openStatus : 2
             * openStatusTime : 1541468973
             * switch : {"createTime":"154148973342","mac":"112233445566","switchEn":1,"total":1,"switchArray":[{"type":1,"timeEn":0,"startTime":0,"stopTime":0,"week":0,"nickname":"第一个开关昵称"}]}
             */

            private String _id;
            private String wifiSN;
            private int isAdmin;
            private String adminUid;
            private String adminName;
            private String productSN;
            private String productModel;
            private int appId;
            private String lockNickname;
            private String lockSoftwareVersion;
            private String functionSet;
            private String uid;
            private String uname;
            private int pushSwitch;
            private int amMode;
            private int safeMode;
            private int defences;
            private String language;
            private int operatingMode;
            private int volume;
            private int faceStatus;
            private int powerSave;
            private String bleVersion;
            private String wifiVersion;
            private String mqttVersion;
            private String faceVersion;
            private String lockFirmwareVersion;
            private String randomCode;
            private int distributionNetwork;
            private String wifiName;
            private int power;
            private int updateTime;
            private int createTime;
            private int openStatus;
            private int openStatusTime;
            @SerializedName("switch")
            private SwitchBean switchX;
            /**
             * adminnickname : wengmaowei@kaadas.com
             * autoLock : 0
             * bleMac : 80:D2:1D:F2:A1:9A
             * bleVersionType :
             * latitude : 0
             * longitude : 0
             * magneticStatus : 2
             * model :
             * password1 : A610A39CE007C4A53BE17590
             * password2 : C8A19FD9
             * peripheralId :
             * systemID :
             * userNickname : wengmaowei@kaadas.com
             * wifiStatus : 1
             */

            private String adminnickname;
            private String autoLock;
            private String bleMac;
            private String bleVersionType;
            private String latitude;
            private String longitude;
            private int magneticStatus;
            private String model;
            private String password1;
            private String password2;
            private String peripheralId;
            private String systemID;
            private String userNickname;
            private String wifiStatus;
            private int autoLockTime;

            public String get_id() {
                return _id;
            }

            public void set_id(String _id) {
                this._id = _id;
            }

            public String getWifiSN() {
                return wifiSN;
            }

            public void setWifiSN(String wifiSN) {
                this.wifiSN = wifiSN;
            }

            public int getIsAdmin() {
                return isAdmin;
            }

            public void setIsAdmin(int isAdmin) {
                this.isAdmin = isAdmin;
            }

            public String getAdminUid() {
                return adminUid;
            }

            public void setAdminUid(String adminUid) {
                this.adminUid = adminUid;
            }

            public String getAdminName() {
                return adminName;
            }

            public void setAdminName(String adminName) {
                this.adminName = adminName;
            }

            public String getProductSN() {
                return productSN;
            }

            public void setProductSN(String productSN) {
                this.productSN = productSN;
            }

            public String getProductModel() {
                return productModel;
            }

            public void setProductModel(String productModel) {
                this.productModel = productModel;
            }

            public int getAppId() {
                return appId;
            }

            public void setAppId(int appId) {
                this.appId = appId;
            }

            public String getLockNickname() {
                return lockNickname;
            }

            public void setLockNickname(String lockNickname) {
                this.lockNickname = lockNickname;
            }

            public String getLockSoftwareVersion() {
                return lockSoftwareVersion;
            }

            public void setLockSoftwareVersion(String lockSoftwareVersion) {
                this.lockSoftwareVersion = lockSoftwareVersion;
            }

            public String getFunctionSet() {
                return functionSet;
            }

            public void setFunctionSet(String functionSet) {
                this.functionSet = functionSet;
            }

            public String getUid() {
                return uid;
            }

            public void setUid(String uid) {
                this.uid = uid;
            }

            public String getUname() {
                return uname;
            }

            public void setUname(String uname) {
                this.uname = uname;
            }

            public int getPushSwitch() {
                return pushSwitch;
            }

            public void setPushSwitch(int pushSwitch) {
                this.pushSwitch = pushSwitch;
            }

            public int getAmMode() {
                return amMode;
            }

            public void setAmMode(int amMode) {
                this.amMode = amMode;
            }

            public int getSafeMode() {
                return safeMode;
            }

            public void setSafeMode(int safeMode) {
                this.safeMode = safeMode;
            }

            public int getDefences() {
                return defences;
            }

            public void setDefences(int defences) {
                this.defences = defences;
            }

            public String getLanguage() {
                return language;
            }

            public void setLanguage(String language) {
                this.language = language;
            }

            public int getOperatingMode() {
                return operatingMode;
            }

            public void setOperatingMode(int operatingMode) {
                this.operatingMode = operatingMode;
            }

            public int getVolume() {
                return volume;
            }

            public void setVolume(int volume) {
                this.volume = volume;
            }

            public int getFaceStatus() {
                return faceStatus;
            }

            public void setFaceStatus(int faceStatus) {
                this.faceStatus = faceStatus;
            }

            public int getPowerSave() {
                return powerSave;
            }

            public void setPowerSave(int powerSave) {
                this.powerSave = powerSave;
            }

            public String getBleVersion() {
                return bleVersion;
            }

            public void setBleVersion(String bleVersion) {
                this.bleVersion = bleVersion;
            }

            public String getWifiVersion() {
                return wifiVersion;
            }

            public void setWifiVersion(String wifiVersion) {
                this.wifiVersion = wifiVersion;
            }

            public String getMqttVersion() {
                return mqttVersion;
            }

            public void setMqttVersion(String mqttVersion) {
                this.mqttVersion = mqttVersion;
            }

            public String getFaceVersion() {
                return faceVersion;
            }

            public void setFaceVersion(String faceVersion) {
                this.faceVersion = faceVersion;
            }

            public String getLockFirmwareVersion() {
                return lockFirmwareVersion;
            }

            public void setLockFirmwareVersion(String lockFirmwareVersion) {
                this.lockFirmwareVersion = lockFirmwareVersion;
            }

            public String getRandomCode() {
                return randomCode;
            }

            public void setRandomCode(String randomCode) {
                this.randomCode = randomCode;
            }

            public int getDistributionNetwork() {
                return distributionNetwork;
            }

            public void setDistributionNetwork(int distributionNetwork) {
                this.distributionNetwork = distributionNetwork;
            }

            public String getWifiName() {
                return wifiName;
            }

            public void setWifiName(String wifiName) {
                this.wifiName = wifiName;
            }

            public int getPower() {
                return power;
            }

            public void setPower(int power) {
                this.power = power;
            }

            public int getUpdateTime() {
                return updateTime;
            }

            public void setUpdateTime(int updateTime) {
                this.updateTime = updateTime;
            }

            public int getCreateTime() {
                return createTime;
            }

            public void setCreateTime(int createTime) {
                this.createTime = createTime;
            }

            public int getOpenStatus() {
                return openStatus;
            }

            public void setOpenStatus(int openStatus) {
                this.openStatus = openStatus;
            }

            public int getOpenStatusTime() {
                return openStatusTime;
            }

            public void setOpenStatusTime(int openStatusTime) {
                this.openStatusTime = openStatusTime;
            }

            public SwitchBean getSwitchX() {
                return switchX;
            }

            public void setSwitchX(SwitchBean switchX) {
                this.switchX = switchX;
            }

            public String getAdminnickname() {
                return adminnickname;
            }

            public void setAdminnickname(String adminnickname) {
                this.adminnickname = adminnickname;
            }

            public String getAutoLock() {
                return autoLock;
            }

            public void setAutoLock(String autoLock) {
                this.autoLock = autoLock;
            }

            public String getBleMac() {
                return bleMac;
            }

            public void setBleMac(String bleMac) {
                this.bleMac = bleMac;
            }

            public String getBleVersionType() {
                return bleVersionType;
            }

            public void setBleVersionType(String bleVersionType) {
                this.bleVersionType = bleVersionType;
            }

            public String getLatitude() {
                return latitude;
            }

            public void setLatitude(String latitude) {
                this.latitude = latitude;
            }

            public String getLongitude() {
                return longitude;
            }

            public void setLongitude(String longitude) {
                this.longitude = longitude;
            }

            public int getMagneticStatus() {
                return magneticStatus;
            }

            public void setMagneticStatus(int magneticStatus) {
                this.magneticStatus = magneticStatus;
            }

            public String getModel() {
                return model;
            }

            public void setModel(String model) {
                this.model = model;
            }

            public String getPassword1() {
                return password1;
            }

            public void setPassword1(String password1) {
                this.password1 = password1;
            }

            public String getPassword2() {
                return password2;
            }

            public void setPassword2(String password2) {
                this.password2 = password2;
            }

            public String getPeripheralId() {
                return peripheralId;
            }

            public void setPeripheralId(String peripheralId) {
                this.peripheralId = peripheralId;
            }

            public String getSystemID() {
                return systemID;
            }

            public void setSystemID(String systemID) {
                this.systemID = systemID;
            }

            public String getUserNickname() {
                return userNickname;
            }

            public void setUserNickname(String userNickname) {
                this.userNickname = userNickname;
            }

            public String getWifiStatus() {
                return wifiStatus;
            }

            public void setWifiStatus(String wifiStatus) {
                this.wifiStatus = wifiStatus;
            }

            public int getAutoLockTime() {
                return autoLockTime;
            }

            public void setAutoLockTime(int autoLockTime) {
                this.autoLockTime = autoLockTime;
            }

            public static class SwitchBean implements Parcelable {
                /**
                 * createTime : 154148973342
                 * mac : 112233445566
                 * switchEn : 1
                 * total : 1
                 * switchArray : [{"type":1,"timeEn":0,"startTime":0,"stopTime":0,"week":0,"nickname":"第一个开关昵称"}]
                 */

                private String createTime;
                private String mac;
                private int switchEn;
                private int total;
                private List<SwitchArrayBean> switchArray;

                public String getCreateTime() {
                    return createTime;
                }

                public void setCreateTime(String createTime) {
                    this.createTime = createTime;
                }

                public String getMac() {
                    return mac;
                }

                public void setMac(String mac) {
                    this.mac = mac;
                }

                public int getSwitchEn() {
                    return switchEn;
                }

                public void setSwitchEn(int switchEn) {
                    this.switchEn = switchEn;
                }

                public int getTotal() {
                    return total;
                }

                public void setTotal(int total) {
                    this.total = total;
                }

                public List<SwitchArrayBean> getSwitchArray() {
                    return switchArray;
                }

                public void setSwitchArray(List<SwitchArrayBean> switchArray) {
                    this.switchArray = switchArray;
                }

                public static class SwitchArrayBean implements Parcelable {
                    /**
                     * type : 1
                     * timeEn : 0
                     * startTime : 0
                     * stopTime : 0
                     * week : 0
                     * nickname : 第一个开关昵称
                     */

                    private int type;
                    private int timeEn;
                    private int startTime;
                    private int stopTime;
                    private int week;
                    private String nickname;

                    public int getType() {
                        return type;
                    }

                    public void setType(int type) {
                        this.type = type;
                    }

                    public int getTimeEn() {
                        return timeEn;
                    }

                    public void setTimeEn(int timeEn) {
                        this.timeEn = timeEn;
                    }

                    public int getStartTime() {
                        return startTime;
                    }

                    public void setStartTime(int startTime) {
                        this.startTime = startTime;
                    }

                    public int getStopTime() {
                        return stopTime;
                    }

                    public void setStopTime(int stopTime) {
                        this.stopTime = stopTime;
                    }

                    public int getWeek() {
                        return week;
                    }

                    public void setWeek(int week) {
                        this.week = week;
                    }

                    public String getNickname() {
                        return nickname;
                    }

                    public void setNickname(String nickname) {
                        this.nickname = nickname;
                    }


                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(Parcel dest, int flags) {
                        dest.writeInt(this.type);
                        dest.writeInt(this.timeEn);
                        dest.writeInt(this.startTime);
                        dest.writeInt(this.stopTime);
                        dest.writeInt(this.week);
                        dest.writeString(this.nickname);
                    }

                    public SwitchArrayBean() {
                    }

                    protected SwitchArrayBean(Parcel in) {
                        this.type = in.readInt();
                        this.timeEn = in.readInt();
                        this.startTime = in.readInt();
                        this.stopTime = in.readInt();
                        this.week = in.readInt();
                        this.nickname = in.readString();
                    }

                    public static final Creator<SwitchArrayBean> CREATOR = new Creator<SwitchArrayBean>() {
                        @Override
                        public SwitchArrayBean createFromParcel(Parcel source) {
                            return new SwitchArrayBean(source);
                        }

                        @Override
                        public SwitchArrayBean[] newArray(int size) {
                            return new SwitchArrayBean[size];
                        }
                    };
                }


                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    dest.writeString(this.createTime);
                    dest.writeString(this.mac);
                    dest.writeInt(this.switchEn);
                    dest.writeInt(this.total);
                    dest.writeTypedList(this.switchArray);
                }

                public SwitchBean() {
                }

                protected SwitchBean(Parcel in) {
                    this.createTime = in.readString();
                    this.mac = in.readString();
                    this.switchEn = in.readInt();
                    this.total = in.readInt();
                    this.switchArray = in.createTypedArrayList(SwitchArrayBean.CREATOR);
                }

                public static final Creator<SwitchBean> CREATOR = new Creator<SwitchBean>() {
                    @Override
                    public SwitchBean createFromParcel(Parcel source) {
                        return new SwitchBean(source);
                    }

                    @Override
                    public SwitchBean[] newArray(int size) {
                        return new SwitchBean[size];
                    }
                };
            }


            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this._id);
                dest.writeString(this.wifiSN);
                dest.writeInt(this.isAdmin);
                dest.writeString(this.adminUid);
                dest.writeString(this.adminName);
                dest.writeString(this.productSN);
                dest.writeString(this.productModel);
                dest.writeInt(this.appId);
                dest.writeString(this.lockNickname);
                dest.writeString(this.lockSoftwareVersion);
                dest.writeString(this.functionSet);
                dest.writeString(this.uid);
                dest.writeString(this.uname);
                dest.writeInt(this.pushSwitch);
                dest.writeInt(this.amMode);
                dest.writeInt(this.safeMode);
                dest.writeInt(this.defences);
                dest.writeString(this.language);
                dest.writeInt(this.operatingMode);
                dest.writeInt(this.volume);
                dest.writeInt(this.faceStatus);
                dest.writeInt(this.powerSave);
                dest.writeString(this.bleVersion);
                dest.writeString(this.wifiVersion);
                dest.writeString(this.mqttVersion);
                dest.writeString(this.faceVersion);
                dest.writeString(this.lockFirmwareVersion);
                dest.writeString(this.randomCode);
                dest.writeInt(this.distributionNetwork);
                dest.writeString(this.wifiName);
                dest.writeInt(this.power);
                dest.writeInt(this.updateTime);
                dest.writeInt(this.createTime);
                dest.writeInt(this.openStatus);
                dest.writeInt(this.openStatusTime);
                dest.writeParcelable(this.switchX, flags);
                dest.writeString(this.adminnickname);
                dest.writeString(this.autoLock);
                dest.writeString(this.bleMac);
                dest.writeString(this.bleVersionType);
                dest.writeString(this.latitude);
                dest.writeString(this.longitude);
                dest.writeInt(this.magneticStatus);
                dest.writeString(this.model);
                dest.writeString(this.password1);
                dest.writeString(this.password2);
                dest.writeString(this.peripheralId);
                dest.writeString(this.systemID);
                dest.writeString(this.userNickname);
                dest.writeString(this.wifiStatus);
                dest.writeInt(this.autoLockTime);
            }

            public WifiListBean() {
            }

            protected WifiListBean(Parcel in) {
                this._id = in.readString();
                this.wifiSN = in.readString();
                this.isAdmin = in.readInt();
                this.adminUid = in.readString();
                this.adminName = in.readString();
                this.productSN = in.readString();
                this.productModel = in.readString();
                this.appId = in.readInt();
                this.lockNickname = in.readString();
                this.lockSoftwareVersion = in.readString();
                this.functionSet = in.readString();
                this.uid = in.readString();
                this.uname = in.readString();
                this.pushSwitch = in.readInt();
                this.amMode = in.readInt();
                this.safeMode = in.readInt();
                this.defences = in.readInt();
                this.language = in.readString();
                this.operatingMode = in.readInt();
                this.volume = in.readInt();
                this.faceStatus = in.readInt();
                this.powerSave = in.readInt();
                this.bleVersion = in.readString();
                this.wifiVersion = in.readString();
                this.mqttVersion = in.readString();
                this.faceVersion = in.readString();
                this.lockFirmwareVersion = in.readString();
                this.randomCode = in.readString();
                this.distributionNetwork = in.readInt();
                this.wifiName = in.readString();
                this.power = in.readInt();
                this.updateTime = in.readInt();
                this.createTime = in.readInt();
                this.openStatus = in.readInt();
                this.openStatusTime = in.readInt();
                this.switchX = in.readParcelable(SwitchBean.class.getClassLoader());
                this.adminnickname = in.readString();
                this.autoLock = in.readString();
                this.bleMac = in.readString();
                this.bleVersionType = in.readString();
                this.latitude = in.readString();
                this.longitude = in.readString();
                this.magneticStatus = in.readInt();
                this.model = in.readString();
                this.password1 = in.readString();
                this.password2 = in.readString();
                this.peripheralId = in.readString();
                this.systemID = in.readString();
                this.userNickname = in.readString();
                this.wifiStatus = in.readString();
                this.autoLockTime = in.readInt();
            }

            public static final Creator<WifiListBean> CREATOR = new Creator<WifiListBean>() {
                @Override
                public WifiListBean createFromParcel(Parcel source) {
                    return new WifiListBean(source);
                }

                @Override
                public WifiListBean[] newArray(int size) {
                    return new WifiListBean[size];
                }
            };
        }

        public static class ProductInfoListBean {
            /**
             * _id : 5d66619c497ecf326f25469b
             * developmentModel : 1
             * productModel : k8
             * snHead : WF1
             * adminUrl : 47.106.94.189/deviceModelFiles/1566990166590/android_admin_xxx.png
             * deviceListUrl : 47.106.94.189/deviceModelFiles/1566990488685/android_device_list_xxx.png
             * authUrl : 47.106.94.189/deviceModelFiles/1566990492106/android_auth_xxx.png
             * adminUrl@1x : 47.106.94.189/deviceModelFiles/1566990495762/ios_admin_xxx@1x.png
             * deviceListUrl@1x : 47.106.94.189/deviceModelFiles/1566990500347/ios_device_list_xxx@1x.png
             * authUrl@1x : 47.106.94.189/deviceModelFiles/1566990503571/ios_auth_xxx@1x.png
             * adminUrl@2x : 47.106.94.189/deviceModelFiles/1566990506856/ios_admin_xxx@2x.png
             * deviceListUrl@2x : 47.106.94.189/deviceModelFiles/1566990510246/ios_device_list_xxx@2x.png
             * authUrl@2x : 47.106.94.189/deviceModelFiles/1566990513628/ios_auth_xxx@2x.png
             * adminUrl@3x : 47.106.94.189/deviceModelFiles/1566990518026/ios_admin_xxx@3x.png
             * deviceListUrl@3x : 47.106.94.189/deviceModelFiles/1566990522179/ios_device_list_xxx@3x.png
             * authUrl@3x : 47.106.94.189/deviceModelFiles/1566990530690/ios_auth_xxx@3x.png
             * createTime : 2019-08-28 19:12:28.060
             */

            private String _id;
            private String developmentModel;
            private String productModel;
            private String snHead;
            private String adminUrl;
            private String deviceListUrl;
            private String authUrl;
            @SerializedName("adminUrl@1x")
            private String _$AdminUrl1x309; // FIXME check this code
            @SerializedName("deviceListUrl@1x")
            private String _$DeviceListUrl1x114; // FIXME check this code
            @SerializedName("authUrl@1x")
            private String _$AuthUrl1x198; // FIXME check this code
            @SerializedName("adminUrl@2x")
            private String _$AdminUrl2x1; // FIXME check this code
            @SerializedName("deviceListUrl@2x")
            private String _$DeviceListUrl2x136; // FIXME check this code
            @SerializedName("authUrl@2x")
            private String _$AuthUrl2x173; // FIXME check this code
            @SerializedName("adminUrl@3x")
            private String _$AdminUrl3x305; // FIXME check this code
            @SerializedName("deviceListUrl@3x")
            private String _$DeviceListUrl3x159; // FIXME check this code
            @SerializedName("authUrl@3x")
            private String _$AuthUrl3x21; // FIXME check this code
            private String createTime;

            public String get_id() {
                return _id;
            }

            public void set_id(String _id) {
                this._id = _id;
            }

            public String getDevelopmentModel() {
                return developmentModel;
            }

            public void setDevelopmentModel(String developmentModel) {
                this.developmentModel = developmentModel;
            }

            public String getProductModel() {
                return productModel;
            }

            public void setProductModel(String productModel) {
                this.productModel = productModel;
            }

            public String getSnHead() {
                return snHead;
            }

            public void setSnHead(String snHead) {
                this.snHead = snHead;
            }

            public String getAdminUrl() {
                return adminUrl;
            }

            public void setAdminUrl(String adminUrl) {
                this.adminUrl = adminUrl;
            }

            public String getDeviceListUrl() {
                return deviceListUrl;
            }

            public void setDeviceListUrl(String deviceListUrl) {
                this.deviceListUrl = deviceListUrl;
            }

            public String getAuthUrl() {
                return authUrl;
            }

            public void setAuthUrl(String authUrl) {
                this.authUrl = authUrl;
            }

            public String get_$AdminUrl1x309() {
                return _$AdminUrl1x309;
            }

            public void set_$AdminUrl1x309(String _$AdminUrl1x309) {
                this._$AdminUrl1x309 = _$AdminUrl1x309;
            }

            public String get_$DeviceListUrl1x114() {
                return _$DeviceListUrl1x114;
            }

            public void set_$DeviceListUrl1x114(String _$DeviceListUrl1x114) {
                this._$DeviceListUrl1x114 = _$DeviceListUrl1x114;
            }

            public String get_$AuthUrl1x198() {
                return _$AuthUrl1x198;
            }

            public void set_$AuthUrl1x198(String _$AuthUrl1x198) {
                this._$AuthUrl1x198 = _$AuthUrl1x198;
            }

            public String get_$AdminUrl2x1() {
                return _$AdminUrl2x1;
            }

            public void set_$AdminUrl2x1(String _$AdminUrl2x1) {
                this._$AdminUrl2x1 = _$AdminUrl2x1;
            }

            public String get_$DeviceListUrl2x136() {
                return _$DeviceListUrl2x136;
            }

            public void set_$DeviceListUrl2x136(String _$DeviceListUrl2x136) {
                this._$DeviceListUrl2x136 = _$DeviceListUrl2x136;
            }

            public String get_$AuthUrl2x173() {
                return _$AuthUrl2x173;
            }

            public void set_$AuthUrl2x173(String _$AuthUrl2x173) {
                this._$AuthUrl2x173 = _$AuthUrl2x173;
            }

            public String get_$AdminUrl3x305() {
                return _$AdminUrl3x305;
            }

            public void set_$AdminUrl3x305(String _$AdminUrl3x305) {
                this._$AdminUrl3x305 = _$AdminUrl3x305;
            }

            public String get_$DeviceListUrl3x159() {
                return _$DeviceListUrl3x159;
            }

            public void set_$DeviceListUrl3x159(String _$DeviceListUrl3x159) {
                this._$DeviceListUrl3x159 = _$DeviceListUrl3x159;
            }

            public String get_$AuthUrl3x21() {
                return _$AuthUrl3x21;
            }

            public void set_$AuthUrl3x21(String _$AuthUrl3x21) {
                this._$AuthUrl3x21 = _$AuthUrl3x21;
            }

            public String getCreateTime() {
                return createTime;
            }

            public void setCreateTime(String createTime) {
                this.createTime = createTime;
            }
        }
    }
}
