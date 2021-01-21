package com.revolo.lock.ui.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.revolo.lock.bean.test.TestUserManagementBean;

import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends ViewModel {

    private MutableLiveData<List<TestUserManagementBean>> mUsers;

    public UserViewModel() {
        mUsers = new MutableLiveData<>();
        initTestData(mUsers);
    }

    public LiveData<List<TestUserManagementBean>> getUsers() {
        return mUsers;
    }

    private void initTestData(MutableLiveData<List<TestUserManagementBean>> users) {
        List<TestUserManagementBean> beanList = new ArrayList<>();
        TestUserManagementBean bean1 = new TestUserManagementBean("Jack", 1, 1);
        beanList.add(bean1);
        TestUserManagementBean bean2 = new TestUserManagementBean("Marry", 2, 2);
        beanList.add(bean2);
        TestUserManagementBean bean3 = new TestUserManagementBean("Jim", 1, 3);
        beanList.add(bean3);
        TestUserManagementBean bean4 = new TestUserManagementBean("Tick", 2, 3);
        beanList.add(bean4);
        users.setValue(beanList);
    }

}