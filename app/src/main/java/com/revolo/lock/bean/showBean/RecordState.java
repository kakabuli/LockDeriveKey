package com.revolo.lock.bean.showBean;

import androidx.annotation.IntDef;
import androidx.core.content.ContextCompat;

import com.revolo.lock.R;

/**
 * author :
 * time   : 2021/2/7
 * E-mail : wengmaowei@kaadas.com
 * desc   :
 */
public class RecordState {

    @IntDef(value = {SOMEONE_USE_A_PWD_TO_UNLOCK, SOMEONE_USE_GEO_FENCE_TO_UNLOCK, NOTHING,
            SOMEONE_USE_THE_APP_TO_UNLOCK, SOMEONE_USE_MECHANICAL_KEY_TO_UNLOCK, LOCKING_INSIDE_THE_DOOR,
            DOUBLE_LOCK_INSIDE_THE_DOOR, MULTI_FUNCTIONAL_BUTTON_LOCKING, ONE_TOUCH_LOCK_OUTSIDE_THE_DOOR,
            SOMEONE_LOCKED_THE_DOOR_BY_APP, SOMEONE_LOCKED_THE_DOOR_BY_MECHANICAL_KEY, DURESS_PASSWORD_UNLOCK,
            LOCK_DOWN_ALARM, LOW_BATTERY_ALARM, JAM_ALARM, DOOR_OPENED_DETECTED, DOOR_CLOSED_DETECTED,
            THE_USER_ADDED_A_PWD, THE_USER_DELETED_A_PWD, THE_USER_ADDED_SOMEONE_IN_FAMILY_GROUP,
            THE_USER_REMOVED_SOMEONE_FROM_FAMILY_GROUP, USER_ADDED_SOMEONE_AS_GUEST_USER,
            USER_REMOVED_SOMEONE_FROM_GUEST_USER, LOCK_RESTORE})
    public @interface OpRecordState{}

    public static final int SOMEONE_USE_A_PWD_TO_UNLOCK = 1;
    public static final int SOMEONE_USE_GEO_FENCE_TO_UNLOCK = 2;
    public static final int SOMEONE_USE_THE_APP_TO_UNLOCK = 3;
    public static final int SOMEONE_USE_MECHANICAL_KEY_TO_UNLOCK = 4;
    public static final int LOCKING_INSIDE_THE_DOOR = 5;
    public static final int DOUBLE_LOCK_INSIDE_THE_DOOR = 6;
    public static final int MULTI_FUNCTIONAL_BUTTON_LOCKING = 7;
    public static final int ONE_TOUCH_LOCK_OUTSIDE_THE_DOOR = 8;
    public static final int SOMEONE_LOCKED_THE_DOOR_BY_APP = 9;
    public static final int SOMEONE_LOCKED_THE_DOOR_BY_MECHANICAL_KEY = 10;
    public static final int DURESS_PASSWORD_UNLOCK = 11;
    public static final int LOCK_DOWN_ALARM = 12;
    public static final int LOW_BATTERY_ALARM = 13;
    public static final int JAM_ALARM = 14;
    public static final int DOOR_OPENED_DETECTED = 15;
    public static final int DOOR_CLOSED_DETECTED = 16;
    public static final int THE_USER_ADDED_A_PWD = 17;
    public static final int THE_USER_DELETED_A_PWD = 18;
    public static final int THE_USER_ADDED_SOMEONE_IN_FAMILY_GROUP = 19;
    public static final int THE_USER_REMOVED_SOMEONE_FROM_FAMILY_GROUP = 20;
    public static final int USER_ADDED_SOMEONE_AS_GUEST_USER = 21;
    public static final int USER_REMOVED_SOMEONE_FROM_GUEST_USER = 22;
    public static final int LOCK_RESTORE = 23;
    public static final int NOTHING = -1;

}
