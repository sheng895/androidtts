package com.gykj.paddle.lite.demo.tts;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.support.v7.app.ActionBar;

import com.gykj.voicetts.Utils;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    ListPreference lpChoosePreInstalledModel = null;
    CheckBoxPreference cbEnableCustomSettings = null;
    EditTextPreference etModelPath = null;
    ListPreference lpCPUThreadNum = null;
    ListPreference lpCPUPowerMode = null;

    ListPreference lpSPEAKIds = null;

    List<String> preInstalledModelPaths = null;
    List<String> preInstalledCPUThreadNums = null;
    List<String> preInstalledCPUPowerModes = null;

    List<String> preInstalledSpeackIds = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Initialized pre-installed models
        preInstalledModelPaths = new ArrayList<String>();
        preInstalledCPUThreadNums = new ArrayList<String>();
        preInstalledCPUPowerModes = new ArrayList<String>();
        preInstalledSpeackIds = new ArrayList<String>();
        preInstalledModelPaths.add(getString(R.string.MODEL_PATH_DEFAULT));
        preInstalledCPUThreadNums.add(getString(R.string.CPU_THREAD_NUM_DEFAULT));
        preInstalledCPUPowerModes.add(getString(R.string.CPU_POWER_MODE_DEFAULT));
        preInstalledSpeackIds.add(getString(R.string.SPEACK_ID_NUM_KEY));


        // Setup UI components
        lpChoosePreInstalledModel = (ListPreference) findPreference(getString(R.string.CHOOSE_PRE_INSTALLED_MODEL_KEY));
        String[] preInstalledModelNames = new String[preInstalledModelPaths.size()];
        for (int i = 0; i < preInstalledModelPaths.size(); i++) {
            preInstalledModelNames[i] = preInstalledModelPaths.get(i).substring(preInstalledModelPaths.get(i).lastIndexOf("/") + 1);
        }
        lpChoosePreInstalledModel.setEntries(preInstalledModelNames);
        lpChoosePreInstalledModel.setEntryValues(preInstalledModelPaths.toArray(new String[preInstalledModelPaths.size()]));
        lpCPUThreadNum = (ListPreference) findPreference(getString(R.string.CPU_THREAD_NUM_KEY));
        lpCPUPowerMode = (ListPreference) findPreference(getString(R.string.CPU_POWER_MODE_KEY));
        lpSPEAKIds = (ListPreference) findPreference(getString(R.string.SPEACK_ID_NUM_KEY));
        cbEnableCustomSettings = (CheckBoxPreference) findPreference(getString(R.string.ENABLE_CUSTOM_SETTINGS_KEY));
        etModelPath = (EditTextPreference) findPreference(getString(R.string.MODEL_PATH_KEY));
        etModelPath.setTitle("Model Path (SDCard: " + Utils.getSDCardDirectory() + ")");
    }

    private void reloadPreferenceAndUpdateUI() {
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        boolean enableCustomSettings = sharedPreferences.getBoolean(getString(R.string.ENABLE_CUSTOM_SETTINGS_KEY), false);
        String modelPath = sharedPreferences.getString(getString(R.string.CHOOSE_PRE_INSTALLED_MODEL_KEY), getString(R.string.MODEL_PATH_DEFAULT));
        int modelIdx = lpChoosePreInstalledModel.findIndexOfValue(modelPath);
        if (modelIdx >= 0 && modelIdx < preInstalledModelPaths.size()) {
            if (!enableCustomSettings) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.MODEL_PATH_KEY), preInstalledModelPaths.get(modelIdx));
                editor.putString(getString(R.string.CPU_THREAD_NUM_KEY), preInstalledCPUThreadNums.get(modelIdx));
                editor.putString(getString(R.string.CPU_POWER_MODE_KEY), preInstalledCPUPowerModes.get(modelIdx));
                editor.putString(getString(R.string.SPEACK_ID_NUM_KEY), preInstalledSpeackIds.get(modelIdx));
                editor.commit();
            }
            lpChoosePreInstalledModel.setSummary(modelPath);
        }
        cbEnableCustomSettings.setChecked(enableCustomSettings);
        etModelPath.setEnabled(enableCustomSettings);
        lpCPUThreadNum.setEnabled(enableCustomSettings);
        lpCPUPowerMode.setEnabled(enableCustomSettings);
        lpSPEAKIds.setEnabled(enableCustomSettings);
        modelPath = sharedPreferences.getString(getString(R.string.MODEL_PATH_KEY), getString(R.string.MODEL_PATH_DEFAULT));
        String cpuThreadNum = sharedPreferences.getString(getString(R.string.CPU_THREAD_NUM_KEY), getString(R.string.CPU_THREAD_NUM_DEFAULT));
        String cpuPowerMode = sharedPreferences.getString(getString(R.string.CPU_POWER_MODE_KEY), getString(R.string.CPU_POWER_MODE_DEFAULT));
        String speakId = sharedPreferences.getString(getString(R.string.SPEACK_ID_NUM_KEY), getString(R.string.CPU_POWER_MODE_DEFAULT));

        etModelPath.setSummary(modelPath);
        etModelPath.setText(modelPath);
        lpCPUThreadNum.setValue(cpuThreadNum);
        lpCPUThreadNum.setSummary(cpuThreadNum);
        lpCPUPowerMode.setValue(cpuPowerMode);
        lpCPUPowerMode.setSummary(cpuPowerMode);
        lpSPEAKIds.setValue(speakId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        reloadPreferenceAndUpdateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.CHOOSE_PRE_INSTALLED_MODEL_KEY))) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.ENABLE_CUSTOM_SETTINGS_KEY), false);
            editor.commit();
        }
        reloadPreferenceAndUpdateUI();
    }
}
