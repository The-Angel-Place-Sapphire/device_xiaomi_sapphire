/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.chargecontrol;

import android.os.Bundle;

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;
import com.android.settingslib.collapsingtoolbar.R;

public class ChargeControlActivity extends CollapsingToolbarBaseActivity {

    private static final String TAG_CHARGE_CONTROL = "chargecontrol";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                new ChargeControlFragment(), TAG_CHARGE_CONTROL).commit();
    }
}
