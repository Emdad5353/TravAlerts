package com.emdad.travalerts.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {
    Context cn;
    public ConnectionDetector(Context cn) {
        this.cn = cn;
    }
    public boolean isConnected() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) cn.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm != null ? cm.getAllNetworkInfo() : new NetworkInfo[0];

        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI") && ni.isConnected())
                haveConnectedWifi = true;

            if (ni.getTypeName().equalsIgnoreCase("MOBILE") && ni.isConnected())
                haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
