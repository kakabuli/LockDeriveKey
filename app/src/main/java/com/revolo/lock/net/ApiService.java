package com.revolo.lock.net;

import com.revolo.lock.bean.request.AcceptShareBeanReq;
import com.revolo.lock.bean.request.AdminAddDeviceBeanReq;
import com.revolo.lock.bean.request.AlexaAppUrlAndWebUrlReq;
import com.revolo.lock.bean.request.AlexaSkillEnableReq;
import com.revolo.lock.bean.request.AuthenticationBeanReq;
import com.revolo.lock.bean.request.ChangeBleVerBeanReq;
import com.revolo.lock.bean.request.ChangeDeviceHardVerBeanReq;
import com.revolo.lock.bean.request.ChangeDeviceNameBeanReq;
import com.revolo.lock.bean.request.ChangeFeaturesBeanReq;
import com.revolo.lock.bean.request.ChangeKeyNickBeanReq;
import com.revolo.lock.bean.request.ChangeOpenLockParameterBeanReq;
import com.revolo.lock.bean.request.ChangeUserPwdBeanReq;
import com.revolo.lock.bean.request.CheckAllOTABeanReq;
import com.revolo.lock.bean.request.CheckDoorSensorStateBeanReq;
import com.revolo.lock.bean.request.CheckOTABeanReq;
import com.revolo.lock.bean.request.DelDeviceBeanReq;
import com.revolo.lock.bean.request.DelInvalidShareBeanReq;
import com.revolo.lock.bean.request.DelKeyBeanReq;
import com.revolo.lock.bean.request.DelSharedUserBeanReq;
import com.revolo.lock.bean.request.DeleteDeviceTokenBeanReq;
import com.revolo.lock.bean.request.DeleteSystemMessageReq;
import com.revolo.lock.bean.request.DeviceTokenBeanReq;
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.bean.request.EnableSharedUserBeanReq;
import com.revolo.lock.bean.request.FeedBackBeanReq;
import com.revolo.lock.bean.request.ForgotPwdBeanReq;
import com.revolo.lock.bean.request.GainKeyBeanReq;
import com.revolo.lock.bean.request.GetAllSharedUserFromAdminUserBeanReq;
import com.revolo.lock.bean.request.GetAllSharedUserFromLockBeanReq;
import com.revolo.lock.bean.request.GetCodeBeanReq;
import com.revolo.lock.bean.request.GetDevicesFromUidAndSharedUidBeanReq;
import com.revolo.lock.bean.request.GetLockKeyNickBeanReq;
import com.revolo.lock.bean.request.GetNotDisturbModeBeanReq;
import com.revolo.lock.bean.request.GetPwd1BeanReq;
import com.revolo.lock.bean.request.GetVersionBeanReq;
import com.revolo.lock.bean.request.LockIsBindBeanReq;
import com.revolo.lock.bean.request.LockKeyAddBeanReq;
import com.revolo.lock.bean.request.LockRecordBeanReq;
import com.revolo.lock.bean.request.MailLoginBeanReq;
import com.revolo.lock.bean.request.MailRegisterBeanReq;
import com.revolo.lock.bean.request.Oauth2AccountBeanReq;
import com.revolo.lock.bean.request.OpenDoorRecordSearchBeanReq;
import com.revolo.lock.bean.request.PostNotDisturbModeBeanReq;
import com.revolo.lock.bean.request.SearchAlarmRecordBeanReq;
import com.revolo.lock.bean.request.SearchKeyListBeanReq;
import com.revolo.lock.bean.request.SearchProductNoBeanReq;
import com.revolo.lock.bean.request.SettingDuressPwdReceiveEMailBeanReq;
import com.revolo.lock.bean.request.StartAllOTAUpdateBeanReq;
import com.revolo.lock.bean.request.StartOTAUpdateBeanReq;
import com.revolo.lock.bean.request.SystemMessageListReq;
import com.revolo.lock.bean.request.UpdateDoorSensorStateBeanReq;
import com.revolo.lock.bean.request.UpdateLocalBeanReq;
import com.revolo.lock.bean.request.UpdateLockInfoReq;
import com.revolo.lock.bean.request.UpdateLockRecordBeanReq;
import com.revolo.lock.bean.request.UpdateSharedUserNickNameBeanReq;
import com.revolo.lock.bean.request.UpdateUserAuthorityTypeBeanReq;
import com.revolo.lock.bean.request.UpdateUserFirstLastNameBeanReq;
import com.revolo.lock.bean.request.UploadAlarmRecordBeanReq;
import com.revolo.lock.bean.request.UploadOpenDoorRecordBeanReq;
import com.revolo.lock.bean.request.UserByMailExistsBeanReq;
import com.revolo.lock.bean.respone.AcceptShareBeanRsp;
import com.revolo.lock.bean.respone.AdminAddDeviceBeanRsp;
import com.revolo.lock.bean.respone.AlexaAppUrlAndWebUrlBeanRsp;
import com.revolo.lock.bean.respone.AlexaSkillEnableBeanRsp;
import com.revolo.lock.bean.respone.AuthenticationBeanRsp;
import com.revolo.lock.bean.respone.ChangeBleVerBeanRsp;
import com.revolo.lock.bean.respone.ChangeDeviceHardVerBeanRsp;
import com.revolo.lock.bean.respone.ChangeDeviceNameBeanRsp;
import com.revolo.lock.bean.respone.ChangeFeaturesBeanRsp;
import com.revolo.lock.bean.respone.ChangeKeyNickBeanRsp;
import com.revolo.lock.bean.respone.ChangeOpenLockParameterBeanRsp;
import com.revolo.lock.bean.respone.ChangeUserPwdBeanRsp;
import com.revolo.lock.bean.respone.CheckAllOTABeanRsp;
import com.revolo.lock.bean.respone.CheckDoorSensorStateBeanRsp;
import com.revolo.lock.bean.respone.CheckOTABeanRsp;
import com.revolo.lock.bean.respone.DelDeviceBeanRsp;
import com.revolo.lock.bean.respone.DelInvalidShareBeanRsp;
import com.revolo.lock.bean.respone.DelKeyBeanRsp;
import com.revolo.lock.bean.respone.DelSharedUserBeanRsp;
import com.revolo.lock.bean.respone.DeviceTokenBeanRsp;
import com.revolo.lock.bean.respone.DeviceUnbindBeanRsp;
import com.revolo.lock.bean.respone.EnableSharedUserBeanRsp;
import com.revolo.lock.bean.respone.FeedBackBeanRsp;
import com.revolo.lock.bean.respone.ForgotPwdRsp;
import com.revolo.lock.bean.respone.GainKeyBeanRsp;
import com.revolo.lock.bean.respone.GetAllSharedUserFromAdminUserBeanRsp;
import com.revolo.lock.bean.respone.GetAllSharedUserFromLockBeanRsp;
import com.revolo.lock.bean.respone.GetCodeBeanRsp;
import com.revolo.lock.bean.respone.GetDevicesFromUidAndSharedUidBeanRsp;
import com.revolo.lock.bean.respone.GetLockKeyNickBeanRsp;
import com.revolo.lock.bean.respone.GetPwd1BeanRsp;
import com.revolo.lock.bean.respone.GetVersionBeanRsp;
import com.revolo.lock.bean.respone.LockIsBindBeanRsp;
import com.revolo.lock.bean.respone.LockKeyAddBeanRsp;
import com.revolo.lock.bean.respone.LockRecordBeanRsp;
import com.revolo.lock.bean.respone.LogoutBeanRsp;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.bean.respone.MailRegisterBeanRsp;
import com.revolo.lock.bean.respone.NotDisturbModeBeanRsp;
import com.revolo.lock.bean.respone.Oauth2AccountBeanRsp;
import com.revolo.lock.bean.respone.OpenDoorRecordSearchBeanRsp;
import com.revolo.lock.bean.respone.QuestionBeanRsp;
import com.revolo.lock.bean.respone.SearchAlarmRecordBeanRsp;
import com.revolo.lock.bean.respone.SearchKeyListBeanRsp;
import com.revolo.lock.bean.respone.SearchProductNoBeanRsp;
import com.revolo.lock.bean.respone.SettingDuressPwdReceiveEMailBeanRsp;
import com.revolo.lock.bean.respone.StartAllOTAUpdateBeanRsp;
import com.revolo.lock.bean.respone.StartOTAUpdateBeanRsp;
import com.revolo.lock.bean.respone.SystemMessageListBeanRsp;
import com.revolo.lock.bean.respone.UpdateDoorSensorStateBeanRsp;
import com.revolo.lock.bean.respone.UpdateLocalBeanRsp;
import com.revolo.lock.bean.respone.UpdateLockInfoRsp;
import com.revolo.lock.bean.respone.UpdateLockRecordBeanRsp;
import com.revolo.lock.bean.respone.UpdateSharedUserNickNameBeanRsp;
import com.revolo.lock.bean.respone.UpdateUserAuthorityTypeBeanRsp;
import com.revolo.lock.bean.respone.UpdateUserFirstLastNameBeanRsp;
import com.revolo.lock.bean.respone.UploadAlarmRecordBeanRsp;
import com.revolo.lock.bean.respone.UploadOpenDoorRecordBeanRsp;
import com.revolo.lock.bean.respone.UploadUserAvatarBeanRsp;
import com.revolo.lock.bean.respone.UserByMailExistsBeanRsp;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    /**
     * 邮箱登录
     *
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/login/getuserbymail")
    Observable<MailLoginBeanRsp> login(@Body MailLoginBeanReq req, @Header("url_name") String urlName);

    /**
     * 判断锁是否被绑定
     *
     * @param token   用户登录获取到的token值
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/checkadmindev")
    Observable<LockIsBindBeanRsp> lockIsBind(@Header("token") String token, @Body LockIsBindBeanReq req, @Header("url_name") String urlName);

    /**
     * 获取pwd1
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/model/getpwdBySN")
    Observable<GetPwd1BeanRsp> getPwd1(@Header("token") String token, @Body GetPwd1BeanReq req, @Header("url_name") String urlName);

    /**
     * 管理员添加设备
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/createadmindev")
    Observable<AdminAddDeviceBeanRsp> adminAddDevice(@Header("token") String token, @Body AdminAddDeviceBeanReq req, @Header("url_name") String urlName);

    /**
     * 设备解绑
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/unbind")
    Observable<DeviceUnbindBeanRsp> unbindDevice(@Header("token") String token, @Body DeviceUnbindBeanReq req, @Header("url_name") String urlName);

    /**
     * 修改蓝牙版本类型
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateBleVersionType")
    Observable<ChangeBleVerBeanRsp> changeBleVer(@Header("token") String token, @Body ChangeBleVerBeanReq req, @Header("url_name") String urlName);

    /**
     * 修改设备固件版本
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateBleVersion")
    Observable<ChangeDeviceHardVerBeanRsp> changeDeviceHardVer(@Header("token") String token, @Body ChangeDeviceHardVerBeanReq req, @Header("url_name") String urlName);

    /**
     * 修改功能集
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateFunctionSet")
    Observable<ChangeFeaturesBeanRsp> updateFunctionSet(@Header("token") String token, @Body ChangeFeaturesBeanReq req, @Header("url_name") String urlName);

    /**
     * 删除设备 todo 存疑，解绑可以达到同种效果，似无必要，联调时确定
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/deleteadmindev")
    Observable<DelDeviceBeanRsp> delDevice(@Header("token") String token, @Body DelDeviceBeanReq req, @Header("url_name") String urlName);

    /**
     * 修改设备昵称
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateAdminlockNickName")
    Observable<ChangeDeviceNameBeanRsp> changeDeviceNickName(@Header("token") String token, @Body ChangeDeviceNameBeanReq req, @Header("url_name") String urlName);

    /**
     * 搜索设备型号（模糊查询）
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/deviceModel/search")
    Observable<SearchProductNoBeanRsp> searchDevice(@Header("token") String token, @Body SearchProductNoBeanReq req, @Header("url_name") String urlName);

    /**
     * 上传开门记录
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/uploadopenlocklist")
    Observable<UploadOpenDoorRecordBeanRsp> uploadOpenDoorRecord(@Header("token") String token, @Body UploadOpenDoorRecordBeanReq req, @Header("url_name") String urlName);

    /**
     * 开门记录查询
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/findopenlockrecord")
    Observable<OpenDoorRecordSearchBeanRsp> searchOpenLockRecord(@Header("token") String token, @Body OpenDoorRecordSearchBeanReq req, @Header("url_name") String urlName);

    /**
     * 上传报警记录
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/uploadalarmlist")
    Observable<UploadAlarmRecordBeanRsp> uploadAlarmRecord(@Header("token") String token, @Body UploadAlarmRecordBeanReq req, @Header("url_name") String urlName);

    /**
     * 查询报警记录
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wifi/alarm/list")
    Observable<SearchAlarmRecordBeanRsp> searchAlarmRecord(@Header("token") String token, @Body SearchAlarmRecordBeanReq req, @Header("url_name") String urlName);

    /**
     * 门锁秘钥添加
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/pwdadd")
    Observable<LockKeyAddBeanRsp> addLockKey(@Header("token") String token, @Body LockKeyAddBeanReq req, @Header("url_name") String urlName);

    /**
     * 查询密钥列表
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/pwdlist")
    Observable<SearchKeyListBeanRsp> searchLockKey(@Header("token") String token, @Body SearchKeyListBeanReq req, @Header("url_name") String urlName);

    /**
     * 删除秘钥
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/pwddelete")
    Observable<DelKeyBeanRsp> delKey(@Header("token") String token, @Body DelKeyBeanReq req, @Header("url_name") String urlName);

    /**
     * 修改秘钥昵称
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/nicknameupdate")
    Observable<ChangeKeyNickBeanRsp> changeKeyNickName(@Header("token") String token, @Body ChangeKeyNickBeanReq req, @Header("url_name") String urlName);

    /**
     * 获取门锁秘钥昵称(单个)
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/getNickname")
    Observable<GetLockKeyNickBeanRsp> getKeyNickName(@Header("token") String token, @Body GetLockKeyNickBeanReq req, @Header("url_name") String urlName);

    /**
     * 更新门磁状态
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateMagneticStatus")
    Observable<UpdateDoorSensorStateBeanRsp> updateDoorSensorState(@Header("token") String token, @Body UpdateDoorSensorStateBeanReq req, @Header("url_name") String urlName);

    /**
     * 更新地理围栏数据
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/update/setlockelecfence")
    Observable<UpdateLocalBeanRsp> updateockeLecfence(@Header("token") String token, @Body UpdateLocalBeanReq req, @Header("url_name") String urlName);

    /**
     * 更新鉴权 pwd2
     *
     * @param token
     * @param req
     * @param urlName
     * @return
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wifi/device/passwordMod")
    Observable<AuthenticationBeanRsp> updateocAuthentication(@Header("token") String token, @Body AuthenticationBeanReq req, @Header("url_name") String urlName);


    /**
     * 查询门磁状态
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/findMagneticStatus")
    Observable<CheckDoorSensorStateBeanRsp> checkDoorSensorState(@Header("token") String token, @Body CheckDoorSensorStateBeanReq req, @Header("url_name") String urlName);

    /**
     * 修改无感开锁参数
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateApproachParameters")
    Observable<ChangeOpenLockParameterBeanRsp> changeOpenLockParameter(@Header("token") String token, @Body ChangeOpenLockParameterBeanReq req, @Header("url_name") String urlName);

    /**
     * 发送邮箱验证码
     *
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/mail/sendemailtoken")
    Observable<GetCodeBeanRsp> getCode(@Body GetCodeBeanReq req, @Header("url_name") String urlName);

    /**
     * 邮箱注册
     *
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/reg/putuserbyemail")
    Observable<MailRegisterBeanRsp> register(@Body MailRegisterBeanReq req, @Header("url_name") String urlName);

    /**
     * 检测升级文件（单组件）
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/ota/checkUpgrade")
    Observable<CheckOTABeanRsp> checkOtaVer(@Header("token") String token, @Body CheckOTABeanReq req, @Header("url_name") String urlName);

    /**
     * 确认升级（单设备单组件）
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wifi/device/ota")
    Observable<StartOTAUpdateBeanRsp> startOtaUpdate(@Header("token") String token, @Body StartOTAUpdateBeanReq req, @Header("url_name") String urlName);

    /**
     * 用户检查升级（多组件）
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/ota/multiCheckUpgrade")
    Observable<CheckAllOTABeanRsp> checkAllOtaVer(@Header("token") String token, @Body CheckAllOTABeanReq req, @Header("url_name") String urlName);

    /**
     * 确认升级（多组件）
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wifi/device/multiOta")
    Observable<StartAllOTAUpdateBeanRsp> startAllOtaUpdate(@Header("token") String token, @Body StartAllOTAUpdateBeanReq req, @Header("url_name") String urlName);

    /**
     * 忘记密码
     *
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/edit/forgetPwd")
    Observable<ForgotPwdRsp> forgotPwd(@Body ForgotPwdBeanReq req, @Header("url_name") String urlName);

    /**
     * 修改密码
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/edit/postUserPwd")
    Observable<ChangeUserPwdBeanRsp> changeUserPwd(@Header("token") String token, @Body ChangeUserPwdBeanReq req, @Header("url_name") String urlName);

    /**
     * 创建分享链接
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/gainKey")
    Observable<GainKeyBeanRsp> gainKey(@Header("token") String token, @Body GainKeyBeanReq req, @Header("url_name") String urlName);

    /**
     * 获取锁下的所有分享用户列表
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/list")
    Observable<GetAllSharedUserFromLockBeanRsp> getAllSharedUserFromLock(@Header("token") String token, @Body GetAllSharedUserFromLockBeanReq req, @Header("url_name") String urlName);

    /**
     * 修改分享用户昵称
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/updateNickName")
    Observable<UpdateSharedUserNickNameBeanRsp> updateSharedUserNickName(@Header("token") String token, @Body UpdateSharedUserNickNameBeanReq req, @Header("url_name") String urlName);

    /**
     * 删除无效分享链接
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/delShareKey")
    Observable<DelInvalidShareBeanRsp> delInvalidShare(@Header("token") String token, @Body DelInvalidShareBeanReq req, @Header("url_name") String urlName);

    /**
     * 启用/禁用分享用户权限
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/updateEnable")
    Observable<EnableSharedUserBeanRsp> enableSharedUser(@Header("token") String token, @Body EnableSharedUserBeanReq req, @Header("url_name") String urlName);

    /**
     * 删除分享用户
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/delShareUser")
    Observable<DelSharedUserBeanRsp> delSharedUser(@Header("token") String token, @Body DelSharedUserBeanReq req, @Header("url_name") String urlName);

    /**
     * 修改邀请用户类型
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/updateUserType")
    Observable<UpdateUserAuthorityTypeBeanRsp> updateUserAuthorityType(@Header("token") String token, @Body UpdateUserAuthorityTypeBeanReq req, @Header("url_name") String urlName);

    /**
     * 获取管理员下的所有分享用户
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/userList")
    Observable<GetAllSharedUserFromAdminUserBeanRsp> getAllSharedUserFromAdminUser(@Header("token") String token, @Body GetAllSharedUserFromAdminUserBeanReq req, @Header("url_name") String urlName);

    /**
     * 设置胁迫密码邮箱
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/set/duressEmail")
    Observable<SettingDuressPwdReceiveEMailBeanRsp> settingDuressPwdReceiveEMail(@Header("token") String token, @Body SettingDuressPwdReceiveEMailBeanReq req, @Header("url_name") String urlName);

    /**
     * 接收邀请
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/share/add")
    Observable<AcceptShareBeanRsp> acceptShare(@Header("token") String token, @Body AcceptShareBeanReq req, @Header("url_name") String urlName);

    /**
     * 登出
     *
     * @param token   用户权限码
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/logout")
    Observable<LogoutBeanRsp> logout(@Header("token") String token, @Header("url_name") String urlName);

    /**
     * 设置/修改用户名称
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/edit/postUserName")
    Observable<UpdateUserFirstLastNameBeanRsp> updateUserFirstLastName(@Header("token") String token, @Body UpdateUserFirstLastNameBeanReq req, @Header("url_name") String urlName);

    /**
     * 上传头像
     *
     * @param token   用户权限码
     * @param partLis 图片文件
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
//    @Headers({"Content-Type: multipart/form-data"})
    @Multipart
    @POST("/user/edit/uploadUserAvatar")
    Observable<UploadUserAvatarBeanRsp> uploadUserAvatar(@Header("token") String token, @Part List<MultipartBody.Part> partLis, @Header("url_name") String urlName);

    /**
     * 获取分享用户的设备列表
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/user/devList")
    Observable<GetDevicesFromUidAndSharedUidBeanRsp> getDevicesFromUidAndSharedUid(@Header("token") String token, @Body GetDevicesFromUidAndSharedUidBeanReq req, @Header("url_name") String urlName);

    /**
     * 获取操作记录
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/operation/list")
    Observable<LockRecordBeanRsp> getLockRecordList(@Header("token") String token, @Body LockRecordBeanReq req, @Header("url_name") String urlName);

    /**
     * 上传操作记录
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/uploadOperationList")
    Observable<UpdateLockRecordBeanRsp> updateLockRecordList(@Header("token") String token, @Body UpdateLockRecordBeanReq req, @Header("url_name") String urlName);

    /**
     * 用户反馈接口
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/suggest/putmsg")
    Observable<FeedBackBeanRsp> feedback(@Header("token") String token, @Body FeedBackBeanReq req, @Header("url_name") String urlName);

    @Headers({"Content-type:application/json"})
    @GET("/FAQ/list/{languageType}")
    Observable<QuestionBeanRsp> faqList(@Header("token") String token, @Path("languageType") int languageType);

    /**
     * 更新锁属性
     *
     * @param token   用户权限码
     * @param req     请求实体
     * @param urlName 用于区分，可能后期替换不一样的接口
     */
    @Headers({"Content-Type: application/json"})
    @POST("/wpflock/device/updateLockAttributes")
    Observable<UpdateLockInfoRsp> updateLockInfo(@Header("token") String token, @Body UpdateLockInfoReq req, @Header("url_name") String urlName);

    /**
     * 系统消息列表
     *
     * @param token 用户权限码
     * @param req   请求实体
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/notification/record")
    Observable<SystemMessageListBeanRsp> systemMessageList(@Header("token") String token, @Body SystemMessageListReq req);

    /**
     * 系统消息删除
     *
     * @param token 用户权限码
     * @param req   请求实体
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/del/notification/record")
    Observable<DelInvalidShareBeanRsp> systemMessageDelete(@Header("token") String token, @Body DeleteSystemMessageReq req);

    /**
     * 获取Alexa App和LWA Web登陆url
     *
     * @param token 用户权限码
     * @param req   请求实体
     */
    @Headers({"Content-Type: application/json"})
    @POST("/smart/authorize")
    Observable<AlexaAppUrlAndWebUrlBeanRsp> getAppUrlAndWebUrl(@Header("token") String token, @Body AlexaAppUrlAndWebUrlReq req);

    /**
     * 关联AlexaSkill并启用技能
     *
     * @param token 用户权限码
     * @param req   请求实体
     */
    @Headers({"Content-Type: application/json"})
    @POST("/zetark-oauth2-server/skillEnable")
    Observable<AlexaSkillEnableBeanRsp> skillEnable(@Header("token") String token, @Body AlexaSkillEnableReq req);

    /**
     * 添加推送token
     *
     * @param token 用户权限码
     * @param req   请求实体
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/upload/deviceToken")
    Observable<DeviceTokenBeanRsp> deviceToken(@Header("token") String token, @Body DeviceTokenBeanReq req);

    /**
     * 删除推送token
     *
     * @param token 用户权限码
     * @param req   请求实体
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/del/deviceToken")
    Observable<DeviceTokenBeanRsp> deleteDeviceToken(@Header("token") String token, @Body DeleteDeviceTokenBeanReq req);

    /**
     * 验证邮箱是否注册
     *
     * @param req 请求实体
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/login/getuserbymailexists")
    Observable<UserByMailExistsBeanRsp> getUserByMailExists(@Body UserByMailExistsBeanReq req);

    /**
     * 设置勿打扰模式
     *
     * @param token 用户权限码
     * @param req   请求实体
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/edit/postPushSwitch")
    Observable<NotDisturbModeBeanRsp> postPushSwitch(@Header("token") String token, @Body PostNotDisturbModeBeanReq req);

    /**
     * 勿打扰模式状态
     *
     * @param token 用户权限码
     * @param req   请求实体
     */
    @Headers({"Content-Type: application/json"})
    @POST("/user/get/getPushSwitch")
    Observable<NotDisturbModeBeanRsp> getPushSwitch(@Header("token") String token, @Body GetNotDisturbModeBeanReq req);

    /**
     * 获取app版本信息
     *
     * @param token 用户权限码
     * @param req   请求实体
     */
    @Headers({"Content-Type: application/json"})
    @POST("/boss/versions/get")
    Observable<GetVersionBeanRsp> getVersion(@Header("token") String token, @Body GetVersionBeanReq req);

    /**
     * 获取用户第三方账户授权信息
     *
     * @param token 用户权限码
     * @param req   请求实体
     */
    @Headers({"Content-Type: application/json"})
    @POST("/smart/oauth2/account")
    Observable<Oauth2AccountBeanRsp> oauth2Account(@Header("token") String token, @Body Oauth2AccountBeanReq req);
}
