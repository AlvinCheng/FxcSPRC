package com.foxconn.fxcsprcservice;

/**
 * Created by alvin on 2018/4/19.
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.net.*;
import java.util.concurrent.*;

import com.foxconn.fxcsprcservice.utils.Debug;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
public class TcpSocketThread extends Thread {
        private static final String TAG = "TCP";
        private ServerSocket mTcpServiceSocket;
        private String mProtocolType = "IPPROTO_TCP";
        private String reciveString ="";
        private int  count =  0 ;//client count
        private ExecutorService executorService;
        private final int POOL_SIZE=10;
        private static int which = 0;
     //   private static List<Socket> mClientList = new ArrayList<>();
        private Socket tcpSocket;
        private Context mContext;
        private InputStream mInputStream;
        private OutputStream mOutputStream;
        private static final int INPUT_STREAM_READ_TIMEOUT = 300;
        private Handler mHandler;
        private Handler mHandlerThread = new Handler();
        private static boolean IsNetConnect = true;

        public TcpSocketThread(Context mTCPContext , int port) {
            try {
                mTcpServiceSocket = new ServerSocket(port,10);
			  //  mTcpServiceSocket.setSoTimeout(3000);
              //Debug.d(TAG, "Available Processors ="+Runtime.getRuntime().availableProcessors());
             // executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*POOL_SIZE);
                executorService=Executors.newFixedThreadPool(POOL_SIZE);
                mContext=mTCPContext;
                Debug.d(TAG, "Init TCP Socket Thread Start=>");
            } catch (IOException e) {
                e.printStackTrace();
                Debug.d(TAG, "TCP socket error====>" + e.toString());
            }
        }

        public void setConnectState(boolean IsConnect)
        {
            this.IsNetConnect = IsConnect;
        }

        public void run() {
            if (mTcpServiceSocket != null){
                try {
                    while (true) {
                        Debug.d(TAG, "Server Listening=>");
                        tcpSocket = mTcpServiceSocket.accept();

                        if (tcpSocket.isConnected()){
                            Debug.d(TAG, String.format("RemoteSocketAddress = %s LocalSocketAddress = %s ", tcpSocket.getRemoteSocketAddress(), tcpSocket.getLocalSocketAddress()));
                            executorService.execute(new ServerThread(mContext, tcpSocket));
                            //executorService.execute(new HandlerServerReceiveRunnable(mClientList.size() - 1, tcpSocket));
                        }

                        if(!IsNetConnect){
                            Debug.i(TAG, "The time TcpServiceSocket close!=IsDisConnect");
                           // executorService.shutdown();
                        }

                    }
                } catch (SocketTimeoutException e){
                    Debug.i(TAG, "SocketTimeoutExceptionr===error=>" + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    Debug.i(TAG, "Tcp IOException===error>" + e.toString());
                }catch (Exception e) {
                    Debug.i(TAG, "Tcp socket Exception error===>" + e.toString());
                }
                finally{
                    if(mTcpServiceSocket != null){
                        try {
                            mTcpServiceSocket.close();
                            executorService.shutdown();
                            Debug.i(TAG, "The Tcp Service Socket close!==");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Debug.i(TAG, "Tcp socket close error===>" + e.toString());
                        }
                        mTcpServiceSocket = null;
                    }
                }
            }
        }
    /**
     * Receive Client data tread
     */
    class HandlerServerReceiveRunnable implements Runnable {
        private int which;
        private Socket mSocket;

        HandlerServerReceiveRunnable(int which, Socket socket) {
            this.which = which;
            this.mSocket = socket;
        }

        /**
         *  push meagess to Handler
         */
        private void pushMsgToHandler(int which, byte[] data) {
            Message message = mHandler.obtainMessage();
            message.what = which;
            message.obj = data;
            mHandler.sendMessage(message);
        }

        @Override
        public void run() {
            try {
              //  mSocket.setSoTimeout(INPUT_STREAM_READ_TIMEOUT);
                Debug.d(TAG, "HandlerServerReceiveRunnable run====tcpSocket="+tcpSocket);
                while (mSocket != null && mSocket.isConnected()) {
                    InputStream in = mSocket.getInputStream();
                    byte[] data = new byte[0];
                    byte[] buf = new byte[1024];
                    int len;
                    try {
                        while ((len = in.read(buf)) != -1) {
                            byte[] temp = new byte[data.length + len];
                            System.arraycopy(data, 0, temp, 0, data.length);
                            System.arraycopy(buf, 0, temp, data.length, len);
                            data = temp;
                        }
                    } catch (SocketTimeoutException stExp) {
                        Debug.i(TAG, "SocketTimeoutException=only catch ==");
                        // stExp.printStackTrace();
                    }
                    if (data.length != 0) {
                       // pushMsgToHandler(which, data);
                        String result = new String(data, Charset.forName("UTF-8"));
                        Debug.d(TAG, "getInputStream()==client =ALVIN= reciveString=" + result);
                        //serverSendByTcp(result, mSocket);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
