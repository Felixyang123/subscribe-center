package com.wly.center.samples.context;

import com.wly.center.samples.bean.User;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserContext {
    private static final ConcurrentMap<String, User> USER_CACHE = new ConcurrentHashMap<>();

    public static void add(User user) {
        USER_CACHE.putIfAbsent(user.getName(), user);
    }

    public static User put(User user) {
        return USER_CACHE.put(user.getName(), user);
    }

    public static User get(String name) {
        return USER_CACHE.get(name);
    }

    public static User remove(String name) {
        return USER_CACHE.remove(name);
    }
}
