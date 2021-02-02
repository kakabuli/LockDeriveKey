package com.revolo.lock.net;


import com.revolo.lock.bean.request.AdminAddDeviceBeanReq;
import com.revolo.lock.bean.request.ChangeBleVerBeanReq;
import com.revolo.lock.bean.request.ChangeDeviceHardVerBeanReq;
import com.revolo.lock.bean.request.ChangeDeviceNameBeanReq;
import com.revolo.lock.bean.request.ChangeFeaturesBeanReq;
import com.revolo.lock.bean.request.ChangeKeyNickBeanReq;
import com.revolo.lock.bean.request.ChangeOpenLockParameterBeanReq;
import com.revolo.lock.bean.request.CheckDoorSensorStateBeanReq;
import com.revolo.lock.bean.request.DelDeviceBeanReq;
import com.revolo.lock.bean.request.DelKeyBeanReq;
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.bean.request.GetCodeBeanReq;
import com.revolo.lock.bean.request.GetLockKeyNickBeanReq;
import com.revolo.lock.bean.request.GetPwd1BeanReq;
import com.revolo.lock.bean.request.LockIsBindBeanReq;
import com.revolo.lock.bean.request.LockKeyAddBeanReq;
import com.revolo.lock.bean.request.MailLoginBeanReq;
import com.revolo.lock.bean.request.MailRegisterBeanReq;
import com.revolo.lock.bean.request.OpenDoorRecordSearchBeanReq;
import com.revolo.lock.bean.request.SearchAlarmRecordBeanReq;
import com.revolo.lock.bean.request.SearchKeyListBeanReq;
import com.revolo.lock.bean.request.SearchProductNoBeanReq;
import com.revolo.lock.bean.request.UpdateDoorSensorStateBeanReq;
import com.revolo.lock.bean.request.UploadAlarmRecordBeanReq;
import com.revolo.lock.bean.request.UploadOpenDoorRecordBeanReq;
import com.revolo.lock.bean.respone.AdminAddDeviceBeanRsp;
import com.revolo.lock.bean.respone.ChangeBleVerBeanRsp;
import com.revolo.lock.bean.respone.ChangeDeviceHardVerBeanRsp;
import com.revolo.lock.bean.respone.ChangeDeviceNameBeanRsp;
import com.revolo.lock.bean.respone.ChangeFeaturesBeanRsp;
import com.revolo.lock.bean.respone.ChangeKeyNickBeanRsp;
import com.revolo.lock.bean.respone.ChangeOpenLockParameterBeanRsp;
import com.revolo.lock.bean.respone.CheckDoorSensorStateBeanRsp;
import com.revolo.lock.bean.respone.DelDeviceBeanRsp;
import com.revolo.lock.bean.respone.DelKeyBeanRsp;
import com.revolo.lock.bean.respone.DeviceUnbindBeanRsp;
import com.revolo.lock.bean.respone.GetCodeBeanRsp;
import com.revolo.lock.bean.respone.GetLockKeyNickBeanRsp;
import com.revolo.lock.bean.respone.GetPwd1BeanRsp;
import com.revolo.lock.bean.respone.LockIsBindBeanRsp;
import com.revolo.lock.bean.respone.LockKeyAddBeanRsp;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.bean.respone.MailRegisterBeanRsp;
import com.revolo.lock.bean.respone.OpenDoorRecordSearchBeanRsp;
import com.revolo.lock.bean.respone.SearchAlarmRecordBeanRsp;
import com.revolo.lock.bean.respone.SearchKeyListBeanRsp;
import com.revolo.lock.bean.respone.SearchProductNoBeanRsp;
import com.revolo.lock.bean.respone.UpdateDoorSensorStateBeanRsp;
import com.revolo.lock.bean.respone.UploadAlarmRecordBeanRsp;
import com.revolo.lock.bean.respone.UploadOpenDoorRecordBeanRsp;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    @Headers({"Content-Type: application/json"})
    @POST("/user/login/getuserbymail")
    Observable<MailLoginBeanRsp> login(@Body MailLoginBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/checkadmindev")
    Observable<LockIsBindBeanRsp> lockIsBind(@Header("token") String token, @Body LockIsBindBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/model/getpwdBySN")
    Observable<GetPwd1BeanRsp> getPwd1(@Header("token") String token, @Body GetPwd1BeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/createadmindev")
    Observable<AdminAddDeviceBeanRsp> adminAddDevice(@Header("token") String token, @Body AdminAddDeviceBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/unbind")
    Observable<DeviceUnbindBeanRsp> unbindDevice(@Header("token") String token, @Body DeviceUnbindBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/updateBleVersionType")
    Observable<ChangeBleVerBeanRsp> changeBleVer(@Header("token") String token, @Body ChangeBleVerBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/updateBleVersion")
    Observable<ChangeDeviceHardVerBeanRsp> changeDeviceHardVer(@Header("token") String token, @Body ChangeDeviceHardVerBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/updateFunctionSet")
    Observable<ChangeFeaturesBeanRsp> updateFunctionSet(@Header("token") String token, @Body ChangeFeaturesBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/deleteadmindev")
    Observable<DelDeviceBeanRsp> delDevice(@Header("token") String token, @Body DelDeviceBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/updateAdminlockNickName")
    Observable<ChangeDeviceNameBeanRsp> changeDeviceNickName(@Header("token") String token, @Body ChangeDeviceNameBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/deviceModel/search")
    Observable<SearchProductNoBeanRsp> searchDevice(@Header("token") String token, @Body SearchProductNoBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/uploadopenlocklist")
    Observable<UploadOpenDoorRecordBeanRsp> uploadOpenDoorRecord(@Header("token") String token, @Body UploadOpenDoorRecordBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/findopenlockrecord")
    Observable<OpenDoorRecordSearchBeanRsp> searchOpenLockRecord(@Header("token") String token, @Body OpenDoorRecordSearchBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/uploadalarmlist")
    Observable<UploadAlarmRecordBeanRsp> uploadAlarmRecord(@Header("token") String token, @Body UploadAlarmRecordBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wifi/alarm/list")
    Observable<SearchAlarmRecordBeanRsp> searchAlarmRecord(@Header("token") String token, @Body SearchAlarmRecordBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/pwdadd")
    Observable<LockKeyAddBeanRsp> addLockKey(@Header("token") String token, @Body LockKeyAddBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/pwdlist")
    Observable<SearchKeyListBeanRsp> searchLockKey(@Header("token") String token, @Body SearchKeyListBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/pwddelete")
    Observable<DelKeyBeanRsp> delKey(@Header("token") String token, @Body DelKeyBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/nicknameupdate")
    Observable<ChangeKeyNickBeanRsp> changeKeyNickName(@Header("token") String token, @Body ChangeKeyNickBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/getNickname")
    Observable<GetLockKeyNickBeanRsp> getKeyNickName(@Header("token") String token, @Body GetLockKeyNickBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/updateMagneticStatus")
    Observable<UpdateDoorSensorStateBeanRsp> updateDoorSensorState(@Header("token") String token, @Body UpdateDoorSensorStateBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/findMagneticStatus")
    Observable<CheckDoorSensorStateBeanRsp> checkDoorSensorState(@Header("token") String token, @Body CheckDoorSensorStateBeanReq req);

    @Headers({"Content-Type: application/json", "token: {token}"})
    @POST("/wpflock/device/updateApproachParameters")
    Observable<ChangeOpenLockParameterBeanRsp> changeOpenLockParameter(@Header("token") String token, @Body ChangeOpenLockParameterBeanReq req);

    @Headers({"Content-Type: application/json"})
    @POST("/mail/sendemailtoken")
    Observable<GetCodeBeanRsp> getCode(@Body GetCodeBeanReq req);

    @Headers({"Content-Type: application/json"})
    @POST("/user/reg/putuserbyemail")
    Observable<MailRegisterBeanRsp> register(@Body MailRegisterBeanReq req);

}
