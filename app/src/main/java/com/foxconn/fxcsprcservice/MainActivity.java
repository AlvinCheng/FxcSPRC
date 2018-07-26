package com.foxconn.fxcsprcservice;

        import android.os.Bundle;
        import android.app.Activity;

        import java.io.IOException;
        import java.net.DatagramPacket;
        import java.net.DatagramSocket;
        import java.net.SocketException;
        import android.content.Intent;
        import android.os.Bundle;
        import android.os.PersistableBundle;
        import android.support.annotation.Nullable;
        import android.text.TextUtils;
        import android.util.Log;


/**
 * Created by alvin on 2018/5/10.
 */

public class MainActivity extends Activity{

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent();
        intent.setClass(this, SPRCService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);
        // new Thread(new Runnable() {
        //
        // @Override
        // public void run() {
        // // TODO Auto-generated method stub
        // Log.i(TAG, "start==");
        // ServerReceviedByUdp();
        // }
        // }).start();
    }

    public void ServerReceviedByUdp() {
        DatagramSocket socket;
        try {
            socket = new DatagramSocket(9687);
            byte data[] = new byte[4 * 1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            socket.receive(packet);
            String result = new String(packet.getData(), packet.getOffset(),
                    packet.getLength());
            Log.i(TAG, "result==" + result);
        } catch (SocketException e) {
            Log.i(TAG, "SocketException()==");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i(TAG, "IOException()==");
            e.printStackTrace();
        }
    }


}
