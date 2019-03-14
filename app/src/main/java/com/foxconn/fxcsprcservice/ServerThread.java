package com.foxconn.fxcsprcservice;

/**
 * Created by alvin on 2018/4/19.
 */

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import com.foxconn.fxcsprcservice.utils.Debug;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static android.media.AudioManager.STREAM_MUSIC;
import static com.foxconn.fxcsprcservice.CommandTranf.*;

public class ServerThread extends Thread {
    private static final String TAG = "ServerThread";
    private static Context mContext;
    private static String VolumeStr;
    private DataInputStream clientin;
    private Socket tcpSocket;
    private String line = null;
    private String reciveString = "";
    private String result = "";
    private ServerSocket TCPServerSocket;
    private static List<Socket> mClientList = new ArrayList<>();
    private long curTime = 0;
    private long RepeatTime = 0;
    private AudioManager mAudioManager;
    private ContentResolver mContentResolver;
    private static final int CONNECT_TIMEOUT = 1200000;//20 min
    private static int KeyCode = 0;
    private int Repeat_count = 0;
    private int repeat_thread_hold = 3;
    private static boolean IsNumberKey = false;
    // private int prekey = 0;
    private static int prekey = 0;
    private boolean IsRepeat = false;
    private static int prekeyCode = 0;
    private static int key = 0;
    public static boolean KeyThreadExit = false;
    public static boolean IsShow = false;
    private static int which = 0;

    @SuppressWarnings("unused")
    private ServerThread() {
        // can't use Constructor
    }

    public ServerThread(Context ServiceContext, Socket TCPSocket) {
        // Debug.d(TAG, "ServerThread start=======in===========>"+Thread.currentThread().getName() + "accept connection from client");
        tcpSocket = TCPSocket;
        mContext = ServiceContext;
        mClientList.add(tcpSocket);
        for (int c = 0; c < mClientList.size(); c++) {
            // Debug.d(TAG, "Client c[] =" +mClientList.get(c));
            Debug.d(TAG, String.format("accepted from Client : c[%d]=%s", c, mClientList.get(c)));
            //Debug.d(TAG, "Tcp Socket getInetAddress="+ tcpSocket.getInetAddress());
            if (tcpSocket.getInetAddress().equals(mClientList.get(c).getInetAddress())) {
                Debug.d(TAG, "Remove the same IP in Client List ");
                mClientList.remove(c);
            }
            which = c;
        }
        Debug.d(TAG, "Which client =" + which);
        mAudioManager = (AudioManager) mContext.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (null == mAudioManager) {
            Debug.d(TAG, "\"Failed to get AudioManager\" ");
            return;
        }

        /*  remove  keyThread for repeat key
        Thread keyThread = new Thread(new keyThread());
        keyThread.start();
        */

    }

    public ContentResolver getContentResolver() {
        if (mContentResolver == null) {
            mContentResolver = mContext.getContentResolver();
        }
        return mContentResolver;
    }

    public Socket getClient(int which) {
        return which < 0 || which >= mClientList.size() ? null : mClientList.get(which);
    }

    public int getClientCount() {
        return mClientList.size();
    }

    private void serverSendByTcp(String result, Socket tcpSocket) {
        // TODO Auto-generated method stub
        try {
            // Debug.i(TAG, "serverSendByTcp===");
            OutputStream outputStream = tcpSocket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            String returnInfo = "ERROR";
            StringBuffer sb = new StringBuffer();
            if (result.equals(CommandTranf.SPRC_DELI)) {
                sb.append(CommandTranf.SPRC_DELI_SYNC_OK);
                sb.append(CommandTranf.TCP_SYNC_OK);
                sb.append(CommandTranf.SUFFIX);
            } else if (result.contains(SPRC_DISC_MUTE_STATE)) {
                String state = getTVMute();
                sb.append(CommandTranf.SPRC_DISC_MUTE_STATE_RETURN);
                sb.append(state);
                sb.append(CommandTranf.SUFFIX);
            } else if (result.contains(SPRC_DISC_GET_VOLUME)) {
                String VolumeStr = getVolume();
                sb.append(CommandTranf.SPRC_DISC_GET_VOLUME_RETURN);
                sb.append(VolumeStr);
                sb.append(CommandTranf.SUFFIX);
            } else if (result.contains(SPRC_DISC_GET_CURRENT_PAGE_NAME)) {
                //String PageNameStr = getTopActivityInfo(mContext);
                String PageNameStr = getPageName();
                sb.append(CommandTranf.SPRC_DISC_GET_CURRENT_PAGE__NAME_RETURN);
                sb.append(PageNameStr);
                sb.append(CommandTranf.SUFFIX);
            } else if (result.contains(SPRC_DISC_GET_TV_SDK)) {
                String TvSdkStr = getTVSDK();
                String VoiceSdkStr = getVoiceSDK();
                sb.append(CommandTranf.SPRC_DISC_GET_TV_SDK_RETURN);
                sb.append(TvSdkStr + "|" + VoiceSdkStr);
                sb.append(CommandTranf.SUFFIX);
            }
            /*
            else if(result.contains(SPRC_DISC_GET_VOICE_SDK)){
                //String PageNameStr = getTopActivityInfo(mContext);
                String VoiceSdkStr = getVoiceSDK();
                sb.append(CommandTranf.SPRC_DISC_GET_VOICE_SDK_RETURN);
                sb.append(VoiceSdkStr);
                sb.append(CommandTranf.SUFFIX);
            }
            */
            else {
                sb.append(CommandTranf.SPRC_DELI_ACCEPT);
                sb.append(CommandTranf.TCP_ACCEPT);
                sb.append(CommandTranf.SUFFIX);
            }
            returnInfo = sb.toString().trim();
            printWriter.print(returnInfo);
            printWriter.flush();
            Debug.d(TAG, " TV TCP Return Info:" + returnInfo + " To Clinet :(" + tcpSocket.getRemoteSocketAddress() + ")");
            //      Debug.d(TAG, Thread.currentThread().getName() + "closing connection with client");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Debug.d(TAG, "Tcp UnknownHostException===>" + e.toString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            try {
                Debug.d(TAG, "Tcp Socket close");
                tcpSocket.close();
            } catch (IOException e1) {
                Debug.d(TAG, "Tcp IOException==>" + e.toString());
                e1.printStackTrace();
            }
        }
    }

    public String getTVSDK() {
        String mSDK = "";
        try {
            Class<?> CLASS = Class.forName("android.os.SystemProperties");
            mSDK = (String) CLASS.getMethod("get", String.class).invoke(null, "persist.sys.fxc.sdk");
            return mSDK;
        } catch (IllegalAccessException e) {
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

    public String getVoiceSDK() {
        String mVoiceSDK = "";
        try {
            Class<?> CLASS = Class.forName("android.os.SystemProperties");
            mVoiceSDK = (String) CLASS.getMethod("get", String.class).invoke(null, "persist.sys.fxc.voice.sdk");
            return mVoiceSDK;
        } catch (IllegalAccessException e) {
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
        return mVoiceSDK;
    }


    private int querySpeakerMute() {
        int ret = 0;
        Cursor cursor = getContentResolver().query(
                Uri.parse("content://mstar.tv.usersetting/soundsetting"), null, null, null, null);
        if (cursor.moveToFirst()) {
            ret = cursor.getInt(cursor.getColumnIndex("Speaker_Mute"));
        }
        cursor.close();
        return ret;
    }

    private String getTVMute() {
        // AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        String mute = "";
        boolean isMute = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mAudioManager.isStreamMute(STREAM_MUSIC))
                mute = "mute";
            else {
                mute = "unmute";
            }
        } else {
            try {
                Class clz = Class.forName("android.media.AudioManager");
                Method isMasterMute = clz.getMethod("isMasterMute");
                isMute = (boolean) isMasterMute.invoke(mAudioManager);
            } catch (Exception e) {
                Debug.e(TAG, "Error" + e.toString());
                //Do something
            }
            // if (querySpeakerMute()==0)
            if (isMute)
                mute = "mute";
            else
                mute = "unmute";
        }
        Debug.d(TAG, "state=" + mute);
        return mute;
    }

    private String getPageName() {
        String PageName = "";
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(2);
        // if (list != null && list.size() > 0) {
        PageName = list.get(0).topActivity.getClassName();
        //  }
        Debug.d(TAG, "Get top activity name :" + PageName);
        return PageName;
    }

    public static class TopActivityInfo {
        public String packageName = "";
        public String topActivityName = "";
    }

    public static String getTopActivityInfo(Context context) {
        ActivityManager manager = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        TopActivityInfo info = new TopActivityInfo();
        if (Build.VERSION.SDK_INT >= 21) {
            List<ActivityManager.RunningAppProcessInfo> pis = manager.getRunningAppProcesses();
            ActivityManager.RunningAppProcessInfo topAppProcess = pis.get(0);
            if (topAppProcess != null && topAppProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                info.packageName = topAppProcess.processName;
                info.topActivityName = "";
            }
        } else {
            //getRunningTasks() is deprecated since API Level 21 (Android 5.0)
            List localList = manager.getRunningTasks(1);
            ActivityManager.RunningTaskInfo localRunningTaskInfo = (ActivityManager.RunningTaskInfo) localList.get(0);
            info.packageName = localRunningTaskInfo.topActivity.getPackageName();
            info.topActivityName = localRunningTaskInfo.topActivity.getClassName();
        }
        Debug.d(TAG, " Get top activity name :" + info.topActivityName);
        return info.topActivityName;
    }


    private String getVolume() {
        // AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int Volume = mAudioManager.getStreamVolume(STREAM_MUSIC);
        VolumeStr = String.valueOf(Volume);
        Debug.d(TAG, "Get Volume=" + VolumeStr);
        return VolumeStr;
    }

    private void setMute() {
        // AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAudioManager.setStreamMute(STREAM_MUSIC, true);
        } else {
            try {
                Class clz = Class.forName("android.media.AudioManager");
                Method setMasterMute = clz.getMethod("setMasterMute", boolean.class, int.class);
                setMasterMute.invoke(mAudioManager, true, 0);
            } catch (Exception e) {
                Debug.e(TAG, "Error" + e.toString());
                //Do something
            }
        }
        Debug.d(TAG, "Set Mute");
    }

    private void setUnMute() {
        // AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAudioManager.setStreamMute(STREAM_MUSIC, false);
        } else {
            try {
                Class<?> CLASS = Class.forName("android.media.AudioManager");
                CLASS.getMethod("setMasterMute", boolean.class, int.class).invoke(mAudioManager, false, 0);
            } catch (Exception e) {
                Debug.e(TAG, "Error" + e.toString());
                //Do something
            }
        }
        Debug.d(TAG, "set UnMute");
    }

    private void MapCmdToKeyEvent(String CmdStr, DataInputStream clientin) throws InterruptedException {

        if (CmdStr.contains(SPRC_DIRK_NUM_1)) {
            KeyCode = KeyEvent.KEYCODE_1;
        } else if (CmdStr.contains(SPRC_DIRK_NUM_2)) {
            KeyCode = KeyEvent.KEYCODE_2;
        } else if (CmdStr.contains(SPRC_DIRK_NUM_3)) {
            KeyCode = KeyEvent.KEYCODE_3;
        } else if (CmdStr.contains(SPRC_DIRK_NUM_4)) {
            KeyCode = KeyEvent.KEYCODE_4;
        } else if (CmdStr.contains(SPRC_DIRK_NUM_5)) {
            KeyCode = KeyEvent.KEYCODE_5;
        } else if (CmdStr.contains(SPRC_DIRK_NUM_6)) {
            KeyCode = KeyEvent.KEYCODE_6;
        } else if (CmdStr.contains(SPRC_DIRK_NUM_7)) {
            KeyCode = KeyEvent.KEYCODE_7;
        } else if (CmdStr.contains(SPRC_DIRK_NUM_8)) {
            KeyCode = KeyEvent.KEYCODE_8;
        } else if (CmdStr.contains(SPRC_DIRK_NUM_9)) {
            KeyCode = KeyEvent.KEYCODE_9;
        } else if (CmdStr.contains(SPRC_DIRK_NUM_0)) {
            KeyCode = KeyEvent.KEYCODE_0;
        } else if (CmdStr.contains(SPRC_DIRK_CH_UP)) {
            KeyCode = KeyEvent.KEYCODE_CHANNEL_UP;
        } else if (CmdStr.contains(SPRC_DIRK_CH_DOWN)) {
            KeyCode = KeyEvent.KEYCODE_CHANNEL_DOWN;
        } else if (CmdStr.contains(SPRC_DIRK_INPUT)) {
            Intent intent2 = new Intent("com.foxconn.etvg.inputsource.intent.action.FxcInputSourceActivity");
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent2);
            return;
        } else if (CmdStr.contains(SPRC_DIRK_VOLUME_UP)) {
            KeyCode = KeyEvent.KEYCODE_VOLUME_UP;
        } else if (CmdStr.contains(SPRC_DIRK_VOLUME_DOWN)) {
            KeyCode = KeyEvent.KEYCODE_VOLUME_DOWN;
        } else if (CmdStr.contains(SPRC_DIRK_POWER)) {
            KeyCode = KeyEvent.KEYCODE_POWER;
        } else if (CmdStr.contains(SPRC_DIRK_VOLUME_MUTE)) {
            KeyCode = KeyEvent.KEYCODE_VOLUME_MUTE;
        } else if (CmdStr.contains(SPRC_DIRK_DISPLAY)) {
            KeyCode = KeyEvent.KEYCODE_INFO;
        } else if (CmdStr.contains(SPRC_DIRK_MENU)) {
            KeyCode = KeyEvent.KEYCODE_MENU;
        } else if (CmdStr.contains(SPRC_DIRK_ENTER)) {
            // KeyCode = KeyEvent.KEYCODE_ENTER;
            KeyCode = KeyEvent.KEYCODE_DPAD_CENTER;
        } else if (CmdStr.contains(SPRC_DIRK_UP)) {
            KeyCode = KeyEvent.KEYCODE_DPAD_UP;
        } else if (CmdStr.contains(SPRC_DIRK_DOWN)) {
            KeyCode = KeyEvent.KEYCODE_DPAD_DOWN;
        } else if (CmdStr.contains(SPRC_DIRK_HOME)) {
            KeyCode = KeyEvent.KEYCODE_HOME;
        } else if (CmdStr.contains(SPRC_DIRK_RETURN)) {
            KeyCode = KeyEvent.KEYCODE_BACK;
        } else if (CmdStr.contains(SPRC_DIRK_LEFT)) {
            KeyCode = KeyEvent.KEYCODE_DPAD_LEFT;
        } else if (CmdStr.contains(SPRC_DIRK_RIGHT)) {
            KeyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
        } else if (CmdStr.contains(SPRC_DIRK_PAGE_UP)) {
            KeyCode = KeyEvent.KEYCODE_PAGE_UP;
        } else if (CmdStr.contains(SPRC_DIRK_PAGE_DOWN)) {
            KeyCode = KeyEvent.KEYCODE_PAGE_DOWN;
        } else if (CmdStr.startsWith(SPRC_DIRK_SEARCH)) {
            String SearchStr = null;
            String[] SearchStrSplit = null;
            SearchStrSplit = CmdStr.split("\\|");
/*
            for (int i=0; i< SearchStrSplit.length ;i++) {
              //  Debug.d(TAG, "SearchStr[0] ="+token);
              //  Debug.d(TAG, String.format("SearchStrSplit[%d] = %s ", i, SearchStrSplit[i]));
            }
 */
            for (int i = 0; i < SearchStrSplit[2].split("\\#").length; i++) {
                // Debug.d(TAG, String.format("SearchStr[%d] = %s ", i, SearchStrSplit[2].split("\\#")[i]));
                SearchStr = SearchStrSplit[2].split("\\#")[i];
            }
            Debug.d(TAG, "SearchStr =" + SearchStr);
            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(2);
            if (list != null && list.size() > 0) {
                if ("com.example.searchapp".equals(list.get(0).topActivity.getPackageName())) {
                    Intent intent2 = new Intent("com.example.searchapp.broadcast.searchfromoutside");
                    intent2.putExtra("KeyWord", SearchStr);
                    intent2.putExtra("Type", 1); //4//4:china,1:pinyin
                    intent2.putExtra("Font", 1);
                    intent2.putExtra("CmdMode", true);
                    mContext.sendBroadcast(intent2);
                    return;
                }
            }

            Intent searchIntent = new Intent("com.sharp.fxc.iqiyi_sdk_api.intent.action.MAINACTIVITY");
            searchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            searchIntent.putExtra("KeyWord", SearchStr);
            searchIntent.putExtra("Type", 1);
            searchIntent.putExtra("Font", 1);
            searchIntent.putExtra("CmdMode", true);
            mContext.startActivity(searchIntent);
            return;
        } else if (CmdStr.startsWith(SPRC_DIRK_UUID)) {
            String uuid = "", vid_idx = "";
            String[] UUIDStrSplit = CmdStr.split("\\|");
            for (int i = 0; i < UUIDStrSplit[2].split("\\#").length; i++) {

                vid_idx = UUIDStrSplit[3].split("\\#")[i];
            }
            uuid = UUIDStrSplit[2];
            Debug.d(TAG, "UUIDStr =" + uuid);
            Debug.d(TAG, "vid_idx =" + vid_idx);
            /*
            for (String Str:Strs) {
                Str=Strs;
            }*/
            ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
            if (list != null && list.size() > 0) {
                if ("com.sharp.fxc.mediainfo".equals(list.get(0).topActivity.getPackageName())) {
                    Intent intent2 = new Intent("com.sharp.fxc.mediainfo.broadcast.uuidfromoutside");
                    intent2.putExtra("FXC_EPGINFO", uuid);
                    intent2.putExtra("VID_INDEX", vid_idx);
                    mContext.sendBroadcast(intent2);
                    return;
                }
            }
            Intent uuidIntent = new Intent("com.sharp.fxc.intent.action.VideoPlayer");
            uuidIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!vid_idx.equals("0")) {
                uuidIntent.putExtra("FXC_EPGINFO", uuid);
                uuidIntent.putExtra("VID_INDEX", vid_idx);
                uuidIntent.putExtra("FXC_DEVICETYPE", "2");
            } else {
                uuidIntent.putExtra("uuid", uuid);
            }
            mContext.startActivity(uuidIntent);

            return;
        } else if (CmdStr.startsWith(SPRC_DIRK_VOICE)) {
            String[] VoiceStrSplit = CmdStr.split("\\|");
            String VoiceStr = null;
            for (int i = 0; i < VoiceStrSplit[2].split("\\#").length; i++) {
                VoiceStr = VoiceStrSplit[2].split("\\#")[i];
            }
            Debug.d(TAG, "SearchStr =" + VoiceStr);
            Intent intent2 = new Intent("com.sharp.intent.action.voicecontrol.external.input");
            intent2.putExtra("text", VoiceStr);
            mContext.sendBroadcast(intent2);
            return;
        } else if (CmdStr.startsWith(SPRC_DISC_SHORTCUT)) {
            //開啟快捷頁面
            Intent mIntent = new Intent("com.sharp.fxc.shortcut.ShortcutActivity");
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(mIntent);
            return;
        } else if (CmdStr.startsWith(SPRC_DISC_OPEN_MIC)) {
            Intent mIntent = new Intent("com.letv.openmic.with.hardware");
            mContext.sendBroadcast(mIntent);
            return;
        } else if (CmdStr.startsWith(SPRC_DISC_CLOSE_MIC)) {
            // 關閉語音輸入動畫
            Intent mIntent = new Intent("com.letv.closemic.with.hardware");
            mContext.sendBroadcast(mIntent);
            return;
        } else if (CmdStr.startsWith(SPRC_DISC_MIC_VOICE)) {
            String[] MicVoiceStrSplit = CmdStr.split("\\|");
            String MicVoiceStr = null;
            for (int i = 0; i < MicVoiceStrSplit[2].split("\\#").length; i++) {
                MicVoiceStr = MicVoiceStrSplit[2].split("\\#")[i];
            }
            Debug.d(TAG, "MicVoiceStr =" + MicVoiceStr);

            // 送出語音輸入文字 String MicVoiceStr
            Intent mIntent = new Intent("sayinfo");
            mIntent.putExtra("query", MicVoiceStr);
            mIntent.setPackage("com.sharp.fxc.mor.tv");
            mContext.sendBroadcast(mIntent);
            return;
        } else if (CmdStr.startsWith(SPRC_DISC_MOR_VOLUME)) {
            String[] MorVolStrSplit = CmdStr.split("\\|");
            String MorVolStr = null;
            int IntMorVolValue = 0;
            for (int i = 0; i < MorVolStrSplit[2].split("\\#").length; i++) {
                MorVolStr = MorVolStrSplit[2].split("\\#")[i];
            }
            Debug.d(TAG, "MorVolStr =" + MorVolStr);
            IntMorVolValue = Integer.valueOf(MorVolStr);
            // send record int volume to MOR
            Intent intent = new Intent();
            intent.setAction("recording");
            intent.putExtra("recordingVolume", IntMorVolValue);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            mContext.sendBroadcast(intent);
            return;
        } else if (CmdStr.startsWith(SPRC_DIRK_VOICE_JSON)) {
            String[] VoiceJsonStrSplit = CmdStr.split("\\|");
            String VoiceJsonStr = null;
            for (int i = 0; i < VoiceJsonStrSplit[2].split("\\#").length; i++) {
                VoiceJsonStr = VoiceJsonStrSplit[2].split("\\#")[i];
            }
            Debug.d(TAG, "VoiceJsonStr =" + VoiceJsonStr);
            Intent intent2 = new Intent();
            intent2.putExtra("cmd", VoiceJsonStr);
            intent2.setAction("xiaomor.foxconn.device.cmd.result");
            mContext.sendBroadcast(intent2);
            return;
        }
        //=============MEDIA key Broadcast
        else if (CmdStr.contains(SPRC_DIRK_PLAY)) {
            Intent intent2 = new Intent("com.sharp.intent.action.voicecontrol.media");
            intent2.putExtra("control", "play");
            mContext.sendBroadcast(intent2);
            return;
        } else if (CmdStr.contains(SPRC_DIRK_STOP)) {
            Intent intent2 = new Intent("com.sharp.intent.action.voicecontrol.media");
            intent2.putExtra("control", "stop");
            mContext.sendBroadcast(intent2);
            return;
        } else if (CmdStr.contains(SPRC_DIRK_FAST_RETURN)) {
            Intent intent2 = new Intent("com.sharp.intent.action.voicecontrol.media");
            intent2.putExtra("toward", "-20");
            mContext.sendBroadcast(intent2);
        } else if (CmdStr.contains(SPRC_DIRK_FAST_FORWARD)) {
            Intent intent2 = new Intent("com.sharp.intent.action.voicecontrol.media");
            intent2.putExtra("toward", "20");
            mContext.sendBroadcast(intent2);
            return;
        } else if (CmdStr.contains(SPRC_DIRK_PAUSE)) {
            Intent intent2 = new Intent("com.sharp.intent.action.voicecontrol.media");
            intent2.putExtra("control", "pause");
            mContext.sendBroadcast(intent2);
            return;
        } else if (CmdStr.contains(SPRC_DIRK_NEXT)) {
            Intent intent2 = new Intent("com.sharp.intent.action.voicecontrol.media");
            intent2.putExtra("control", "next");
            mContext.sendBroadcast(intent2);
            return;
        } else if (CmdStr.contains(SPRC_DIRK_PREVIOUS)) {
            Intent intent2 = new Intent("com.sharp.intent.action.voicecontrol.media");
            intent2.putExtra("control", "prev");
            mContext.sendBroadcast(intent2);
            return;
        }
        // ===========MEDIA key Broadcast ============
        /*else if(CmdStr.contains(SPRC_DIRK_PLAY)){
            KeyCode = KeyEvent.KEYCODE_MEDIA_PLAY;
        }else if(CmdStr.contains(SPRC_DIRK_STOP)){
            KeyCode = KeyEvent.KEYCODE_MEDIA_STOP;
        }else if(CmdStr.contains(SPRC_DIRK_FAST_RETURN)){
            KeyCode = KeyEvent.KEYCODE_MEDIA_REWIND;
        }else if(CmdStr.contains(SPRC_DIRK_FAST_FORWARD)){
            KeyCode = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
        }else if(CmdStr.contains(SPRC_DIRK_PAUSE)){
            KeyCode = KeyEvent.KEYCODE_MEDIA_PAUSE;
        }else if(CmdStr.contains(SPRC_DIRK_NEXT)){
            KeyCode = KeyEvent.KEYCODE_MEDIA_NEXT;
        }else if(CmdStr.contains(SPRC_DIRK_PREVIOUS)){
            KeyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
        }*/
        else if (CmdStr.contains(SPRC_DISC_MUTE)) {
            setMute();
            return;
        } else if (CmdStr.contains(SPRC_DISC_UNMUTE)) {
            setUnMute();
            return;
        } else {
            //      Debug.d(TAG, "Does not need send key event to AN so return " );
            return;
        }

        if (KeyCode == 0) {
            Debug.d(TAG, "if KeyCode is 0 skip send key event");
            return;
        } else {
            try {
                Instrumentation inst = new Instrumentation();
                /*
                Debug.d(TAG, "repeat prekeyCode =" + prekeyCode);
                //Debug.d(TAG, "repeat uptimeMillis =" + (SystemClock.uptimeMillis()-curTime));
                Debug.d(TAG, "repeat Repeat_count =" + Repeat_count);
                if (prekeyCode != KeyCode) {
                    Repeat_count = 0;
                    IsRepeat = false;
                }
                if (prekeyCode == KeyCode && Repeat_count >= repeat_thread_hold ) {
                    // if (prekeyCode == KeyCode && SystemClock.uptimeMillis() - curTime < 1000) {
                        long downTime = SystemClock.uptimeMillis();
                        long eventTime = SystemClock.uptimeMillis();
                        KeyEvent event1 = new KeyEvent(curTime, curTime, KeyEvent.ACTION_DOWN, KeyCode, 0);
                        inst.sendKeySync(event1);
//                    inst.sendKeySync(new KeyEvent(curTime, curTime, KeyEvent.ACTION_DOWN, KeyCode, 0));
                    Debug.d(TAG, "repeat KeyCode =" + KeyCode +" IsRepeat="+IsRepeat);
                } else
                    */
                {
                    //curTime = SystemClock.uptimeMillis();
                    Debug.d(TAG, "KeyCode =" + KeyCode);
                    // inst.sendKeySync(new KeyEvent(curTime, curTime, KeyEvent.ACTION_DOWN, KeyCode, 0));
                    // inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, key));
                    inst.sendKeyDownUpSync(KeyCode);

                }
                prekeyCode = KeyCode;
            } catch (Exception e) {
                Debug.e(TAG, "Error" + e.toString());
            }
        }

    }

    public static void simulateKeyByCommand(final int KeyCode) {
        try {
            String keyCommand = "input keyevent " + KeyCode;
            Runtime runtime = Runtime.getRuntime();
            Debug.i(TAG, "@@@simulateKeyByCommand keyCode=" + KeyCode);
            Process proc = runtime.exec(keyCommand);
        } catch (IOException e) {
            Debug.e(TAG, "Error" + e.toString());
        }
    }


    private boolean IsSohuInputOnTop() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> task = am.getRunningTasks(1);
        ComponentName componentInfo = task.get(0).topActivity;
        Debug.i(TAG, "IsSohuInputOnTop.componentInfo.getClassName=" + componentInfo.getClassName());
        // if (componentInfo.getClassName().equals("com.sohu.inputmethod.sogou.tv") == true) {
        if (componentInfo.getClassName().equals("com.sharp.fxc.search.SearchCMSActivity") == true) {
            Debug.i(TAG, "Is sohu input method On Top");
            return true;
        }
        Debug.i(TAG, "It is Not sohu input method On Top");
        return false;
    }

    class keyThread implements Runnable {
        @Override
        public void run() {
            long delay_time_ms = 200;
            KeyThreadExit = false;
            Debug.d(TAG, "KeyThreadExit  :" + KeyThreadExit);

            while (tcpSocket.isConnected() && KeyThreadExit == false) {
                try {
                    Instrumentation inst = new Instrumentation();
                    key = KeyCode;
                    KeyCode = 0;
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    boolean isOpen = imm.isActive();//isOpen若返回true，则表示输入法打开
                    // Debug.d(TAG, "isOpen  :" + isOpen);
                    if (isOpen) {
                        Debug.d(TAG, "is InputMethodManager Open  :return keyThread");
                        return;
                    }

                    if (prekey == key && key != 0) {
                        // Debug.d(TAG, "repeat: prekey == key  KeyCode="+key+"  prekey="+prekey);
                        //if(SystemClock.uptimeMillis() - curTime < 200) {
                        if (Repeat_count == repeat_thread_hold) {
                         //   inst.sendKeySync(new KeyEvent(curTime, curTime, KeyEvent.ACTION_DOWN, key, 0));
                            // inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, key));
                            Debug.d(TAG, "keyThread repeat ture KeyCode =" + key + "  prekey=" + prekey);
                           // IsRepeat = true;
                           // Repeat_count = 0;
                        } else {
                           // Repeat_count++;
                            //  inst.sendKeyDownUpSync(key);
                        }
                    } else if (key == 0) {
                        if (prekey != 0) {
                            if (IsRepeat) {
                                Debug.d(TAG, "repeat key release ACTION_UP KeyCode=" + key + "  prekey=" + prekey);
                                // inst.sendKeySync(new KeyEvent(curTime, curTime, KeyEvent.ACTION_UP, prekey, 0));
                                inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, prekey));
                                IsRepeat = false;
                            }
                        }
                    } else {
                        if (key != 0 && prekey != 0 && prekey != key) {
                            //    curTime = SystemClock.uptimeMillis();
                            Debug.d(TAG, " If repeat done and  prekey != key  then ACTION_UP prekey  : KeyCode =" + key + "  prekey=" + prekey);
                            inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, prekey));
                            //           inst.sendKeyDownUpSync(key);
                        }
                        IsRepeat = false;
                        if (prekey != key) {
                            prekey = key;
                            Debug.d(TAG, "repeat: false ACTION_UP  KeyCode=" + key + "  prekey=" + prekey);
                            //inst.sendKeySync(new KeyEvent(curTime, curTime, KeyEvent.ACTION_UP, key, 0));
                            inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, key));
                        }
                    }
                } catch (Exception e) {
                    Debug.e(TAG, "Error" + e.toString());
                    break;
                }

                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            boolean goClose = true;
            tcpSocket.setSoTimeout(CONNECT_TIMEOUT);
            while (tcpSocket != null && tcpSocket.isConnected() && goClose) {
                DataInputStream in = new DataInputStream(tcpSocket.getInputStream());
                clientin = in;
                byte[] buffer = new byte[10000];
                boolean goOut = true;
                StringBuffer buf = new StringBuffer();
                while (goOut) {
                    int x = clientin.read(buffer);
                    //  Debug.d(TAG, "x=in.read()=" + String.valueOf(x));
                    if (x != -1) {
                        byte b = (byte) x;
                        //buf.append((char) b);
                        if (in.available() <= 0) {
                            goOut = false;
                        }
                    } else {
                        Debug.d(TAG, "TCP Socket Go Out=");
                        goOut = false;
                    }
                }
                reciveString = new String(buffer, "UTF8").trim();
                //    reciveString = String.valueOf(buf);
                Debug.d(TAG, "TV Recive Client String  :" + reciveString + " From :(" + tcpSocket.getRemoteSocketAddress() + ")");
                if (reciveString.equals("")) {
                    goClose = false;
                    Debug.d(TAG, "TCP Socket Go Close=");
                }
                // send msg to client
                //  if (!reciveString.equals(CommandTranf.SPRC_DELI)) {
                MapCmdToKeyEvent(reciveString.trim(), clientin);
                // }
                serverSendByTcp(reciveString.trim(), tcpSocket);
            }
        } catch (Exception e) {
            Debug.i(TAG, "Tcp socket receive error====>" + e.toString());
        } finally {
            if (clientin != null) {
                try {
                    Debug.i(TAG, "The tcpSocket=" + tcpSocket);
                    KeyThreadExit = true;
                    clientin.close();
                    tcpSocket.close();
                    Debug.i(TAG, "The Socket close!");
                    this.interrupt();
                    Debug.i(TAG, "The Socket thread interrupt!");
                    mClientList.remove(tcpSocket);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                clientin = null;
            }
        }
    }
}