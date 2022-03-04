package com.example.testpreview2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Preview extends Thread{
    private final static String TAG = "Preview : ";

    private Size mPreviewSize;
    private Context mContext;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private TextureView mTextureView;

    public Preview(Context context, TextureView textureView) {
        mContext = context;
        mTextureView = textureView;
    }

    //뒷카메라의 아이디 찾기
    private String getBackFacingCameraId(CameraManager cManager) {
        try {
            for (final String cameraId : cManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                Log.d(TAG, " **** device ["+cameraId+"] facing:"+cOrientation);
                //Log.e(TAG, "cameraID : "+cameraId);
                if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT) return cameraId;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        //mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_EXTERNAL);
        //cOrientation == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL
        /*String exCamId = null;

        try {
            for (String cameraId : cManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                //LENS_FACING_EXTERNAL will return Value: 2
                if (facing != null && facing.equals(CameraCharacteristics.LENS_FACING_BACK)) {
                    exCamId = cameraId;
                    Log.d("success", "");
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        return exCamId;*/
        /*CameraManager manager =
                (CameraManager)getSystemService(CAMERA_SERVICE);*/
        /*try {
            for (String cameraId : cManager.getCameraIdList()) {
                CameraCharacteristics chars
                        = cManager.getCameraCharacteristics(cameraId);
                // Do something with the characteristics
                int deviceLevel = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                int facing = chars.get(CameraCharacteristics.LENS_FACING);
                Log.d(TAG, " **** device ["+cameraId+"] level:"+deviceLevel+" facing:"+facing);
            }
        } catch(CameraAccessException e){
            e.printStackTrace();
        }*/
        return null;
    }

    public void openCamera() {
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera E");
        try {
            String cameraId = null;
            //CameraMenager로 부터 Camera가 뒤를 향하고 있는 CameraId를 가져온다.
            cameraId = getBackFacingCameraId(manager);
            //받아온 CameraId를 이용해 manager로부터 CameraCharacteristics를 받아온다.
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            //CameraCharacteristics에서 SCALER_STREAM_CONFIGURATION_MAP을 가져온다.
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            //Stream되는 map 사이즈를 mPreviewSize에 넣는다. map의 0번째 부터 넣는다.
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            int permissionCamera = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
            //permission이 허락 되어있는지 확인합니다.
            if(permissionCamera == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CAMERA}, MainActivity.REQUEST_CAMERA);
            } else {
                //허락 되었다면 manager.openCamera(카메라 id, 상태콜백, null)로 camera를 엽니다. manager.openCamera를 해서 mStateCallback에 출력값을 넣어줌
                manager.openCamera(cameraId, mStateCallback, null);

            }
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    //SurfaceTextureListener생성
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener(){

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface,
                                              int width, int height) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onSurfaceTextureAvailable, width="+width+",height="+height);
            //onSurfaceTextureAvailable이라면 openCamera(); SurfaceTextureListener가
            openCamera();
        }

        //밑에는 SurfaceTexture가 변경, 파괴, 업데이트 되었을 시.
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                int width, int height) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // TODO Auto-generated method stub
        }
    };

    //카메라 상태 콜백 객체를 생성하고 익명클래스를 만든다.
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onOpened");
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onError");
        }

    };

    //startPreview 함수를 만든다.
    protected void startPreview() {
        // TODO Auto-generated method stub
        //카메라 디바이스가 null 이거나 mTextureView가 사용 불가능 상태거나 mPreviewSize가 없다면 동작 안한다.
        if(null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            Log.e(TAG, "startPreview fail, return");
        }



        //mTextureView(xml)에서부터 getSurfaceTexture()를 가져온다.
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if(null == texture) {
            Log.e(TAG,"texture is null, return");
            return;
        }

        //mTextureView에서 겉표면 텍스처를 texture라고 했을 때 texture의 기본 버퍼 사이즈를 mPreview에 맞춘다.
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        //Texture를 이용해서 surface를 만든다.
        Surface surface = new Surface(texture);

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    // TODO Auto-generated method stub
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    // TODO Auto-generated method stub
                    Toast.makeText(mContext, "onConfigureFailed", Toast.LENGTH_LONG).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        // TODO Auto-generated method stub
        if(null == mCameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler backgroundHandler = new Handler(thread.getLooper());

        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setSurfaceTextureListener()
    {
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    public void onResume() {
        Log.d(TAG, "onResume");
        setSurfaceTextureListener();
    }

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    public void onPause() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onPause");
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
                Log.d(TAG, "CameraDevice Close");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
            Log.d(TAG, "CameraDevice release");
        }
    }


}
