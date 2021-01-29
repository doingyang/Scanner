package com.project.hmsscan.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huawei.hms.hmsscankit.OnLightVisibleCallBack;
import com.huawei.hms.hmsscankit.OnResultCallback;
import com.huawei.hms.hmsscankit.RemoteView;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.project.hmsscan.manager.HmsScanConfig;
import com.project.hmsscan.R;
import com.project.hmsscan.util.ScreenUtil;
import com.project.hmsscan.util.SoundPoolUtil;
import com.project.hmsscan.manager.HmsScanDelegate;
import com.project.hmsscan.view.ScanBoxView;

import java.io.IOException;

public class HmsScanActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HmsScanActivity";

    public static final String SCAN_RESULT = "scanResult";
    private static final int REQUEST_CODE_PHOTO = 0X1113;

    private FrameLayout frameLayout;
    private ScanBoxView scanBox;
    private ImageView ivBack;
    private TextView tvTitle;
    private ImageView ivAlbum;
    private ImageView ivFlashLight;
    private RemoteView remoteView;
    private SoundPoolUtil mSoundPoolUtil;

    // The width and height of scan area is both 300 dp.
    private final int SCAN_FRAME_SIZE = 300;

    private final int[] imgFlashLight = {R.drawable.scankit_flashlight_on, R.drawable.scankit_flashlight_off};

    private int scanMode = HmsScanDelegate.HMS_SCAN_MODE.FULL.getScanMode();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hms_scan);
        ScreenUtil.setFullScreen(this);

        initView();
        initData();
        initSound();
        initRemoteView(savedInstanceState);
    }

    private void initRemoteView(Bundle savedInstanceState) {
        // Initialize the RemoteView instance, and set callback for the scanning result.
        remoteView = new RemoteView.Builder().setContext(this)
                .setBoundingBox(createBoundingBox())
                .setFormat(HmsScan.ALL_SCAN_TYPE)
                .build();
        // Subscribe to the scanning result callback event.
        remoteView.setOnResultCallback(new OnResultCallback() {
            @Override
            public void onResult(HmsScan[] result) {
                if (result != null && result.length > 0 && result[0] != null && !TextUtils.isEmpty(result[0].getOriginalValue())) {
                    mSoundPoolUtil.play();
                    // 回调结果
                    dealCallBackResult(result[0]);
                }
            }
        });
        // When the light is dim, this API is called back to display the flashlight switch.
        remoteView.setOnLightVisibleCallback(new OnLightVisibleCallBack() {
            @Override
            public void onVisibleChanged(boolean visible) {
                if (visible) {
                    ivFlashLight.setVisibility(View.VISIBLE);
                }
            }
        });
        // Load the customized view to the activity.
        remoteView.onCreate(savedInstanceState);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        frameLayout.addView(remoteView, params);
    }

    /**
     * 创建扫描区域
     */
    private Rect createBoundingBox() {
        try {
            //1. Obtain the screen density to calculate the viewfinder's rectangle.
            DisplayMetrics dm = getResources().getDisplayMetrics();
            float density = dm.density;
            //2. Obtain the screen size.
            int mScreenWidth = getResources().getDisplayMetrics().widthPixels;
            int mScreenHeight = getResources().getDisplayMetrics().heightPixels;

            int scanFrameSize = (int) (SCAN_FRAME_SIZE * density);

            //3. Calculate the viewfinder's rectangle, which in the middle of the layout.
            //Set the scanning area. (Optional. Rect can be null. If no settings are specified, it will be located in the middle of the layout.)
            Rect rect = new Rect();
            rect.left = mScreenWidth / 2 - scanFrameSize / 2;
            rect.right = mScreenWidth / 2 + scanFrameSize / 2;
            rect.top = mScreenHeight / 2 - scanFrameSize / 2;
            rect.bottom = mScreenHeight / 2 + scanFrameSize / 2;

            if (scanMode == HmsScanDelegate.HMS_SCAN_MODE.AREA.getScanMode()) {
                return rect;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * startActivityForResult方式返回结果
     */
    private void dealOnActivityResult(HmsScan hmsScan) {
        Intent intent = new Intent();
        intent.putExtra(SCAN_RESULT, hmsScan);
        setResult(RESULT_OK, intent);
        HmsScanActivity.this.finish();
    }

    /**
     * CallBack方式返回结果
     */
    private void dealCallBackResult(HmsScan hmsScan) {
        HmsScanDelegate.OnScanResultDelegate resultDelegate = HmsScanDelegate.getInstance().getScanResultDelegate();
        if (null != resultDelegate) {
            resultDelegate.onScanResult(this, hmsScan);
        }
        HmsScanActivity.this.finish();
    }

    /**
     * Handle the return results from the album.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                HmsScan[] hmsScans = ScanUtil.decodeWithBitmap(HmsScanActivity.this, bitmap, new HmsScanAnalyzerOptions.Creator().setPhotoMode(true).create());
                if (hmsScans != null && hmsScans.length > 0 && hmsScans[0] != null && !TextUtils.isEmpty(hmsScans[0].getOriginalValue())) {
                    // 回调结果
                    dealCallBackResult(hmsScans[0]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            operationBack();
        } else if (v.getId() == R.id.iv_album) {
            operationAlbum();
        } else if (v.getId() == R.id.iv_flashlight) {
            operationFlashLight();
        }
    }

    private void operationBack() {
        HmsScanActivity.this.finish();
    }

    private void operationAlbum() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        HmsScanActivity.this.startActivityForResult(pickIntent, REQUEST_CODE_PHOTO);
    }

    private void operationFlashLight() {
        if (remoteView.getLightStatus()) {
            remoteView.switchLight();
            ivFlashLight.setImageResource(imgFlashLight[1]);
        } else {
            remoteView.switchLight();
            ivFlashLight.setImageResource(imgFlashLight[0]);
        }
    }

    private void initSound() {
        mSoundPoolUtil = new SoundPoolUtil();
        mSoundPoolUtil.loadDefault(this);
    }

    private void initData() {
        HmsScanConfig.ScanOption option = getIntent().getParcelableExtra("option");
        if (option == null) {
            return;
        }
        scanMode = option.getScanMode();

        scanBox.setMaskColor(option.getMaskColor());
        scanBox.setCornerColor(option.getCornerColor());
        scanBox.setBorderColor(option.getBorderColor());
        scanBox.setBorderSize(option.getBorderSize());
        scanBox.setBorderSize(option.getBorderWidth(), option.getBorderHeight());
        scanBox.setScanLineColor(option.getScanLineColors());
        if (option.isScanHorizontal()) {
            scanBox.setHorizontalScanLine();
        }
        scanBox.setFlashLightOnDrawable(option.getFlashLightOnDrawable());
        scanBox.setFlashLightOffDrawable(option.getFlashLightOffDrawable());
        scanBox.setFlashLightOnText(option.getFlashLightOnText());
        scanBox.setFlashLightOffText(option.getFlashLightOffText());
        if (option.isDropFlashLight()) {
            scanBox.invisibleFlashLightIcon();
        }
        scanBox.setScanNoticeText(option.getScanNoticeText());

        // 标题
        String title = option.getTitle();
        if (title != null && !TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
        // 是否显示相册
        if (option.isShowAlbum()) {
            ivAlbum.setVisibility(View.VISIBLE);
        } else {
            ivAlbum.setVisibility(View.GONE);
        }
    }

    private void initView() {
        frameLayout = findViewById(R.id.rim);
        scanBox = findViewById(R.id.sbv);
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);
        ivAlbum = findViewById(R.id.iv_album);
        ivFlashLight = findViewById(R.id.iv_flashlight);
        //
        ivBack.setOnClickListener(this);
        ivAlbum.setOnClickListener(this);
        ivFlashLight.setOnClickListener(this);
        //
        RelativeLayout titleLayout = findViewById(R.id.layout_scan_title);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) titleLayout.getLayoutParams();
        layoutParams.topMargin = ScreenUtil.getStatusBarHeight(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        remoteView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        remoteView.onResume();
    }

    @Override
    protected void onPause() {
        remoteView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        remoteView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        remoteView.onDestroy();
        mSoundPoolUtil.release();
        super.onDestroy();
    }
}