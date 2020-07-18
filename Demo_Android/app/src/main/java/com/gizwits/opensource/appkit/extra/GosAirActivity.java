package com.gizwits.opensource.appkit.extra;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.opensource.appkit.ControlModule.GosControlModuleBaseActivity;
import com.gizwits.opensource.appkit.extra.ui.MusicLightActivity;
import com.gizwits.opensource.gokit.R;

import java.util.concurrent.ConcurrentHashMap;

public class GosAirActivity extends GosControlModuleBaseActivity implements View.OnClickListener {

    /**
     * The Constant TOAST.
     */
    protected static final int TOAST = 0;

    /**
     * The Constant SETNULL.
     */
    protected static final int SETNULL = 1;

    /**
     * The Constant UPDATE_UI.
     */
    protected static final int UPDATE_UI = 2;

    /**
     * The Constant LOG.
     */
    protected static final int LOG = 3;

    /**
     * The Constant HARDWARE.
     */
    protected static final int HARDWARE = 5;

    /**
     * received message from device.
     */
    private static final int RECEIVED = 6;

    /**
     * The Disconnect
     */
    protected static final int DISCONNECT = 7;
    private static final String TAG = "zzz";
    public static GizWifiDevice mDevice;
    private ConcurrentHashMap<String, Object> deviceStatu;
    private String title;

    /*
     * ===========================================================
	 * 以下key值对应设备硬件信息各明细的名称，用与回调中提取硬件信息字段。
	 * ===========================================================
	 */

    /**
     * The wifiHardVerKey
     */
    private static final String wifiHardVerKey = "wifiHardVersion";

    /**
     * The wifiSoftVerKey
     */
    private static final String wifiSoftVerKey = "wifiSoftVersion";

    /**
     * The mcuHardVerKey
     */
    private static final String mcuHardVerKey = "mcuHardVersion";

    /**
     * The mcuSoftVerKey
     */
    private static final String mcuSoftVerKey = "mcuSoftVersion";

    /**
     * The wifiFirmwareIdKey
     */
    private static final String FirmwareIdKey = "wifiFirmwareId";

    /**
     * The wifiFirmwareVerKey
     */
    private static final String FirmwareVerKey = "wifiFirmwareVer";

    /**
     * The productKey
     */
    private static final String productKey = "productKey";




    /*
     * ===========================================================
	 * 以下key值对应http://site.gizwits.com/v2/datapoint?product_key={productKey}
	 * 中显示的数据点名称，sdk通过该名称作为json的key值来收发指令，demo中使用的key都是对应机智云实验室的微信宠物屋项目所用数据点
	 * ===========================================================
	 */
    /**
     * led红灯开关 0=关 1=开.
     */
    private static final String KEY_RED_SWITCH = "LED_OnOff";

    /**
     * 指定led颜色值 0=自定义 1=黄色 2=紫色 3=粉色.
     */
    private static final String KEY_LIGHT_COLOR = "LED_Color";

    /**
     * led灯红色值 0-254.
     */
    private static final String KEY_LIGHT_RED = "LED_R";

    /**
     * led灯绿色值 0-254.
     */
    private static final String KEY_LIGHT_GREEN = "LED_G";

    /**
     * led灯蓝色值 0-254.
     */
    private static final String KEY_LIGHT_BLUE = "LED_B";

    /**
     * 电机转速 －5～－1 电机负转 0 停止 1～5 电机正转.
     */
    private static final String KEY_SPEED = "Motor_Speed";

    /**
     * 红外探测 0无障碍 1有障碍.
     */
    private static final String KEY_INFRARED = "Infrared";

    /**
     * 环境温度.
     */
    private static final String KEY_TEMPERATURE = "Temperature";

    /**
     * 环境湿度.
     */
    private static final String KEY_HUMIDITY = "Humidity";

    private Runnable mRunnable = new Runnable() {
        public void run() {
            if (isDeviceCanBeControlled()) {
                progressDialog.cancel();
            } else {
                toastDeviceNoReadyAndExit();
            }
        }

    };
    /**
     * The handler.
     */
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case LOG:
                    toastFaultsOrAlertsMessage(msg);
                    break;
                case TOAST:
                    toastInfoMessage(msg);
                    break;
                case HARDWARE:
                    showHardwareInfo((String) msg.obj);
                    break;
                case DISCONNECT:
                    toastDeviceDisconnectAndExit();
                    break;
                case UPDATE_UI:
                    progressDialog.cancel();
                    getDataFromDateMap();
                    break;
            }
        }
    };


    private TextView mTvTemperature;
    private TextView mTvHumitity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gos_air);
        initDevice();
        initViews();
        initEvents();
        mDevice.setListener(gizWifiDeviceListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getStatusOfDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(mRunnable);
        mDevice.setSubscribe(false);
        mDevice.setListener(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivSetting:
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.smartlight, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {
        switch (menu.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_music_light:
                Intent intent = new Intent();
                intent.putExtra("device", mDevice);
                intent.setClass(getApplicationContext(), MusicLightActivity.class);
                startActivity(intent);
            default:
                break;
        }
        return super.onOptionsItemSelected(menu);
    }


    private void getDataFromDateMap() {
        for (String key : deviceStatu.keySet()) {
            if (key.equals(KEY_RED_SWITCH)) {
//                isPowerOn = (boolean) deviceStatu.get(key);
            }
            if (key.equals(KEY_HUMIDITY)) {
                mTvHumitity.setText(deviceStatu.get(key).toString());
            }
            if (key.equals(KEY_TEMPERATURE)) {
                mTvTemperature.setText(deviceStatu.get(key).toString());
            }
        }
    }


    /*
    * 设备上报数据回调
    */
    protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
                                  java.util.concurrent.ConcurrentHashMap<String, Object> dataMap, int sn) {

        if (result != GizWifiErrorCode.GIZ_SDK_SUCCESS) {
            Message msg = new Message();
            msg.what = TOAST;
            msg.obj = toastError(result);
            return;
        }
        if (dataMap.isEmpty()) {
            return;
        }

        progressDialog.cancel();

        if (dataMap.get("data") != null) {
            Log.i("Apptest", dataMap.get("data").toString());
            Message msg = new Message();
            msg.obj = dataMap.get("data");
            deviceStatu = (ConcurrentHashMap<String, Object>) dataMap.get("data");
            msg.what = UPDATE_UI;
            handler.sendMessage(msg);
        }

        if (dataMap.get("alerts") != null) {
            Message msg = new Message();
            msg.obj = dataMap.get("alerts");
            msg.what = LOG;
            handler.sendMessage(msg);
        }

        if (dataMap.get("faults") != null) {
            Message msg = new Message();
            msg.obj = dataMap.get("faults");
            msg.what = LOG;
            handler.sendMessage(msg);
        }

        if (dataMap.get("binary") != null) {
            Log.i("info", "Binary data:" + bytesToHex((byte[]) dataMap.get("binary")));
        }
    }

    /*
     * 获取设备硬件信息回调
     */
    protected void didGetHardwareInfo(GizWifiErrorCode result, GizWifiDevice device,
                                      java.util.concurrent.ConcurrentHashMap<String, String> hardwareInfo) {
        Log.i("Apptest", hardwareInfo.toString());
        StringBuffer sb = new StringBuffer();
        Message msg = new Message();
        if (GizWifiErrorCode.GIZ_SDK_SUCCESS != result) {
            msg.what = TOAST;
            msg.obj = toastError(result);
        } else {
            sb.append("Wifi Hardware Version:" + hardwareInfo.get(wifiHardVerKey) + "\r\n");
            sb.append("Wifi Software Version:" + hardwareInfo.get(wifiSoftVerKey) + "\r\n");
            sb.append("MCU Hardware Version:" + hardwareInfo.get(mcuHardVerKey) + "\r\n");
            sb.append("MCU Software Version:" + hardwareInfo.get(mcuSoftVerKey) + "\r\n");
            sb.append("Wifi Firmware Id:" + hardwareInfo.get(FirmwareIdKey) + "\r\n");
            sb.append("Wifi Firmware Version:" + hardwareInfo.get(FirmwareVerKey) + "\r\n");
            sb.append("Product Key:" + "\r\n" + hardwareInfo.get(productKey) + "\r\n");

            // 设备属性
            sb.append("Device ID:" + "\r\n" + mDevice.getDid() + "\r\n");
            sb.append("Device IP:" + mDevice.getIPAddress() + "\r\n");
            sb.append("Device MAC:" + mDevice.getMacAddress() + "\r\n");

            msg.what = HARDWARE;
            msg.obj = sb.toString();
        }

        handler.sendMessage(msg);
    }

    /*
     * 设置设备信息回调
     */
    protected void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
        progressDialog.cancel();
        Message msg = new Message();
        msg.what = TOAST;
        String toastText;
        if (GizWifiErrorCode.GIZ_SDK_SUCCESS == result) {
            toastText = (String) getText(R.string.set_info_successful);
        } else {
            toastText = toastError(result);
        }
        msg.obj = toastText;
        handler.sendMessage(msg);
    }

    /*
     * 设备状态改变回调，只有设备状态为可控才可以下发控制命令
     */
    protected void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {
        if (device == mDevice) {
            if (GizWifiDeviceNetStatus.GizDeviceUnavailable == netStatus
                    || GizWifiDeviceNetStatus.GizDeviceOffline == netStatus) {
                handler.sendEmptyMessage(DISCONNECT);
            } else {
                handler.removeCallbacks(mRunnable);
                progressDialog.cancel();
                mDevice.getDeviceStatus();
            }
        }
    }


    private void initViews() {
        mTvHumitity = (TextView) findViewById(R.id.tv_humidity);
        mTvTemperature = (TextView) findViewById(R.id.tv_temperature);

        String waitingText = (String) getText(R.string.waiting_device_ready);
        setProgressDialog(waitingText, true, false);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (progressDialog.isShowing()) {
                        GosAirActivity.this.finish();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void initEvents() {
    }

    private void initDevice() {
        Intent intent = getIntent();
        mDevice = (GizWifiDevice) intent.getParcelableExtra("GizWifiDevice");
        deviceStatu = new ConcurrentHashMap<String, Object>();

        if (TextUtils.isEmpty(mDevice.getAlias())) {
            title = mDevice.getProductName();
        } else {
            title = mDevice.getAlias();
        }
    }


    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }

    /**
     * 展示设备硬件信息
     *
     * @param hardwareInfo
     */
    private void showHardwareInfo(String hardwareInfo) {
        String hardwareInfoTitle = (String) getText(R.string.hardwareInfo);
        new AlertDialog.Builder(this).setTitle(hardwareInfoTitle).setMessage(hardwareInfo)
                .setPositiveButton(R.string.besure, null).show();
    }

    private void toastDeviceDisconnectAndExit() {
        Toast.makeText(GosAirActivity.this, R.string.disconnect, Toast.LENGTH_SHORT).show();
        finish();
    }


    private void toastInfoMessage(Message msg) {
        String info = msg.obj + "";
        Toast.makeText(GosAirActivity.this, info, Toast.LENGTH_SHORT).show();
    }

    private void toastFaultsOrAlertsMessage(Message msg) {
        StringBuilder sb = new StringBuilder();
        ConcurrentHashMap<String, Object> map = (ConcurrentHashMap<String, Object>) msg.obj;

        for (String key : map.keySet()) {
            if ((Boolean) map.get(key)) {
                sb.append(key + "1" + "\r\n");
            }
        }

        if (sb.length() != 0) {
            Toast.makeText(GosAirActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDeviceCanBeControlled() {
        return mDevice.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceControlled;
    }

    private void toastDeviceNoReadyAndExit() {
        Toast.makeText(this, R.string.device_no_ready, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Description:
     */
    private void getStatusOfDevice() {
        // 设备是否可控
        if (isDeviceCanBeControlled()) {
            // 可控则查询当前设备状态
            mDevice.getDeviceStatus();
        } else {
            // 显示等待栏
            progressDialog.show();
            if (mDevice.isLAN()) {
                // 小循环10s未连接上设备自动退出
                handler.postDelayed(mRunnable, 10000);
            } else {
                // 大循环20s未连接上设备自动退出
                handler.postDelayed(mRunnable, 20000);
            }
        }
    }

    /**
     * 发送指令
     *
     * @param key   数据点对应的标识名
     * @param value 需要改变的值
     */
    private void sendCommand(String key, Object value) {
        int sn = 5;
        ConcurrentHashMap<String, Object> hashMap = new ConcurrentHashMap<String, Object>();
        hashMap.put(key, value);
        mDevice.write(hashMap, sn);
        Log.i("Apptest", hashMap.toString());
    }

    /**
     * 同时改变多个状态
     *
     * @param hashMap
     */
    private void sendMultiCommand(ConcurrentHashMap<String, Object> hashMap) {
        int sn = 5;
        mDevice.write(hashMap, sn);
        Log.i("Apptest", hashMap.toString());
    }

    private void sendColors(int r, int g, int b) {
        int sn = 5;
        r = r == 255 ? 254 : r;
        g = g == 255 ? 254 : g;
        b = b == 255 ? 254 : b;
        ConcurrentHashMap<String, Object> hashMap = new ConcurrentHashMap<String, Object>();
        hashMap.put(KEY_LIGHT_RED, r);
        hashMap.put(KEY_LIGHT_BLUE, b);
        hashMap.put(KEY_LIGHT_GREEN, g);
        mDevice.write(hashMap, sn);
        Log.i("Apptest", hashMap.toString());
    }
}