package com.foxconn.fxcsprcservice;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import com.foxconn.fxcsprcservice.utils.Debug;
import com.foxconn.fxcsprcservice.utils.NetUtil;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import android.os.Process;

//import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class SPRCService extends Service {
    private static final String TAG = "SPRCService";
    private static final int UDP_PORT = 9687;
    private static final int TCP_PORT = 9688;
    private static Context mContext;
    private static String mRemoteIp = "255.255.255.255";
    private static String mLocalIp = "255.255.255.255";
    private static String mProtocolType = "IPPROTO_UDP";
    private static String mModelName = "LCD-SPRC";
    private static String mSerialNum = "888888888888";
 //   private static String mSDK = "IQIYI";
    private static String mTvFwVersion = "V2.09";
    private static String mTvBtMAC = "00:00:00:00:00:00";
    private static String mVerName = "";
    private static String mVerCode = "";
    private static String mTvCommandVersion = "27";
    DatagramSocket mUdpSocket = null;
    private NetBroadcastReceiver netBroadcastReceiver=null;
    private TcpSocketThread tcpSocketThread;
    ServerSocket mTcpServiceSocket = null;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Debug.d(TAG, "onStartCommand   startId=" + startId);
        IntentFilter ifilter = new IntentFilter();
        netBroadcastReceiver = new NetBroadcastReceiver();
  //      ifilter.addAction(CONNECTIVITY_ACTION);
  //      ifilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        ifilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        ifilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netBroadcastReceiver, ifilter);
        mContext = getApplicationContext();
        mTvFwVersion =getTVVersion();
        mSerialNum = getTVSN();
    //    mSDK = getTVSDK();
        mModelName = getTVModel();
       // mTvBtMAC = getTVBTMAC();
        mTvBtMAC =getTVBtAddress();
        mLocalIp = getLocalIpAddress();
        mVerName = getVersionName(mContext);
        mVerCode = getVersionCode(mContext);
        UdpSocketThread udpSocketThread = new UdpSocketThread(mContext,UDP_PORT,
                mLocalIp, mModelName, mSerialNum, mTvFwVersion,mVerName,mVerCode,mTvCommandVersion);
       // udpSocketThread.setOSPriority(Process.THREAD_PRIORITY_URGENT_AUDIO); // -19
        udpSocketThread.setOSPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY); // -8
        udpSocketThread.setPriority(Thread.MAX_PRIORITY); // 10
        udpSocketThread.start();
        tcpSocketThread = new TcpSocketThread(mContext,TCP_PORT);
      //  tcpSocketThread.setPriority(Thread.MAX_PRIORITY); // 10
        tcpSocketThread.start();
     //   HandleKeyEventThread handleKeyEventThread = new HandleKeyEventThread();
     //   handleKeyEventThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    public class NetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String strAction = intent.getAction();
            Debug.d(TAG, "get Broadcast Receiver Action="+strAction);
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                int netWorkState = NetUtil.getNetWorkState(context);
                if (netWorkState == 1) {
                    mLocalIp = getLocalIpAddress();
                    Debug.d(TAG, "get Broadcast Receiver LocalIp="+mLocalIp);
                }
            }
        else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected = state == NetworkInfo.State.CONNECTED;
               // Debug.d(TAG, "isConnected:" + isConnected);
                if (isConnected) {
                    tcpSocketThread.setConnectState(true);
                    Debug.d(TAG, "Connect");
                } else {
                    Debug.d(TAG, "Disconnect");
                    tcpSocketThread.setConnectState(false);
                }
            }
        }
        }
    }

    private static String getTVBtAddress() {
        String mTvBtMAC = android.provider.Settings.Secure.getString(mContext.getContentResolver(), "bluetooth_address");
        return mTvBtMAC;
    }

    /**
     * get App versionCode
     * @param context
     * @return
     */
    public  String getVersionCode(Context context){
        PackageManager packageManager=context.getPackageManager();
        PackageInfo packageInfo;
        String versionCode= "" ;
        try  {
            packageInfo=packageManager.getPackageInfo(context.getPackageName(), 0 );
            versionCode=packageInfo.versionCode+ "" ;
        }  catch  (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return  versionCode;
    }

    /**
     * get App versionName
     * @param context
     * @return
     */
    public  String getVersionName(Context context){
        PackageManager packageManager=context.getPackageManager();
        PackageInfo packageInfo;
        String versionName= "" ;
        try  {
            packageInfo=packageManager.getPackageInfo(context.getPackageName(), 0 );
            versionName=packageInfo.versionName;
        }  catch  (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return  versionName;
    }


    public String getTVModel() {
        try {
            Class<?> CLASS = Class.forName("android.os.SystemProperties");
            String ModelName= (String) CLASS.getMethod("get", String.class).invoke(null, "persist.sys.fxc.model");
            return ModelName;

        }catch (IllegalAccessException e) {
            Debug.i(TAG, "IllegalAccessException====>" + e.toString());
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            Debug.i(TAG, "NoSuchMethodException====>" + e.toString());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Debug.i(TAG, "InvocationTargetException====>" + e.toString());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Debug.i(TAG, "ClassNotFoundException====>" + e.toString());
            e.printStackTrace();
        }
        return mModelName;
    }
    public String getTVVersion() {
        try{
            Class<?> CLASS = Class.forName("android.os.SystemProperties");
            String TvFwVersion= (String) CLASS.getMethod("get", String.class).invoke(null, "fxc.system.version");
            return TvFwVersion;
        }catch (IllegalAccessException e) {
            Debug.i(TAG, "IllegalAccessException====>" + e.toString());
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            Debug.i(TAG, "NoSuchMethodException====>" + e.toString());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Debug.i(TAG, "InvocationTargetException====>" + e.toString());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Debug.i(TAG, "ClassNotFoundException====>" + e.toString());
            e.printStackTrace();
        }
        return mTvFwVersion;
    }

    public String getTVBTMAC() {
        try {
            Class<?> CLASS = Class.forName("android.os.SystemProperties");
            String TvBtMAC= (String) CLASS.getMethod("get", String.class).invoke(null, "persist.sys.tvbtmac");
            if("".equals(TvBtMAC))
                TvBtMAC="00:00:00:00:00:00";
            return TvBtMAC;
        }catch (IllegalAccessException e) {
            Debug.i(TAG, "IllegalAccessException====>" + e.toString());
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            Debug.i(TAG, "NoSuchMethodException====>" + e.toString());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Debug.i(TAG, "InvocationTargetException====>" + e.toString());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Debug.i(TAG, "ClassNotFoundException====>" + e.toString());
            e.printStackTrace();
        }
        return mTvBtMAC;
    }

    public String getTVSN() {
        try {
            Class<?> CLASS = Class.forName("android.os.SystemProperties");
            String SerialNum= (String) CLASS.getMethod("get", String.class).invoke(null, "persist.sys.fxc.sn");
            return SerialNum;
        }catch (IllegalAccessException e) {
            Debug.i(TAG, "IllegalAccessException====>" + e.toString());
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            Debug.i(TAG, "NoSuchMethodException====>" + e.toString());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Debug.i(TAG, "InvocationTargetException====>" + e.toString());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Debug.i(TAG, "ClassNotFoundException====>" + e.toString());
            e.printStackTrace();
        }
        return mSerialNum;
    }

    /*
    public String getTVSDK() {
        try {
            Class<?> CLASS = Class.forName("android.os.SystemProperties");
            mSDK= (String) CLASS.getMethod("get", String.class).invoke(null, "persist.sys.fxc.sdk");
            return mSDK;
        }catch (IllegalAccessException e) {
            Debug.i(TAG, "IllegalAccessException====>" + e.toString());
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            Debug.i(TAG, "NoSuchMethodException====>" + e.toString());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Debug.i(TAG, "InvocationTargetException====>" + e.toString());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Debug.i(TAG, "ClassNotFoundException====>" + e.toString());
            e.printStackTrace();
        }
        return mSDK;
    }
*/

    public String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> enumerationNi = NetworkInterface
                    .getNetworkInterfaces();
            while (enumerationNi.hasMoreElements()) {
                NetworkInterface networkInterface = enumerationNi.nextElement();
                String interfaceName = networkInterface.getDisplayName();
                if (interfaceName.equals("wlan0")) {
                    Enumeration<InetAddress> enumIpAddr = networkInterface
                            .getInetAddresses();
                    while (enumIpAddr.hasMoreElements()) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && inetAddress instanceof Inet4Address) {
                            String ip = inetAddress.getHostAddress().trim();
                            return ip;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Debug.d(TAG, "on Destroy　");
        unregisterReceiver(netBroadcastReceiver);
        Intent intent = new Intent();
        intent.setClass(this, SPRCService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);
    }
}
