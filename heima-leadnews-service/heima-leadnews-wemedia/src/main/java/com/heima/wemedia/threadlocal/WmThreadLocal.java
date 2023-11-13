package com.heima.wemedia.threadlocal;

import com.heima.model.wemedia.pojos.WmUser;

public class WmThreadLocal {

    private static final ThreadLocal<WmUser> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    public static void setWmUser(WmUser wmUser) {
        WM_USER_THREAD_LOCAL.set(wmUser);
    }

    public static WmUser getWmUser() {
        return WM_USER_THREAD_LOCAL.get();
    }

    public static void clean() {
        WM_USER_THREAD_LOCAL.remove();
    }
}
