package me.devilsen.czxing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.devilsen.czxing.code.BarcodeFormat;
import me.devilsen.czxing.code.BarcodeReader;
import me.devilsen.czxing.util.BarCodeUtil;
import me.devilsen.czxing.util.ScreenUtil;
import me.devilsen.czxing.util.SoundPoolUtil;
import me.devilsen.czxing.view.ScanActivityDelegate;
import me.devilsen.czxing.view.ScanBoxView;
import me.devilsen.czxing.view.ScanListener;
import me.devilsen.czxing.view.ScanView;

/**
 * desc : 扫码页面
 * date : 2019-06-25
 *
 * @author : dongSen
 */
public class ScanActivity extends AppCompatActivity implements ScanListener, View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int MESSAGE_WHAT_START_SCAN = 10;
    private static final long DELAY_TIME = 800;

    private TextView mTvScanTitle;
    private TextView mTvScanAlbum;
    private ScanView mScanView;
    private SoundPoolUtil mSoundPoolUtil;

    private boolean isContinuousScan;

    private boolean mSaveInstanceStateFlag;
    private ScanActivityDelegate.OnScanDelegate scanDelegate;
    private ScanActivityDelegate.OnClickAlbumDelegate clickAlbumDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        BarCodeUtil.setDebug(BuildConfig.DEBUG);
        ScreenUtil.setFullScreen(this);

        initView();

        scanDelegate = ScanActivityDelegate.getInstance().getScanDelegate();
        clickAlbumDelegate = ScanActivityDelegate.getInstance().getOnClickAlbumDelegate();

        mSoundPoolUtil = new SoundPoolUtil();
        mSoundPoolUtil.loadDefault(this);

        initData();
    }

    private void initView() {
        RelativeLayout titleLayout = findViewById(R.id.layout_scan_title);
        LinearLayout llScanBack = findViewById(R.id.ll_scan_back);
        mTvScanTitle = findViewById(R.id.tv_scan_title);
        mTvScanAlbum = findViewById(R.id.tv_scan_album);
        mScanView = findViewById(R.id.scan_view);

        llScanBack.setOnClickListener(this);
        mScanView.setScanListener(this);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) titleLayout.getLayoutParams();
        layoutParams.topMargin = ScreenUtil.getStatusBarHeight(this);
    }

    private void initData() {
        ScannerManager.ScanOption option = getIntent().getParcelableExtra("option");
        if (option == null) {
            return;
        }
        mScanView.setScanMode(option.getScanMode());
        mScanView.setBarcodeFormat(option.getBarcodeFormat());
        mScanView.onFlashLightClick();

        ScanBoxView scanBox = mScanView.getScanBox();
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
        BarcodeReader.getInstance().enableCVDetect(option.enableOpenCVDetect);

        // 标题
        String title = option.getTitle();
        if (title != null && !TextUtils.isEmpty(title)) {
            mTvScanTitle.setText(title);
        }
        // 是否显示相册
        if (option.isShowAlbum()) {
            mTvScanAlbum.setVisibility(View.VISIBLE);
            mTvScanAlbum.setOnClickListener(this);
        } else {
            mTvScanAlbum.setVisibility(View.INVISIBLE);
            mTvScanAlbum.setOnClickListener(null);
        }
        // 连续扫描
        isContinuousScan = option.isContinuousScan();
    }

    private void openCameraAndStartScan() {
        mScanView.openCamera(); // 打开后置摄像头开始预览，但是并未开始识别
        mScanView.startScan();  // 显示扫描框，并开始识别
        if (isContinuousScan) {
            mScanView.resetZoom();// 重置相机扩大倍数
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSaveInstanceStateFlag = false;
        requestPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScanView.stopScan();
        mScanView.closeCamera(); // 关闭摄像头预览，并且隐藏扫描框
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSaveInstanceStateFlag = true;
    }

    @Override
    protected void onDestroy() {
        mScanView.onDestroy(); // 销毁二维码扫描控件
        mSoundPoolUtil.release();
        super.onDestroy();
        /*注意：这里横竖屏切换需要注意生命周期!!!*/
        // 可能是旋转窗口，不移除代理
        if (mSaveInstanceStateFlag) {
            return;
        }
        ScanActivityDelegate.getInstance().setScanResultDelegate(null);
        ScanActivityDelegate.getInstance().setOnClickAlbumDelegate(null);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll_scan_back) {
            finish();
        } else if (id == R.id.tv_scan_album) {
            if (clickAlbumDelegate != null) {
                clickAlbumDelegate.onClickAlbum(this);
            }
        }
    }

    @Override
    public void onScanSuccess(String result, BarcodeFormat format) {
        mSoundPoolUtil.play();

        if (scanDelegate != null) {
            scanDelegate.onScanResult(this, result, format);
        }

        // 连续扫码，不关闭界面
        if (isContinuousScan) {
            handler.sendEmptyMessageDelayed(MESSAGE_WHAT_START_SCAN, DELAY_TIME);
            return;
        }
        finish();
    }

    @Override
    public void onOpenCameraError() {
        Log.e("onOpenCameraError", "onOpenCameraError");
        // 对于一些不可知的问题，比如在一些手机上多次重复打开会出现打开错误，只能用这个方法来规避这个问题
        // 在相机打开失败的情况下，停留在此页面也没有意义
        finish();
    }

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            mScanView.startScan();
            return true;
        }
    });

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
        } else {
            openCameraAndStartScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraAndStartScan();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && clickAlbumDelegate != null) {
            clickAlbumDelegate.onSelectData(requestCode, data);
            finish();
        }
    }
}