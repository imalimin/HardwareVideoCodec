package com.lmy.codec.helper;

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
}
