/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;

/**
 * Created by lmyooyo@gmail.com on 2018/4/25.
 */

public class GLHelper {
    private final static int PBO_SUPPORT_VERSION = 0x30000;

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

    public static boolean isSupportPBO(Context context) {
        return GLHelper.glVersion(context) > PBO_SUPPORT_VERSION;
    }
}
