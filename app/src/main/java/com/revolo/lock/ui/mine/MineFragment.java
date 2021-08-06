package com.revolo.lock.ui.mine;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
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

import com.blankj.utilcode.util.TimeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.revolo.lock.App;
import com.revolo.lock.Constant;
import com.revolo.lock.R;
import com.revolo.lock.bean.request.AlexaAppUrlAndWebUrlReq;
import com.revolo.lock.bean.respone.AlexaAppUrlAndWebUrlBeanRsp;
import com.revolo.lock.net.HttpRequest;
import com.revolo.lock.net.ObservableDecorator;
import com.revolo.lock.room.entity.User;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class MineFragment extends Fragment {

    private MineViewModel mMineViewModel;
    private ImageView ivAvatar;
    private TextView tvDayDetail;
    private TextView tvHiName;
    private View vMark;

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
        root.findViewById(R.id.clJoinAlexa).setOnClickListener(v -> joinAlexa());
        root.findViewById(R.id.clHelp).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), HelpActivity.class));
        });
        vMark = root.findViewById(R.id.vMark);
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

        vMark.setVisibility(Constant.isNewAppVersion ? View.VISIBLE : View.GONE);
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
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)        // 缓存
                .skipMemoryCache(false)                            //不做内存缓存
                .error(R.drawable.mine_personal_img_headportrait_default);          //错误图片
//                .placeholder(R.drawable.mine_personal_img_headportrait_default);   //预加载图片
        Glide.with(this)
                .load(url)
//                .placeholder(R.drawable.mine_personal_img_headportrait_default)
                .apply(requestOptions)
                .into(ivAvatar);
    }


    private void joinAlexa() {

        String token = App.getInstance().getUserBean().getToken();
        if (TextUtils.isEmpty(token)) {
            Timber.e("token is empty");
            return;
        }

        String userMail = App.getInstance().getUser().getMail();
        if (TextUtils.isEmpty(userMail)) {
            Timber.e("userMail is empty");
            return;
        }

        AlexaAppUrlAndWebUrlReq urlReq = new AlexaAppUrlAndWebUrlReq();
        urlReq.setType(1);
        urlReq.setUserMail(userMail);
        Observable<AlexaAppUrlAndWebUrlBeanRsp> appUrlAndWebUrl = HttpRequest.getInstance().getAppUrlAndWebUrl(token, urlReq);
        ObservableDecorator.decorate(appUrlAndWebUrl).safeSubscribe(new Observer<AlexaAppUrlAndWebUrlBeanRsp>() {
            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

            }

            @Override
            public void onNext(@io.reactivex.annotations.NonNull AlexaAppUrlAndWebUrlBeanRsp alexaAppUrlAndWebUrlBeanRsp) {
                if (alexaAppUrlAndWebUrlBeanRsp.getCode().equals("200")) {
                    if (alexaAppUrlAndWebUrlBeanRsp.getData() != null) {
                        AlexaAppUrlAndWebUrlBeanRsp.DataBean data = alexaAppUrlAndWebUrlBeanRsp.getData();
                        String appUrl = data.getAppUrl();
                        String webFallbackUrl = data.getWebFallbackUrl();
                        getActivity().runOnUiThread(() -> {
                            if (schemeValid(appUrl)) {
                                gotoAlexa(appUrl);
                            } else {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                Uri uri = Uri.parse(webFallbackUrl);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }


    private final static int REQUEST_CODE = 0xf01;

    private void gotoAlexa(String url) {
        Intent action = new Intent(Intent.ACTION_VIEW);
        action.setData(Uri.parse(url));
        startActivityForResult(action, REQUEST_CODE);
    }

    private boolean schemeValid(String url) {
        PackageManager manager = getActivity().getPackageManager();
        Intent action = new Intent(Intent.ACTION_VIEW);
        action.setData(Uri.parse(url));
        List<ResolveInfo> resolveInfos = manager.queryIntentActivities(action, PackageManager.GET_RESOLVED_FILTER);
        return resolveInfos != null && !resolveInfos.isEmpty();
    }
}