package com.project.hmsscan;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.hmsscankit.WriterException;
import com.huawei.hms.ml.scan.HmsBuildBitmapOption;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;

import com.project.hmsscan.manager.HmsScanConfig;
import com.project.hmsscan.manager.HmsScanDelegate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author : YDY
 * date : 2021/1/28
 * desc : 一维码/二维码 扫描、读取、生成管理类
 */
public class HmsScanner {

    private final String TAG = "HmsScanner";

    public static final int REQUEST_CODE_SCAN_DEFAULT = 0X01;
    public static final int REQUEST_CODE_SCAN_CUSTOM = 0X02;

    private HmsScanner() {
    }

    private static class InstanceHolder {
        private static final HmsScanner INSTANCE = new HmsScanner();
    }

    public static HmsScanner getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 扫码结果回调接口
     */
    public interface HmsScanCallBack {
        /**
         * 扫码结果回调
         *
         * @param result 扫码结果
         */
        void onScanResult(String result);
    }

    /**
     * 开始扫码：调用自定义界面
     * 注：使用前需要先获取相机和文件读写权限
     *
     * @param context  context
     * @param callBack 扫码结果回调
     */
    public void startScan(Context context, HmsScanCallBack callBack) {
        Resources resources = context.getResources();
        List<Integer> scanColors = Arrays.asList(
                resources.getColor(R.color.hms_scan_1),
                resources.getColor(R.color.hms_scan_2),
                resources.getColor(R.color.hms_scan_3));

        HmsScanConfig.with(context)
                .setBorderColor(resources.getColor(R.color.hms_scan_border))    // 扫码框边框颜色
                .setCornerColor(resources.getColor(R.color.hms_scan_corner))    // 扫码框角颜色
                .setScanLineColors(scanColors)                                  // 扫描线颜色（这是一个渐变颜色）
                .setScanMode(HmsScanDelegate.HMS_SCAN_MODE.AREA.getScanMode())  // 扫描模式（区域/全屏）
                .setTitle(resources.getString(R.string.hms_scan_title))         // 扫码界面标题
                .showAlbum(false)                                               // 是否显示相册(默认为false)
                .setOnScanResultDelegate(new HmsScanDelegate.OnScanResultDelegate() {   // 接管扫码成功的数据
                    @Override
                    public void onScanResult(Activity activity, HmsScan hmsScan) {
                        if (null != hmsScan) {
                            Log.i(TAG, "onScanResult: getOriginalValue=" + hmsScan.getOriginalValue());
                            Log.i(TAG, "onScanResult: getShowResult=" + hmsScan.getShowResult());
                            String result = hmsScan.getOriginalValue();
                            if (callBack != null) {
                                callBack.onScanResult(result);
                            }
                        }
                    }
                })
                .start();
    }

    /**
     * 开始扫码：调用hms默认扫码界面，结果在onActivityResult中返回
     * 注：使用前需要先获取相机和文件读写权限
     *
     * @param activity    activity
     * @param requestCode requestCode
     */
    public void startScanDefault(Activity activity, int requestCode) {
        HmsScanAnalyzerOptions options = new HmsScanAnalyzerOptions.Creator()
                .setHmsScanTypes(HmsScan.ALL_SCAN_TYPE)
                .create();
        ScanUtil.startScan(activity, requestCode, options);
    }

    /**
     * 读取码
     *
     * @param bitmap 码图片
     * @return 码信息
     */
    public String readCode(Context context, Bitmap bitmap) {
        try {
            HmsScan[] hmsScans = ScanUtil.decodeWithBitmap(context, bitmap, new HmsScanAnalyzerOptions.Creator().setPhotoMode(true).create());
            if (hmsScans != null && hmsScans.length > 0 && hmsScans[0] != null && !TextUtils.isEmpty(hmsScans[0].getOriginalValue())) {
                return hmsScans[0].getOriginalValue();
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 生成码
     *
     * @param content         码图信息
     * @param type            码图类型
     * @param width           码图宽度
     * @param height          码图高度
     * @param backgroundColor 码图背景色
     * @param color           码图颜色
     * @param margin          码图边框宽度
     * @param logo            码图logo
     */
    public Bitmap createCode(String content, int type, int width, int height, int backgroundColor, int color, int margin, Bitmap logo) {
        HmsBuildBitmapOption options = new HmsBuildBitmapOption.Creator()
                .setBitmapBackgroundColor(backgroundColor)  // 设置码图背景色，如果不调用，默认背景色为白色（Color.WHITE）
                .setBitmapColor(color)                      // 设置码图颜色，如果不调用，默认码图颜色为黑色（Color.BLACK）
                .setBitmapMargin(margin)                    // 设置码图边框宽度，如果不调用，默认码图边框宽度为1
                .setQRLogoBitmap(logo)                      // 设置码图logo
                .create();
        return createCode(content, type, width, height, options);
    }

    private Bitmap createCode(String content, int type, int width, int height, HmsBuildBitmapOption options) {
        try {
            return ScanUtil.buildBitmap(content, type, width, height, options);
        } catch (WriterException e) {
            return null;
        }
    }

    /* 码类型：
     * FORMAT_UNKNOWN(-1),
     * ALL_SCAN_TYPE(0),
     * QRCODE_SCAN_TYPE(0),
     * AZTEC_SCAN_TYPE(1),
     * DATAMATRIX_SCAN_TYPE(2),
     * PDF417_SCAN_TYPE(3),
     * CODE39_SCAN_TYPE(4),
     * CODE93_SCAN_TYPE(5),
     * CODE128_SCAN_TYPE(6),
     * EAN13_SCAN_TYPE(7),
     * EAN8_SCAN_TYPE(8),
     * ITF14_SCAN_TYPE(9),
     * UPCCODE_A_SCAN_TYPE(10),
     * UPCCODE_E_SCAN_TYPE(11),
     * CODABAR_SCAN_TYPE(12);
     */

    /**
     * 生成二维码
     *
     * @param content 内容
     * @param logo    logo图片
     * @return 二维码
     */
    public Bitmap createQrCode(String content, Bitmap logo) {
        int type = HmsScan.QRCODE_SCAN_TYPE;
        int width = 400;
        int height = 400;
        HmsBuildBitmapOption options = new HmsBuildBitmapOption.Creator()
                .setBitmapBackgroundColor(Color.WHITE)
                .setBitmapColor(Color.BLACK)
                .setBitmapMargin(2)
                .setQRLogoBitmap(logo)
                .create();
        return createCode(content, type, width, height, options);
    }

    private void deal(final HmsScan hmsScan) {
        String codeFormat = "";
        if (hmsScan.getScanType() == HmsScan.QRCODE_SCAN_TYPE) {
            codeFormat = "QR code";
        } else if (hmsScan.getScanType() == HmsScan.AZTEC_SCAN_TYPE) {
            codeFormat = "AZTEC code";
        } else if (hmsScan.getScanType() == HmsScan.DATAMATRIX_SCAN_TYPE) {
            codeFormat = "DATAMATRIX code";
        } else if (hmsScan.getScanType() == HmsScan.PDF417_SCAN_TYPE) {
            codeFormat = "PDF417 code";
        } else if (hmsScan.getScanType() == HmsScan.CODE93_SCAN_TYPE) {
            codeFormat = "CODE93";
        } else if (hmsScan.getScanType() == HmsScan.CODE39_SCAN_TYPE) {
            codeFormat = "CODE39";
        } else if (hmsScan.getScanType() == HmsScan.CODE128_SCAN_TYPE) {
            codeFormat = "CODE128";
        } else if (hmsScan.getScanType() == HmsScan.EAN13_SCAN_TYPE) {
            codeFormat = "EAN13 code";
        } else if (hmsScan.getScanType() == HmsScan.EAN8_SCAN_TYPE) {
            codeFormat = "EAN8 code";
        } else if (hmsScan.getScanType() == HmsScan.ITF14_SCAN_TYPE) {
            codeFormat = "ITF14 code";
        } else if (hmsScan.getScanType() == HmsScan.UPCCODE_A_SCAN_TYPE) {
            codeFormat = "UPCCODE_A";
        } else if (hmsScan.getScanType() == HmsScan.UPCCODE_E_SCAN_TYPE) {
            codeFormat = "UPCCODE_E";
        } else if (hmsScan.getScanType() == HmsScan.CODABAR_SCAN_TYPE) {
            codeFormat = "CODABAR";
        }

        String resultType = "";
        if (hmsScan.getScanType() == HmsScan.QRCODE_SCAN_TYPE) {
            if (hmsScan.getScanTypeForm() == HmsScan.PURE_TEXT_FORM) {
                resultType = "Text";
            } else if (hmsScan.getScanTypeForm() == HmsScan.EVENT_INFO_FORM) {
                resultType = "Event";
            } else if (hmsScan.getScanTypeForm() == HmsScan.CONTACT_DETAIL_FORM) {
                resultType = "Contact";
            } else if (hmsScan.getScanTypeForm() == HmsScan.DRIVER_INFO_FORM) {
                resultType = "License";
            } else if (hmsScan.getScanTypeForm() == HmsScan.EMAIL_CONTENT_FORM) {
                resultType = "Email";
            } else if (hmsScan.getScanTypeForm() == HmsScan.LOCATION_COORDINATE_FORM) {
                resultType = "Location";
            } else if (hmsScan.getScanTypeForm() == HmsScan.TEL_PHONE_NUMBER_FORM) {
                resultType = "Tel";
            } else if (hmsScan.getScanTypeForm() == HmsScan.SMS_FORM) {
                resultType = "SMS";
            } else if (hmsScan.getScanTypeForm() == HmsScan.WIFI_CONNECT_INFO_FORM) {
                resultType = "Wi-Fi";
            } else if (hmsScan.getScanTypeForm() == HmsScan.URL_FORM) {
                resultType = "WebSite";
            } else {
                resultType = "Text";
            }
        } else if (hmsScan.getScanType() == HmsScan.EAN13_SCAN_TYPE) {
            if (hmsScan.getScanTypeForm() == HmsScan.ISBN_NUMBER_FORM) {
                resultType = "ISBN";
            } else if (hmsScan.getScanTypeForm() == HmsScan.ARTICLE_NUMBER_FORM) {
                resultType = "Product";
            }
        } else if (hmsScan.getScanType() == HmsScan.EAN8_SCAN_TYPE || hmsScan.getScanType() == HmsScan.UPCCODE_A_SCAN_TYPE || hmsScan.getScanType() == HmsScan.UPCCODE_E_SCAN_TYPE) {
            if (hmsScan.getScanTypeForm() == HmsScan.ARTICLE_NUMBER_FORM) {
                resultType = "Product";
            }
        }
    }

    public PointF getCornerPointsCenter(HmsScan hmsScan) {
        Point[] points = hmsScan.getCornerPoints();
        Log.i(TAG, "onScanResult: getCornerPoints=" + Arrays.toString(points));
        // [Point(517, 777), Point(358, 703), Point(432, 545), Point(592, 617)]
        if (null != points && points.length > 0) {
            List<Integer> xList = new ArrayList<>();
            List<Integer> yList = new ArrayList<>();
            for (int i = 0; i < points.length; i++) {
                Point point = points[i];
                xList.add(point.x);
                yList.add(point.y);
            }
            int minX = Collections.min(xList);
            int maxX = Collections.max(xList);
            int minY = Collections.min(yList);
            int maxY = Collections.max(yList);

            float centerX = (minX + maxX) / 2f;
            float centerY = (minY + maxY) / 2f;
            Log.i(TAG, "getCenter: centerX = " + centerX + "centerY = " + centerY);

            return new PointF(centerX, centerY);
        }
        return null;
    }
}