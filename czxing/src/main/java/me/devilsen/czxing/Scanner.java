package me.devilsen.czxing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.devilsen.czxing.code.BarcodeFormat;
import me.devilsen.czxing.code.BarcodeReader;
import me.devilsen.czxing.code.BarcodeWriter;
import me.devilsen.czxing.code.CodeResult;
import me.devilsen.czxing.util.BarCodeUtil;
import me.devilsen.czxing.view.ScanActivityDelegate;
import me.devilsen.czxing.view.ScanView;

/**
 * @author : YDY
 * date : 2019/11/11
 * desc : 二维码/条码扫描、读取、生成管理类
 * 注：二维码/条码读取、生成都是耗时操作，务必在子线程中执行
 */
public class Scanner {

    private Scanner() {
    }

    private static class InstanceHolder {
        private static Scanner INSTANCE = new Scanner();
    }

    public static Scanner getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final int CODE_SELECT_IMAGE = 1;
    private final String TAG = "Scanner";

    /**
     * 读取二维码/条码信息
     * @param bitmap 二维码/条码图片
     * @return 信息
     */
    public String readCode(Bitmap bitmap) {
        if (bitmap != null) {
            CodeResult result = BarcodeReader.getInstance().read(bitmap);
            if (result != null && !TextUtils.isEmpty(result.getText())) {
                return result.getText();
            }
        }
        return null;
    }

    /**
     * 生成二维码
     * @param context    context
     * @param content    内容
     * @param logoBitmap logo图片
     * @return 二维码
     */
    public Bitmap createQrCode(Context context, String content, Bitmap logoBitmap) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        BarcodeWriter writer = new BarcodeWriter();
        Bitmap bitmap;
        if (logoBitmap == null) {
            bitmap = writer.write(content, BarCodeUtil.dp2px(context, 150), Color.BLACK);
        } else {
            bitmap = writer.write(content, BarCodeUtil.dp2px(context, 150), Color.BLACK, logoBitmap);
        }
        return bitmap;
    }

    /**
     * 生成条码
     * @param context context
     * @param content 内容
     * @return 条码
     */
    public Bitmap createBarCode(Context context, String content) {
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        BarcodeWriter writer = new BarcodeWriter();
        return writer.writeBarCode(content,
                BarCodeUtil.dp2px(context, 150),
                BarCodeUtil.dp2px(context, 80),
                Color.BLACK);
    }

    /**
     * 扫描二维码/条码
     * @param context  context
     * @param callBack 扫描结果回调
     */
    public void startScan(Context context, ScanCallBack callBack) {
        Resources resources = context.getResources();
        List<Integer> scanColors = Arrays.asList(
                resources.getColor(R.color.czxing_scan_1),
                resources.getColor(R.color.czxing_scan_2),
                resources.getColor(R.color.czxing_scan_3));

        ScannerManager
                .with(context)
                .setBorderColor(resources.getColor(R.color.czxing_scan_border)) // 扫码框边框颜色
                .setCornerColor(resources.getColor(R.color.czxing_scan_corner)) // 扫码框角颜色
                .setScanLineColors(scanColors)                                  // 扫描线颜色（这是一个渐变颜色）
                .setScanMode(ScanView.SCAN_MODE_BIG)                            // 扫描区域
                .setTitle(resources.getString(R.string.czxing_scan_title))      // 扫码界面标题
                /*.showAlbum(false)*/                                           // 是否显示相册(默认为false)
                /*.setOnClickAlbumDelegate(new ScanActivityDelegate.OnClickAlbumDelegate() {
                    @Override
                    public void onClickAlbum(Activity activity) {
                        // 点击“相册”按钮触发
                        Intent albumIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        activity.startActivityForResult(albumIntent, CODE_SELECT_IMAGE);
                    }

                    @Override
                    public void onSelectData(int requestCode, Intent data) {
                        // 从相册所选择图片返回的数据
                        if (requestCode == CODE_SELECT_IMAGE) {
                            Bitmap bitmap = selectPic(context, data);
                            if (bitmap != null) {
                                CodeResult result = BarcodeReader.getInstance().read(bitmap);
                                if (result != null && !TextUtils.isEmpty(result.getText())) {
                                    if (callBack != null) {
                                        callBack.onScanResult(dealResult(result.getText()), result.getFormat());
                                    }
                                }
                            }
                        }
                    }
                })*/
                .setOnScanResultDelegate(new ScanActivityDelegate.OnScanDelegate() { // 接管扫码成功的数据
                    @Override
                    public void onScanResult(Activity activity, String result, BarcodeFormat format) {
                        // 有回调，则必然有值；对于旧二维码，内容有中文的result可能包含乱码，需要进行处理
                        Log.i(TAG, "onScanResult: result=" + result);
                        String dealResult = dealResult(result);
                        if (callBack != null) {
                            callBack.onScanResult(dealResult, format);
                        }
                    }
                })
                .start();
    }

    /**
     * 从相册选择二维码/条码图片
     */
    private Bitmap selectPic(Context context, Intent intent) {
        Uri selectImageUri = intent.getData();
        if (selectImageUri == null) {
            return null;
        }
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(selectImageUri, filePathColumn, null, null, null);
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        return BitmapFactory.decodeFile(picturePath);
    }

    public interface ScanCallBack {
        /**
         * 扫描结果回调
         * @param result 扫码内容
         * @param format 扫码类型
         */
        void onScanResult(String result, BarcodeFormat format);
    }

    /**
     * 扫描结果编码处理
     */
    private String dealResult(String strResult) {
        try {
            String encode = getEncoding(strResult);
//            Log.i(TAG, "dealResult: encode=" + encode);
            if (!TextUtils.isEmpty(encode)) {
                String encodeResult = new String(strResult.getBytes(encode), "GB2312");
//                Log.i(TAG, "dealResult: encodeResult=" + encodeResult);
                return encodeResult;
            }
        } catch (UnsupportedEncodingException e) {
            //
        }
        return strResult;
    }

    /**
     * 判断字符串的编码
     */
    private String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                return encode;
            }
        } catch (Exception exception) {
            //
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                return encode;
            }
        } catch (Exception exception1) {
            //
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                return encode;
            }
        } catch (Exception exception2) {
            //
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                return encode;
            }
        } catch (Exception exception3) {
            //
        }
        return "";
    }

    /**
     * 判断字符串是否是乱码
     *
     * @param strName 字符串
     * @return 是否是乱码
     */
    private boolean isMessyCode(String strName) {
        try {
            Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
            Matcher m = p.matcher(strName);
            String after = m.replaceAll("");
            String temp = after.replaceAll("\\p{P}", "");
            char[] ch = temp.trim().toCharArray();
            int length = (null != ch) ? ch.length : 0;
            for (int i = 0; i < length; i++) {
                char c = ch[i];
                if (!Character.isLetterOrDigit(c)) {
                    String str = "" + ch[i];
                    if (!str.matches("[\u4e00-\u9fa5]")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}