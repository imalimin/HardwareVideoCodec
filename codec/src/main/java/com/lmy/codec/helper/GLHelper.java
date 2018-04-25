package com.lmy.codec.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;

/**
 * Created by lmyooyo@gmail.com on 2018/4/25.
 */

public class GLHelper {
    public static native void glReadPixels(int x,
                                           int y,
                                           int width,
                                           int height,
                                           int format,
                                           int type);

    /**
     * @param context
     * @return hex
     */
    public static int glVersion(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        if (null == info) return 0;
        return info.reqGlEsVersion;
    }
}
