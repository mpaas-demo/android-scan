package com.mpaas.aar.demo.custom.widget;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alipay.android.phone.scancode.export.adapter.MPScanError;
import com.alipay.android.phone.scancode.export.adapter.MPScanResult;
import com.alipay.mobile.scansdk.ui2.MPCustomScanView;
import com.mpaas.aar.demo.custom.R;
import com.mpaas.aar.demo.custom.util.DialogUtil;

import java.util.ArrayList;
import java.util.List;

public class MyScanView extends MPCustomScanView implements View.OnClickListener {

    private static final String TAG = "MyScanView";

    private MyRayView rayView;
    private View torchView;
    private ImageView ivTorch;
    private TextView tvTorchDesc;
    private boolean isTorchOn;
    private final List<MyIconView> iconViews = new ArrayList<>();
    private Callback callback;

    public MyScanView(Context context) {
        super(context);
        inflate(context);
        setOnTouchListener(this);
    }

    private void inflate(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_my_scan, this, true);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_gallery).setOnClickListener(this);
        rayView = findViewById(R.id.ray);
        torchView = findViewById(R.id.ll_torch);
        torchView.setOnClickListener(this);
        ivTorch = findViewById(R.id.iv_torch);
        tvTorchDesc = findViewById(R.id.tv_torch_desc);
    }

    public interface Callback {

        void onBackClicked(View view);

        void onGalleryClicked(View view);

        void onMaIconClicked(View view, MPScanResult mpScanResult);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onCameraOpenFailed() {
        rayView.stopAnimation();
        DialogUtil.alert((Activity) getContext(), "无法打开相机");
    }

    @Override
    public void onStartScan() {
        rayView.startAnimation();
    }

    @Override
    public void onPreviewShow() {
        rayView.startAnimation();
    }

    @Override
    public void onStopScan() {
        rayView.stopAnimation();
    }

    @Override
    public void onGetAvgGray(int gray) {
        if (gray < 50) { // 太暗
            if (!isTorchVisible()) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        updateTorchView();
                        torchView.setVisibility(VISIBLE);
                    }
                });
            }
        } else if (gray > 90) { // 太亮
            if (isTorchVisible() && !isTorchOn) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        updateTorchView();
                        torchView.setVisibility(INVISIBLE);
                    }
                });
            }
        }
    }

    @Override
    public void onScanFinished(Context context, List<MPScanResult> list) {
        for (final MPScanResult result : list) {
            final MyIconView iconView = new MyIconView(context, this, result);
            addView(iconView, iconView.getLayoutParams());
            iconViews.add(iconView);
            iconView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    iconView.setSelected(true);
                    for (MyIconView view : iconViews) {
                        if (view.isSelected()) {
                            continue;
                        }
                        view.setVisibility(GONE);
                    }
                    if (callback != null) {
                        callback.onMaIconClicked(iconView, iconView.getResult());
                    }
                }
            });
        }
        post(new Runnable() {
            @Override
            public void run() {
                for (MyIconView view : iconViews) {
                    view.show();
                }
            }
        });
    }

    @Override
    public void onScanFailed(Context context, MPScanError mpScanError) {
        DialogUtil.alert((Activity) context, mpScanError.getMsg());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            if (callback != null) {
                callback.onBackClicked(v);
            }
        } else if (id == R.id.iv_gallery) {
            if (callback != null) {
                callback.onGalleryClicked(v);
            }
        } else if (id == R.id.ll_torch) {
            onTorchViewClicked();
        }
    }

    private void onTorchViewClicked() {
        isTorchOn = switchTorch();
        updateTorchView();
        if (!isTorchOn) {
            torchView.setVisibility(INVISIBLE);
        }
    }

    private void updateTorchView() {
        if (isTorchOn) {
            ivTorch.setImageResource(R.drawable.my_torch_on);
            tvTorchDesc.setText("轻触关闭");
        } else {
            ivTorch.setImageResource(R.drawable.my_torch_off);
            tvTorchDesc.setText("轻触照亮");
        }
    }

    private boolean isTorchVisible() {
        return torchView != null && torchView.getVisibility() == VISIBLE;
    }

    public void removeAllIconViews() {
        if (iconViews.size() > 0) {
            for (MyIconView view : iconViews) {
                removeView(view);
            }
            iconViews.clear();
        }
    }
}
