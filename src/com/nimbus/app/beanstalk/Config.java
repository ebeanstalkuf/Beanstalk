package com.nimbus.app.beanstalk;

final public class Config {
    public static final String CLIENT_ID = "00000000480DC92A";

    public static final String[] SCOPES = {
        "wl.signin",
        "wl.basic",
        "wl.offline_access",
        "wl.skydrive_update",
        "wl.contacts_create",
    };

    private Config() {
        throw new AssertionError("Unable to create Config object.");
    }
}