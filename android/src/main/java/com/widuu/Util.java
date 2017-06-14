package com.widuu;

/**
 * Created by widuu on 2017/6/14.
 */

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;


public class Util {

    public static final String BAIDU_APP_ID = "BAIDU_APP_ID";
    public static final String BAIDU_API_KEY = "BAIDU_API_KEY";
    public static final String BAIDU_SECRET_KEY = "BAIDU_SECRET_KEY";

    public static boolean isEmpty(String s) {
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
        if (s.trim().length() == 0)
            return true;
        return false;
    }


    // BAIDU ApiKey
    public static String getApiKey(Context context, String tag) {
        Bundle metaData = null;
        String appKey = null;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != ai)
                metaData = ai.metaData;
            if (null != metaData) {
                switch (tag){
                    case "appid":
                        appKey = metaData.getString(BAIDU_APP_ID);
                        break;
                    case "appkey":
                        appKey = metaData.getString(BAIDU_API_KEY);
                        break;
                    case "secretkey":
                        appKey = metaData.getString(BAIDU_SECRET_KEY);
                        break;
                }

                if ( null == appKey ) {
                    appKey = null;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {

        }
        return appKey;
    }
}


