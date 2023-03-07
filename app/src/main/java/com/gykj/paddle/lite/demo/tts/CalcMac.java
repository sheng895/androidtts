package com.gykj.paddle.lite.demo.tts;


public class CalcMac {

    public static String TAG = CalcMac.class.getSimpleName();

    static {
        System.loadLibrary("fonttextclient");
    }

    public static synchronized void init(String Path){
        Native_Jni(Path);
    }

    public static synchronized String getPhoneIds(String text){
        return Native_JniCalcText(text);
    }

    private static native  void Native_Jni(String Path);
    private static native  String Native_JniCalcText(String text);
}

