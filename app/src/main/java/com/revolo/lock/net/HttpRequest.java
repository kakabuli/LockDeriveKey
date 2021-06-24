package com.revolo.lock.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.revolo.lock.bean.request.AcceptShareBeanReq;
import com.revolo.lock.bean.request.AdminAddDeviceBeanReq;
import com.revolo.lock.bean.request.AlexaAppUrlAndWebUrlReq;
import com.revolo.lock.bean.request.AlexaSkillEnableReq;
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
import com.revolo.lock.bean.request.DeleteSystemMessageReq;
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
import com.revolo.lock.bean.request.StartAllOTAUpdateBeanReq;
import com.revolo.lock.bean.request.StartOTAUpdateBeanReq;
import com.revolo.lock.bean.request.SystemMessageListReq;
import com.revolo.lock.bean.request.UpdateDoorSensorStateBeanReq;
import com.revolo.lock.bean.request.UpdateLockInfoReq;
import com.revolo.lock.bean.request.UpdateLockRecordBeanReq;
import com.revolo.lock.bean.request.UpdateSharedUserNickNameBeanReq;
import com.revolo.lock.bean.request.UpdateUserAuthorityTypeBeanReq;
import com.revolo.lock.bean.request.UpdateUserFirstLastNameBeanReq;
import com.revolo.lock.bean.request.UploadAlarmRecordBeanReq;
import com.revolo.lock.bean.request.UploadOpenDoorRecordBeanReq;
import com.revolo.lock.bean.respone.AcceptShareBeanRsp;
import com.revolo.lock.bean.respone.AdminAddDeviceBeanRsp;
import com.revolo.lock.bean.respone.AlexaAppUrlAndWebUrlBeanRsp;
import com.revolo.lock.bean.respone.AlexaSkillEnableBeanRsp;
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
import com.revolo.lock.bean.respone.LockIsBindBeanRsp;
import com.revolo.lock.bean.respone.LockKeyAddBeanRsp;
import com.revolo.lock.bean.respone.LockRecordBeanRsp;
import com.revolo.lock.bean.respone.LogoutBeanRsp;
import com.revolo.lock.bean.respone.MailLoginBeanRsp;
import com.revolo.lock.bean.respone.MailRegisterBeanRsp;
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
import com.revolo.lock.bean.respone.UpdateLockInfoRsp;
import com.revolo.lock.bean.respone.UpdateLockRecordBeanRsp;
import com.revolo.lock.bean.respone.UpdateSharedUserNickNameBeanRsp;
import com.revolo.lock.bean.respone.UpdateUserAuthorityTypeBeanRsp;
import com.revolo.lock.bean.respone.UpdateUserFirstLastNameBeanRsp;
import com.revolo.lock.bean.respone.UploadAlarmRecordBeanRsp;
import com.revolo.lock.bean.respone.UploadOpenDoorRecordBeanRsp;
import com.revolo.lock.bean.respone.UploadUserAvatarBeanRsp;

import java.io.File;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpRequest {

    private final ApiService service;

    private static final String HOST_ALPHA = "https://api.irevolohome.com:443";                   // alpha 生产服务器
    private static final String HOST_TEST = "https://internal.irevolo.com:8090";                    // 国内服务器测试接口
    private static final String ABROAD_HOST = "https://revolotest.sfeiya.com:8090";                      // 海外服务器测试接口
    private static final String LOCAL_HOST_248 = "https://192.168.118.248:443";                           // 长沙本地服务器测试接口
    private static final String LOCAL_HOST_249 = "https://192.168.118.249:443";                           // 长沙本地服务器测试接口2
    public static final String HOST = LOCAL_HOST_249;
    private static final String CHECK_OTA_HOST_TEST = "https://test1.juziwulian.com:9111";          // 国内服务器测试接口
    private static final String CHECK_OTA_HOST_ABROAD = "https://ota-global.juziwulian.com:9111";   // 海外服务器接口
    public static final String CHECK_OTA_HOST = CHECK_OTA_HOST_ABROAD;

    private static HttpRequest ourInstance;

    public static HttpRequest getInstance() {
        if (ourInstance == null) {
            synchronized (HttpRequest.class) {
                if (ourInstance == null) {
                    ourInstance = new HttpRequest();
                }
            }
        }
        return ourInstance;
    }

    private HttpRequest() {

        // TODO: 2021/1/26 现在是忽略证书，后期需要修正
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(new HttpLogger());//创建拦截对象
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);//这一句一定要记得写，否则没有数据输出
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(logInterceptor)
                .addInterceptor(new ChangeUrlInterceptor())
                .connectTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                })//配置
                .hostnameVerifier(SSLSocketClient.getHostnameVerifier())//配置
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST)
                .addConverterFactory(GsonConverterFactory.create(gson))    // Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())    // RxJava
                .client(client)
                .build();

        service = retrofit.create(ApiService.class);

    }

    private static final String NORMAL = "normal";
    private static final String OTA_CHECK = "otaCheck";

    public Observable<MailLoginBeanRsp> login(MailLoginBeanReq req) {
        return service.login(req, NORMAL);
    }

    public Observable<LockIsBindBeanRsp> lockIsBind(String token, LockIsBindBeanReq req) {
        return service.lockIsBind(token, req, NORMAL);
    }

    ;

    public Observable<GetPwd1BeanRsp> getPwd1(String token, GetPwd1BeanReq req) {
        return service.getPwd1(token, req, NORMAL);
    }

    ;

    public Observable<AdminAddDeviceBeanRsp> adminAddDevice(String token, AdminAddDeviceBeanReq req) {
        return service.adminAddDevice(token, req, NORMAL);
    }

    ;

    public Observable<DeviceUnbindBeanRsp> unbindDevice(String token, DeviceUnbindBeanReq req) {
        return service.unbindDevice(token, req, NORMAL);
    }

    ;

    public Observable<ChangeBleVerBeanRsp> changeBleVer(String token, ChangeBleVerBeanReq req) {
        return service.changeBleVer(token, req, NORMAL);
    }

    ;

    public Observable<ChangeDeviceHardVerBeanRsp> changeDeviceHardVer(String token, ChangeDeviceHardVerBeanReq req) {
        return service.changeDeviceHardVer(token, req, NORMAL);
    }

    ;

    public Observable<ChangeFeaturesBeanRsp> updateFunctionSet(String token, ChangeFeaturesBeanReq req) {
        return service.updateFunctionSet(token, req, NORMAL);
    }

    ;

    public Observable<DelDeviceBeanRsp> delDevice(String token, DelDeviceBeanReq req) {
        return service.delDevice(token, req, NORMAL);
    }

    ;

    public Observable<ChangeDeviceNameBeanRsp> changeDeviceNickName(String token, ChangeDeviceNameBeanReq req) {
        return service.changeDeviceNickName(token, req, NORMAL);
    }

    ;

    public Observable<SearchProductNoBeanRsp> searchDevice(String token, SearchProductNoBeanReq req) {
        return service.searchDevice(token, req, NORMAL);
    }

    ;

    public Observable<UploadOpenDoorRecordBeanRsp> uploadOpenDoorRecord(String token, UploadOpenDoorRecordBeanReq req) {
        return service.uploadOpenDoorRecord(token, req, NORMAL);
    }

    ;

    public Observable<OpenDoorRecordSearchBeanRsp> searchOpenLockRecord(String token, OpenDoorRecordSearchBeanReq req) {
        return service.searchOpenLockRecord(token, req, NORMAL);
    }

    ;

    public Observable<UploadAlarmRecordBeanRsp> uploadAlarmRecord(String token, UploadAlarmRecordBeanReq req) {
        return service.uploadAlarmRecord(token, req, NORMAL);
    }

    ;

    public Observable<SearchAlarmRecordBeanRsp> searchAlarmRecord(String token, SearchAlarmRecordBeanReq req) {
        return service.searchAlarmRecord(token, req, NORMAL);
    }

    ;

    public Observable<LockKeyAddBeanRsp> addLockKey(String token, LockKeyAddBeanReq req) {
        return service.addLockKey(token, req, NORMAL);
    }

    ;

    public Observable<SearchKeyListBeanRsp> searchLockKey(String token, SearchKeyListBeanReq req) {
        return service.searchLockKey(token, req, NORMAL);
    }

    ;

    public Observable<DelKeyBeanRsp> delKey(String token, DelKeyBeanReq req) {
        return service.delKey(token, req, NORMAL);
    }

    ;

    public Observable<ChangeKeyNickBeanRsp> changeKeyNickName(String token, ChangeKeyNickBeanReq req) {
        return service.changeKeyNickName(token, req, NORMAL);
    }

    ;

    public Observable<GetLockKeyNickBeanRsp> getKeyNickName(String token, GetLockKeyNickBeanReq req) {
        return service.getKeyNickName(token, req, NORMAL);
    }

    ;

    public Observable<UpdateDoorSensorStateBeanRsp> updateDoorSensorState(String token, UpdateDoorSensorStateBeanReq req) {
        return service.updateDoorSensorState(token, req, NORMAL);
    }

    ;

    public Observable<CheckDoorSensorStateBeanRsp> checkDoorSensorState(String token, CheckDoorSensorStateBeanReq req) {
        return service.checkDoorSensorState(token, req, NORMAL);
    }

    ;

    public Observable<ChangeOpenLockParameterBeanRsp> changeOpenLockParameter(String token, ChangeOpenLockParameterBeanReq req) {
        return service.changeOpenLockParameter(token, req, NORMAL);
    }

    ;

    public Observable<GetCodeBeanRsp> getCode(GetCodeBeanReq req) {
        return service.getCode(req, NORMAL);
    }

    public Observable<MailRegisterBeanRsp> register(MailRegisterBeanReq req) {
        return service.register(req, NORMAL);
    }

    public Observable<CheckOTABeanRsp> checkOtaVer(String token, CheckOTABeanReq req) {
        return service.checkOtaVer(token, req, NORMAL);
    }

    public Observable<StartOTAUpdateBeanRsp> startOtaUpdate(String toke, StartOTAUpdateBeanReq req) {
        return service.startOtaUpdate(toke, req, NORMAL);
    }

    public Observable<CheckAllOTABeanRsp> checkAllOtaVer(String token, CheckAllOTABeanReq req) {
        return service.checkAllOtaVer(token, req, NORMAL);
    }

    public Observable<StartAllOTAUpdateBeanRsp> startAllOtaUpdate(String toke, StartAllOTAUpdateBeanReq req) {
        return service.startAllOtaUpdate(toke, req, NORMAL);
    }

    public Observable<ForgotPwdRsp> forgotPwd(ForgotPwdBeanReq req) {
        return service.forgotPwd(req, NORMAL);
    }

    public Observable<ChangeUserPwdBeanRsp> changeUserPwd(String token, ChangeUserPwdBeanReq req) {
        return service.changeUserPwd(token, req, NORMAL);
    }

    public Observable<GainKeyBeanRsp> gainKey(String token, GainKeyBeanReq req) {
        return service.gainKey(token, req, NORMAL);
    }

    public Observable<GetAllSharedUserFromLockBeanRsp> getAllSharedUserFromLock(String token, GetAllSharedUserFromLockBeanReq req) {
        return service.getAllSharedUserFromLock(token, req, NORMAL);
    }

    public Observable<UpdateSharedUserNickNameBeanRsp> updateSharedUserNickName(String token, UpdateSharedUserNickNameBeanReq req) {
        return service.updateSharedUserNickName(token, req, NORMAL);
    }

    ;

    public Observable<DelInvalidShareBeanRsp> delInvalidShare(String token, DelInvalidShareBeanReq req) {
        return service.delInvalidShare(token, req, NORMAL);
    }

    ;

    public Observable<EnableSharedUserBeanRsp> enableSharedUser(String token, EnableSharedUserBeanReq req) {
        return service.enableSharedUser(token, req, NORMAL);
    }

    ;

    public Observable<DelSharedUserBeanRsp> delSharedUser(String token, DelSharedUserBeanReq req) {
        return service.delSharedUser(token, req, NORMAL);
    }

    ;

    public Observable<UpdateUserAuthorityTypeBeanRsp> updateUserAuthorityType(String token, UpdateUserAuthorityTypeBeanReq req) {
        return service.updateUserAuthorityType(token, req, NORMAL);
    }

    ;

    public Observable<GetAllSharedUserFromAdminUserBeanRsp> getAllSharedUserFromAdminUser(String token, GetAllSharedUserFromAdminUserBeanReq req) {
        return service.getAllSharedUserFromAdminUser(token, req, NORMAL);
    }

    ;

    public Observable<SettingDuressPwdReceiveEMailBeanRsp> settingDuressPwdReceiveEMail(String token, SettingDuressPwdReceiveEMailBeanReq req) {
        return service.settingDuressPwdReceiveEMail(token, req, NORMAL);
    }

    ;

    public Observable<AcceptShareBeanRsp> acceptShare(String token, AcceptShareBeanReq req) {
        return service.acceptShare(token, req, NORMAL);
    }

    ;

    public Observable<LogoutBeanRsp> logout(String token) {
        return service.logout(token, NORMAL);
    }

    public Observable<UpdateUserFirstLastNameBeanRsp> updateUserFirstLastName(String token, UpdateUserFirstLastNameBeanReq req) {
        return service.updateUserFirstLastName(token, req, NORMAL);
    }

    public Observable<UploadUserAvatarBeanRsp> uploadUserAvatar(String token, String uid, File file) {
        //1.创建MultipartBody.Builder对象
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM); //表单类型
        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), file);//表单类型

        //3.调用MultipartBody.Builder的addFormDataPart()方法添加表单数据
        builder.addFormDataPart("uid", uid);//传入服务器需要的key，和相应value值
        builder.addFormDataPart("file", file.getName(), body); //添加图片数据，body创建的请求体

        //4.创建List<MultipartBody.Part> 集合，
        //  调用MultipartBody.Builder的build()方法会返回一个新创建的MultipartBody
        //  再调用MultipartBody的parts()方法返回MultipartBody.Part集合
        List<MultipartBody.Part> parts = builder.build().parts();
        return service.uploadUserAvatar(token, parts, NORMAL);
    }

    public Observable<GetDevicesFromUidAndSharedUidBeanRsp> getDevicesFromUidAndSharedUid(String token, GetDevicesFromUidAndSharedUidBeanReq req) {
        return service.getDevicesFromUidAndSharedUid(token, req, NORMAL);
    }

    public Observable<LockRecordBeanRsp> getLockRecordList(String token, LockRecordBeanReq req) {
        return service.getLockRecordList(token, req, NORMAL);
    }

    public Observable<UpdateLockRecordBeanRsp> updateLockRecordList(String token, UpdateLockRecordBeanReq req) {
        return service.updateLockRecordList(token, req, NORMAL);
    }

    public Observable<FeedBackBeanRsp> feedback(String token, FeedBackBeanReq req) {
        return service.feedback(token, req, NORMAL);
    }

    public Observable<QuestionBeanRsp> faqList(String token, int languageType) {
        return service.faqList(token, languageType);
    }

    public Observable<UpdateLockInfoRsp> updateLockInfo(String token, UpdateLockInfoReq req) {
        return service.updateLockInfo(token, req, NORMAL);
    }

    public Observable<SystemMessageListBeanRsp> systemMessageList(String token, SystemMessageListReq req) {
        return service.systemMessageList(token, req);
    }

    public Observable<SystemMessageListBeanRsp> deleteSystemMessage(String token, DeleteSystemMessageReq req) {
        return service.systemMessageDelete(token, req);
    }

    public Observable<AlexaAppUrlAndWebUrlBeanRsp> getAppUrlAndWebUrl(String token, AlexaAppUrlAndWebUrlReq req) {
        return service.getAppUrlAndWebUrl(token, req);
    }

    public Observable<AlexaSkillEnableBeanRsp> skillEnable(String token, AlexaSkillEnableReq req) {
        return service.skillEnable(token, req);
    }
}
