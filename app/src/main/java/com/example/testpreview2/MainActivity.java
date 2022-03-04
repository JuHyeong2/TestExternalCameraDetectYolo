package com.example.testpreview2;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends Activity{

    private TextureView mCameraTextureView;
    //프리뷰 클래스 생성
    private Preview mPreview;

    //메인 액티비티를 여기로 지정
    Activity mainActivity = this;

    TextView text;
    private int TIMEOUT = 1000;


    //상수 지정
    private static final String TAG = "MAINACTIVITY";
    static final int REQUEST_CAMERA = 1;

    /*private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";*/
    private static final String ACTION_USB_PERMISSION = BuildConfig.APPLICATION_ID + ".USB_PERMISSION";
    UsbDevice device;
    UsbManager mUsbManager;
    boolean open = false;
    UsbDeviceConnection connection;
    UsbInterface devIf;
    UsbEndpoint mEndpointIntr;
    private final BroadcastReceiver mUsbDeviceReceiver  = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    // 다음과 같이 activity의 인텐트에서 연결된 기기를 나타내는 usbdevice를 얻을 수 있음
                    device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){

                            //connection = mUsbManager.openDevice(device);
                            //open = true;
                            Log.d(TAG, "permission for device " + device);
                        }
                    }
                    else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.textView);
        usbTest();
        //registerReceiver(mUsbDeviceReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
        //registerReceiver(mUsbDeviceReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
       // registerReceiver(mUsbDeviceReceiver, new IntentFilter(ACTION_USB_PERMISSION));

        /*PendingIntent mPermissionIntent;
        mPermissionIntent = PendingIntent.getBroadcast(this,0,new Intent(ACTION_USB_PERMISSION),0);
        registerReceiver(mUsbDeviceReceiver, new IntentFilter(ACTION_USB_PERMISSION));
        registerReceiver(mUsbDeviceReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED));
        mUsbManager =(UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        String i = "";
        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();
            //device = deviceList.get(device.getDeviceName());
            Log.d(TAG, device+"");
            if(device.getVendorId()==1133 && device.getProductId() == 2449) {
                if (mUsbManager.hasPermission(device)) {
                    Log.d(TAG, "request sent");
                    mUsbManager.requestPermission(device, mPermissionIntent);
                *//*} else {
                    Log.d(TAG, "got permission to use device");
                    open = true;
                }
                if (!open) {
                    i += "waiting for device permission";
                } else {
                *//**//*if(!connection.releaseInterface(devIf))
                {
                    i += "releaseInterface fauled";
                }*//**//*
                    connection.close();
                    i += "device closed";
                    open = false;*//*
                }
            }
        }*/


        //mUsbManager.requestPermission(device, mPermissionIntent);



        /*connection = mUsbManager.openDevice(device);
        if (connection == null)
        {
            return;
        }*/

        //mCameraTextureView에 UI속의 cameraTextureView를 지정해 준다.
        //mCameraTextureView = (TextureView) findViewById(R.id.cameraTextureView);
        //View v = inflater.inflate(R.layout.fragment_blank, container, false);
        mCameraTextureView = (TextureView) findViewById(R.id.cameraTextureView);
        //Preview클래스에 현재 Activity mainActivity와 mCameraTextureView라는 xml 속 UI를 넣어준다.
        mPreview = new Preview(this, mCameraTextureView);
    }

    private void usbTest() {
        UsbDevice device = (UsbDevice) getIntent().getParcelableExtra(
                UsbManager.EXTRA_DEVICE);
        if (device == null)
            text.setText("device null");
        else
            text.setText("device not null");

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        text.setText(text.getText() + "\nDevices connected: "
                + deviceList.values().size());
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();
            text.setText(text.getText() + "\nDevice name: "
                    + device.getDeviceName());
            text.setText(text.getText() + "\nDevice protocol: "
                    + device.getDeviceProtocol());
            text.setText(text.getText() + "\nDevice id: "
                    + device.getDeviceId());
            text.setText(text.getText() + "\nDevice product id: "
                    + device.getProductId());
            text.setText(text.getText() + "\nDevice vendor id: "
                    + device.getVendorId());
            text.setText(text.getText() + "\nDevice class: "
                    + device.getDeviceClass());
            text.setText(text.getText() + "\nDevice subclass: "
                    + device.getDeviceSubclass());
            text.setText(text.getText() + "\nDevice interface count: "
                    + device.getInterfaceCount());
            text.setText(text.getText() + "\n\n");
        }

        // communicate with device
        UsbInterface intf = device.getInterface(0);
        UsbEndpoint endpoint = intf.getEndpoint(0);
        UsbDeviceConnection connection = manager.openDevice(device);
        connection.claimInterface(intf, true);

        for (int i = 0; i < intf.getEndpointCount(); i++) {
            UsbEndpoint ep = intf.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    endpoint = ep;
                    text.setText("Found: "+i);
                }
            }
        }


        // byte[] opensession = { 0x0C, 0x00, 0x00, 0x00, 0x01, 0x00, 0x02,
        // 0x10,
        // 0x00, 0x00, 0x00, 0x00 };
        // connection.bulkTransfer(endpoint, opensession, opensession.length,
        // TIMEOUT);

        byte[] getEvent = { 0x0C, 0x00, 0x00, 0x00, 0x01, 0x00, toByte(0xC7),
                toByte(0x90), 0x00, 0x00, 0x00, 0x00 };
        int status = connection.bulkTransfer(endpoint, getEvent,
                getEvent.length, TIMEOUT);
        //text.setText("Status: " + status);

        byte[] capture = { 0x14, 0x00, 0x00, 0x00, 0x10, 0x00, 0x0E, 0x10,
                0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00 };
        connection.bulkTransfer(endpoint, capture, capture.length, TIMEOUT);

        // teminate communication
        BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        // call your method that cleans up and closes
                        // communication with the device
                    }
                }
            }
        };

    }

    public static byte toByte(int c) {
        return (byte) (c <= 0x7f ? c : ((c % 0x80) - 0x80));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                for (int i = 0; i < permissions.length; i++) {
                    //permissions배열에서 i번째 permission을 permission이라고 받고, 마찬가지로 grantResult도
                    String permission = permissions[i];
                    int grantResult = grantResults[i];

                    //permission이 Manifest.permission.CAMERA와 같다면, 즉 카메라 권한을 받았다면
                    if (permission.equals(Manifest.permission.CAMERA)) {
                        //카메라 권한과 동시에 허락을 받았다면
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            mCameraTextureView = (TextureView) findViewById(R.id.cameraTextureView);
                            mPreview = new Preview(this, mCameraTextureView);
                            mPreview.openCamera();
                            Log.d(TAG,"mPreview set");
                        } else {
                            Toast.makeText(this.getApplicationContext(),"Should have camera permission to run", Toast.LENGTH_LONG).show();
                            //finish();
                        }
                    }
                }
                //Switch끝
                break;
        }
    }


    @Override
    public void onResume() {
        //다시 시작 될 때 mPreview의 onResume()함ㅅ후가 불려진다.
        super.onResume();
        /*Intent intent = new Intent(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        Log.d(TAG, "intent: " + intent);
        String action = intent.getAction();
        Log.d(TAG, "action : "+action);
        //UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            setDevice(device);
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            if (this.device != null && this.device.equals(device)) {
                setDevice(null);
            }
        }*/
        //mPreview.onResume();
    }

    @Override
    public void onPause() {
        //정지 될 때 mPreview의 onPause()함수가 불려진다.
        super.onPause();
        //mPreview.onPause();
    }

    /*public void setDevice(UsbDevice device){
        Log.d(TAG, "setDevice " + device);
        if (device.getInterfaceCount() > 0) {
            Log.e(TAG, "could not find interface");
            return;
        }
        UsbInterface intf = device.getInterface(0);
        // device should have one endpoint
        if (intf.getEndpointCount() > 0) {
            Log.e(TAG, "could not find endpoint");
            return;
        }
        System.out.println("Got endpoint");
        // endpoint should be of type interrupt
        UsbEndpoint ep = intf.getEndpoint(0);
        if (ep.getType() != UsbConstants.USB_ENDPOINT_XFER_INT) {
            Log.e(TAG, "endpoint is not interrupt type");
            return;
        }
        this.device = device;
        mEndpointIntr = ep;
        if (device != null) {
            UsbDeviceConnection connection = mUsbManager.openDevice(device);
            if (connection != null && connection.claimInterface(intf, true)) {
                Log.d(TAG, "open SUCCESS");
                this.connection = connection;
                Thread thread = new Thread(this);
                thread.start();

            } else {
                Log.d(TAG, "open FAIL");
                this.connection = null;
            }
        }
    }*/


    /*private void sendCommand(int control) {
        synchronized (this) {
            if (this.connection != null) {
                byte[] message = new byte[1];
                message[0] = (byte)control;
                // Send command via a control request on endpoint zero
                if(this.connection.controlTransfer(0x21, 0x9, 0x200, 0, message, message.length, 0) > 0){
                    Log.d(TAG, "Sending Failed");
                }
            }
        }
    }*/

    /*public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        UsbRequest request = new UsbRequest();
        request.initialize(this.connection, mEndpointIntr);
        byte status = -1;
        while (true) {
            // queue a request on the interrupt endpoint
            request.queue(buffer, 1);
            // send poll status command
            if (this.connection.requestWait() == request) {
                byte newStatus = buffer.get(0);
                if (newStatus != status) {
                    Log.d(TAG, "got status " + newStatus);
                    status = newStatus;
                    sendCommand(7);

                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            } else {
                Log.e(TAG, "requestWait failed, exiting");
                break;
            }
        }
    }*/

}