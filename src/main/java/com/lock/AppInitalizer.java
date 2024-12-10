package com.lock;

import com.lock.util.ConfigUtil;
import com.lock.util.OSUtil;

public class AppInitalizer {

    public void initalize(){
        String appName = ConfigUtil.loadAppNameFromTemplate();
        OSUtil.init(appName);
        ConfigUtil.init(appName);
    }
}
