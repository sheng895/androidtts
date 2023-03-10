package com.gykj.paddle.lite.demo.tts;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AdapterView.OnItemSelectedListener {
    public static final int REQUEST_LOAD_MODEL = 0;
    public static final int REQUEST_RUN_MODEL = 1;
    public static final int RESPONSE_LOAD_MODEL_SUCCESSED = 0;
    public static final int RESPONSE_LOAD_MODEL_FAILED = 1;
    public static final int RESPONSE_RUN_MODEL_SUCCESSED = 2;
    public static final int RESPONSE_RUN_MODEL_FAILED = 3;
    private static final String TAG = Predictor.class.getSimpleName();
    protected ProgressDialog pbLoadModel = null;
    protected ProgressDialog pbRunModel = null;
    // Receive messages from worker thread
    protected Handler receiver = null;
    // Send command to worker thread
    protected Handler sender = null;
    // Worker thread to load&run model
    protected HandlerThread worker = null;
    // UI components of image classification
    protected TextView tvInputSetting;
    protected TextView tvInferenceTime;
    protected Button btn_play;
    protected Button btn_pause;
    protected Button btn_stop;
    // Model settings of image classification
    protected String modelPath = "";
    protected int cpuThreadNum = 4;
    protected String cpuPowerMode = "";
    protected Predictor predictor = new Predictor();
    int sampleRate = 12000;
    private final String wavName = "tts_output.wav";
    private final String wavFile = Environment.getExternalStorageDirectory() + File.separator + wavName;
    private final String AMmodelName = "fastspeech2_mix_arm.nb";
    private final String VOCmodelName = "mb_melgan_csmsc_arm.nb";

    private Map<String, String> phonemap = new HashMap<>();

    private Map<String, String> pinyinmap = new HashMap<>();
    private int[][] phones = {};
    private String content;

    private AudioTrack audioTrack;
    private byte[] audioData;


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                ttsSpeak(content);
//                this.audioTrack.stop();
//                this.audioTrack.release();

                break;
            case R.id.btn_pause:
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.pause();
//                }
                this.onPause();
                break;
            case R.id.btn_stop:
//                if (mediaPlayer.isPlaying()) {
//                    mediaPlayer.reset();
//                    initMediaPlayer();
//                }
                this.audioTrack.stop();
                break;
            default:
                break;
        }
    }

    private  void ttsSpeak(String text)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String charSplit = "[：；。？！,;?!]";
                for (int i = 0; i < charSplit.length(); i++) {
                    content = content.replace(charSplit.charAt(i), '，');
                }
                String[] segmentText = content.split("，");
                predictor.isLoaded();
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                        AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                        40000, AudioTrack.MODE_STREAM);
                try {
                    audioData = Utils.rawToByte(40000, sampleRate).toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                audioTrack.write(audioData, 0, audioData.length);
                audioTrack.play();
                for (String str : segmentText) {
                    String codes = CalcMac.getPhoneIds(str);
                    String[] codevioce = codes.split(",");
                    int[] ft = new int[codevioce.length + 1];
                    int index = 0;
                    for (String s : codevioce) {
                        ft[index] = Integer.valueOf(s);
                        index++;
                    }
                    ft[index] = 277;
                    predictor.runSegmentModel(ft);
                    try {
                        audioData = Utils.segToByte(predictor.singlewav, predictor.maxwav).toByteArray();
                        audioTrack.write(audioData, 0, audioData.length);
                        audioTrack.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    @Override
    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // The MediaPlayer has moved to the Error state, must be reset!
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestAllPermissions();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化控件
        Spinner spinner = findViewById(R.id.spinner1);
        // 建立数据源
        String[] sentences = getResources().getStringArray(R.array.text);
        // 建立 Adapter 并且绑定数据源
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, sentences);
        // 第一个参数表示在哪个 Activity 上显示，第二个参数是系统下拉框的样式，第三个参数是数组。
        spinner.setAdapter(adapter);//绑定Adapter到控件
        spinner.setOnItemSelectedListener(this);

        btn_play = findViewById(R.id.btn_play);
        btn_pause = findViewById(R.id.btn_pause);
        btn_stop = findViewById(R.id.btn_stop);

        btn_play.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

        btn_play.setVisibility(View.INVISIBLE);
        btn_pause.setVisibility(View.INVISIBLE);
        btn_stop.setVisibility(View.INVISIBLE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

        // Prepare the worker thread for mode loading and inference
        receiver = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RESPONSE_LOAD_MODEL_SUCCESSED:
                        pbLoadModel.dismiss();
                        onLoadModelSuccessed();
                        break;
                    case RESPONSE_LOAD_MODEL_FAILED:
                        pbLoadModel.dismiss();
                        Toast.makeText(MainActivity.this, "Load model failed!", Toast.LENGTH_SHORT).show();
                        onLoadModelFailed();
                        break;
                    case RESPONSE_RUN_MODEL_SUCCESSED:
                        pbRunModel.dismiss();
                        onRunModelSuccessed();
                        break;
                    case RESPONSE_RUN_MODEL_FAILED:
                        pbRunModel.dismiss();
                        Toast.makeText(MainActivity.this, "Run model failed!", Toast.LENGTH_SHORT).show();
                        onRunModelFailed();
                        break;
                    default:
                        break;
                }
            }
        };

        worker = new HandlerThread("Predictor Worker");
        worker.start();
        sender = new Handler(worker.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REQUEST_LOAD_MODEL:
                        // Load model and reload test image
                        if (onLoadModel()) {
                            receiver.sendEmptyMessage(RESPONSE_LOAD_MODEL_SUCCESSED);
                        } else {
                            receiver.sendEmptyMessage(RESPONSE_LOAD_MODEL_FAILED);
                        }
                        break;
                    case REQUEST_RUN_MODEL:
                        // Run model if model is loaded
                        if (onRunModel()) {
                            receiver.sendEmptyMessage(RESPONSE_RUN_MODEL_SUCCESSED);
                        } else {
                            receiver.sendEmptyMessage(RESPONSE_RUN_MODEL_FAILED);
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        // Setup the UI components
        tvInputSetting = findViewById(R.id.tv_input_setting);
        tvInferenceTime = findViewById(R.id.tv_inference_time);
        tvInputSetting.setMovementMethod(ScrollingMovementMethod.getInstance());


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        String externalPath = this.getExternalFilesDir(null).getAbsolutePath();
        AssetCopyer.copyAllAssets(this.getApplicationContext(), externalPath);
//        File file = new File(externalPath);
//        if(!file.exists()) {
//            AssetCopyer.copyAllAssets(this.getApplicationContext(), externalPath);
//        }

        CalcMac.init(externalPath);

//        String dssss= CalcMac.getPhoneIds("这几天雨水不断，人们恨不得待在家里不出门。");

//        predictor.init(MainActivity.this, modelPath, AMmodelName, VOCmodelName, cpuThreadNum,
//                cpuPowerMode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean settingsChanged = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String model_path = sharedPreferences.getString(getString(R.string.MODEL_PATH_KEY),
                getString(R.string.MODEL_PATH_DEFAULT));

        settingsChanged |= !model_path.equalsIgnoreCase(modelPath);

        int cpu_thread_num = Integer.parseInt(sharedPreferences.getString(getString(R.string.CPU_THREAD_NUM_KEY),
                getString(R.string.CPU_THREAD_NUM_DEFAULT)));
        settingsChanged |= cpu_thread_num != cpuThreadNum;
        String cpu_power_mode =
                sharedPreferences.getString(getString(R.string.CPU_POWER_MODE_KEY),
                        getString(R.string.CPU_POWER_MODE_DEFAULT));
        settingsChanged |= !cpu_power_mode.equalsIgnoreCase(cpuPowerMode);

        if (settingsChanged) {
            modelPath = model_path;
            cpuThreadNum = cpu_thread_num;
            cpuPowerMode = cpu_power_mode;
            // Update UI
            tvInputSetting.setText("Model: " + modelPath.substring(modelPath.lastIndexOf("/") + 1) + "\n" + "CPU" +
                    " Thread Num: " + cpuThreadNum + "\n" + "CPU Power Mode: " + cpuPowerMode + "\n");
            tvInputSetting.scrollTo(0, 0);
            // Reload model if configure has been changed
            loadModel();
        }
    }

    public void loadModel() {
        pbLoadModel = ProgressDialog.show(this, "", "Loading model...", false, false);
        sender.sendEmptyMessage(REQUEST_LOAD_MODEL);
    }

    public void runModel() {
        pbRunModel = ProgressDialog.show(this, "", "Running model...", false, false);
        sender.sendEmptyMessage(REQUEST_RUN_MODEL);
    }

    public boolean onLoadModel() {
        return predictor.init(MainActivity.this, modelPath, AMmodelName, VOCmodelName, cpuThreadNum,
                cpuPowerMode);
    }

    public boolean onRunModel() {

//        return predictor.isLoaded() && predictor.runModel(phones);
        return true;
    }

    public boolean onLoadModelSuccessed() {
        // Load test image from path and run model
//        runModel();
        return true;
    }

    public void onLoadModelFailed() {
    }

    public void onRunModelSuccessed() {
        // Obtain results and update UI
        btn_play.setVisibility(View.VISIBLE);
        btn_pause.setVisibility(View.VISIBLE);
        btn_stop.setVisibility(View.VISIBLE);
//        tvInferenceTime.setText("Inference done！\nInference time: " + predictor.inferenceTime() + " ms"
//                + "\nRTF: " + predictor.inferenceTime() * sampleRate / (predictor.wav.size() * 1000) + "\nAudio saved in " + wavFile);
                tvInferenceTime.setText("Inference done！\nInference time: " + predictor.inferenceTime() + " ms"
                + "\nRTF: " + predictor.inferenceTime() * sampleRate / 1000 );
//        try {
////            Utils.rawToWave(wavFile, predictor.wav, sampleRate);
//              audioData= Utils.rawToByte(predictor.wav,predictor.maxwav,sampleRate).toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public void onRunModelFailed() {
    }


    public void onSettingsClicked() {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_action_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.settings:
                onSettingsClicked();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        if (predictor != null) {
            predictor.releaseModel();
        }
        worker.quit();
        super.onDestroy();
    }

    private boolean requestAllPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
            return false;
        }
        return true;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) {
////            phones = sentencesToChoose[position - 1];
            content = ((TextView) view).getText().toString();
            runModel();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void releaseAudioTrack() {
        if (this.audioTrack != null) {
            Log.d(TAG, "Stopping");
            audioTrack.stop();
            Log.d(TAG, "Releasing");
            audioTrack.release();
            Log.d(TAG, "Nulling");
        }
    }

    public void onPause() {
        super.onPause();
        this.releaseAudioTrack();
    }
}
