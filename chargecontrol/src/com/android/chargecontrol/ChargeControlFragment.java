/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.chargecontrol;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;

public class ChargeControlFragment extends SettingsBasePreferenceFragment implements
        OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Same device protected store the app reads before unlock
        getPreferenceManager().setStorageDeviceProtected();
        addPreferencesFromResource(R.xml.charge_control_settings);
        ListPreference mode = findPreference(ChargeUtils.KEY_CHARGE_MODE);
        mode.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (ChargeUtils.KEY_CHARGE_MODE.equals(preference.getKey())) {
            String mode = (String) newValue;
            AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> ChargeUtils.applyMode(mode));
        }
        return true;
    }
}
