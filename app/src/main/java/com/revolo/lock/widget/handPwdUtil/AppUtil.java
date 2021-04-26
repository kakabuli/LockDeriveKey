package com.revolo.lock.widget.handPwdUtil;

import android.content.Context;
import android.view.WindowManager;

public class AppUtil {

	private AppUtil() {
	}

	/**
	 * 获取屏幕分辨率
	 * @param context
	 * @return
	 */
	public static int[] getScreenDisplay(Context context) {
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int width = windowManager.getDefaultDisplay().getWidth()-80;// 手机屏幕的宽度
		int height = windowManager.getDefaultDisplay().getHeight();// 手机屏幕的高度
		return new int[]{ width, height };
	}


}
