package com.revolo.lock.ui.mine;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.revolo.lock.App;
import com.revolo.lock.room.entity.User;

public class MineViewModel extends ViewModel {

    private MutableLiveData<User> mUser;

    public MineViewModel() {
        mUser = new MutableLiveData<>();
        User user = App.getInstance().getUser();
        if(user != null) {
            mUser.setValue(user);
        }
    }

    public LiveData<User> getUser() { return mUser; }

}