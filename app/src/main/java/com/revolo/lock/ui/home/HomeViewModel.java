package com.revolo.lock.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.revolo.lock.bean.TestLockBean;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<TestLockBean>> mTestLockBean;

    public HomeViewModel() {

        List<TestLockBean> testLockBeans = new ArrayList<>();
        TestLockBean testLockBean1 = new TestLockBean("10V0204110001", 1, 1, 1);
        testLockBeans.add(testLockBean1);
        TestLockBean testLockBean2 = new TestLockBean("10V0204110002", 2, 1, 1);
        testLockBeans.add(testLockBean2);
        TestLockBean testLockBean3 = new TestLockBean("10V0204110003", 1, 2, 2);
        testLockBeans.add(testLockBean3);

        mTestLockBean = new MutableLiveData<>();
        mTestLockBean.setValue(testLockBeans);
    }

    public LiveData<List<TestLockBean>> getTestLockBeans() { return mTestLockBean; }
}