package com.revolo.lock.ui.sign;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.revolo.lock.App;
import com.revolo.lock.R;
import com.revolo.lock.base.BaseActivity;
import com.revolo.lock.room.entity.User;

import java.io.File;

/**
 * author : Jack
 * time   : 2020/12/23
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class RegisterAddAvatarNextActivity extends BaseActivity {

    private User mUser;
    private ImageView mIvAvatar;

    @Override
    public void initData(@Nullable Bundle bundle) {

    }

    @Override
    public int bindLayout() {
        return R.layout.activity_register_add_avatar_next;
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState, @Nullable View contentView) {
        useCommonTitleBar(getString(R.string.register));
        mUser = App.getInstance().getUser();
        mIvAvatar = findViewById(R.id.ivAvatar);
        applyDebouncingClickListener(findViewById(R.id.btnAddDevice), findViewById(R.id.btnAddNextTime));
    }

    @Override
    public void doBusiness() {
        refreshAvatar();
    }

    @Override
    public void onDebouncingClick(@NonNull View view) {
        if(view.getId() == R.id.btnAddNextTime) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        if(view.getId() == R.id.btnAddDevice) {
            // TODO: 2021/3/23
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void refreshAvatar() {
        String avatarUrl = mUser.getAvatarUrl();
        String avatarLocalPath = mUser.getAvatarLocalPath();
        String url;
        if(TextUtils.isEmpty(avatarLocalPath)) {
            url = avatarUrl;
        } else {
            File file = new File(avatarLocalPath);
            if(file == null) {
                url = avatarUrl;
            } else {
                if(file.exists()) {
                    url = avatarLocalPath;
                } else {
                    url = avatarUrl;
                }
            }
        }
        RequestOptions requestOptions = RequestOptions.circleCropTransform()
                .diskCacheStrategy(DiskCacheStrategy.NONE)        //不做磁盘缓存
                .skipMemoryCache(true)                            //不做内存缓存
                .error(R.drawable.default_avatar)          //错误图片
                .placeholder(R.drawable.default_avatar);   //预加载图片
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_avatar)
                .apply(requestOptions)
                .into(mIvAvatar);
    }

}
