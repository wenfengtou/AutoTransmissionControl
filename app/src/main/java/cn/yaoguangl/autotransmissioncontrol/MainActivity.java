package cn.yaoguangl.autotransmissioncontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleIndicateCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private TextView mHintTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHintTextView = findViewById(R.id.hint_tv);
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance().enableBluetooth();
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setSplitWriteNum(20)
                .setConnectOverTime(10000)
                .setOperateTimeout(5000);

        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {

            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                Log.i("AutoTrans", "onScanning " + bleDevice.getMac() + " " +  bleDevice.getName());
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                for (BleDevice bleDevice : scanResultList) {
                    if (bleDevice.getName() != null && bleDevice.getName().equals("HC-08")) {
                        connectBlueTooth(bleDevice);
                        break;
                    }
                }
            }
        });

    }

    public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private void connectBlueTooth(BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {

            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {

            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(bleDevice.getName() + " :\n");
                List<BluetoothGattService> bluetoothGattServiceList = gatt.getServices();
                for(BluetoothGattService bluetoothGattService : bluetoothGattServiceList) {
                    stringBuilder.append( "服务 :" + bluetoothGattService.getUuid() + " \n");
                    List<BluetoothGattCharacteristic> bluetoothGattCharacteristicList = bluetoothGattService.getCharacteristics();
                    for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattCharacteristicList) {
                        stringBuilder.append( "特性 :" + bluetoothGattCharacteristic.getUuid() + " \n");
                        if (bluetoothGattCharacteristic.getUuid().toString().equals(HEART_RATE_MEASUREMENT)) {
                            Log.i("AutoTrans", "start notify service" + bluetoothGattService.getUuid().toString() + " chara " +  bluetoothGattCharacteristic.getUuid().toString());
                            /*
                            BleManager.getInstance().notify(bleDevice, bluetoothGattService.getUuid().toString(), bluetoothGattCharacteristic.getUuid().toString(), new BleNotifyCallback() {
                                @Override
                                public void onNotifySuccess() {

                                }

                                @Override
                                public void onNotifyFailure(BleException exception) {

                                }

                                @Override
                                public void onCharacteristicChanged(byte[] data) {

                                }
                            });
                             */

                            BleManager.getInstance().indicate(bleDevice, bluetoothGattService.getUuid().toString(), bluetoothGattCharacteristic.getUuid().toString(), new BleIndicateCallback() {
                                @Override
                                public void onIndicateSuccess() {

                                }

                                @Override
                                public void onIndicateFailure(BleException exception) {

                                }

                                @Override
                                public void onCharacteristicChanged(byte[] data) {
                                    try {
                                        Log.i("AutoTrans", "data = " + new String(data, 0, data.length, "GB2312"));
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                    }
                }
                mHintTextView.setText(stringBuilder.toString());
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {

            }
        });
    }


    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

}
