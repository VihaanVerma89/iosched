package com.google.android.apps.iosched.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

import com.google.android.apps.iosched.Config;

import java.util.List;

/**
 * Created by vihaan on 7/7/14.
 */
public class WiFiUtils {
    public static final String PREF_WIFI_AP_CONFIG = "pref_wifi_ap_config";

    public static final String WIFI_CONFIG_DONE = "done";
    public static final String WIFI_CONFIG_REQUESTED = "requested";

    public static boolean shouldBypassWiFiSetup(final Context context)
    {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if(wifiManager.isWifiEnabled())
        {
            final List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
            final String conferenceSSID = String.format("\"%s\"", Config.WIFI_SSID);

            for(WifiConfiguration config: configs)
            {
                if(conferenceSSID.equalsIgnoreCase(config.SSID))
                    return true;
            }

        }
        return WIFI_CONFIG_DONE.equals(getWiFiConfigStatus(context));

    }

    // Stored preferences associated with WiFi AP configuration.
    public static String getWiFiConfigStatus(final Context context) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PREF_WIFI_AP_CONFIG, null);
    }

    public static void setWifiConfigStatus(final Context context, final String status) {
        if (!WIFI_CONFIG_DONE.equals(status) && !WIFI_CONFIG_REQUESTED.equals(status)) {
            throw new IllegalArgumentException("Invalid WiFi Config status : " + status);
        }
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_WIFI_AP_CONFIG, status).commit();
    }
}
