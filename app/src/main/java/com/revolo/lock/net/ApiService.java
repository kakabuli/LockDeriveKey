package com.revolo.lock.net;


import com.revolo.lock.bean.request.AcceptShareBeanReq;
import com.revolo.lock.bean.request.AdminAddDeviceBeanReq;
import com.revolo.lock.bean.request.ChangeBleVerBeanReq;
import com.revolo.lock.bean.request.ChangeDeviceHardVerBeanReq;
import com.revolo.lock.bean.request.ChangeDeviceNameBeanReq;
import com.revolo.lock.bean.request.ChangeFeaturesBeanReq;
import com.revolo.lock.bean.request.ChangeKeyNickBeanReq;
import com.revolo.lock.bean.request.ChangeOpenLockParameterBeanReq;
import com.revolo.lock.bean.request.ChangeUserPwdBeanReq;
import com.revolo.lock.bean.request.CheckDoorSensorStateBeanReq;
import com.revolo.lock.bean.request.CheckOTABeanReq;
import com.revolo.lock.bean.request.DelDeviceBeanReq;
import com.revolo.lock.bean.request.DelInvalidShareBeanReq;
import com.revolo.lock.bean.request.DelKeyBeanReq;
import com.revolo.lock.bean.request.DelSharedUserBeanReq;
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.bean.request.EnableSharedUserBeanReq;
import com.revolo.lock.bean.request.ForgotPwdBeanReq;
import com.revolo.lock.bean.request.GainKeyBeanReq;
import com.revolo.lock.bean.request.GetAllSharedUserFromAdminUserBeanReq;
import com.revolo.lock.bean.request.GetCodeBeanReq;
import com.revolo.lock.bean.request.GetAllSharedUserFromLockBeanReq;
import com.revolo.lock.bean.request.GetDevicesFromUidAndSharedUidBeanReq;
import com.revolo.lock.bean.request.GetLockKeyNickBeanReq;
import com.revolo.lock.bean.request.GetPwd1BeanReq;
import com.revolo.lock.bean.request.LockIsBindBeanReq;
import com.revolo.lock.bean.request.LockKeyAddBeanReq;
import com.revolo.lock.bean.request.LockRecordBeanReq;
import com.revolo.lock.bean.request.MailLoginBeanReq;
import com.revolo.lock.bean.request.MailRegisterBeanReq;
import com.revolo.lock.bean.request.OpenDoorRecordSearchBeanReq;
import com.revolo.lock.bean.request.SearchAlarmRecordBeanReq;
import com.revolo.lock.bean.request.SearchKeyListBeanReq;
import com.revolo.lock.bean.request.SearchProductNoBeanReq;
import com.revolo.lock.bean.request.SettingDuressPwdReceiveEMailBeanReq;
import com.revolo.lock.bean.request.StartOTAUpdateBeanReq;
import com.revolo.lock.bean.request.UpdateDoorSensorStateBeanReq;
import com.revolo.lock.bean.request.UpdateLockRecordBeanReq;
import com.revolo.lock.bean.request.UpdateSharedUserNickNameBeanReq;
import com.revolo.lock.bean.request.UpdateUserAuthorityTypeBeanReq;
import com.revolo.lock.bean.request.UpdateUserFirstLastNameBeanReq;
import com.revolo.lock.bean.request.UploadAlarmRecordBeanReq;
import com.revolo.lock.bean.request.UploadOpenDoorRecordBeanReq;
import com.revolo.lock.bean.respone.AcceptShareBeanRsp;
import com.revolo.lock.bean.respone.AdminAddDeviceBeanRsp;
import com.revolo.lock.bean.respone.ChangeBleVerBeanRsp;
import com.revolo.lock.bean.respone.ChangeDeviceHardVerBeanRsp;
import com.revolo.lock.bean.respone.ChangeDeviceNameBeanRsp;
import com.revolo.lock.bean.respone.ChangeFeaturesBeanRsp;
import com.revolo.lock.bean.respone.ChangeKeyNickBeanRsp;
import com.revolo.lock.bean.respone.ChangeOpenLockParameterBeanRsp;
import com.revolo.lock.bean.respone.ChangeUserPwdBeanRsp;
import com.revolo.lock.bean.respone.CheckDoorSensorStateBeanRsp;
import com.revolo.lock.bean.respone.CheckOTABeanRsp;
import com.revolo.lock.bean.respone.DelDeviceBeanRsp;
import com.revolo.lock.bean.respone.DelInvalidShareBeanRsp;
import com.revolo.lock.bean.respone.DelKeyBeanRsp;
import com.revolo.lock.bean.respone.DelSharedUserBeanRsp;
import com.revolo.lock.bean.respone.DeviceUnbindBeanRsp;
import com.revolo.lock.bean.respone.EnableSharedUserBeanRsp;
import com.revolo.lock.bean.respone.ForgotPwdRsp;
import com.revolo.lock.bean.respone.GainKeyBeanRsp;
import com.revolo.lock.bean.respone.GetAllSharedUserFromAdminUserBeanRsp;
import com.revolo.lock.bean.respone.GetCodeBeanRsp;
import com.revolo.lock.bean.respone.GetAllSharedUserFromLockBeanRsp;
import com.revolo.lock.bean.respone.GetDevicesFromUidAndSharedUidBeanRsp;
import com.revolo.lock.bean.respone.GetLockKeyNickBeanRsp;
import com.revolo.lock.bean.respone.GetPwd1BeanRsp;
import com.revolo.lock.bean.respone.LockIsBindBeanRsp;
import com.revolo.lock.bean.respone.LockKeyAddBeanRsp;
import com.revolo.lock.bean.respone.LockRecordBeanRsp;
import com.revolo.lock.bean.respone.LogoutBeanRsp;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.bean.respone.MailRegisterBeanRsp;
import com.revolo.lock.bean.respone.OpenDoorRecordSearchBeanRsp;
import com.revolo.lock.bean.respone.SearchAlarmRecordBeanRsp;
import com.revolo.lock.bean.respone.SearchKeyListBeanRsp;
import com.revolo.lock.bean.respone.SearchProductNoBeanRsp;
import com.revolo.lock.bean.respone.SettingDuressPwdReceiveEMailBeanRsp;
import com.revolo.lock.bean.respone.StartOTAUpdateBeanRsp;
import com.revolo.lock.bean.respone.UpdateDoorSensorStateBeanRsp;
import com.revolo.lock.bean.respone.UpdateLockRecordBeanRsp;
import com.revolo.lock.bean.respone.UpdateSharedUserNickNameBeanRsp;
import com.revolo.lock.bean.respone.UpdateUserAuthorityTypeBeanRsp;
import com.revolo.lock.bean.respone.UpdateUserFirstLastNameBeanRsp;
import com.revolo.lock.bean.respone.UploadAlarmRecordBeanRsp;
import com.revolo.lock.bean.respone.UploadOpenDoorRecordBeanRsp;
import com.revolo.lock.bean.respone.UploadUserAvatarBeanRsp;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

public interface ApiService {

    @Headers({"Content-Type: application/json"})
    @POST("/user/login/getuserbymail")
    Observable<MailLoginBeanRsp> login(@Body MailLoginBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/checkadmindev")
    Observable<LockIsBindBeanRsp> lockIsBind(@Header("token") String token, @Body LockIsBindBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/model/getpwdBySN")
    Observable<GetPwd1BeanRsp> getPwd1(@Header("token") String token, @Body GetPwd1BeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/createadmindev")
    Observable<AdminAddDeviceBeanRsp> adminAddDevice(@Header("token") String token, @Body AdminAddDeviceBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/unbind")
    Observable<DeviceUnbindBeanRsp> unbindDevice(@Header("token") String token, @Body DeviceUnbindBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateBleVersionType")
    Observable<ChangeBleVerBeanRsp> changeBleVer(@Header("token") String token, @Body ChangeBleVerBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateBleVersion")
    Observable<ChangeDeviceHardVerBeanRsp> changeDeviceHardVer(@Header("token") String token, @Body ChangeDeviceHardVerBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateFunctionSet")
    Observable<ChangeFeaturesBeanRsp> updateFunctionSet(@Header("token") String token, @Body ChangeFeaturesBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/deleteadmindev")
    Observable<DelDeviceBeanRsp> delDevice(@Header("token") String token, @Body DelDeviceBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateAdminlockNickName")
    Observable<ChangeDeviceNameBeanRsp> changeDeviceNickName(@Header("token") String token, @Body ChangeDeviceNameBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/deviceModel/search")
    Observable<SearchProductNoBeanRsp> searchDevice(@Header("token") String token, @Body SearchProductNoBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/uploadopenlocklist")
    Observable<UploadOpenDoorRecordBeanRsp> uploadOpenDoorRecord(@Header("token") String token, @Body UploadOpenDoorRecordBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/findopenlockrecord")
    Observable<OpenDoorRecordSearchBeanRsp> searchOpenLockRecord(@Header("token") String token, @Body OpenDoorRecordSearchBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/uploadalarmlist")
    Observable<UploadAlarmRecordBeanRsp> uploadAlarmRecord(@Header("token") String token, @Body UploadAlarmRecordBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wifi/alarm/list")
    Observable<SearchAlarmRecordBeanRsp> searchAlarmRecord(@Header("token") String token, @Body SearchAlarmRecordBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/pwdadd")
    Observable<LockKeyAddBeanRsp> addLockKey(@Header("token") String token, @Body LockKeyAddBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/pwdlist")
    Observable<SearchKeyListBeanRsp> searchLockKey(@Header("token") String token, @Body SearchKeyListBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/pwddelete")
    Observable<DelKeyBeanRsp> delKey(@Header("token") String token, @Body DelKeyBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/nicknameupdate")
    Observable<ChangeKeyNickBeanRsp> changeKeyNickName(@Header("token") String token, @Body ChangeKeyNickBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/getNickname")
    Observable<GetLockKeyNickBeanRsp> getKeyNickName(@Header("token") String token, @Body GetLockKeyNickBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateMagneticStatus")
    Observable<UpdateDoorSensorStateBeanRsp> updateDoorSensorState(@Header("token") String token, @Body UpdateDoorSensorStateBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/findMagneticStatus")
    Observable<CheckDoorSensorStateBeanRsp> checkDoorSensorState(@Header("token") String token, @Body CheckDoorSensorStateBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateApproachParameters")
    Observable<ChangeOpenLockParameterBeanRsp> changeOpenLockParameter(@Header("token") String token, @Body ChangeOpenLockParameterBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/mail/sendemailtoken")
    Observable<GetCodeBeanRsp> getCode(@Body GetCodeBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/user/reg/putuserbyemail")
    Observable<MailRegisterBeanRsp> register(@Body MailRegisterBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/ota/checkUpgrade")
    Observable<CheckOTABeanRsp> checkOtaVer(@Header("token") String token, @Body CheckOTABeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wifi/device/ota")
    Observable<StartOTAUpdateBeanRsp> startOtaUpdate(@Header("token") String token, @Body StartOTAUpdateBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/user/edit/forgetPwd")
    Observable<ForgotPwdRsp> forgotPwd(@Body ForgotPwdBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/user/edit/postUserPwd")
    Observable<ChangeUserPwdBeanRsp> changeUserPwd(@Header("token") String token, @Body ChangeUserPwdBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/gainKey")
    Observable<GainKeyBeanRsp> gainKey(@Header("token") String token, @Body GainKeyBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/list")
    Observable<GetAllSharedUserFromLockBeanRsp> getAllSharedUserFromLock(@Header("token") String token, @Body GetAllSharedUserFromLockBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/updateNickName")
    Observable<UpdateSharedUserNickNameBeanRsp> updateSharedUserNickName(@Header("token") String token, @Body UpdateSharedUserNickNameBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/delShareKey")
    Observable<DelInvalidShareBeanRsp> delInvalidShare(@Header("token") String token, @Body DelInvalidShareBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/updateEnable")
    Observable<EnableSharedUserBeanRsp> enableSharedUser(@Header("token") String token, @Body EnableSharedUserBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/delShareUser")
    Observable<DelSharedUserBeanRsp> delSharedUser(@Header("token") String token, @Body DelSharedUserBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/updateUserType")
    Observable<UpdateUserAuthorityTypeBeanRsp> updateUserAuthorityType(@Header("token") String token, @Body UpdateUserAuthorityTypeBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/userList")
    Observable<GetAllSharedUserFromAdminUserBeanRsp> getAllSharedUserFromAdminUser(@Header("token") String token, @Body GetAllSharedUserFromAdminUserBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/set/duressEmail")
    Observable<SettingDuressPwdReceiveEMailBeanRsp> settingDuressPwdReceiveEMail(@Header("token") String token, @Body SettingDuressPwdReceiveEMailBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/add")
    Observable<AcceptShareBeanRsp> acceptShare(@Header("token") String token, @Body AcceptShareBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/user/logout")
    Observable<LogoutBeanRsp> logout(@Header("token") String token, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/user/edit/postUserName")
    Observable<UpdateUserFirstLastNameBeanRsp> updateUserFirstLastName(@Header("token") String token, @Body UpdateUserFirstLastNameBeanReq req, @Header("url_name") String  urlName);

    @Multipart
    @Headers({"Content-Type: application/json"})
    @POST("/user/edit/uploaduserhead")
    Observable<UploadUserAvatarBeanRsp> uploadUserAvatar(@Header("token") String token, @PartMap Map<String, RequestBody> params, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/user/devList")
    Observable<GetDevicesFromUidAndSharedUidBeanRsp> getDevicesFromUidAndSharedUid(@Header("token") String token, @Body GetDevicesFromUidAndSharedUidBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/operation/list")
    Observable<LockRecordBeanRsp> getLockRecordList(@Header("token") String token, @Body LockRecordBeanReq req, @Header("url_name") String  urlName);

    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/uploadOperationList")
    Observable<UpdateLockRecordBeanRsp> updateLockRecordList(@Header("token") String token, @Body UpdateLockRecordBeanReq req, @Header("url_name") String  urlName);

}
