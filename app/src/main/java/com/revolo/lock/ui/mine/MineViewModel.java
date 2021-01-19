package com.revolo.lock.ui.mine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.revolo.lock.bean.TestUserBean;

public class MineViewModel extends ViewModel {

    private MutableLiveData<TestUserBean> mUser;

    public MineViewModel() {
        mUser = new MutableLiveData<>();
        TestUserBean userBean = new TestUserBean("John", "JohnXXX@gmail.com");
        mUser.setValue(userBean);
    }

    public LiveData<TestUserBean> getUser() { return mUser; }

}