
package com.foxconn.fxcsprcservice.utils;

import android.util.Log;

public class Debug {

    private static String TAG = "SPRC_";

    private static boolean bLogEcho = true;

    // v 1, d 2, i 3, w 4, e 5
    // v : function tracer
    // d :

    public static void setLogEcho(boolean bVal) {
        bLogEcho = bVal;
    }

    // critical debug message
    public static void e(String tag, String msg) {
        if (bLogEcho) {
            Log.e(TAG + tag, msg);
        }
    }

    // warning debug message
    public static void w(String tag, String msg) {
        if (bLogEcho) {
            Log.w(TAG + tag, msg);
        }
    }

    // normal message
    public static void i(String tag, String msg) {
        if (bLogEcho) {
            Log.i(TAG + tag, msg);
        }
    }

    // for some value print
    public static void d(String tag, String msg) {
        if (bLogEcho) {
            Log.d(TAG + tag, msg);
        }
    }

    // for function tracing
    public static void v(String tag, String msg) {
        if (bLogEcho) {
            Log.v(TAG + tag, msg);
        }
    }
}
