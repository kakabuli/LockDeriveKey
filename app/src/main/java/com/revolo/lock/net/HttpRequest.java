package com.revolo.lock.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.revolo.lock.bean.request.AdminAddDeviceBeanReq;
import com.revolo.lock.bean.request.ChangeBleVerBeanReq;
import com.revolo.lock.bean.request.ChangeDeviceHardVerBeanReq;
import com.revolo.lock.bean.request.ChangeDeviceNameBeanReq;
import com.revolo.lock.bean.request.ChangeFeaturesBeanReq;
import com.revolo.lock.bean.request.ChangeKeyNickBeanReq;
import com.revolo.lock.bean.request.ChangeOpenLockParameterBeanReq;
import com.revolo.lock.bean.request.CheckDoorSensorStateBeanReq;
import com.revolo.lock.bean.request.CheckOTABeanReq;
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
import com.revolo.lock.bean.request.StartOTAUpdateBeanReq;
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
import com.revolo.lock.bean.respone.CheckOTABeanRsp;
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
import com.revolo.lock.bean.respone.StartOTAUpdateBeanRsp;
import com.revolo.lock.bean.respone.UpdateDoorSensorStateBeanRsp;
import com.revolo.lock.bean.respone.UploadAlarmRecordBeanRsp;
import com.revolo.lock.bean.respone.UploadOpenDoorRecordBeanRsp;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.X509TrustManager;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpRequest {

    private final ApiService service;

    private static final String HOST_TEST = "https://test1.juziwulian.com:8090";      // 国内服务器测试接口
    private static final String ABROAD_HOST = "https://test.irevolo.com:8090";        // 海外服务器测试接口
    public static final String HOST = ABROAD_HOST;
    private static final String CHECK_OTA_HOST_TEST = "https://test1.juziwulian.com:9111";
    private static final String CHECK_OTA_HOST_ABROAD = "https://test.irevolo.com:9111";
    public static final String CHECK_OTA_HOST =  CHECK_OTA_HOST_ABROAD;
    

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
    };

    public Observable<GetPwd1BeanRsp> getPwd1(String token, GetPwd1BeanReq req){
        return service.getPwd1(token, req, NORMAL);
    };

    public Observable<AdminAddDeviceBeanRsp> adminAddDevice(String token, AdminAddDeviceBeanReq req){
        return service.adminAddDevice(token, req, NORMAL);
    };

    public Observable<DeviceUnbindBeanRsp> unbindDevice(String token, DeviceUnbindBeanReq req){
        return service.unbindDevice(token, req, NORMAL);
    };

    public Observable<ChangeBleVerBeanRsp> changeBleVer(String token, ChangeBleVerBeanReq req){
        return service.changeBleVer(token, req, NORMAL);
    };

    public Observable<ChangeDeviceHardVerBeanRsp> changeDeviceHardVer(String token, ChangeDeviceHardVerBeanReq req){
        return service.changeDeviceHardVer(token, req, NORMAL);
    };

    public Observable<ChangeFeaturesBeanRsp> updateFunctionSet(String token, ChangeFeaturesBeanReq req){
        return service.updateFunctionSet(token, req, NORMAL);
    };

    public Observable<DelDeviceBeanRsp> delDevice(String token, DelDeviceBeanReq req){
        return service.delDevice(token, req, NORMAL);
    };

    public Observable<ChangeDeviceNameBeanRsp> changeDeviceNickName(String token, ChangeDeviceNameBeanReq req){
        return service.changeDeviceNickName(token, req, NORMAL);
    };

    public Observable<SearchProductNoBeanRsp> searchDevice(String token, SearchProductNoBeanReq req){
        return service.searchDevice(token, req, NORMAL);
    };

    public Observable<UploadOpenDoorRecordBeanRsp> uploadOpenDoorRecord(String token, UploadOpenDoorRecordBeanReq req){
        return service.uploadOpenDoorRecord(token, req, NORMAL);
    };

    public Observable<OpenDoorRecordSearchBeanRsp> searchOpenLockRecord(String token, OpenDoorRecordSearchBeanReq req){
        return service.searchOpenLockRecord(token, req, NORMAL);
    };

    public Observable<UploadAlarmRecordBeanRsp> uploadAlarmRecord(String token, UploadAlarmRecordBeanReq req){
        return service.uploadAlarmRecord(token, req, NORMAL);
    };

    public Observable<SearchAlarmRecordBeanRsp> searchAlarmRecord(String token, SearchAlarmRecordBeanReq req){
        return service.searchAlarmRecord(token, req, NORMAL);
    };

    public Observable<LockKeyAddBeanRsp> addLockKey(String token, LockKeyAddBeanReq req){
        return service.addLockKey(token, req, NORMAL);
    };

    public Observable<SearchKeyListBeanRsp> searchLockKey(String token, SearchKeyListBeanReq req){
        return service.searchLockKey(token, req, NORMAL);
    };

    public Observable<DelKeyBeanRsp> delKey(String token, DelKeyBeanReq req){
        return service.delKey(token, req, NORMAL);
    };

    public Observable<ChangeKeyNickBeanRsp> changeKeyNickName(String token, ChangeKeyNickBeanReq req){
        return service.changeKeyNickName(token, req, NORMAL);
    };

    public Observable<GetLockKeyNickBeanRsp> getKeyNickName(String token, GetLockKeyNickBeanReq req){
        return service.getKeyNickName(token, req, NORMAL);
    };

    public Observable<UpdateDoorSensorStateBeanRsp> updateDoorSensorState(String token, UpdateDoorSensorStateBeanReq req){
        return service.updateDoorSensorState(token, req, NORMAL);
    };

    public Observable<CheckDoorSensorStateBeanRsp> checkDoorSensorState(String token, CheckDoorSensorStateBeanReq req){
        return service.checkDoorSensorState(token, req, NORMAL);
    };

    public Observable<ChangeOpenLockParameterBeanRsp> changeOpenLockParameter(String token, ChangeOpenLockParameterBeanReq req){
        return service.changeOpenLockParameter(token, req, NORMAL);
    };

    public Observable<GetCodeBeanRsp> getCode(GetCodeBeanReq req) {
        return service.getCode(req, NORMAL);
    }

    public Observable<MailRegisterBeanRsp> register(MailRegisterBeanReq req) {
        return service.register(req, NORMAL);
    }

    /**
     * 检测升级文件，该接口需要使用9111端口
     */
    public Observable<CheckOTABeanRsp> checkOtaVer(String token, CheckOTABeanReq req) {
        return service.checkOtaVer(token, req, OTA_CHECK);
    }

    public Observable<StartOTAUpdateBeanRsp> startOtaUpdate(String toke, StartOTAUpdateBeanReq req) {
        return service.startOtaUpdate(toke, req, NORMAL);
    }

}
