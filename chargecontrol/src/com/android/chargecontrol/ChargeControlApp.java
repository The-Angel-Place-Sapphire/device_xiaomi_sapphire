/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.chargecontrol;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.UserHandle;

public class ChargeControlApp extends Application {

    // Let the PD policy manager finish its own negotiation first
    private static final long CHECK_DELAY_MS = 5000;
    private static final long BOOT_CHECK_DELAY_MS = 15000;

    private Handler mHandler;

    private final Runnable mCheck = () -> {
        ChargeUtils.applyMode(ChargeUtils.getMode(this));
        ChargeUtils.fixStuckPd();
    };

    private final BroadcastReceiver mPowerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scheduleCheck(CHECK_DELAY_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        // Charger state is global, only the system user handles it
        if (UserHandle.myUserId() != UserHandle.USER_SYSTEM) return;

        HandlerThread thread = new HandlerThread("ChargeControl");
        thread.start();
        mHandler = new Handler(thread.getLooper());

        registerReceiver(mPowerReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED),
                null, mHandler);
        // Covers a charger already attached at boot
        scheduleCheck(BOOT_CHECK_DELAY_MS);
    }

    void scheduleCheck(long delayMs) {
        if (mHandler == null) return;
        mHandler.removeCallbacks(mCheck);
        mHandler.postDelayed(mCheck, delayMs);
    }
}
