package com.foxconn.fxcsprcservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.foxconn.fxcsprcservice.utils.Debug;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by alvin on 2018/5/9.
 */

public class BootUpReceiverToStartService extends BroadcastReceiver {
    private static final String TAG = "BootUpReceiverToStartService";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SPRCService", "onReceive");
        try{
            Class<?> CLASS = Class.forName("android.os.SystemProperties");
            String CtsEnable= (String) CLASS.getMethod("get", String.class).invoke(null, "ro.cts.enable");
            if(!CtsEnable.equals("true"))
            {
                Debug.d(TAG, "SPRC Service START ");
                intent = new Intent();
                intent.setClass(context, SPRCService.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(intent);
            }
            else
                Debug.d(TAG, "When CTS Enable SPRC Service STOP");

        }catch (IllegalAccessException e) {
            Debug.i((String) TAG, "IllegalAccessException====>" + e.toString());
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }




    }
}
