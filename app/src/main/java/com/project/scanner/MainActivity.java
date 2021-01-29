package com.project.scanner;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.project.hmsscan.HmsScanner;
import com.project.hmsscan.activity.DefinedActivity;

import java.util.List;

import me.devilsen.czxing.Scanner;
import me.devilsen.czxing.code.BarcodeFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_SCAN_DEFAULT = 0X01;
    private static final int REQUEST_CODE_SCAN_CUSTOM = 0X02;

    private Button btnCzxing;
    private Button btnHmsDefault;
    private Button btnHmsCustom;
    private TextView tvScanResult;
    private ImageView ivQrcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        requestPermission();
    }

    private void scanCZXing() {
        Scanner.getInstance().startScan(this, new Scanner.ScanCallBack() {
            @Override
            public void onScanResult(String result, BarcodeFormat format) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvScanResult.setText("CZXing：" + result);
                    }
                });
            }
        });
    }

    private void scanHmsDefault() {
        HmsScanner.getInstance().startScanDefault(this, REQUEST_CODE_SCAN_DEFAULT);
    }

    private void scanHmsCustom() {
//        startActivityForResult(new Intent(this, DefinedActivity.class), REQUEST_CODE_SCAN_CUSTOM);

        HmsScanner.getInstance().startScan(this, new HmsScanner.HmsScanCallBack() {
            @Override
            public void onScanResult(String result) {
                tvScanResult.setText("HmsScanner：" + result);
                Bitmap qrCode = HmsScanner.getInstance().createQrCode(result, null);
                ivQrcode.setImageBitmap(qrCode);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_czxing:
                scanCZXing();
                break;
            case R.id.btn_hms_default:
                scanHmsDefault();
                break;
            case R.id.btn_hms_custom:
                scanHmsCustom();
                break;
            default:
                break;
        }
    }

    private void initView() {
        btnCzxing = findViewById(R.id.btn_czxing);
        btnHmsDefault = findViewById(R.id.btn_hms_default);
        btnHmsCustom = findViewById(R.id.btn_hms_custom);
        tvScanResult = findViewById(R.id.tv_scan_result);
        ivQrcode = findViewById(R.id.iv_code);
        //
        btnCzxing.setOnClickListener(this);
        btnHmsDefault.setOnClickListener(this);
        btnHmsCustom.setOnClickListener(this);
    }

    private void requestPermission() {
        PermissionX
                .init(this)
                .permissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {

                        } else {

                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        HmsScan hmsScan = null;
        String source = "";
        if (requestCode == REQUEST_CODE_SCAN_DEFAULT) {
            hmsScan = data.getParcelableExtra(ScanUtil.RESULT);
            source = "Default：";
        } else if (requestCode == REQUEST_CODE_SCAN_CUSTOM) {
            hmsScan = data.getParcelableExtra(DefinedActivity.SCAN_RESULT);
            source = "Custom：";
        }
        if (hmsScan != null && !TextUtils.isEmpty(hmsScan.getOriginalValue())) {
            final String value = hmsScan.getOriginalValue();
            tvScanResult.setText(source + value);
        } else {
            tvScanResult.setText("没有结果哟，点击按钮，开始扫码");
        }
    }

}