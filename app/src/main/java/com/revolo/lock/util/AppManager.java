package com.revolo.lock.util;

import android.app.Activity;

import java.util.Stack;

import timber.log.Timber;

/**
 * author :
 * time   : 2021/4/13
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public final class AppManager {

    /**
     * 维护activity的栈结构
     **/
    private Stack<Activity> mActivityStack;
    private static volatile AppManager sInstance;

    /**
     * 单例
     *
     * @return 返回AppManager的单例
     */
    public static AppManager getInstance() {
        if (sInstance == null) {
            synchronized (AppManager.class) {
                if (sInstance == null) {
                    sInstance = new AppManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 添加Activity到堆栈
     *
     * @param activity activity实例
     */
    public void addActivity(Activity activity) {
        if (mActivityStack == null) {
            mActivityStack = new Stack<>();
        }
        mActivityStack.add(activity);
    }

    /**
     * 获取当前Activity（栈中最后一个压入的）
     *
     * @return 当前（栈顶）activity
     */
    public Activity currentActivity() {
        if (mActivityStack != null && !mActivityStack.isEmpty()) {
            return mActivityStack.lastElement();
        }
        return null;
    }

    /**
     * 结束除当前activtiy以外的所有activity
     * 注意：不能使用foreach遍历并发删除，会抛出java.util.ConcurrentModificationException的异常
     *
     * @param activity 不需要结束的activity
     */
    public void finishOtherActivity(Activity activity) {
        if (mActivityStack != null) {
            for (Activity temp : mActivityStack) {
                if (temp != null && temp != activity) {
                    finishActivity(temp);
                }
            }
        }
    }

    /**
     * 结束除这一类activtiy以外的所有activity
     * 注意：不能使用foreach遍历并发删除，会抛出java.util.ConcurrentModificationException的异常
     *
     * @param cls 不需要结束的activity
     */
    public void finishOtherActivity(Class<?> cls) {
        if (mActivityStack != null) {
            for (Activity activity : mActivityStack) {
                if (!activity.getClass().equals(cls)) {
                    finishActivity(activity);
                }
            }
        }
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        if (mActivityStack != null && !mActivityStack.isEmpty()) {
            Activity activity = mActivityStack.lastElement();
            finishActivity(activity);
        }
    }

    /**
     * 结束指定的Activity
     *
     * @param activity 指定的activity实例
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            if (mActivityStack != null) {// 兼容未使用AppManager管理的实例
                mActivityStack.remove(activity);
            }
            activity.finish();
        }
    }

    public void removeActivity(Activity activity) {
        if(activity != null) {
            if(mActivityStack != null) {
                mActivityStack.remove(activity);
            }
        }
    }

    /**
     * 结束指定类名的所有Activity
     *
     * @param cls 指定的类的class
     */
    public void finishActivity(Class<?> cls) {
        if (mActivityStack != null) {
            for (Activity activity : mActivityStack) {
                if (activity.getClass().equals(cls)) {
                    finishActivity(activity);
                }
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        if (mActivityStack != null) {
            for (int i = 0, size = mActivityStack.size(); i < size; i++) {
                if (null != mActivityStack.get(i)) {
                    mActivityStack.get(i).finish();
                }
            }
            mActivityStack.clear();
        }
    }

    /**
     * 退出应用程序
     */
    public void exitApp() {
        try {
            finishAllActivity();
            // 退出JVM(java虚拟机),释放所占内存资源,0表示正常退出(非0的都为异常退出)
            System.exit(0);
            // 从操作系统中结束掉当前程序的进程
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            Timber.e(e);
        }
    }

}
