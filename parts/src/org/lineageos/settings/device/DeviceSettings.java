/*
 * Copyright (C) 2018-2019 The Xiaomi-SDM660 Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.lineageos.settings.device;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import org.lineageos.settings.device.kcal.KCalSettingsActivity;
import org.lineageos.settings.device.preferences.SecureSettingListPreference;
import org.lineageos.settings.device.preferences.SecureSettingSwitchPreference;
import org.lineageos.settings.device.preferences.VibrationSeekBarPreference;
import org.lineageos.settings.device.preferences.NotificationLedSeekBarPreference;
import org.lineageos.settings.device.preferences.CustomSeekBarPreference;

import java.lang.Math.*;

public class DeviceSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final  String CATEGORY_AUDIO_AMPLIFY = "audio_amplify";
    public static final  String PREF_EARPIECE_GAIN = "earpiece_gain";
    public static final  String PREF_HEADPHONE_GAIN = "headphone_gain";
    public static final  String PREF_MIC_GAIN = "mic_gain";
    public static final  String EARPIECE_GAIN_PATH = "/sys/kernel/sound_control/earpiece_gain";
    public static final  String HEADPHONE_GAIN_PATH = "/sys/kernel/sound_control/headphone_gain";
    public static final  String MIC_GAIN_PATH = "/sys/kernel/sound_control/mic_gain";

    private static final String PREF_ENABLE_DIRAC = "dirac_enabled";
    private static final String PREF_HEADSET = "dirac_headset_pref";
    private static final String PREF_PRESET = "dirac_preset_pref";

    private static final String CATEGORY_DISPLAY = "display";
    private static final String PREF_DEVICE_KCAL = "device_kcal";

    private SecureSettingSwitchPreference mEnableDirac;
    private SecureSettingListPreference mHeadsetType;
    private SecureSettingListPreference mPreset;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_zenparts, rootKey);
        
        // Amplify Audio 
        PreferenceCategory gainCategory = (PreferenceCategory) findPreference(CATEGORY_AUDIO_AMPLIFY);
        // Earpiece Gain
        if (FileUtils.fileWritable(EARPIECE_GAIN_PATH)) {
           CustomSeekBarPreference earpieceGain = (CustomSeekBarPreference) findPreference(PREF_EARPIECE_GAIN);
           earpieceGain.setOnPreferenceChangeListener(this);
        } else {
          gainCategory.removePreference(findPreference(PREF_EARPIECE_GAIN));
        }
        // Headphone Gain
        if (FileUtils.fileWritable(HEADPHONE_GAIN_PATH)) {
           CustomSeekBarPreference headphoneGain = (CustomSeekBarPreference) findPreference(PREF_HEADPHONE_GAIN);
           headphoneGain.setOnPreferenceChangeListener(this);
        } else {
          gainCategory.removePreference(findPreference(PREF_HEADPHONE_GAIN));
        }
        // Mic Gain
        if (FileUtils.fileWritable(MIC_GAIN_PATH)) {
           CustomSeekBarPreference micGain = (CustomSeekBarPreference) findPreference(PREF_MIC_GAIN);
           micGain.setOnPreferenceChangeListener(this);
        } else {
          gainCategory.removePreference(findPreference(PREF_MIC_GAIN));
        }

        // Display Category
        PreferenceCategory displayCategory = (PreferenceCategory) findPreference(CATEGORY_DISPLAY);
        // KCAL
        Preference kcal = findPreference(PREF_DEVICE_KCAL);
        kcal.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), KCalSettingsActivity.class);
            startActivity(intent);
            return true;
        });

        // Dirac
        boolean enhancerEnabled;
        try {
            enhancerEnabled = DiracService.sDiracUtils.isDiracEnabled();
        } catch (java.lang.NullPointerException e) {
            getContext().startService(new Intent(getContext(), DiracService.class));
            try {
                enhancerEnabled = DiracService.sDiracUtils.isDiracEnabled();
            } catch (NullPointerException ne) {
                // Avoid crash
                ne.printStackTrace();
                enhancerEnabled = false;
            }
        }

        // Dirac Switch
        mEnableDirac = (SecureSettingSwitchPreference) findPreference(PREF_ENABLE_DIRAC);
        mEnableDirac.setOnPreferenceChangeListener(this);
        mEnableDirac.setChecked(enhancerEnabled);
        // Headset
        mHeadsetType = (SecureSettingListPreference) findPreference(PREF_HEADSET);
        mHeadsetType.setOnPreferenceChangeListener(this);
        // Preset
        mPreset = (SecureSettingListPreference) findPreference(PREF_PRESET);
        mPreset.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String key = preference.getKey();
        switch (key) {

            case PREF_EARPIECE_GAIN:
                FileUtils.setValue(EARPIECE_GAIN_PATH, (int) value);
                break;

            case PREF_HEADPHONE_GAIN:
                FileUtils.setValue(HEADPHONE_GAIN_PATH, value + " " + value);
                break;

            case PREF_MIC_GAIN:
                FileUtils.setValue(MIC_GAIN_PATH, (int) value);
                break;

            case PREF_ENABLE_DIRAC:
                try {
                    DiracService.sDiracUtils.setEnabled((boolean) value);
                } catch (java.lang.NullPointerException e) {
                    getContext().startService(new Intent(getContext(), DiracService.class));
                    DiracService.sDiracUtils.setEnabled((boolean) value);
                }
                break;

            case PREF_HEADSET:
                try {
                    DiracService.sDiracUtils.setHeadsetType(Integer.parseInt(value.toString()));
                } catch (java.lang.NullPointerException e) {
                    getContext().startService(new Intent(getContext(), DiracService.class));
                    DiracService.sDiracUtils.setHeadsetType(Integer.parseInt(value.toString()));
                }
                break;

            case PREF_PRESET:
                try {
                    DiracService.sDiracUtils.setLevel(String.valueOf(value));
                } catch (java.lang.NullPointerException e) {
                    getContext().startService(new Intent(getContext(), DiracService.class));
                    DiracService.sDiracUtils.setLevel(String.valueOf(value));
                }
                break;

            default:
                break;
        }
        return true;
    }

    private boolean isAppNotInstalled(String uri) {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }
}
