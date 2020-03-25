
package com.mpaas.aar.demo.custom;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.alipay.mobile.bqcscanservice.BQCScanCallback;
import com.alipay.mobile.bqcscanservice.BQCScanEngine;
import com.alipay.mobile.bqcscanservice.BQCScanError;
import com.alipay.mobile.bqcscanservice.CameraHandler;
import com.alipay.mobile.bqcscanservice.MPaasScanService;
import com.alipay.mobile.bqcscanservice.impl.AlipayBqcLogger;
import com.alipay.mobile.bqcscanservice.impl.MPaasScanServiceImpl;
import com.alipay.mobile.common.logging.api.LoggerFactory;
import com.alipay.mobile.mascanengine.MaPictureEngineService;
import com.alipay.mobile.mascanengine.MaScanCallback;
import com.alipay.mobile.mascanengine.MaScanResult;
import com.alipay.mobile.mascanengine.MultiMaScanResult;
import com.alipay.mobile.mascanengine.impl.MaPictureEngineServiceImpl;
import com.alipay.mobile.scansdk.camera.ScanHandler;
import com.alipay.mobile.scansdk.camera.ScanType;
import com.mpaas.aar.demo.custom.widget.APTextureView;
import com.mpaas.aar.demo.custom.widget.ScanView;

public class CustomScanActivity extends Activity {

    private final String TAG = CustomScanActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PERMISSION = 1;
    private static final int REQUEST_CODE_PHOTO = 2;

    private ImageView mTorchBtn;
    private APTextureView mSurfaceView;
    private ScanView mScanView;
    private MPaasScanService mPaasScanService;

    private boolean isFirstStart = true;
    private boolean isPermissionGranted;
    private boolean isScanning;
    private boolean isPaused;

    private CameraHandler cameraHandler;
    private ScanHandler scanHandler;
    private Rect scanRect;
    private long postcode = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scan);

        // 设置沉浸模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mSurfaceView = (APTextureView) findViewById(R.id.surface_view);
        mScanView = (ScanView) findViewById(R.id.scan_view);
        findViewById(R.id.gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });
        mTorchBtn = (ImageView) findViewById(R.id.torch);
        mTorchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTorch();
            }
        });
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initScanService();
        initScanHandler();
        cameraHandler = mPaasScanService.getCameraHandler();
        checkCameraPermission();
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PHOTO);
    }

    private void switchTorch() {
        if (mPaasScanService != null) {
            boolean torchOn = mPaasScanService.isTorchOn();
            mPaasScanService.setTorch(!torchOn);
            mTorchBtn.setSelected(!torchOn);
        }
    }

    private void initScanService() {
        mPaasScanService = new MPaasScanServiceImpl();
        mPaasScanService.serviceInit(null);
        mPaasScanService.setTraceLogger(new AlipayBqcLogger());
        mPaasScanService.setEngineParameters(null);
    }

    private void initScanHandler() {
        scanHandler = new ScanHandler();
        scanHandler.setMPaasScanService(mPaasScanService);
        scanHandler.setContext(this, new ScanHandler.ScanResultCallbackProducer() {
            @Override
            public BQCScanEngine.EngineCallback makeScanResultCallback(ScanType type) {
                BQCScanEngine.EngineCallback maCallback = null;
                if (type == ScanType.SCAN_MA) {
                    maCallback = new MaScanCallback() {
                        @Override
                        public void onResultMa(final MultiMaScanResult multiMaScanResult) {
                            mPaasScanService.setScanEnable(false);
                            scanHandler.shootSound();
                            onScanSuccess(multiMaScanResult.maScanResults[0]);
                        }
                    };
                }
                return maCallback;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
        if (isScanning) {
            stopScan();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
        if (!isFirstStart && isPermissionGranted) {
            startScan();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scanHandler != null) {
            scanHandler.removeContext();
            scanHandler.destroy();
        }
    }

    private void checkCameraPermission() {
        if (PermissionChecker.checkSelfPermission(
                this, Manifest.permission.CAMERA) != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION);
        } else {
            onPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            int length = Math.min(permissions.length, grantResults.length);
            for (int i = 0; i < length; i++) {
                if (TextUtils.equals(permissions[i], Manifest.permission.CAMERA)) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Utils.toast(this, getString(R.string.camera_no_permission));
                    } else {
                        onPermissionGranted();
                    }
                    break;
                }
            }
        }
    }

    private void onPermissionGranted() {
        isPermissionGranted = true;
        startScan();
    }

    private void scanFromUri(Uri uri) {
        final Bitmap bitmap = Utils.uri2Bitmap(this, uri);
        if (bitmap == null) {
            notifyScanResult(true, null);
            finish();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MaPictureEngineService service = new MaPictureEngineServiceImpl();
                    final MaScanResult result = service.process(bitmap);
                    scanHandler.shootSound();
                    onScanSuccess(result);
                }
            }, "scanFromUri").start();
        }
    }

    private void onScanSuccess(final MaScanResult result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result == null) {
                    notifyScanResult(true, null);
                } else {
                    Intent intent = new Intent();
                    intent.setData(Uri.parse(result.text));
                    notifyScanResult(true, intent);
                }
                CustomScanActivity.this.finish();
            }
        });
    }

    private void startScan() {
        try {
            isScanning = true;
            cameraHandler.init(this, bqcScanCallback);
            cameraHandler.openCamera();
        } catch (Exception e) {
            isScanning = false;
            LoggerFactory.getTraceLogger().error(TAG, "startScan: Exception " + e.getMessage());
        }
    }

    private void stopScan() {
        mScanView.onStopScan();
        cameraHandler.closeCamera();
        scanHandler.disableScan();
        cameraHandler.release(postcode);
        isScanning = false;
        if (isFirstStart) {
            isFirstStart = false;
        }
    }

    private void initScanRect() {
        if (scanRect == null) {
            scanRect = mScanView.getScanRect(
                    mPaasScanService.getCamera(), mSurfaceView.getWidth(), mSurfaceView.getHeight());

            float cropWidth = mScanView.getCropWidth();
            LoggerFactory.getTraceLogger().debug(TAG, "cropWidth: " + cropWidth);
            if (cropWidth > 0) {
                // 预览放大 ＝ 屏幕宽 ／ 裁剪框宽
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                float screenWith = wm.getDefaultDisplay().getWidth();
                float screenHeight = wm.getDefaultDisplay().getHeight();
                float previewScale = screenWith / cropWidth;
                if (previewScale < 1.0f) {
                    previewScale = 1.0f;
                }
                if (previewScale > 1.5f) {
                    previewScale = 1.5f;
                }
                LoggerFactory.getTraceLogger().debug(TAG, "previewScale: " + previewScale);
                Matrix transform = new Matrix();
                transform.setScale(previewScale, previewScale, screenWith / 2, screenHeight / 2);
                mSurfaceView.setTransform(transform);
            }
        }
        mPaasScanService.setScanRegion(scanRect);
    }

    private void notifyScanResult(boolean isProcessed, Intent resultData) {
        ScanHelper.getInstance().notifyScanResult(isProcessed, resultData);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        notifyScanResult(false, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (requestCode == REQUEST_CODE_PHOTO) {
            scanFromUri(data.getData());
        }
    }

    private BQCScanCallback bqcScanCallback = new BQCScanCallback() {
        @Override
        public void onParametersSetted(final long pcode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    postcode = pcode;
                    mPaasScanService.setDisplay(mSurfaceView);
                    cameraHandler.onSurfaceViewAvailable();
                    scanHandler.registerAllEngine(false);
                    scanHandler.setScanType(ScanType.SCAN_MA);
                    scanHandler.enableScan();
                    mScanView.onStartScan();
                }
            });
        }

        @Override
        public void onSurfaceAvaliable() {
            if (!isPaused && mPaasScanService != null) {
                cameraHandler.onSurfaceViewAvailable();
            }
        }

//        @Override
//        public void onSurfaceUpdated() {
//
//        }

        @Override
        public void onPreviewFrameShow() {
            if (!isPaused) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            initScanRect();
                        }
                    }
                });
            }
        }

        @Override
        public void onError(final BQCScanError bqcError) {
            if (!isPaused) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.toast(CustomScanActivity.this, getString(R.string.camera_open_error));
                    }
                });
            }
        }

        @Override
        public void onCameraOpened() {
        }

        @Override
        public void onPreOpenCamera() {

        }

        @Override
        public void onStartingPreview() {

        }

        @Override
        public void onCameraAutoFocus(boolean success) {
        }

        @Override
        public void onOuterEnvDetected(boolean shouldShow) {
        }

        @Override
        public void onCameraReady() {
        }

        @Override
        public void onCameraClose() {
        }

        @Override
        public void onCameraFrameRecognized(boolean b, long l) {

        }

        @Override
        public void onEngineLoadSuccess() {

        }

        @Override
        public void onSetEnable() {

        }

        @Override
        public void onCameraManualFocusResult(boolean b) {

        }

        @Override
        public void onCameraParametersSetFailed() {

        }
    };
}
