package com.mpaas.aar.demo.scan;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.alipay.android.phone.scancode.export.ScanRequest;
import com.alipay.android.phone.scancode.export.adapter.MPScan;
import com.alipay.android.phone.scancode.export.adapter.MPScanCallbackAdapter;
import com.alipay.android.phone.scancode.export.adapter.MPScanResult;
import com.alipay.android.phone.scancode.export.adapter.MPScanStarter;
import com.mpaas.aar.demo.custom.util.DialogUtil;

import java.util.ArrayList;
import java.util.List;

public class ScanFullScreenActivity extends Activity {

    private ScanRequest scanRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_fullscreen);

        findViewById(R.id.btn_start_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFullScreen();
            }
        });
        findViewById(R.id.btn_start_scan_continuously).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanFullScreenContinuously();
            }
        });
        findViewById(R.id.btn_recognize_type).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRecognizeType();
            }
        });
        findViewById(R.id.btn_hint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHint();
            }
        });
        findViewById(R.id.btn_open_torch_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOpenTorchText();
            }
        });
        findViewById(R.id.btn_close_torch_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCloseTorchText();
            }
        });
        findViewById(R.id.btn_hide_album).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHideAlbum();
            }
        });
        findViewById(R.id.btn_multi_ma_marker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMultiMaMarker();
            }
        });
        findViewById(R.id.btn_multi_ma_tip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMultiMaTip();
            }
        });
        findViewById(R.id.btn_target_ma_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTargetMaColor();
            }
        });

        scanRequest = new ScanRequest();
    }

    private void scanFullScreen() {
        MPScan.startMPaasScanFullScreenActivity(this, scanRequest, new MPScanCallbackAdapter() {

            @Override
            public boolean onScanFinish(final Context context, MPScanResult mpScanResult, final MPScanStarter mpScanStarter) {
                DialogUtil.alert((Activity) context,
                        mpScanResult != null ? mpScanResult.getText() : "没有识别到码", new DialogUtil.AlertCallback() {
                            @Override
                            public void onConfirm() {
                                ((Activity) context).finish();
                            }
                        });
                return true;
            }
        });
    }

    private void scanFullScreenContinuously() {
        MPScan.startMPaasScanFullScreenActivity(this, scanRequest, new MPScanCallbackAdapter() {

            @Override
            public boolean onScanFinish(final Context context, MPScanResult mpScanResult, final MPScanStarter mpScanStarter) {
                DialogUtil.alert((Activity) context,
                        mpScanResult != null ? mpScanResult.getText() : "没有识别到码", new DialogUtil.AlertCallback() {
                            @Override
                            public void onConfirm() {
                                mpScanStarter.restart();
                            }
                        });
                return false;
            }
        });
    }

    private void setRecognizeType() {
        String[] types = {
                "二维码",
                "条形码",
                "DM码",
                "PDF417码"
        };
        DialogUtil.multiply(this, "选择识别类型", types, new DialogUtil.MultiplyCallback() {
            @Override
            public void onConfirm(boolean[] isChecked) {
                List<ScanRequest.RecognizeType> recognizeTypes = new ArrayList<>();
                for (int i = 0; i < isChecked.length; i++) {
                    if (isChecked[i]) {
                        switch (i) {
                            case 0:
                                recognizeTypes.add(ScanRequest.RecognizeType.QR_CODE);
                                break;
                            case 1:
                                recognizeTypes.add(ScanRequest.RecognizeType.BAR_CODE);
                                break;
                            case 2:
                                recognizeTypes.add(ScanRequest.RecognizeType.DM_CODE);
                                break;
                            case 3:
                                recognizeTypes.add(ScanRequest.RecognizeType.PDF417_Code);
                                break;
                        }
                    }
                }
                scanRequest.setRecognizeType(recognizeTypes.toArray(new ScanRequest.RecognizeType[0]));
            }
        });
    }

    private void setHint() {
        DialogUtil.prompt(this, new DialogUtil.PromptCallback() {
            @Override
            public void onConfirm(String msg) {
                scanRequest.setViewText(msg);
            }
        });
    }

    private void setOpenTorchText() {
        DialogUtil.prompt(this, new DialogUtil.PromptCallback() {
            @Override
            public void onConfirm(String msg) {
                scanRequest.setOpenTorchText(msg);
            }
        });
    }

    private void setCloseTorchText() {
        DialogUtil.prompt(this, new DialogUtil.PromptCallback() {
            @Override
            public void onConfirm(String msg) {
                scanRequest.setCloseTorchText(msg);
            }
        });
    }

    private void setHideAlbum() {
        String[] types = {
                "是",
                "否"
        };
        DialogUtil.radio(this, "设置不显示相册", types, new DialogUtil.RadioCallback() {
            @Override
            public void onConfirm(int which) {
                switch (which) {
                    case 0:
                        scanRequest.setNotSupportAlbum(true);
                        break;
                    case 1:
                        scanRequest.setNotSupportAlbum(false);
                        break;
                }
            }
        });
    }

    private void setMultiMaMarker() {
        String[] images = {
                "默认",
                "绿箭头"
        };
        DialogUtil.radio(this, "设置多码标识图片", images, new DialogUtil.RadioCallback() {
            @Override
            public void onConfirm(int which) {
                switch (which) {
                    case 0:
                        scanRequest.setMultiMaMarker(-1);
                        break;
                    case 1:
                        scanRequest.setMultiMaMarker(R.drawable.green_arrow);
                        break;
                }
            }
        });
    }

    private void setMultiMaTip() {
        DialogUtil.prompt(this, new DialogUtil.PromptCallback() {
            @Override
            public void onConfirm(String msg) {
                scanRequest.setMultiMaTipText(msg);
            }
        });
    }

    private void setTargetMaColor() {
        String[] images = {
                "默认",
                "绿色",
        };
        DialogUtil.radio(this, "设置选中单码标识颜色", images, new DialogUtil.RadioCallback() {
            @Override
            public void onConfirm(int which) {
                switch (which) {
                    case 0:
                        scanRequest.setMaTargetColor(null);
                        break;
                    case 1:
                        scanRequest.setMaTargetColor("#32CD32");
                        break;
                }
            }
        });
    }
}
