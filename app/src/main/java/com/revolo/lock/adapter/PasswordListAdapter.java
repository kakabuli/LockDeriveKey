package com.revolo.lock.adapter;

import android.text.TextUtils;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.test.TestPwdBean;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.ble.BleCommandFactory;
import com.revolo.lock.room.entity.DevicePwd;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import timber.log.Timber;

import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_TIME_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_WEEK_KEY;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 密码列表
 */
public class PasswordListAdapter extends BaseQuickAdapter<DevicePwd, BaseViewHolder> {
    public PasswordListAdapter(int layoutResId, @Nullable List<DevicePwd> data) {
        super(layoutResId, data);
    }

    public PasswordListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, DevicePwd devicePwd) {
        if(devicePwd != null) {
            TextView tvPwdName = baseViewHolder.getView(R.id.tvPwdName);
            TextView tvDetail = baseViewHolder.getView(R.id.tvDetail);
            if(devicePwd.getPwdState() == 1) {
                baseViewHolder.setImageResource(R.id.ivPwdState, R.drawable.ic_home_icon_password);
                tvPwdName.setTextColor(ContextCompat.getColor(getContext(), R.color.c333333));
                tvDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.c666666));
            } else {
                baseViewHolder.setImageResource(R.id.ivPwdState, R.drawable.ic_home_icon_password_overdue);
                tvPwdName.setTextColor(ContextCompat.getColor(getContext(), R.color.cCCCCCC));
                tvDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.cCCCCCC));
            }

            baseViewHolder.setText(R.id.tvPwdName, devicePwd.getPwdName());
            baseViewHolder.setText(R.id.tvDetail, getPwdDetail(devicePwd));
        }
    }

    private String getPwdDetail(DevicePwd devicePwd) {
        int attribute = devicePwd.getAttribute();
        String detail = "";
        if(attribute == KEY_SET_ATTRIBUTE_ALWAYS) {
            detail = "Permanent password";
        } else if(attribute == KEY_SET_ATTRIBUTE_TIME_KEY) {
            long startTimeMill = devicePwd.getStartTime()*1000;
            long endTimeMill = devicePwd.getEndTime()*1000;
            detail = "start: "
                    + TimeUtils.millis2String(startTimeMill, "MM,dd,yyyy   HH:mm")
                    + "\n" + "end: "
                    + TimeUtils.millis2String(endTimeMill, "MM,dd,yyyy   HH:mm");
        } else if(attribute == KEY_SET_ATTRIBUTE_WEEK_KEY) {
            byte[] weekBytes = BleByteUtil.byteToBit(devicePwd.getWeekly());
            String weekly = "";
            if(weekBytes[0] == 0x01) {
                weekly += "Sun";
            }
            if(weekBytes[1] == 0x01) {
                weekly += TextUtils.isEmpty(weekly)?"Mon":"、Mon";
            }
            if(weekBytes[2] == 0x01) {
                weekly += TextUtils.isEmpty(weekly)?"Tues":"、Tues";
            }
            if(weekBytes[3] == 0x01) {
                weekly += TextUtils.isEmpty(weekly)?"Wed":"、Wed";
            }
            if(weekBytes[4] == 0x01) {
                weekly += TextUtils.isEmpty(weekly)?"Thur":"、Thur";
            }
            if(weekBytes[5] == 0x01) {
                weekly += TextUtils.isEmpty(weekly)?"Fri":"、Fri";
            }
            if(weekBytes[6] == 0x01) {
                weekly += TextUtils.isEmpty(weekly)?"Sat":"、Sat";
            }
            weekly += "\n";
            long startTimeMill = devicePwd.getStartTime()*1000;
            long endTimeMill = devicePwd.getEndTime()*1000;
            detail = weekly
                    + TimeUtils.millis2String(startTimeMill, "HH:mm")
                    + " - "
                    + TimeUtils.millis2String(endTimeMill, "HH:mm");
        }
        return detail;
    }

}
