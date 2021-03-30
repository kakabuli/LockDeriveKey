package com.kaadas.lock.shulan.utils;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.kaadas.lock.shulan.BuildConfig;

/**
 * Created by walter on 2016/6/30.
 */
public class LogUtils {
	public static final boolean DEBUG = BuildConfig.DEBUG;

	public static final boolean isOutFile = BuildConfig.DEBUG;
	/**
	 * 日志输出级别NONE
	 */
	public static final int LEVEL_NONE = 0;
	/**
	 * 日志输出级别V
	 */
	public static final int LEVEL_VERBOSE = 1;
	/**
	 * 日志输出级别D
	 */
	public static final int LEVEL_DEBUG = 2;
	/**
	 * 日志输出级别I
	 */
	public static final int LEVEL_INFO = 3;
	/**
	 * 日志输出级别W
	 */
	public static final int LEVEL_WARN = 4;
	/**
	 * 日志输出级别E
	 */
	public static final int LEVEL_ERROR = 5;
	/**
	 * 日志输出时的TAG
	 */
	private static String mTag = "-shulan-";
	/**
	 * 是否允许输出log 的级别
	 */
	private static int mDebuggable = 5;
	/**
	 * 用于记时的变量
	 */
	private static long mTimestamp = 5;
	
	
	/**
	 * 以级别为 d 的形式输出LOG
	 */
	public static void v(String msg) {
		if(DEBUG){
			if (mDebuggable >= LEVEL_VERBOSE) {
				Log.v(mTag, msg);
			}
		}
	}

	/**
	 * 以级别为 d 的形式输出LOG
	 */
	public static void d(String msg) {
		if(DEBUG){
			if (mDebuggable >= LEVEL_DEBUG) {
				Log.d(mTag, msg);
			}
		}
	}
	public static void d(String Tag, String msg) {
		if(DEBUG){
			if (mDebuggable >= LEVEL_DEBUG) {
				Log.d(Tag, msg);
			}
		}
	}
	
	/**
	 * 以级别为 i 的形式输出LOG
	 */
	public static void i(String msg) {
		if(DEBUG){
			if (mDebuggable >= LEVEL_INFO) {
				Log.i(mTag, msg);
			}
		}
	}
	public static void i(String tag, String msg) {
		if(DEBUG){
			if (mDebuggable >= LEVEL_INFO) {
				Log.i(tag, msg);
			}
		}
	}
	
	
	/**
	 * 以级别为 w 的形式输出LOG
	 */
	public static void w(String msg) {
		if(DEBUG){
			if (mDebuggable >= LEVEL_WARN) {
				Log.w(mTag, msg);
			}
		}
	}
	



	/**
	 * 以级别为 w 的形式输出Throwable
	 */
	public static void w(Throwable tr) {
		if(DEBUG){
			if (mDebuggable >= LEVEL_WARN) {
				Log.w(mTag, "", tr);
			}
		}
	}
	
	
	/**
	 * 以级别为 w 的形式输出LOG信息和Throwable
	 */
	public static void w(String msg, Throwable tr) {
		if(DEBUG){
			if (mDebuggable >= LEVEL_WARN && null != msg) {
				Log.w(mTag, msg, tr);
			}
		}
	}
	
	
	/**
	 * 以级别为 e 的形式输出LOG
	 */
	public static void e(String msg) {
		if (mDebuggable >= LEVEL_ERROR) {
			if (isOutFile){
//				Logger.getInstance().save( msg);
			}else {
				if(DEBUG)
					Log.e(mTag, msg);
			}
		}
	}
	/**
	 * 以级别为 e 的形式输出LOG
	 */
	public static void e(String TAG, String msg) {

		if (mDebuggable >= LEVEL_ERROR) {
			if (isOutFile){
//				Logger.getInstance().save(mTag+TAG+ msg);
			}else {
				if(DEBUG)
					Log.e(mTag+TAG, msg);
			}

		}
	}



	/**
	 * 以级别为 e 的形式输出Throwable
	 */
	public static void e(Throwable tr) {
		if(DEBUG){
			if (mDebuggable >= LEVEL_ERROR) {
				Log.e(mTag, "", tr);
			}
		}
	}
	
	
	/**
	 * 以级别为 e 的形式输出LOG信息和Throwable
	 */
	public static void e(String msg, Throwable tr) {
		if(DEBUG){
			if (mDebuggable >= LEVEL_ERROR && null != msg) {
				Log.e(mTag, msg, tr);
			}
		}
	}
	
	
	/**
	 * 以级别为 e 的形式输出msg信息,附带时间戳，用于输出一个时间段结束点* @param msg 需要输出的msg
	 */
	public static void elapsed(String msg) {
		if(DEBUG) {
			long currentTime = System.currentTimeMillis();
			long elapsedTime = currentTime - mTimestamp;
			mTimestamp = currentTime;
			LogUtils.e("[Elapsed：" + elapsedTime + "]" + msg);
		}
	}

    public static void e(FragmentActivity activity) {
    }
}
