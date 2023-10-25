package com.mpaas.aar.demo.custom;

import android.content.Intent;
import android.view.View;

import com.alipay.android.phone.scancode.export.adapter.MPScanResult;
import com.alipay.mobile.scansdk.activity.MPaasToolsCaptureActivity;
import com.alipay.mobile.scansdk.ui2.MPCustomScanView;
import com.mpaas.aar.demo.custom.util.DialogUtil;
import com.mpaas.aar.demo.custom.util.ThreadUtil;
import com.mpaas.aar.demo.custom.widget.MyScanView;

import java.util.List;

public class MyScanActivity extends MPaasToolsCaptureActivity {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private static final int REQUEST_CODE_PHOTO = 2;
    private MyScanView myScanView;

    @Override
    protected MPCustomScanView getCustomScanView() {
        myScanView = new MyScanView(this);
        myScanView.setCallback(new MyScanView.Callback() {
            @Override
            public void onBackClicked(View view) {
                finish();
            }

            @Override
            public void onGalleryClicked(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_PHOTO);
            }

            @Override
            public void onMaIconClicked(View view, MPScanResult mpScanResult) {
                handleMPScanResult(mpScanResult);
            }
        });
        return myScanView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (requestCode == REQUEST_CODE_PHOTO) {
            if (data == null) {
                DialogUtil.alert(this, "获取图片异常");
                return;
            }
            ThreadUtil.runOnSubThread(new Runnable() {
                @Override
                public void run() {
                    List<MPScanResult> mpScanResults = myScanView.scanFromPath(data.getDataString());
                    handleMPScanResult(mpScanResults != null ? mpScanResults.get(0) : null);
                }
            });
        }
    }

    private void handleMPScanResult(MPScanResult mpScanResult) {
        String msg;
        if (mpScanResult == null) {
            msg = "没有识别到码";
        } else {
            msg = mpScanResult.getText();
        }
        DialogUtil.alert(MyScanActivity.this, msg, new DialogUtil.AlertCallback() {
            @Override
            public void onConfirm() {
                myScanView.removeAllIconViews();
                finish();
            }
        });
    }
}
