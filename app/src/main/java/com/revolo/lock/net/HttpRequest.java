package com.revolo.lock.net;

import com.blankj.utilcode.util.EncryptUtils;
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
import com.revolo.lock.bean.request.DelDeviceBeanReq;
import com.revolo.lock.bean.request.DelKeyBeanReq;
import com.revolo.lock.bean.request.DeviceUnbindBeanReq;
import com.revolo.lock.bean.request.GetLockKeyNickBeanReq;
import com.revolo.lock.bean.request.GetPwd1BeanReq;
import com.revolo.lock.bean.request.LockIsBindBeanReq;
import com.revolo.lock.bean.request.LockKeyAddBeanReq;
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
import com.revolo.lock.bean.respone.GetLockKeyNickBeanRsp;
import com.revolo.lock.bean.respone.GetPwd1BeanRsp;
import com.revolo.lock.bean.respone.LockIsBindBeanRsp;
import com.revolo.lock.bean.respone.LockKeyAddBeanRsp;
import com.revolo.lock.bean.respone.OpenDoorRecordSearchBeanRsp;
import com.revolo.lock.bean.respone.SearchAlarmRecordBeanRsp;
import com.revolo.lock.bean.respone.SearchKeyListBeanRsp;
import com.revolo.lock.bean.respone.SearchProductNoBeanRsp;
import com.revolo.lock.bean.respone.UpdateDoorSensorStateBeanRsp;
import com.revolo.lock.bean.respone.UploadAlarmRecordBeanRsp;
import com.revolo.lock.bean.respone.UploadOpenDoorRecordBeanRsp;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import timber.log.Timber;

public class HttpRequest {

    private ApiService service;

    private static final String HOST_TEST = "https://test1.juziwulian.com:8090";      // 服务器测试接口
    

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

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(HOST_TEST)
                .addConverterFactory(GsonConverterFactory.create(gson))    // Gson
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())    // RxJava
                .client(client)
                .build();

        service = retrofit.create(ApiService.class);

    }

    Observable<LockIsBindBeanRsp> lockIsBind(String token, LockIsBindBeanReq req) {
        return service.lockIsBind(token, req);
    };

    Observable<GetPwd1BeanRsp> getPwd1(String token, GetPwd1BeanReq req){
        return service.getPwd1(token, req);
    };

    Observable<AdminAddDeviceBeanRsp> adminAddDevice(String token, AdminAddDeviceBeanReq req){
        return service.adminAddDevice(token, req);
    };

    Observable<DeviceUnbindBeanRsp> unbindDevice(String token, DeviceUnbindBeanReq req){
        return service.unbindDevice(token, req);
    };

    Observable<ChangeBleVerBeanRsp> changeBleVer(String token, ChangeBleVerBeanReq req){
        return service.changeBleVer(token, req);
    };

    Observable<ChangeDeviceHardVerBeanRsp> changeDeviceHardVer(String token, ChangeDeviceHardVerBeanReq req){
        return service.changeDeviceHardVer(token, req);
    };

    Observable<ChangeFeaturesBeanRsp> updateFunctionSet(String token, ChangeFeaturesBeanReq req){
        return service.updateFunctionSet(token, req);
    };

    Observable<DelDeviceBeanRsp> delDevice(String token, DelDeviceBeanReq req){
        return service.delDevice(token, req);
    };

    Observable<ChangeDeviceNameBeanRsp> changeDeviceNickName(String token, ChangeDeviceNameBeanReq req){
        return service.changeDeviceNickName(token, req);
    };

    Observable<SearchProductNoBeanRsp> searchDevice(String token, SearchProductNoBeanReq req){
        return service.searchDevice(token, req);
    };

    Observable<UploadOpenDoorRecordBeanRsp> uploadOpenDoorRecord(String token, UploadOpenDoorRecordBeanReq req){
        return service.uploadOpenDoorRecord(token, req);
    };

    Observable<OpenDoorRecordSearchBeanRsp> searchOpenLockRecord(String token, OpenDoorRecordSearchBeanReq req){
        return service.searchOpenLockRecord(token, req);
    };

    Observable<UploadAlarmRecordBeanRsp> uploadAlarmRecord(String token, UploadAlarmRecordBeanReq req){
        return service.uploadAlarmRecord(token, req);
    };

    Observable<SearchAlarmRecordBeanRsp> searchAlarmRecord(String token, SearchAlarmRecordBeanReq req){
        return service.searchAlarmRecord(token, req);
    };

    Observable<LockKeyAddBeanRsp> addLockKey(String token, LockKeyAddBeanReq req){
        return service.addLockKey(token, req);
    };

    Observable<SearchKeyListBeanRsp> searchLockKey(String token, SearchKeyListBeanReq req){
        return service.searchLockKey(token, req);
    };

    Observable<DelKeyBeanRsp> delKey(String token, DelKeyBeanReq req){
        return service.delKey(token, req);
    };

    Observable<ChangeKeyNickBeanRsp> changeKeyNickName(String token, ChangeKeyNickBeanReq req){
        return service.changeKeyNickName(token, req);
    };

    Observable<GetLockKeyNickBeanRsp> getKeyNickName(String token, GetLockKeyNickBeanReq req){
        return service.getKeyNickName(token, req);
    };

    Observable<UpdateDoorSensorStateBeanRsp> updateDoorSensorState(String token, UpdateDoorSensorStateBeanReq req){
        return service.updateDoorSensorState(token, req);
    };

    Observable<CheckDoorSensorStateBeanRsp> checkDoorSensorState(String token, CheckDoorSensorStateBeanReq req){
        return service.checkDoorSensorState(token, req);
    };

    Observable<ChangeOpenLockParameterBeanRsp> changeOpenLockParameter(String token, ChangeOpenLockParameterBeanReq req){
        return service.changeOpenLockParameter(token, req);
    };


}
