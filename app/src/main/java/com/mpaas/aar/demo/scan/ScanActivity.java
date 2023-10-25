package com.mpaas.aar.demo.scan;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.alipay.android.phone.scancode.export.adapter.MPScan;
import com.mpaas.aar.demo.custom.MyScanActivity;

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
        // 如果未在 Application 中调用 mPaaS 初始化方法，需要在使用自定义扫码前调一次 MPScan.init
        MPScan.init(this);

        Intent intent = new Intent(ScanActivity.this, MyScanActivity.class);
        startActivity(intent);
    }
}
