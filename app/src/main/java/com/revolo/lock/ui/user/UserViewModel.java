package com.revolo.lock.ui.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.revolo.lock.bean.respone.GetAllSharedUserFromAdminUserBeanRsp;

import java.util.List;


public class UserViewModel extends ViewModel {

    private MutableLiveData<List<GetAllSharedUserFromAdminUserBeanRsp.DataBean>> mUsers;

    public UserViewModel() {
        mUsers = new MutableLiveData<>();
        refreshData(mUsers);
    }

    public LiveData<List<GetAllSharedUserFromAdminUserBeanRsp.DataBean>> getUsers() {
        return mUsers;
    }

    private void refreshData(MutableLiveData<List<GetAllSharedUserFromAdminUserBeanRsp.DataBean>> users) {

    }


}