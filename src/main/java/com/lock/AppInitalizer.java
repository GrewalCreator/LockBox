package com.lock;

import com.lock.util.ConfigUtil;
import com.lock.util.OSUtil;
import com.lock.util.SecureUtil;
import com.lock.util.LoggerUtil;

public class AppInitalizer {
    private final String APP_NAME = "LockBox";

    public void initApplication(){
        initAppDir();
        createLogDir();
        createConfigFile();
        createSecureFiles();
        verifyAppVersion();
    }

    private void initAppDir(){
        OSUtil.init(APP_NAME);
    }

    private void createLogDir(){
        LoggerUtil.initLogger();
    }

    private void createConfigFile(){
        ConfigUtil.init(APP_NAME);
    }

    private void createSecureFiles(){
        SecureUtil.initSecurity();;
    }

    private void verifyAppVersion(){
        // Check Github Release Version
        return;
    }
}
