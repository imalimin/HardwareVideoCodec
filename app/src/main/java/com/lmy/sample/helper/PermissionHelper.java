package com.lmy.sample.helper;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lmy on 2017/2/18.
 */

public class PermissionHelper {
    private final static String TAG = PermissionHelper.class.getSimpleName();
    public final static int REQUEST_MY = 0x0102;
    public final static String[] PERMISSIONS_BASE = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};

    public static boolean requestPermissions(@NonNull Activity activity, @NonNull String[] permissions) {
        if (shouldShowRequestPermissionRationale(activity, permissions))
            return false;
        boolean val = checkSelfPermission(activity, permissions);
        if (!val)
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_MY);
        return val;
    }

    public static boolean requestPermissions(@NonNull Fragment fragment, @NonNull String[] permissions) {
        if (shouldShowRequestPermissionRationale(fragment, permissions))
            return false;
        boolean val = checkSelfPermission(fragment.getContext(), permissions);
        if (!val)
            fragment.requestPermissions(permissions, REQUEST_MY);
        return val;
    }

    public static boolean checkSelfPermission(@NonNull Context context, @NonNull String[] permissions) {
        boolean val = true;
        for (String p : permissions) {
            val = val && (ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED);
        }
        return val;
    }

    public static boolean shouldShowRequestPermissionRationale(@NonNull Activity activity, @NonNull String[] permissions) {
        List<String> list = new ArrayList<>();
        for (String p : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, p))
                list.add(p);
        }
        if (list.size() == 0) return false;
        ActivityCompat.requestPermissions(activity, list.toArray(new String[]{}), REQUEST_MY);
        return true;
    }

    public static boolean shouldShowRequestPermissionRationale(@NonNull Fragment fragment, @NonNull String[] permissions) {
        List<String> list = new ArrayList<>();
        for (String p : permissions) {
            if (fragment.shouldShowRequestPermissionRationale(p))
                list.add(p);
        }
        if (list.size() == 0) return false;
        ActivityCompat.requestPermissions(fragment.getActivity(), list.toArray(new String[]{}), REQUEST_MY);
        return true;
    }

    public static boolean checkGrantResults(int[] grantResults) {
        if (grantResults == null) return true;
        for (int g : grantResults) {
            if (g != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    /**
     * 去应用权限管理界面
     */
    public static void gotoPermissionManager(Context context) {
        String packageName = context.getPackageName();
        Intent intent;
        ComponentName comp;
        //防止刷机出现的问题
        try {
            switch (Build.MANUFACTURER) {
                case "Huawei":
                    intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("packageName", packageName);
                    comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                    break;
                case "Meizu":
                    intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.putExtra("packageName", packageName);
                    context.startActivity(intent);
                    break;
                case "Xiaomi":
                    String rom = getSystemProperty("ro.miui.ui.version.name");
                    if ("v5".equals(rom)) {
                        Uri packageURI = Uri.parse("package:" + context.getApplicationInfo().packageName);
                        intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                    } else {//if ("v6".equals(rom) || "v7".equals(rom)) {
                        intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                        intent.putExtra("extra_pkgname", context.getPackageName());
                    }
                    context.startActivity(intent);
                    break;
                case "Sony":
                    intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("packageName", packageName);
                    comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                    break;
                case "OPPO":
                    intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("packageName", packageName);
                    comp = new ComponentName("com.color.safecenter", "com.color.safecenter.permission.PermissionManagerActivity");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                    break;
                case "LG":
                    intent = new Intent("android.intent.action.MAIN");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("packageName", packageName);
                    comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                    break;
                case "Letv":
                    intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("packageName", packageName);
                    comp = new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                    break;
                default:
                    getAppDetailSettingIntent(context);
                    break;
            }
        } catch (Exception e) {
            getAppDetailSettingIntent(context);
        }
    }

    /**
     * 获取系统属性值
     */
    private static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read sysprop " + propName);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing InputStream");
                }
            }
        }
        return line;
    }

    //以下代码可以跳转到应用详情，可以通过应用详情跳转到权限界面(6.0系统测试可用)
    private static void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        launchApp(context, localIntent);
    }

    /**
     * 安全的启动APP
     */
    private static void launchApp(Context ctx, Intent intent) {
        if (ctx == null)
            throw new NullPointerException("ctx is null");
        try {
            ctx.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}