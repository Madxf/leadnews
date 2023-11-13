package com.heima.utils.threadlocal;

import com.heima.model.user.pojos.ApUser;

public class AppThreadLocal {

    private static final ThreadLocal<ApUser> APP_USER_THREAD_LOCAL = new ThreadLocal<>();

    public static void setApUser(ApUser apUser) {
        APP_USER_THREAD_LOCAL.set(apUser);
    }

    public static ApUser getApUser() {
        return APP_USER_THREAD_LOCAL.get();
    }

    public static void clean() {
        APP_USER_THREAD_LOCAL.remove();
    }
}
