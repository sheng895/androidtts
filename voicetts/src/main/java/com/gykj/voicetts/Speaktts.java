package com.gykj.voicetts;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.IOException;
import java.util.Date;

public class Speaktts {

    private static String content;
    private static AudioTrack audioTrack;
    private static byte[] audioData;
    protected static Predictor predictor = new Predictor();

    public static String message;
    public static Thread thread;

    public static boolean init(Context appCtx,String modelPath, String AMmodelName, String VOCmodelName, int cpuThreadNum,
                               String cpuPowerMode, int speakId){
        return predictor.init(appCtx, modelPath, AMmodelName, VOCmodelName, cpuThreadNum,
                cpuPowerMode,speakId);
    }


    public static void SpeakText(String text, int sampleRate, int speakId) {
        content = text;
        predictor.speakId = speakId;
        thread= new Thread(new Runnable() {
            @Override
            public void run() {
                content = content.replace("\n", "").replace("\r", "");
                String charSplit = "[：；。？！,;?!]《》（）()、#";
                for (int i = 0; i < charSplit.length(); i++) {
                    content = content.replace(charSplit.charAt(i), '，');
                }
                String[] segmentText = content.split("，");
                predictor.isLoaded();
                int minBufferSize = AudioTrack.getMinBufferSize(sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT);
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                        AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                        minBufferSize, AudioTrack.MODE_STREAM);
//                try {
//                    audioData = Utils.rawToByte(40000, sampleRate).toByteArray();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                audioTrack.write(audioData, 0, audioData.length);
                audioTrack.play();
                float inferenceTime = 0;
                long totalLength = 0;
                for (String str : segmentText) {
                    str = str.trim();
                    if (str == null || str.length() == 0)
                        continue;
                    String codes = CalcMac.getPhoneIds(str);
                    String[] codevioce = codes.split(",");
                    int[] ft = new int[codevioce.length + 1];
                    int index = 0;
                    for (String s : codevioce) {
                        if (s.equals(""))
                            ft[index] = 277;
                        else
                            ft[index] = Integer.valueOf(s);
                        index++;
                    }
                    ft[index] = 277;
                    Date start = new Date();
                    predictor.runSegmentModel(ft);
                    Date end = new Date();
                    inferenceTime += (end.getTime() - start.getTime());
                    totalLength += predictor.singlewav.length;
                    try {
                        audioData = Utils.segToByte(predictor.singlewav, predictor.maxwav).toByteArray();
                        audioTrack.write(audioData, 0, audioData.length);
                        audioTrack.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //计算RTF
                message = "Inference done！\nInference time: " + inferenceTime + " ms"
                        + "\nRTF: " + 1.00 * inferenceTime * sampleRate / (totalLength * 1000);
            }
        });
        thread.start();

    }

    public static void pauseAudioTrack() {
        if (audioTrack != null) {
            thread.interrupt();
            audioTrack.pause();
            audioTrack.flush();
        }
    }


    public static void StopAudioTrack() {
        if (audioTrack != null) {
            thread.interrupt();
            audioTrack.release();
        }
    }

    public static void onDestroy() {
        if (predictor != null) {
            predictor.releaseModel();
        }
    }
}
