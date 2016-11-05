package com.example.fussen.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fussen.bluetooth.bean.LogUtil;
import com.example.fussen.bluetooth.bean.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button open;
    private Button scanner;
    private Button stop;
    private Button btn_disconnect;
    private Button pause;
    private Button play;
    private Button close;
    private ListView listview;
    private BluetoothManager manager;
    private BluetoothAdapter bluetoothAdapter;


    private List<BluetoothDevice> mData = new ArrayList<>();
    private MyAdapter ListAdapter;
    private BluetoothGatt mBluetoothGatt;

    private boolean isComment = false;

    public static final UUID UUID1 = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID2 = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID3 = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

    private static final int FIND_SERVICE = 1;
    private static final int SEND_DATA_FAIL = 2;
    private static final int SEND_DATA_SUCCESS = 3;
    private static final int CONNECT_SUCCESS = 4;
    private static final int CONNECT_FIAL = 5;
    private List<BluetoothGattService> services = new ArrayList<>();

    private List<BluetoothGattCharacteristic> characteristics = new ArrayList<>();

    private boolean mScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        //初始化蓝牙
        initBluetooth();
    }

    private void initView() {
        open = (Button) findViewById(R.id.open);

        scanner = (Button) findViewById(R.id.scanner);
        close = (Button) findViewById(R.id.close);
        stop = (Button) findViewById(R.id.stop);
        btn_disconnect = (Button) findViewById(R.id.disconnect);
        play = (Button) findViewById(R.id.play);
        pause = (Button) findViewById(R.id.pause);

        listview = (ListView) findViewById(R.id.listview);

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        btn_disconnect.setOnClickListener(this);
        open.setOnClickListener(this);
        stop.setOnClickListener(this);
        scanner.setOnClickListener(this);
        close.setOnClickListener(this);
        listview.setOnItemClickListener(this);
    }

    private void initBluetooth() {

        //第一步 检查设备时候支持BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "请注意，您的手机不支持BLE", Toast.LENGTH_SHORT).show();
        }

        //第二步 拿到蓝牙管理器
        manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothAdapter = manager.getAdapter();

    }

    @Override
    public void onClick(View view) {
        if (view == open) {

            //检查蓝牙是否已打开 如未打开 则打开蓝牙
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
                Toast.makeText(this, "打开蓝牙", Toast.LENGTH_SHORT).show();
            }

        } else if (view == scanner) {


            //扫描蓝牙
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "请先打开蓝牙后扫描", Toast.LENGTH_SHORT).show();
                return;
            }
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                Toast.makeText(this, "正在扫描中,不要重复扫描", Toast.LENGTH_SHORT).show();
                return;
            }

            //开始扫描
            scannerBluetooth();
            Toast.makeText(this, "开始扫描蓝牙", Toast.LENGTH_SHORT).show();

        } else if (view == close) {

            //关闭蓝牙
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.stopLeScan(mLeScanCallback);
            }
            if (bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "关闭蓝牙", Toast.LENGTH_SHORT).show();
                bluetoothAdapter.disable();
            }
        } else if (view == stop) {
            //停止扫描
            if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.stopLeScan(mLeScanCallback);
                Toast.makeText(this, "停止扫描", Toast.LENGTH_SHORT).show();
            }
        } else if (view == play) {

            playMusic();
        } else if (view == pause) {
            pauseMusic();
        } else if (view == btn_disconnect) {
            //断开连接
            mBluetoothGatt.disconnect();
        }
    }

    private void pauseMusic() {

        byte[] data = new byte[6];
        data[0] = 0x55;
        data[1] = (byte) 0xAA;
        data[2] = 0x00;
        data[3] = 0x03;
        data[4] = 0x02;
        data[5] = (byte) 0xFB;
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            ToastUtil.showToast("蓝牙异常，请重新连接", this);
        }


        //向特征中写入数据
        if (services.get(0) != null) {
            characteristics.get(1).setValue(data);
            mBluetoothGatt.writeCharacteristic(characteristics.get(1));

        }
    }

    private void playMusic() {

        byte[] data = new byte[6];
        data[0] = 0x55;
        data[1] = (byte) 0xAA;
        data[2] = 0x00;
        data[3] = 0x03;
        data[4] = 0x01;
        data[5] = (byte) 0xFC;
        if (bluetoothAdapter == null || mBluetoothGatt == null) {
            ToastUtil.showToast("蓝牙异常，请重新连接", this);
            return;
        }

        if (services.get(0) != null) {
            characteristics.get(1).setValue(data);
            mBluetoothGatt.writeCharacteristic(characteristics.get(1));
        }
    }


    /**
     * 扫面蓝牙设备
     */
    private void scannerBluetooth() {


        //10秒钟后停止扫描，扫描蓝牙设备是很费资源的
        mhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                bluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }, 10000);

        mScanning = true;
        //需要参数 BluetoothAdapter.LeScanCallback(返回的扫描结果)
        bluetoothAdapter.startLeScan(mLeScanCallback);

    }


    /**
     * 蓝牙扫面结果的回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            LogUtil.fussenLog().d("1008611" + "=====已扫描到蓝牙设备==");

            if (bluetoothDevice != null && bluetoothDevice.getName() != null) {

                if (mData.size() == 0) {
                    mData.add(bluetoothDevice);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //将扫描的到的bluetoothDevice添加到集合中 展示出来
                            ListAdapter = new MyAdapter(mData);
                            listview.setAdapter(ListAdapter);
                        }
                    });

                    for (int y = 0; y < mData.size(); y++) {
                        LogUtil.fussenLog().d("1008611" + "=====蓝牙设备name==" + mData.get(y).getName());
                        LogUtil.fussenLog().d("1008611" + "=====address==" + mData.get(y).getAddress());
                    }
                } else {
                    for (int x = 0; x < mData.size(); x++) {
                        isComment = mData.get(x).getAddress().equals(bluetoothDevice.getAddress()) ? true : false;
                        if (isComment) {
                            isComment = false;
                            break;
                        } else {
                            mData.add(bluetoothDevice);
                            ListAdapter.setData(mData);
                        }
                    }
                }

            } else {
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        ToastUtil.showToast("正在连接中,请稍后", this);
        //停止扫描

        if (mScanning) {
            bluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }

        //通过蓝牙设备地址 获取远程设备 开始连接
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mData.get(i).getAddress());

        //第二个参数 是否要自动连接
        mBluetoothGatt = device.connectGatt(MainActivity.this, false, mBluetoothGattCallback);


    }


    private Handler mhandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            int type = msg.what;

            switch (type) {
                case FIND_SERVICE:

                    for (int x = 0; x < services.size(); x++) {
                        characteristics = services.get(2).getCharacteristics();
                        break;
                    }
                    break;

                case SEND_DATA_SUCCESS:
                    ToastUtil.showToast("数据发送成功", MainActivity.this);
                    break;
                case SEND_DATA_FAIL:
                    ToastUtil.showToast("数据发送失败", MainActivity.this);
                    break;
                case CONNECT_SUCCESS:
                    ToastUtil.showToast("连接成功", MainActivity.this);
                    break;
                case CONNECT_FIAL:
                    ToastUtil.showToast("连接失败", MainActivity.this);
                    break;

            }
        }
    };


    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {


        /**
         * 连接成功后发现设备服务的回调
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            LogUtil.fussenLog().d("10086" + "===搜到服务===");
            if (status == BluetoothGatt.GATT_SUCCESS) {//发现该设备的服务

                //拿到该服务 1,通过UUID拿到指定的服务  2,可以拿到该设备上所有服务的集合

                List<BluetoothGattService> serviceList = mBluetoothGatt.getServices();

                //可以遍历获得该设备上的服务集合，通过服务可以拿到该服务的UUID，和该服务里的所有属性Characteristic
                for (int x = 0; x < serviceList.size(); x++) {
                    LogUtil.fussenLog().d("10086" + "=======BluetoothGattService蓝牙的服务==UUID===getUuid()==" + serviceList.get(x).getUuid());
                    services.add(serviceList.get(x));
                }
                Message message = new Message();
                message.what = FIND_SERVICE;
                mhandler.sendMessage(message);

                //可以通过service拿到服务的特性 BluetoothGattCharacteristic  ---描述

            } else {//未发现该设备的服务
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast("未发现服务", MainActivity.this);
                    }
                });

            }
        }


        /**
         * 蓝牙连接状态改变后调用 此回调 (断开，连接)
         * @param gatt
         * @param status
         * @param newState
         */

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            LogUtil.fussenLog().d("10086" + "===newState===" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {//连接成功
                Message message = new Message();
                message.what = CONNECT_SUCCESS;
                mhandler.sendMessage(message);

                //连接成功后去发现该连接的设备的服务
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//连接失败 或者连接断开都会调用此方法
                Message message = new Message();
                message.what = CONNECT_FIAL;
                mhandler.sendMessage(message);
            }
        }


        /**
         * Characteristic数据发送后调用此回调
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {//写入成功
                Message message = new Message();
                message.what = SEND_DATA_SUCCESS;
                mhandler.sendMessage(message);

            } else if (status == BluetoothGatt.GATT_FAILURE) {//写入失败
                Message message = new Message();
                message.what = SEND_DATA_FAIL;
                mhandler.sendMessage(message);
            } else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) {// 没有写入的权限

            }
        }


        //某Characteristic的状态为可读时的回调
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            //读取到值，在这里读数据

            if (status == BluetoothGatt.GATT_SUCCESS) {
                 readCharacterisricValue(characteristic);

                // 订阅远端设备的characteristic，
                // 当此characteristic发生改变时当回调mBtGattCallback中的onCharacteristicChanged方法
                mBluetoothGatt.setCharacteristicNotification(mReadCharacteristric,
                        true);
                BluetoothGattDescriptor descriptor = mReadCharacteristric
                        .getDescriptor(UUID
                                .fromString("00002902-0000-1000-8000-00805f9b34fb"));
                if (descriptor != null) {
                    byte[] val = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                    descriptor.setValue(val);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }
            }
        }


        // 订阅了远端设备的Characteristic信息后，
        // 当远端设备的Characteristic信息发生改变后,回调此方法
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            readCharacterisricValue(characteristic);

        }

    };


    /**
     * 读取BluetoothGattCharacteristic中的数据
     *
     * @param characteristic
     */
    private void readCharacterisricValue(BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        StringBuffer buffer = new StringBuffer("0x");
        int i;
        for (byte b : data) {
            i = b & 0xff;
            buffer.append(Integer.toHexString(i));
        }
    }

    public class MyAdapter extends BaseAdapter {

        private List<BluetoothDevice> list;

        public MyAdapter(List<BluetoothDevice> data) {
            this.list = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        public void setData(List<BluetoothDevice> data) {
            this.list = data;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View v = View.inflate(MainActivity.this, R.layout.item, null);

            TextView name = (TextView) v.findViewById(R.id.name);
            TextView address = (TextView) v.findViewById(R.id.adress);
            name.setText(list.get(i).getName() + ": ");
            address.setText(list.get(i).getAddress());
            return v;
        }
    }


    @Override
    protected void onDestroy() {

        //释放资源
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        super.onDestroy();
    }
}
