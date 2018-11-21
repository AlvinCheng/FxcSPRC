package com.foxconn.fxcsprcservice;

import android.content.Context;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.List;
import android.os.Process;

import com.foxconn.fxcsprcservice.utils.Debug;
import com.foxconn.fxcsprcservice.utils.NetUtil;

/**
 * Created by alvin on 2018/4/19.
 */

public class UdpSocketThread extends Thread {
    private static final String TAG = "UDP";
    private int mOSPriority = Process.THREAD_PRIORITY_DEFAULT;
    private DatagramSocket mUdpSocket;
    private Context mContext;
    private static final int UDP_PORT = 9687;
    private static final int TCP_PORT = 9688;
    private static String mRemoteIp = "255.255.255.255";
    private static String mLocalIp = "255.255.255.255";
    private static String mProtocolType = "IPPROTO_UDP";
    private static String mModelName = "LCD-SPRC";
    private static String mSerialNum = "8888888888";
    private static String mTvFwVersion = "V2.09";
    private static String mVerName = "";
    private static String mVerCode = "";
    private static String mTvBtMAC = "00:00:00:00:00:00";
    private static String mTvCommandVersion = "27";

    public UdpSocketThread(Context mUDPContext ,int port, String localIp, String modelName,
                           String serialNum, String tvFwVersion, String tvBtMac, String vername, String vercode,String TvCommandVersion) {
        try {
            mContext=mUDPContext;
            mUdpSocket = new DatagramSocket(UDP_PORT);
            mLocalIp = localIp;
            mModelName = modelName;
            mSerialNum = serialNum;
            mTvFwVersion = tvFwVersion;
            mTvBtMAC = tvBtMac;
            mVerName = vername;
            mVerCode = vercode;
            mTvCommandVersion = TvCommandVersion;
            Debug.d(TAG, "ModelName =" + mModelName);
            Debug.d(TAG, "TvFwVersion =" + mTvFwVersion);
            Debug.d(TAG, "SerialNum =" + mSerialNum);
            Debug.d(TAG, "LocalIp =" + mLocalIp);
            Debug.d(TAG, "BtAddress =" + mTvBtMAC);
            Debug.d(TAG, "SPRC Version :V" + mVerName);
     //       Debug.d(TAG, "SPRC VerCode =" + mVerCode);
            Debug.d(TAG, "Init UDP Socket Thread Start");
        } catch (IOException e) {
            e.printStackTrace();
            Debug.d(TAG, "UDP Socket error====>" + e.toString());
        }
    }

    public void setOSPriority(int p) {
        mOSPriority = p;
    }

    public String getRemoteIp() {
        return mRemoteIp;
    }

    private void serverSendByUdp(String result) {
        mProtocolType = "IPPROTO_TCP";
        //Debug.d(TAG, "ProtocolType==" + mProtocolType);
        try {
            InetAddress serverAddress = InetAddress.getByName(mRemoteIp);
            String returnInfo = "ERROR";
            StringBuffer sb = new StringBuffer();
            if (result.equals(CommandTranf.DEBG_TEST)) {
                sb.append(CommandTranf.DEBG_TEST_RETURN);
                sb.append(CommandTranf.TEST);
                sb.append(CommandTranf.SUFFIX);
            } else if (result.equals(CommandTranf.SPRC_DISC_ALL)) {
                sb.append(CommandTranf.SPRC_DISC_ALL_RETURN);
                sb.append(mLocalIp);
                sb.append("|");
                sb.append(mProtocolType);
                sb.append("|");
                sb.append(TCP_PORT);
                sb.append("|");
                sb.append(mModelName);
                sb.append("|");
                sb.append(mTvFwVersion);
                sb.append("|");
                sb.append(mSerialNum);
                sb.append("|");
                sb.append(mTvBtMAC);
                sb.append("|");
                sb.append(mTvCommandVersion);
                sb.append(CommandTranf.SUFFIX);
            } else if (result.equals(CommandTranf.SPRC_DISC_IP)){
                sb.append(CommandTranf.SPRC_DISC_IP_RETURN);
                sb.append(mLocalIp);
                sb.append(CommandTranf.SUFFIX);
            }else if (result.equals(CommandTranf.SPRC_DISC_PROTOCOL)) {
                sb.append(CommandTranf.SPRC_DISC_PROTOCOL_RETURN);
                sb.append(mProtocolType);
                sb.append(CommandTranf.SUFFIX);
            }else if (result.equals(CommandTranf.SPRC_DISC_PORT)) {
                sb.append(CommandTranf.SPRC_DISC_PORT_RETURN);
                sb.append(TCP_PORT);
                sb.append(CommandTranf.SUFFIX);
            }else if (result.equals(CommandTranf.SPRC_DISC_MODEL_NAME)) {
                sb.append(CommandTranf.SPRC_DISC_MODEL_NAME_RETURN);
                sb.append(mModelName);
                sb.append(CommandTranf.SUFFIX);
            }else if (result.equals(CommandTranf.SPRC_DISC_FW_VERSION)) {
                sb.append(CommandTranf.SPRC_DISC_FW_VERSION_RETURN);
                sb.append(mTvFwVersion);
                sb.append(CommandTranf.SUFFIX);
            }else if (result.equals(CommandTranf.SPRC_DISC_SN)) {
                sb.append(CommandTranf.SPRC_DISC_SN_RETURN);
                sb.append(mSerialNum);
                sb.append(CommandTranf.SUFFIX);
            }else if (result.equals(CommandTranf.SPRC_DISC_BT)) {
                sb.append(CommandTranf.SPRC_DISC_BT_RETURN);
                sb.append(mTvBtMAC);
                sb.append(CommandTranf.SUFFIX);
            }else if (result.equals(CommandTranf.SPRC_DISC_TVCMD_VERSION)) {
                sb.append(CommandTranf.SPRC_DISC_TVCMD_VERSION_RETURN);
                sb.append(mTvCommandVersion);
                sb.append(CommandTranf.SUFFIX);
            }

            returnInfo = sb.toString().trim();
            Debug.d(TAG, "TV Broadcast UDP  :" + returnInfo);
            byte data[] = returnInfo.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length,
                    serverAddress, UDP_PORT);
            mUdpSocket.send(packet);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Process.setThreadPriority(mOSPriority);
        if (mUdpSocket != null) {
            try {
                while (true) {
                    byte data[] = new byte[4 * 1024];
                    DatagramPacket packet = new DatagramPacket(data,
                            data.length);
                    mUdpSocket.receive(packet);
                    mRemoteIp = packet.getAddress().getHostAddress();
                    Debug.d(TAG, "Remote Ip==" + mRemoteIp);
                    mLocalIp = getLocalIp();
                    Debug.d(TAG, "Local Ip==" + mLocalIp);
                    String result = new String(packet.getData(),
                            packet.getOffset(), packet.getLength());
                    if (result != null && !TextUtils.isEmpty(result)) {
                        Debug.d(TAG, "TV Get UDP :" + result);
                        serverSendByUdp(result);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Debug.i(TAG, "UDP Socket Receive Error====>" + e.toString());
            }
        }
    }



    public String getLocalIp() throws SocketException {
        try {
            Enumeration<NetworkInterface> en;
            for (en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                String interfaceName = intf.getDisplayName();
               // Debug.d(TAG, "getLocalIp interfaceName==" + interfaceName);
                /*
                if(NetUtil.isNetworkAvailable(mContext)){
                    Debug.d(TAG, "isNetworkAvailable interfaceName==" + interfaceName);
                }*/
              //  if (interfaceName.equals("wlan0")){
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                            if (inetAddress instanceof Inet4Address) { // only care IPv4 address
                                return inetAddress.getHostAddress().toString();
                            }
                        }
                    }
               // }
            }
        } catch (SocketException ex) {
            Debug.i(TAG, "UDP WifiPreference IpAddress ====>" + ex.toString());

        }
        return mLocalIp;
    }
}