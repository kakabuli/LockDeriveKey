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
             * _id : 60667c3b93f7f684d7fbae63
             * adminUid : 5e66f9a42c98082e5c528342
             * wifiSN : W010211310006
             * adminName : yangxianjie@kaadas.com
             * adminnickname : null
             * appId : 0
             * autoLock : 0
             * bleMac : 80:D2:1D:F2:A2:00
             * bleVersion : V1.01.003
             * bleVersionType :
             * createTime : 1617329211
             * firstName : yangxianjie@kaadas.com
             * functionSet : 255
             * isAdmin : 1
             * lastName : yangxianjie@kaadas.com
             * latitude : 0
             * lockNickname : W010211310006
             * longitude : 0
             * magneticStatus : 2
             * model : WP01
             * password1 : A14870FE3C6896917FCC545C
             * password2 : 07DE6D0B
             * peripheralId :
             * pushSwitch : 1
             * systemID :
             * uid : 5e66f9a42c98082e5c528342
             * uname : yangxianjie@kaadas.com
             * updateTime : 1617342580
             * userNickname : null
             * userNumberId : 0
             * faceVersion :
             * lockFirmwareVersion : V1.01.003
             * lockSoftwareVersion : V1.01.003
             * mqttVersion : 3.1.1
             * power : 92
             * productModel :
             * wifiVersion : V5.01.001
             * amMode : 0
             * autoLockTime : 0
             * defences : 0
             * doorSensor : 0
             * duress : 0
             * elecFence : null
             * elecFenceSensitivity : 0
             * elecFenceTime : null
             * faceStatus : null
             * language : zh
             * operatingMode : 0
             * powerSave : 0
             * safeMode : 0
             * volume : 0
             * openStatus : 2
             * openStatusTime : 1617342580
             * wifiStatus : 0
             */

            private String _id;
            private String adminUid;
            private String wifiSN;
            private String adminName;
            private String adminnickname;
            private Integer appId;
            private String autoLock;
            private String bleMac;
            private String bleVersion;
            private String bleVersionType;
            private Integer createTime;
            private String firstName;
            private Integer functionSet;
            private Integer isAdmin;
            private String lastName;
            private String latitude;
            private String lockNickname;
            private String longitude;
            private Integer magneticStatus;
            private String model;
            private String password1;
            private String password2;
            private String peripheralId;
            private Integer pushSwitch;
            private String systemID;
            private String uid;
            private String uname;
            private Integer updateTime;
            private String userNickname;
            private Integer userNumberId;
            private String faceVersion;
            private String lockFirmwareVersion;
            private String lockSoftwareVersion;
            private String mqttVersion;
            private Integer power;
            private String productModel;
            private String wifiVersion;
            private Integer amMode;
            private Integer autoLockTime;
            private Integer defences;
            private Integer doorSensor;
            private Integer duress;
            private Integer elecFence;
            private Integer elecFenceSensitivity;
            private Integer elecFenceTime;
            private Integer faceStatus;
            private String language;
            private Integer operatingMode;
            private Integer powerSave;
            private Integer safeMode;
            private Integer volume;
            private Integer openStatus;
            private Integer openStatusTime;
            private String wifiStatus;
            private String wifiName;
            private String randomCode;

            public String get_id() {
                return _id;
            }

            public void set_id(String _id) {
                this._id = _id;
            }

            public String getAdminUid() {
                return adminUid;
            }

            public void setAdminUid(String adminUid) {
                this.adminUid = adminUid;
            }

            public String getWifiSN() {
                return wifiSN;
            }

            public void setWifiSN(String wifiSN) {
                this.wifiSN = wifiSN;
            }

            public String getAdminName() {
                return adminName;
            }

            public void setAdminName(String adminName) {
                this.adminName = adminName;
            }

            public String getAdminnickname() {
                return adminnickname;
            }

            public void setAdminnickname(String adminnickname) {
                this.adminnickname = adminnickname;
            }

            public Integer getAppId() {
                return appId;
            }

            public void setAppId(Integer appId) {
                this.appId = appId;
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

            public String getBleVersion() {
                return bleVersion;
            }

            public void setBleVersion(String bleVersion) {
                this.bleVersion = bleVersion;
            }

            public String getBleVersionType() {
                return bleVersionType;
            }

            public void setBleVersionType(String bleVersionType) {
                this.bleVersionType = bleVersionType;
            }

            public Integer getCreateTime() {
                return createTime;
            }

            public void setCreateTime(Integer createTime) {
                this.createTime = createTime;
            }

            public String getFirstName() {
                return firstName;
            }

            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }

            public Integer getFunctionSet() {
                return functionSet;
            }

            public void setFunctionSet(Integer functionSet) {
                this.functionSet = functionSet;
            }

            public Integer getIsAdmin() {
                return isAdmin;
            }

            public void setIsAdmin(Integer isAdmin) {
                this.isAdmin = isAdmin;
            }

            public String getLastName() {
                return lastName;
            }

            public void setLastName(String lastName) {
                this.lastName = lastName;
            }

            public String getLatitude() {
                return latitude;
            }

            public void setLatitude(String latitude) {
                this.latitude = latitude;
            }

            public String getLockNickname() {
                return lockNickname;
            }

            public void setLockNickname(String lockNickname) {
                this.lockNickname = lockNickname;
            }

            public String getLongitude() {
                return longitude;
            }

            public void setLongitude(String longitude) {
                this.longitude = longitude;
            }

            public Integer getMagneticStatus() {
                return magneticStatus;
            }

            public void setMagneticStatus(Integer magneticStatus) {
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

            public Integer getPushSwitch() {
                return pushSwitch;
            }

            public void setPushSwitch(Integer pushSwitch) {
                this.pushSwitch = pushSwitch;
            }

            public String getSystemID() {
                return systemID;
            }

            public void setSystemID(String systemID) {
                this.systemID = systemID;
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

            public Integer getUpdateTime() {
                return updateTime;
            }

            public void setUpdateTime(Integer updateTime) {
                this.updateTime = updateTime;
            }

            public String getUserNickname() {
                return userNickname;
            }

            public void setUserNickname(String userNickname) {
                this.userNickname = userNickname;
            }

            public Integer getUserNumberId() {
                return userNumberId;
            }

            public void setUserNumberId(Integer userNumberId) {
                this.userNumberId = userNumberId;
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

            public String getLockSoftwareVersion() {
                return lockSoftwareVersion;
            }

            public void setLockSoftwareVersion(String lockSoftwareVersion) {
                this.lockSoftwareVersion = lockSoftwareVersion;
            }

            public String getMqttVersion() {
                return mqttVersion;
            }

            public void setMqttVersion(String mqttVersion) {
                this.mqttVersion = mqttVersion;
            }

            public Integer getPower() {
                return power;
            }

            public void setPower(Integer power) {
                this.power = power;
            }

            public String getProductModel() {
                return productModel;
            }

            public void setProductModel(String productModel) {
                this.productModel = productModel;
            }

            public String getWifiVersion() {
                return wifiVersion;
            }

            public void setWifiVersion(String wifiVersion) {
                this.wifiVersion = wifiVersion;
            }

            public Integer getAmMode() {
                return amMode;
            }

            public void setAmMode(Integer amMode) {
                this.amMode = amMode;
            }

            public Integer getAutoLockTime() {
                return autoLockTime;
            }

            public void setAutoLockTime(Integer autoLockTime) {
                this.autoLockTime = autoLockTime;
            }

            public Integer getDefences() {
                return defences;
            }

            public void setDefences(Integer defences) {
                this.defences = defences;
            }

            public Integer getDoorSensor() {
                return doorSensor;
            }

            public void setDoorSensor(Integer doorSensor) {
                this.doorSensor = doorSensor;
            }

            public Integer getDuress() {
                return duress;
            }

            public void setDuress(Integer duress) {
                this.duress = duress;
            }

            public Integer getElecFence() {
                return elecFence;
            }

            public void setElecFence(Integer elecFence) {
                this.elecFence = elecFence;
            }

            public Integer getElecFenceSensitivity() {
                return elecFenceSensitivity;
            }

            public void setElecFenceSensitivity(Integer elecFenceSensitivity) {
                this.elecFenceSensitivity = elecFenceSensitivity;
            }

            public Integer getElecFenceTime() {
                return elecFenceTime;
            }

            public void setElecFenceTime(Integer elecFenceTime) {
                this.elecFenceTime = elecFenceTime;
            }

            public Integer getFaceStatus() {
                return faceStatus;
            }

            public void setFaceStatus(Integer faceStatus) {
                this.faceStatus = faceStatus;
            }

            public String getLanguage() {
                return language;
            }

            public void setLanguage(String language) {
                this.language = language;
            }

            public Integer getOperatingMode() {
                return operatingMode;
            }

            public void setOperatingMode(Integer operatingMode) {
                this.operatingMode = operatingMode;
            }

            public Integer getPowerSave() {
                return powerSave;
            }

            public void setPowerSave(Integer powerSave) {
                this.powerSave = powerSave;
            }

            public Integer getSafeMode() {
                return safeMode;
            }

            public void setSafeMode(Integer safeMode) {
                this.safeMode = safeMode;
            }

            public Integer getVolume() {
                return volume;
            }

            public void setVolume(Integer volume) {
                this.volume = volume;
            }

            public Integer getOpenStatus() {
                return openStatus;
            }

            public void setOpenStatus(Integer openStatus) {
                this.openStatus = openStatus;
            }

            public Integer getOpenStatusTime() {
                return openStatusTime;
            }

            public void setOpenStatusTime(Integer openStatusTime) {
                this.openStatusTime = openStatusTime;
            }

            public String getWifiStatus() {
                return wifiStatus;
            }

            public void setWifiStatus(String wifiStatus) {
                this.wifiStatus = wifiStatus;
            }

            public String getWifiName() {
                return wifiName;
            }

            public void setWifiName(String wifiName) {
                this.wifiName = wifiName;
            }

            public String getRandomCode() {
                return randomCode;
            }

            public void setRandomCode(String randomCode) {
                this.randomCode = randomCode;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(this._id);
                dest.writeString(this.adminUid);
                dest.writeString(this.wifiSN);
                dest.writeString(this.adminName);
                dest.writeString(this.adminnickname);
                dest.writeValue(this.appId);
                dest.writeString(this.autoLock);
                dest.writeString(this.bleMac);
                dest.writeString(this.bleVersion);
                dest.writeString(this.bleVersionType);
                dest.writeValue(this.createTime);
                dest.writeString(this.firstName);
                dest.writeValue(this.functionSet);
                dest.writeValue(this.isAdmin);
                dest.writeString(this.lastName);
                dest.writeString(this.latitude);
                dest.writeString(this.lockNickname);
                dest.writeString(this.longitude);
                dest.writeValue(this.magneticStatus);
                dest.writeString(this.model);
                dest.writeString(this.password1);
                dest.writeString(this.password2);
                dest.writeString(this.peripheralId);
                dest.writeValue(this.pushSwitch);
                dest.writeString(this.systemID);
                dest.writeString(this.uid);
                dest.writeString(this.uname);
                dest.writeValue(this.updateTime);
                dest.writeString(this.userNickname);
                dest.writeValue(this.userNumberId);
                dest.writeString(this.faceVersion);
                dest.writeString(this.lockFirmwareVersion);
                dest.writeString(this.lockSoftwareVersion);
                dest.writeString(this.mqttVersion);
                dest.writeValue(this.power);
                dest.writeString(this.productModel);
                dest.writeString(this.wifiVersion);
                dest.writeValue(this.amMode);
                dest.writeValue(this.autoLockTime);
                dest.writeValue(this.defences);
                dest.writeValue(this.doorSensor);
                dest.writeValue(this.duress);
                dest.writeValue(this.elecFence);
                dest.writeValue(this.elecFenceSensitivity);
                dest.writeValue(this.elecFenceTime);
                dest.writeValue(this.faceStatus);
                dest.writeString(this.language);
                dest.writeValue(this.operatingMode);
                dest.writeValue(this.powerSave);
                dest.writeValue(this.safeMode);
                dest.writeValue(this.volume);
                dest.writeValue(this.openStatus);
                dest.writeValue(this.openStatusTime);
                dest.writeString(this.wifiStatus);
                dest.writeString(this.wifiName);
                dest.writeString(this.randomCode);
            }

            public void readFromParcel(Parcel source) {
                this._id = source.readString();
                this.adminUid = source.readString();
                this.wifiSN = source.readString();
                this.adminName = source.readString();
                this.adminnickname = source.readString();
                this.appId = (Integer) source.readValue(Integer.class.getClassLoader());
                this.autoLock = source.readString();
                this.bleMac = source.readString();
                this.bleVersion = source.readString();
                this.bleVersionType = source.readString();
                this.createTime = (Integer) source.readValue(Integer.class.getClassLoader());
                this.firstName = source.readString();
                this.functionSet = (Integer) source.readValue(Integer.class.getClassLoader());
                this.isAdmin = (Integer) source.readValue(Integer.class.getClassLoader());
                this.lastName = source.readString();
                this.latitude = source.readString();
                this.lockNickname = source.readString();
                this.longitude = source.readString();
                this.magneticStatus = (Integer) source.readValue(Integer.class.getClassLoader());
                this.model = source.readString();
                this.password1 = source.readString();
                this.password2 = source.readString();
                this.peripheralId = source.readString();
                this.pushSwitch = (Integer) source.readValue(Integer.class.getClassLoader());
                this.systemID = source.readString();
                this.uid = source.readString();
                this.uname = source.readString();
                this.updateTime = (Integer) source.readValue(Integer.class.getClassLoader());
                this.userNickname = source.readString();
                this.userNumberId = (Integer) source.readValue(Integer.class.getClassLoader());
                this.faceVersion = source.readString();
                this.lockFirmwareVersion = source.readString();
                this.lockSoftwareVersion = source.readString();
                this.mqttVersion = source.readString();
                this.power = (Integer) source.readValue(Integer.class.getClassLoader());
                this.productModel = source.readString();
                this.wifiVersion = source.readString();
                this.amMode = (Integer) source.readValue(Integer.class.getClassLoader());
                this.autoLockTime = (Integer) source.readValue(Integer.class.getClassLoader());
                this.defences = (Integer) source.readValue(Integer.class.getClassLoader());
                this.doorSensor = (Integer) source.readValue(Integer.class.getClassLoader());
                this.duress = (Integer) source.readValue(Integer.class.getClassLoader());
                this.elecFence = (Integer) source.readValue(Integer.class.getClassLoader());
                this.elecFenceSensitivity = (Integer) source.readValue(Integer.class.getClassLoader());
                this.elecFenceTime = (Integer) source.readValue(Integer.class.getClassLoader());
                this.faceStatus = (Integer) source.readValue(Integer.class.getClassLoader());
                this.language = source.readString();
                this.operatingMode = (Integer) source.readValue(Integer.class.getClassLoader());
                this.powerSave = (Integer) source.readValue(Integer.class.getClassLoader());
                this.safeMode = (Integer) source.readValue(Integer.class.getClassLoader());
                this.volume = (Integer) source.readValue(Integer.class.getClassLoader());
                this.openStatus = (Integer) source.readValue(Integer.class.getClassLoader());
                this.openStatusTime = (Integer) source.readValue(Integer.class.getClassLoader());
                this.wifiStatus = source.readString();
                this.wifiName = source.readString();
                this.randomCode = source.readString();
            }

            public WifiListBean() {
            }

            protected WifiListBean(Parcel in) {
                this._id = in.readString();
                this.adminUid = in.readString();
                this.wifiSN = in.readString();
                this.adminName = in.readString();
                this.adminnickname = in.readString();
                this.appId = (Integer) in.readValue(Integer.class.getClassLoader());
                this.autoLock = in.readString();
                this.bleMac = in.readString();
                this.bleVersion = in.readString();
                this.bleVersionType = in.readString();
                this.createTime = (Integer) in.readValue(Integer.class.getClassLoader());
                this.firstName = in.readString();
                this.functionSet = (Integer) in.readValue(Integer.class.getClassLoader());
                this.isAdmin = (Integer) in.readValue(Integer.class.getClassLoader());
                this.lastName = in.readString();
                this.latitude = in.readString();
                this.lockNickname = in.readString();
                this.longitude = in.readString();
                this.magneticStatus = (Integer) in.readValue(Integer.class.getClassLoader());
                this.model = in.readString();
                this.password1 = in.readString();
                this.password2 = in.readString();
                this.peripheralId = in.readString();
                this.pushSwitch = (Integer) in.readValue(Integer.class.getClassLoader());
                this.systemID = in.readString();
                this.uid = in.readString();
                this.uname = in.readString();
                this.updateTime = (Integer) in.readValue(Integer.class.getClassLoader());
                this.userNickname = in.readString();
                this.userNumberId = (Integer) in.readValue(Integer.class.getClassLoader());
                this.faceVersion = in.readString();
                this.lockFirmwareVersion = in.readString();
                this.lockSoftwareVersion = in.readString();
                this.mqttVersion = in.readString();
                this.power = (Integer) in.readValue(Integer.class.getClassLoader());
                this.productModel = in.readString();
                this.wifiVersion = in.readString();
                this.amMode = (Integer) in.readValue(Integer.class.getClassLoader());
                this.autoLockTime = (Integer) in.readValue(Integer.class.getClassLoader());
                this.defences = (Integer) in.readValue(Integer.class.getClassLoader());
                this.doorSensor = (Integer) in.readValue(Integer.class.getClassLoader());
                this.duress = (Integer) in.readValue(Integer.class.getClassLoader());
                this.elecFence = (Integer) in.readValue(Integer.class.getClassLoader());
                this.elecFenceSensitivity = (Integer) in.readValue(Integer.class.getClassLoader());
                this.elecFenceTime = (Integer) in.readValue(Integer.class.getClassLoader());
                this.faceStatus = (Integer) in.readValue(Integer.class.getClassLoader());
                this.language = in.readString();
                this.operatingMode = (Integer) in.readValue(Integer.class.getClassLoader());
                this.powerSave = (Integer) in.readValue(Integer.class.getClassLoader());
                this.safeMode = (Integer) in.readValue(Integer.class.getClassLoader());
                this.volume = (Integer) in.readValue(Integer.class.getClassLoader());
                this.openStatus = (Integer) in.readValue(Integer.class.getClassLoader());
                this.openStatusTime = (Integer) in.readValue(Integer.class.getClassLoader());
                this.wifiStatus = in.readString();
                this.wifiName = in.readString();
                this.randomCode = in.readString();
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
