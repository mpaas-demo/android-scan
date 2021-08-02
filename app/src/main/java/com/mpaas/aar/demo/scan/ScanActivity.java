package com.mpaas.aar.demo.scan;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.mpaas.aar.demo.custom.ScanHelper;

/**
 * Created by xingcheng on 2018/8/8.
 */

public class ScanActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.scan_title);
        }

        findViewById(R.id.standard_ui_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScanActivity.this, ScanRequestActivity.class));
            }
        });

        findViewById(R.id.standard_ui_fullscreen_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScanActivity.this, ScanFullScreenActivity.class));
            }
        });

        findViewById(R.id.custom_ui_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanWithCustomUI();
            }
        });
    }

    private void scanWithCustomUI() {
        ScanHelper.getInstance().scan(this, new ScanHelper.ScanCallback() {
            @Override
            public void onScanResult(boolean isProcessed, Intent result) {
                if (!isProcessed) {
                    // 扫码界面点击物理返回键或左上角返回键
                    return;
                }

                if (result == null || result.getData() == null) {
                    Toast.makeText(ScanActivity.this, R.string.scan_failure, Toast.LENGTH_SHORT).show();
                    return;
                }
                DialogUtil.alert(ScanActivity.this, result.getData().toString());
            }
        });
    }
}
