package com.revolo.lock.adapter;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.revolo.lock.R;
import com.revolo.lock.bean.DevicePwdBean;
import com.revolo.lock.ble.BleByteUtil;
import com.revolo.lock.util.ZoneUtil;

import org.jetbrains.annotations.NotNull;

import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_ALWAYS;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_TIME_KEY;
import static com.revolo.lock.ble.BleCommandState.KEY_SET_ATTRIBUTE_WEEK_KEY;

/**
 * author : Jack
 * time   : 2021/1/13
 * E-mail : wengmaowei@kaadas.com
 * desc   : 密码列表
 */
public class PasswordListAdapter extends BaseQuickAdapter<DevicePwdBean, BaseViewHolder> {

    private String timeZone;
    private OnDeletePassWordListener mOnDeleteListener;

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public PasswordListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, DevicePwdBean devicePwdBean) {
        if (devicePwdBean != null) {
            TextView tvPwdName = baseViewHolder.getView(R.id.tvPwdName);
            TextView tvDetail = baseViewHolder.getView(R.id.tvDetail);
            // TODO: 2021/2/21 要提前判断密码是否失效
            if (isTimeInvalid(devicePwdBean)) {
                baseViewHolder.setBackgroundResource(R.id.ivPwdState, R.drawable.ic_home_icon_password_overdue);
                tvPwdName.setTextColor(ContextCompat.getColor(getContext(), R.color.cCCCCCC));
                tvDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.cCCCCCC));
            } else {
                baseViewHolder.setBackgroundResource(R.id.ivPwdState, R.drawable.ic_home_icon_password);
                tvPwdName.setTextColor(ContextCompat.getColor(getContext(), R.color.c333333));
                tvDetail.setTextColor(ContextCompat.getColor(getContext(), R.color.c666666));
            }
            String pwdName = devicePwdBean.getPwdName();
            if (TextUtils.isEmpty(pwdName) || pwdName.equals(devicePwdBean.getPwdNum() + "")) {
                pwdName = String.format(getContext().getString(R.string.tip_pwd_default_name), devicePwdBean.getPwdNum() + "");
            }
            baseViewHolder.setText(R.id.tvPwdName, pwdName);
            baseViewHolder.setText(R.id.tvDetail, getPwdDetail(devicePwdBean));
        }
        TextView textView = baseViewHolder.getView(R.id.tv_delete);
        textView.setOnClickListener(v -> mOnDeleteListener.onDeleteClickListener(textView, baseViewHolder.getAdapterPosition()));
    }

    private String getPwdDetail(DevicePwdBean devicePwdBean) {
        int attribute = devicePwdBean.getAttribute();
        String detail = "";
        if (attribute == KEY_SET_ATTRIBUTE_ALWAYS) {
            detail = "Permanent PIN Key";
        } else if (attribute == KEY_SET_ATTRIBUTE_TIME_KEY) {
            long startTimeMill = devicePwdBean.getStartTime() * 1000;
            long endTimeMill = devicePwdBean.getEndTime() * 1000;

            String startTime = ZoneUtil.getDate(timeZone, startTimeMill, "dd/MM/yyyy HH:mm");
            String endTime = ZoneUtil.getDate(timeZone, endTimeMill, "dd/MM/yyyy HH:mm");

            String subStartTime = startTime.substring(0, 2);
            String subEndTime = endTime.substring(0, 2);

            String replaceStartTime = "";
            String replaceEndTime = "";

            if (subStartTime.endsWith("1")) {
                replaceStartTime = subStartTime + "st";
            } else if (subStartTime.endsWith("2")) {
                replaceStartTime = subStartTime + "nd";
            } else if (subStartTime.endsWith("3")) {
                replaceStartTime = subStartTime + "rd";
            } else {
                replaceStartTime = subStartTime + "th";
            }

            if (subEndTime.endsWith("1")) {
                replaceEndTime = subEndTime + "st";
            } else if (subEndTime.endsWith("2")) {
                replaceEndTime = subEndTime + "nd";
            } else if (subEndTime.endsWith("3")) {
                replaceEndTime = subEndTime + "rd";
            } else {
                replaceEndTime = subEndTime + "th";
            }

            startTime = replaceStartTime + startTime.substring(2);
            endTime = replaceEndTime + endTime.substring(2);

            detail = "Start: " + startTime + "\n" + "End: " + endTime;
        } else if (attribute == KEY_SET_ATTRIBUTE_WEEK_KEY) {
            byte[] weekBytes = BleByteUtil.byteToBit(devicePwdBean.getWeekly());
            String weekly = "";
            if (weekBytes[6] == 0x01) {
                weekly += "Sun.";
            }
            if (weekBytes[5] == 0x01) {
                weekly += "Mon.";
            }
            if (weekBytes[4] == 0x01) {
                weekly += "Tues.";
            }
            if (weekBytes[3] == 0x01) {
                weekly += "Wed.";
            }
            if (weekBytes[2] == 0x01) {
                weekly += "Thur.";
            }
            if (weekBytes[1] == 0x01) {
                weekly += "Fri.";
            }
            if (weekBytes[0] == 0x01) {
                weekly += "Sat.";
            }
            weekly += "\n";
            long startTimeMill = devicePwdBean.getStartTime() * 1000;
            long endTimeMill = devicePwdBean.getEndTime() * 1000;
            detail = weekly
                    + ZoneUtil.getDate(timeZone, startTimeMill, "HH:mm")
                    + " - "
                    + ZoneUtil.getDate(timeZone, endTimeMill, "HH:mm");
        }
        return detail;
    }


    /**
     * 判断密码时间是否失效
     */
    private boolean isTimeInvalid(DevicePwdBean devicePwdBean) {

        if (devicePwdBean.getAttribute() == 0) {
            // 永久秘钥
            return false;
        }

        if (devicePwdBean.getAttribute() == 1) {
            // 时间策略秘钥
            long startTime = devicePwdBean.getStartTime() * 1000;
            long endTime = devicePwdBean.getEndTime() * 1000;
            long nowTime = ZoneUtil.getTestTime(timeZone);
            // new Date().getTime();
            if (nowTime <= endTime) {
                return false;
            } else {
                return true;
            }
        }

        if (devicePwdBean.getAttribute() == 2) {
            // 胁迫秘钥
            return false;
        }

        if (devicePwdBean.getAttribute() == 3) {
            // 管理员秘钥
            return false;
        }

        if (devicePwdBean.getAttribute() == 4) {
            // 无权限秘钥
            return false;
        }

        if (devicePwdBean.getAttribute() == 5) {
            // 周策略秘钥
            return false;
//            long startTime = devicePwdBean.getStartTime() * 1000;
//            long endTime = devicePwdBean.getEndTime() * 1000;
//            long nowTime = new Date().getTime();
//            if (startTime <= nowTime && nowTime <= endTime) {
//                return false;
//            } else {
//                return true;
//            }
        }
        return true;
    }

    public static boolean isInteger(String str) {
        return str.matches("-?[0-9]+.？[0-9]*");
    }

    public void setOnDeletePassWordListener(OnDeletePassWordListener onDeleteListener) {
        this.mOnDeleteListener = onDeleteListener;
    }

    public interface OnDeletePassWordListener {

        void onDeleteClickListener(View view, int position);
    }
}
