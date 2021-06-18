package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.bean.respone.LogoutBeanRsp;
import com.revolo.lock.dialog.SelectDialog;
import com.revolo.lock.manager.LockMessage;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.AppDatabase;
import com.revolo.lock.room.entity.User;
import com.revolo.lock.ui.sign.LoginActivity;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static com.revolo.lock.Constant.REVOLO_SP;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_CLASE_DEVICE;
import static com.revolo.lock.manager.LockMessageCode.MSG_LOCK_MESSAGE_USER;

public class MineFragment extends Fragment {

    private MineViewModel mMineViewModel;
    private ImageView ivAvatar;
    private TextView tvDayDetail;
    private TextView tvHiName;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mMineViewModel =
                new ViewModelProvider(this).get(MineViewModel.class);
        View root = inflater.inflate(R.layout.fragment_mine, container, false);
        tvHiName = root.findViewById(R.id.tvHiName);
        tvDayDetail = root.findViewById(R.id.tvDayDetail);
        root.findViewById(R.id.clUserDetail).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UserPageActivity.class);
            startActivity(intent);
        });
        ivAvatar = root.findViewById(R.id.ivAvatar);
        root.findViewById(R.id.clMessage).setOnClickListener(v -> startActivity(new Intent(getContext(), MessageListActivity.class)));
        root.findViewById(R.id.clSetting).setOnClickListener(v -> startActivity(new Intent(getContext(), SettingActivity.class)));
        root.findViewById(R.id.clAbout).setOnClickListener(v -> startActivity(new Intent(getContext(), AboutActivity.class)));
        root.findViewById(R.id.clFeedback).setOnClickListener(v -> startActivity(new Intent(getContext(), FeedbackActivity.class)));
//        root.findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutDialog());
        root.findViewById(R.id.clHelp).setOnClickListener(v -> {
//            String url = "https://alexa.amazon.com/spa/skill-account-linking-consent?fragment=skill-account-linking-consent&client_id=amzn1.application-oa2-client.f37d0df669ae40de9974517af080afc0&scope=alexa::skills:account_linking&response_type=code&redirect_uri=https://test.irevolo.com/zetark-oauth2-server/alexa&skill_stage=development&state=ANzKzFbXxieNyCqTLYMi";
//            boolean b = schemeValid(url);
//            if (b){
//                ToastUtils.showShort("true");
//            }else {
//                ToastUtils.showShort("false");
//            }

            startActivity(new Intent(getContext(), HelpActivity.class));
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        mMineViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            String userName = user.getFirstName();
            // TODO: 2021/3/7 名字后面需要更改其他显示
            tvHiName.setText(getString(R.string.hi_name, TextUtils.isEmpty(userName) ? "" : userName));
            long registerTime = user.getRegisterTime();
            tvDayDetail.setText(getString(R.string.day_detail, daysBetween(TimeUtils.getNowMills() / 1000, registerTime)));
        });
        refreshAvatar(mMineViewModel.getUser().getValue());
    }

    //计算间隔日，比较两个时间是否同一天，如果两个时间都是同一天的话，返回0。
    // 两个比较的时间都不是同一天的话，根据传参位置 可返回正数/负数。两个比较的时间都是同一天的话，返回0。
    private static int daysBetween(long now, long createTime) {
        Timber.d("daysBetween nowTime: %1d, createTime: %2d", now, createTime);
        return (int) ((now - createTime) / (3600 * 24));
    }

    private void refreshAvatar(User user) {
        String avatarUrl = user.getAvatarUrl();
        String avatarLocalPath = user.getAvatarLocalPath();
        String url;
        if (TextUtils.isEmpty(avatarLocalPath)) {
            url = avatarUrl;
        } else {
            File file = new File(avatarLocalPath);
            if (file.exists()) {
                url = avatarLocalPath;
            } else {
                url = avatarUrl;
            }
        }
        RequestOptions requestOptions = RequestOptions.circleCropTransform()
                .diskCacheStrategy(DiskCacheStrategy.NONE)        //不做磁盘缓存
                .skipMemoryCache(true)                            //不做内存缓存
                .error(R.drawable.mine_personal_img_headportrait_default)          //错误图片
                .placeholder(R.drawable.mine_personal_img_headportrait_default);   //预加载图片
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.mine_personal_img_headportrait_default)
                .apply(requestOptions)
                .into(ivAvatar);
    }

    private void showLogoutDialog() {
        SelectDialog dialog = new SelectDialog(getActivity());
        dialog.setMessage(getString(R.string.dialog_tip_log_out));
        dialog.setOnCancelClickListener(v -> dialog.dismiss());
        dialog.setOnConfirmListener(v -> {
            dialog.dismiss();
            logout();
        });
        dialog.show();
    }

    private void logout() {
        if (App.getInstance().getUserBean() == null) {
            return;
        }
        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            return;
        }
        Observable<LogoutBeanRsp> observable = HttpRequest.getInstance().logout(token);
        ObservableDecorator.decorate(observable).safeSubscribe(new Observer<LogoutBeanRsp>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull LogoutBeanRsp logoutBeanRsp) {
                String code = logoutBeanRsp.getCode();
                if (TextUtils.isEmpty(code)) {
                    Timber.e("code is empty");
                    return;
                }
                if (!code.equals("200")) {
                    if (code.equals("444")) {
                        App.getInstance().logout(true, getActivity());
                        return;
                    }
                    String msg = logoutBeanRsp.getMsg();
                    Timber.e("code: %1s, msg: %2s", code, msg);
                    if (!TextUtils.isEmpty(msg)) {
                        ToastUtils.showShort(msg);
                        return;
                    }
                }

                User user = App.getInstance().getUser();
                AppDatabase.getInstance(getActivity()).userDao().delete(user);
                App.getInstance().getUserBean().setToken(""); // 清空token
                SPUtils.getInstance(REVOLO_SP).put(Constant.USER_LOGIN_INFO, ""); // 清空登录信息
                //清理设备信息
                LockMessage message = new LockMessage();
                message.setMessageType(MSG_LOCK_MESSAGE_USER);
                message.setMessageCode(MSG_LOCK_MESSAGE_CLASE_DEVICE);
                EventBus.getDefault().post(message);
                // App.getInstance().removeDeviceList();
                getActivity().finish();
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Timber.e(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }
}