package com.foxconn.fxcsprcservice;

/**
 * Created by alvin on 2018/4/19.
 */

public class CommandTranf {
    public static final String TEST = "TEST";
    public static final String SUFFIX = "#";
    public static final String TCP_SYNC_OK = "SYNC_OK";
    public static final String TCP_ACCEPT = "ACCEPT";
    public static final String TCP_REJECT = "REJECT";

    //UDP
    //Discovery TV deviceconfiguration(Transmission base on IP/UDP)
    public static final String DEBG_TEST = "DEBG#TEST#15#1#1#N/A#";
    public static final String DEBG_TEST_RETURN = "DEBG#TEST#9#1#1#";

    public static final String SPRC_DISC_ALL = "SPRC#DISC#16#1#1#N/A#";
    public static final String SPRC_DISC_ALL_RETURN = "SPRC#DISC#16#1#1#";

    public static final String SPRC_DISC_IP = "SPRC#DISC#16#2#1#N/A#";
    public static final String SPRC_DISC_IP_RETURN  = "SPRC#DISC#16#2#1#";

    public static final String SPRC_DISC_PROTOCOL = "SPRC#DISC#16#3#1#N/A#";
    public static final String SPRC_DISC_PROTOCOL_RETURN = "SPRC#DISC#16#3#1#";

    public static final String SPRC_DISC_PORT = "SPRC#DISC#16#4#1#N/A#";
    public static final String SPRC_DISC_PORT_RETURN = "SPRC#DISC#16#4#1#";

    public static final String SPRC_DISC_MODEL_NAME = "SPRC#DISC#16#5#1#N/A#";
    public static final String SPRC_DISC_MODEL_NAME_RETURN = "SPRC#DISC#16#5#1#";

    public static final String SPRC_DISC_FW_VERSION = "SPRC#DISC#16#6#1#N/A#";
    public static final String SPRC_DISC_FW_VERSION_RETURN  = "SPRC#DISC#16#6#1#";

    public static final String SPRC_DISC_SN = "SPRC#DISC#16#7#1#N/A#";
    public static final String SPRC_DISC_SN_RETURN  = "SPRC#DISC#16#7#1#";

    public static final String SPRC_DISC_BT = "SPRC#DISC#16#8#1#N/A#";
    public static final String SPRC_DISC_BT_RETURN = "SPRC#DISC#16#8#1#";

    public static final String SPRC_DISC_TVCMD_VERSION  = "SPRC#DISC#16#9#1#N/A#";
    public static final String SPRC_DISC_TVCMD_VERSION_RETURN = "SPRC#DISC#16#9#1#";


    // TCP
    // Group ID=17 ,Leader word:DELI Delivery special message.(Transmission base on IP/TCP)
    public static final String SPRC_DELI = "SPRC#DELI#17#1#1#N/A#";
    public static final String SPRC_DELI_SYNC_OK = "SPRC#DELI#17#1#1#";
    public static final String SPRC_DELI_ACCEPT = "SPRC#DELI#17#2#1#";
    public static final String SPRC_DELI_REJECT = "SPRC#DELI#17#3#1#";

    // Group ID=18 , Leader word:DISC ,Discovery TV key function  support status
    public static final String SPRC_DISC_MUTE_STATE = "SPRC#DISC#18#10#1#N/A#";
    public static final String SPRC_DISC_MUTE_STATE_RETURN ="SPRC#DISC#18#10#1#";

    public static final String SPRC_DISC_GET_VOLUME= "SPRC#DISC#18#11#1#N/A#";
    public static final String SPRC_DISC_GET_VOLUME_RETURN = "SPRC#DISC#18#11#1#";

    public static final String SPRC_DISC_GET_CURRENT_PAGE_NAME = "SPRC#DISC#18#12#1#N/A#";
    public static final String SPRC_DISC_GET_CURRENT_PAGE__NAME_RETURN = "SPRC#DISC#18#12#1#";

    public static final String SPRC_DISC_GET_TV_SDK = "SPRC#DISC#18#13#1#N/A#";
    public static final String SPRC_DISC_GET_TV_SDK_RETURN = "SPRC#DISC#18#13#1#";

//    public static final String SPRC_DISC_GET_VOICE_SDK = "SPRC#DISC#18#14#1#N/A#";
//    public static final String SPRC_DISC_GET_VOICE_SDK_RETURN = "SPRC#DISC#18#14#1#";

    // Group ID=19 , Leader word:DIRK ,SHARP IR key code(Transmission base on IP/TCP)
    // Sys =1
    public static final String SPRC_DIRK_NUM_1 = "SPRC#DIRK#19#1#2#1|1#";
    public static final String SPRC_DIRK_NUM_2 = "SPRC#DIRK#19#1#2#1|2#";
    public static final String SPRC_DIRK_NUM_3 = "SPRC#DIRK#19#1#2#1|3#";
    public static final String SPRC_DIRK_NUM_4 = "SPRC#DIRK#19#1#2#1|4#";
    public static final String SPRC_DIRK_NUM_5 = "SPRC#DIRK#19#1#2#1|5#";
    public static final String SPRC_DIRK_NUM_6 = "SPRC#DIRK#19#1#2#1|6#";
    public static final String SPRC_DIRK_NUM_7 = "SPRC#DIRK#19#1#2#1|7#";
    public static final String SPRC_DIRK_NUM_8 = "SPRC#DIRK#19#1#2#1|8#";
    public static final String SPRC_DIRK_NUM_9 = "SPRC#DIRK#19#1#2#1|9#";
    public static final String SPRC_DIRK_NUM_0 = "SPRC#DIRK#19#1#2#1|10#";
    public static final String SPRC_DIRK_CH_UP = "SPRC#DIRK#19#1#2#1|17#";
    public static final String SPRC_DIRK_CH_DOWN = "SPRC#DIRK#19#1#2#1|18#";
    public static final String SPRC_DIRK_INPUT = "SPRC#DIRK#19#1#2#1|19#";
    public static final String SPRC_DIRK_VOLUME_UP = "SPRC#DIRK#19#1#2#1|20#";
    public static final String SPRC_DIRK_VOLUME_DOWN = "SPRC#DIRK#19#1#2#1|21#";
    public static final String SPRC_DIRK_POWER = "SPRC#DIRK#19#1#2#1|22#";
    public static final String SPRC_DIRK_VOLUME_MUTE = "SPRC#DIRK#19#1#2#1|23#";
    public static final String SPRC_DIRK_DISPLAY = "SPRC#DIRK#19#1#2#1|27#";
    public static final String SPRC_DIRK_MENU = "SPRC#DIRK#19#1#2#1|32#";
    public static final String SPRC_DIRK_ENTER = "SPRC#DIRK#19#1#2#1|36#";
    public static final String SPRC_DIRK_UP = "SPRC#DIRK#19#1#2#1|87#";
    public static final String SPRC_DIRK_DOWN = "SPRC#DIRK#19#1#2#1|88#";
    public static final String SPRC_DIRK_HOME = "SPRC#DIRK#19#1#2#1|241#";
    public static final String SPRC_DIRK_RETURN = "SPRC#DIRK#19#1#2#1|243#";
    public static final String SPRC_DIRK_LEFT = "SPRC#DIRK#19#1#2#1|245#";
    public static final String SPRC_DIRK_RIGHT = "SPRC#DIRK#19#1#2#1|246#";
    public static final String SPRC_DIRK_PAGE_UP = "SPRC#DIRK#19#1#2#1|250#";
    public static final String SPRC_DIRK_PAGE_DOWN = "SPRC#DIRK#19#1#2#1|251#";
    public static final String SPRC_DISC_MUTE=  "SPRC#DIRK#19#1#2#1|223#";
    public static final String SPRC_DISC_UNMUTE=  "SPRC#DIRK#19#1#2#1|222#";

    //for MiC voice
    public static final String SPRC_DISC_SHORTCUT=  "SPRC#DIRK#19#1#2#1|221#";
    public static final String SPRC_DISC_OPEN_MIC=  "SPRC#DIRK#19#1#2#1|220#";
    public static final String SPRC_DISC_CLOSE_MIC=  "SPRC#DIRK#19#1#2#1|219#";
    public static final String SPRC_DISC_MIC_VOICE=  "SPRC#DIRK#19#1#3#1|218|";

    //for voice
    public static final String SPRC_DIRK_SEARCH= "SPRC#DIRK#19#1#3#1|249|";
    public static final String SPRC_DIRK_UUID = "SPRC#DIRK#19#1#4#1|252|";
    public static final String SPRC_DIRK_VOICE = "SPRC#DIRK#19#1#3#1|253|";
    public static final String SPRC_DIRK_VOICE_JSON = "SPRC#DIRK#19#1#3#1|254|";
    //Sys =17 for MMP
    public static final String SPRC_DIRK_PLAY= "SPRC#DIRK#19#1#2#17|193#";
    public static final String SPRC_DIRK_STOP= "SPRC#DIRK#19#1#2#17|195#";
    public static final String SPRC_DIRK_FAST_RETURN="SPRC#DIRK#19#1#2#17|196#";
    public static final String SPRC_DIRK_FAST_FORWARD="SPRC#DIRK#19#1#2#17|197#";
    public static final String SPRC_DIRK_PAUSE="SPRC#DIRK#19#1#2#17|199#";
    public static final String SPRC_DIRK_NEXT="SPRC#DIRK#19#1#2#17|203#";
    public static final String SPRC_DIRK_PREVIOUS= "SPRC#DIRK#19#1#2#17|204#";

}
