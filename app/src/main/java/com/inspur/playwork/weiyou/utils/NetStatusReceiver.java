package com.inspur.playwork.weiyou.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by sunyuan on 2016/11/28 0028 16:10.
 * Email: sunyuan@inspur.com
 */

public class NetStatusReceiver extends BroadcastReceiver {

// / 没有连接
    public static final int NETWORN_NONE = 0;
// / wifi连接
    public static final int NETWORN_WIFI = 1;
// / 手机网络数据连接
    public static final int NETWORN_2G = 2;
    public static final int NETWORN_3G = 3;
    public static final int NETWORN_4G = 4;
    public static final int NETWORN_MOBILE = 5;

    private NetStatusListener netStatusListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if(netStatusListener!=null)
            netStatusListener.onNetStatusChanged(getNetworkState(context));
    }  //如果无网络连接activeInfo为null

    /**
     * 返回当前网络连接类型
     * @param context
     * @return
     */
    public static int getNetworkState(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == connManager)
            return NETWORN_NONE;
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable())
            return NETWORN_NONE;
        // Wifi
        NetworkInfo wifiInfo=connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(null!=wifiInfo){
            NetworkInfo.State state = wifiInfo.getState();
            if(null!=state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING)
                    return NETWORN_WIFI;
        }
        // 网络
        NetworkInfo networkInfo=connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(null!=networkInfo){
            NetworkInfo.State state = networkInfo.getState();
            String strSubTypeName = networkInfo.getSubtypeName();
            if(null!=state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    switch (activeNetInfo.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return NETWORN_2G;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return NETWORN_3G;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return NETWORN_4G;
                        default://有机型返回16,17
                            //中国移动 联通 电信 三种3G制式
                            if (strSubTypeName.equalsIgnoreCase("TD-SCDMA") || strSubTypeName.equalsIgnoreCase("WCDMA") || strSubTypeName.equalsIgnoreCase("CDMA2000")){
                                return NETWORN_3G;
                            }else{
                                return NETWORN_MOBILE;
                            }
                    }
                }
        }
        return NETWORN_NONE;
    }

    public void setNetStatusListener(NetStatusListener netStatusListener) {
        this.netStatusListener = netStatusListener;
    }

    public interface NetStatusListener {
        void onNetStatusChanged(int netType);
    }
}