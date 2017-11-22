package com.tyc129.nfcmap;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import java.util.List;

/**
 * Created by Code on 2017/10/29 0029.
 *
 * @author 谈永成
 * @version 1.0
 */
public class ScanQRActivity extends AppCompatActivity
        implements QRCodeView.Delegate, EasyPermissions.PermissionCallbacks {
    @BindView(R.id.view_qr)
    QRCodeView qrCodeView;

    static final String NAME_INTENT_EXTRA = "Result";
    private static final long DURATION_VIBRATION = 200;
    private static final String LOG_TAG = ScanQRActivity.class.getSimpleName();
    private static final int REQUEST_CODE_QR_CODE_PERMISSIONS = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);
        ButterKnife.bind(this);
        if (qrCodeView != null)
            qrCodeView.setDelegate(this);
        requestCodeQRCodePermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();
            stopScanner();
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideScanner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qrCodeView != null)
            qrCodeView.onDestroy();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void showScanner() {
        if (qrCodeView != null) {
            qrCodeView.startCamera();
            qrCodeView.showScanRect();
        }
    }

    private void runScanner() {
        if (qrCodeView != null)
            qrCodeView.startSpot();
    }

    private void stopScanner() {
        if (qrCodeView != null)
            qrCodeView.stopSpot();
    }

    private void hideScanner() {
        if (qrCodeView != null) {
            qrCodeView.stopCamera();
            qrCodeView.hiddenScanRect();
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(DURATION_VIBRATION);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.v(LOG_TAG, "scan result--->" + result);
        vibrate();
        setResult(RESULT_OK,
                new Intent()
                        .putExtra(NAME_INTENT_EXTRA, result));
        finish();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(LOG_TAG, "onScanQRCodeOpenCameraError");
    }

    @AfterPermissionGranted(REQUEST_CODE_QR_CODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        Log.v(LOG_TAG, "permissions");
        String[] perms = {Manifest.permission.CAMERA};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要相机和闪光灯权限",
                    REQUEST_CODE_QR_CODE_PERMISSIONS, perms);
        } else {
            Log.v(LOG_TAG, "init scanner");
            showScanner();
            runScanner();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.v(LOG_TAG, "permissions granted");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.v(LOG_TAG, "permissions denied");
        finish();
    }
}
