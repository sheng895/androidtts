package com.gykj.voicetts;

import android.content.Context;
import android.util.Log;

import com.baidu.paddle.lite.MobileConfig;
import com.baidu.paddle.lite.PaddlePredictor;
import com.baidu.paddle.lite.PowerMode;
import com.baidu.paddle.lite.Tensor;

import java.io.File;


public class Predictor {
    private static final String TAG = Predictor.class.getSimpleName();
    public boolean isLoaded = false;
    public int cpuThreadNum = 1;

    public int speakId = 174;
    public String cpuPowerMode = "LITE_POWER_HIGH";
    public String modelPath = "";
    protected PaddlePredictor AMPredictor = null;
    protected PaddlePredictor VOCPredictor = null;
    protected float inferenceTime = 0;

    protected float[] singlewav;
    protected float maxwav=(float) 0.01;
    Object obj=new Object();
    int sleeptime=0;

    public boolean init(Context appCtx, String modelPath, String AMmodelName, String VOCmodelName, int cpuThreadNum, String cpuPowerMode,int speakId) {
        // Release model if exists
        releaseModel();
        this.speakId=speakId;
        AMPredictor = loadModel(appCtx, modelPath, AMmodelName, cpuThreadNum, cpuPowerMode);
        if (AMPredictor == null) {
            return false;
        }
        VOCPredictor = loadModel(appCtx, modelPath, VOCmodelName, cpuThreadNum, cpuPowerMode);
        if (VOCPredictor == null) {
            return false;
        }
        isLoaded = true;
        return true;
    }

    protected PaddlePredictor loadModel(Context appCtx, String modelPath, String modelName, int cpuThreadNum, String cpuPowerMode) {
        // Load model
        if (modelPath.isEmpty()) {
            return null;
        }
        String realPath = modelPath;
        if (modelPath.charAt(0) != '/') {
            // Read model files from custom path if the first character of mode path is '/'
            // otherwise copy model to cache from assets
            realPath = appCtx.getCacheDir() + "/" + modelPath;
            // push model to mobile
            Utils.copyDirectoryFromAssets(appCtx, modelPath, realPath);
        }
        if (realPath.isEmpty()) {
            return null;
        }
        MobileConfig config = new MobileConfig();
        config.setModelFromFile(realPath + File.separator + modelName);
        Log.e(TAG, "File:" + realPath + File.separator + modelName);
        config.setThreads(cpuThreadNum);
        if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_HIGH")) {
            config.setPowerMode(PowerMode.LITE_POWER_HIGH);
        } else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_LOW")) {
            config.setPowerMode(PowerMode.LITE_POWER_LOW);
        } else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_FULL")) {
            config.setPowerMode(PowerMode.LITE_POWER_FULL);
        } else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_NO_BIND")) {
            config.setPowerMode(PowerMode.LITE_POWER_NO_BIND);
        } else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_RAND_HIGH")) {
            config.setPowerMode(PowerMode.LITE_POWER_RAND_HIGH);
        } else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_RAND_LOW")) {
            config.setPowerMode(PowerMode.LITE_POWER_RAND_LOW);
        } else {
            Log.e(TAG, "Unknown cpu power mode!");
            return null;
        }
        return PaddlePredictor.createPaddlePredictor(config);
    }

    public void releaseModel() {
        AMPredictor = null;
        VOCPredictor = null;
        isLoaded = false;
        cpuThreadNum = 1;
        cpuPowerMode = "LITE_POWER_HIGH";
        modelPath = "";
        speakId=174;
    }

    public boolean runSegmentModel(int[] phones) {
        if (!isLoaded()) {
            return false;
        }
        int[] speakid=new int[1];
        speakid[0]=speakId;
        sleeptime=0;
        Tensor am_output_handle = getAMOutput(phones, speakid, AMPredictor);
        singlewav = getVOCOutput(am_output_handle, VOCPredictor);
        float value=0;
        for (int i=0;i<singlewav.length;i++)
        {
            value=singlewav[i];
            if (value< 0) {
                value = -1 * value;
            }
            if (value > maxwav) {
                maxwav = value;
            }
        }
        return true;
    }

//    public boolean runModel(int[][] codephones) {
//        if (!isLoaded()) {
//            return false;
//        }
//        Date start = new Date();
//        int[] speakid=new int[1];
//        speakid[0]=174;
//        int count=0;
//        List<float[]> list=new ArrayList<>();
//        sleeptime=0;
//        for (int[] phones : codephones)
//        {
//            Tensor am_output_handle = getAMOutput(phones, speakid, AMPredictor);
//            float[] segementwav = getVOCOutput(am_output_handle, VOCPredictor);
//            list.add(segementwav);
//            count+=segementwav.length;
//        }
//        wav=new ArrayList<>();
//        int index=0;
//
//        for(float[] ft : list){
//            float value=0;
//            for (int i=0;i<ft.length;i++)
//            {
//                value=ft[i];
//                if (value< 0) {
//                    value = -1 * value;
//                }
//                if (value > maxwav) {
//                    maxwav = value;
//                }
//            }
//            wav.add(ft);
//        }
//        Date end = new Date();
//        inferenceTime = (end.getTime() - start.getTime());
//        return true;
//    }

    public Tensor getAMOutput(int[] phones,int[] speakid, PaddlePredictor am_predictor) {
        synchronized (obj) {
            Tensor phones_handle = am_predictor.getInput(0);
            long[] dims = {phones.length};
            phones_handle.resize(dims);
            phones_handle.setData(phones);
            if (speakid.length > 0) {
                Tensor speakid_handle = am_predictor.getInput(1);
                long[] speakdims = {speakid.length};
                speakid_handle.resize(speakdims);
                speakid_handle.setData(speakid);
            }
            am_predictor.run();
            Tensor am_output_handle = am_predictor.getOutput(0);
            // [?, 80]
            // long outputShape[] = am_output_handle.shape();
            float[] am_output_data = am_output_handle.getFloatData();
            // [? x 80]
            // long[] am_output_data_shape = {am_output_data.length};
            // Log.e(TAG, Arrays.toString(am_output_data));
            // 打印 mel 数组
            // for (int i=0;i<outputShape[0];i++) {
            //      Log.e(TAG, Arrays.toString(Arrays.copyOfRange(am_output_data,i*80,(i+1)*80)));
            // }
            // voc_predictor 需要知道输入的 shape，所以不能输出转成 float 之后的一维数组
            return am_output_handle;
        }
    }

    public float[] getVOCOutput(Tensor input, PaddlePredictor voc_predictor) {
        Tensor mel_handle = voc_predictor.getInput(0);
        // [?, 80]
        long[] dims = input.shape();
        mel_handle.resize(dims);
        float[] am_output_data = input.getFloatData();
        mel_handle.setData(am_output_data);
        voc_predictor.run();
        Tensor voc_output_handle = voc_predictor.getOutput(0);
        // [? x 300, 1]
        // long[] outputShape = voc_output_handle.shape();
        float[] voc_output_data = voc_output_handle.getFloatData();
        // long[] voc_output_data_shape = {voc_output_data.length};
        return voc_output_data;
    }


    public boolean isLoaded() {
        return AMPredictor != null && VOCPredictor != null && isLoaded;
    }


    public float inferenceTime() {
        return inferenceTime;
    }

}
