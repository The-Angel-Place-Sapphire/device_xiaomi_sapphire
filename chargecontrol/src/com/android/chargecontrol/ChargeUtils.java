/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.chargecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public final class ChargeUtils {

    private static final String TAG = "ChargeControl";

    public static final String KEY_CHARGE_MODE = "charge_mode";
    public static final String MODE_SLOW = "slow";
    public static final String MODE_FAST = "fast";
    public static final String MODE_SUPER = "super";

    // Xiaomi restricted charging: the PD policy manager caps FCC at restrict_cur
    private static final String RESTRICT_CHG = "/sys/class/qcom-battery/restrict_chg";
    private static final String RESTRICT_CUR = "/sys/class/qcom-battery/restrict_cur";
    private static final String TYPEC_MODE = "/sys/class/qcom-battery/typec_mode";
    private static final String USB_ONLINE = "/sys/class/power_supply/usb/online";

    private static final String IIO_DIR = "/sys/bus/iio/devices";
    private static final String SMB5_IIO_NAME = "qpnp-smb5";
    private static final String PD_ACTIVE = "in_index_usb_pd_active_input";
    private static final String POWER_ROLE = "in_index_usb_typec_power_role_input";

    private static final int SLOW_CURRENT_UA = 1500000;
    private static final int FAST_CURRENT_UA = 3000000;

    // QTI_POWER_SUPPLY_TYPEC_PR_NONE (CC lines high-Z) and _DUAL
    private static final String ROLE_NONE = "0";
    private static final String ROLE_DUAL = "1";

    // The charger keeps its PD contract for ~2s after CC goes high-Z;
    // re-enabling CC earlier makes the PMIC flag a legacy cable again.
    private static final long CC_OFF_MS = 3000;
    private static final long PD_SETTLE_MS = 6000;
    private static final int PD_RETRIES = 3;

    private ChargeUtils() {}

    public static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(
                context.createDeviceProtectedStorageContext());
    }

    public static String getMode(Context context) {
        return getPrefs(context).getString(KEY_CHARGE_MODE, MODE_SUPER);
    }

    public static void applyMode(String mode) {
        switch (mode) {
            case MODE_SLOW:
                writeLine(RESTRICT_CUR, String.valueOf(SLOW_CURRENT_UA));
                writeLine(RESTRICT_CHG, "1");
                break;
            case MODE_FAST:
                writeLine(RESTRICT_CUR, String.valueOf(FAST_CURRENT_UA));
                writeLine(RESTRICT_CHG, "1");
                break;
            default:
                writeLine(RESTRICT_CHG, "0");
                break;
        }
        Log.i(TAG, "Applied charge mode " + mode);
    }

    /**
     * If the charger was attached while VBUS was already present (e.g. at boot),
     * the PMIC flags it as a legacy cable and never enables PD, leaving it at
     * 5V/500mA. Toggling CC makes it re-detect the charger. Blocks for seconds.
     */
    public static void fixStuckPd() {
        String iio = findSmb5Iio();
        if (iio == null) return;
        for (int i = 0; i < PD_RETRIES && isPdStuck(iio); i++) {
            Log.i(TAG, "PD not negotiated, resetting Type-C (attempt " + (i + 1) + ")");
            writeLine(iio + "/" + POWER_ROLE, ROLE_NONE);
            SystemClock.sleep(CC_OFF_MS);
            writeLine(iio + "/" + POWER_ROLE, ROLE_DUAL);
            SystemClock.sleep(PD_SETTLE_MS);
        }
    }

    private static boolean isPdStuck(String iio) {
        // A PC (default current) legitimately has no PD; only a 1.5A/3A source does
        String typec = readLine(TYPEC_MODE);
        return "1".equals(readLine(USB_ONLINE))
                && "0".equals(readLine(iio + "/" + PD_ACTIVE))
                && typec != null && typec.contains("high current");
    }

    private static String findSmb5Iio() {
        File[] devices = new File(IIO_DIR).listFiles();
        if (devices == null) return null;
        for (File dev : devices) {
            if (SMB5_IIO_NAME.equals(readLine(dev.getPath() + "/name"))) {
                return dev.getPath();
            }
        }
        return null;
    }

    private static String readLine(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            return line != null ? line.trim() : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static void writeLine(String path, String value) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(value);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write " + path, e);
        }
    }
}
