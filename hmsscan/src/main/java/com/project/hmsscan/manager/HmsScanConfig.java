package com.project.hmsscan.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.project.hmsscan.activity.HmsScanActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫码UI配置
 */
public class HmsScanConfig {

    private Context context;
    private ScanOption scanOption;

    HmsScanConfig(Context context) {
        this.context = context;
        scanOption = new ScanOption();
    }

    public static HmsScanConfig with(Context context) {
        return new HmsScanConfig(context);
    }

    /**
     * 扫码框角颜色
     *
     * @param cornerColor 色值
     */
    public HmsScanConfig setCornerColor(int cornerColor) {
        scanOption.cornerColor = cornerColor;
        return this;
    }

    /**
     * 设置扫码框颜色
     *
     * @param borderColor 色值
     */
    public HmsScanConfig setBorderColor(int borderColor) {
        scanOption.borderColor = borderColor;
        return this;
    }

    /**
     * 设置扫码框四周颜色
     *
     * @param maskColor 色值
     */
    public HmsScanConfig setMaskColor(int maskColor) {
        scanOption.maskColor = maskColor;
        return this;
    }

    /**
     * 设置边框长度(扫码框大小,如果两个setBorderSize方法都调用了，优先使用正方形)
     *
     * @param borderSize px
     */
    public HmsScanConfig setBorderSize(int borderSize) {
        if (borderSize <= 0) {
            throw new IllegalArgumentException("scan box size must > 0");
        }
        scanOption.borderSize = borderSize;
        return this;
    }

    /**
     * 设置边框长度(扫码框大小)
     *
     * @param borderWidth  px
     * @param borderHeight px
     */
    public HmsScanConfig setBorderSize(int borderWidth, int borderHeight) {
        if (borderWidth <= 0 || borderHeight <= 0) {
            throw new IllegalArgumentException("scan box width or height must > 0");
        }
        scanOption.borderWidth = borderWidth;
        scanOption.borderHeight = borderHeight;
        return this;
    }

    /**
     * 扫描模式
     * 1：只扫描扫码框里的内容
     * 2：扫描以屏幕宽为边长的内容
     *
     * @param scanMode
     */
    public HmsScanConfig setScanMode(int scanMode) {
        scanOption.scanMode = scanMode;
        return this;
    }

    /**
     * 设置扫码线移动方向为水平（从左往右）
     */
    public HmsScanConfig setHorizontalScanLine() {
        scanOption.scanHorizontal = true;
        return this;
    }

    /**
     * 设置扫码界面标题
     *
     * @param title 标题内容
     */
    public HmsScanConfig setTitle(String title) {
        scanOption.title = title;
        return this;
    }

    /**
     * 显示从图库选取图片按钮
     *
     * @param showAlbum true：显示   false：隐藏
     */
    public HmsScanConfig showAlbum(boolean showAlbum) {
        scanOption.showAlbum = showAlbum;
        return this;
    }

    /**
     * 连续扫码，当扫描出结果后不关闭扫码界面
     */
    public HmsScanConfig continuousScan() {
        scanOption.continuousScan = true;
        return this;
    }

    /**
     * 设置闪光灯打开时的样式
     *
     * @param flashLightOnDrawable drawable
     */
    public HmsScanConfig setFlashLightOnDrawable(int flashLightOnDrawable) {
        scanOption.flashLightOnDrawable = flashLightOnDrawable;
        return this;
    }

    /**
     * 设置闪光灯关闭时的样式
     *
     * @param flashLightOffDrawable drawable
     */
    public HmsScanConfig setFlashLightOffDrawable(int flashLightOffDrawable) {
        scanOption.flashLightOffDrawable = flashLightOffDrawable;
        return this;
    }

    /**
     * 设置闪光灯打开时的提示文字
     *
     * @param lightOnText 提示文字
     */
    public HmsScanConfig setFlashLightOnText(String lightOnText) {
        scanOption.flashLightOnText = lightOnText;
        return this;
    }

    /**
     * 设置闪光灯关闭时的提示文字
     *
     * @param lightOffText 提示文字
     */
    public HmsScanConfig setFlashLightOffText(String lightOffText) {
        scanOption.flashLightOffText = lightOffText;
        return this;
    }

    /**
     * 设置扫码框下方的提示文字
     *
     * @param scanNoticeText 提示文字
     */
    public HmsScanConfig setScanNoticeText(String scanNoticeText) {
        scanOption.scanNoticeText = scanNoticeText;
        return this;
    }

    /**
     * 隐藏闪光灯图标及文字
     */
    public HmsScanConfig setFlashLightInvisible() {
        scanOption.dropFlashLight = true;
        return this;
    }

    /**
     * 设置扫码线颜色，扫描线最好使用一个渐变颜色
     * 如：
     * --- === #### === ---
     * 我们需要一条线，将它分成5份，拿到1/5、2/5、3/5处的3个颜色就可以画出一条两边细中间粗的扫描线
     *
     * @param scanLineColors 颜色合集
     */
    public HmsScanConfig setScanLineColors(List<Integer> scanLineColors) {
        scanOption.scanLineColors = scanLineColors;
        return this;
    }

    /**
     * 设置扫码代理，获取扫码结果
     *
     * @param delegate 扫码代理
     */
    public HmsScanConfig setOnScanResultDelegate(HmsScanDelegate.OnScanResultDelegate delegate) {
        HmsScanDelegate.getInstance().setScanResultDelegate(delegate);
        return this;
    }

    /**
     * 开启界面
     */
    public void start() {
        Intent intent = new Intent(context, HmsScanActivity.class);
        intent.putExtra("option", scanOption);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static class ScanOption implements Parcelable {

        private int cornerColor;
        private int borderColor;
        private int maskColor;
        private int borderSize;
        private int borderWidth;
        private int borderHeight;
        private int scanMode;
        private String title;
        private boolean showAlbum = false;
        private boolean scanHorizontal;
        private boolean continuousScan;

        private int flashLightOnDrawable;
        private int flashLightOffDrawable;
        private String flashLightOnText;
        private String flashLightOffText;
        private boolean dropFlashLight;
        private String scanNoticeText;

        private List<Integer> scanLineColors;

        public int getCornerColor() {
            return cornerColor;
        }

        public int getBorderColor() {
            return borderColor;
        }

        public int getMaskColor() {
            return maskColor;
        }

        public int getBorderSize() {
            return borderSize;
        }

        public int getBorderWidth() {
            return borderWidth;
        }

        public int getBorderHeight() {
            return borderHeight;
        }

        public int getScanMode() {
            return scanMode;
        }

        public String getTitle() {
            return title;
        }

        public boolean isShowAlbum() {
            return showAlbum;
        }

        public boolean isContinuousScan() {
            return continuousScan;
        }

        public boolean isScanHorizontal() {
            return scanHorizontal;
        }

        public int getFlashLightOnDrawable() {
            return flashLightOnDrawable;
        }

        public int getFlashLightOffDrawable() {
            return flashLightOffDrawable;
        }

        public String getFlashLightOnText() {
            return flashLightOnText;
        }

        public String getFlashLightOffText() {
            return flashLightOffText;
        }

        public boolean isDropFlashLight() {
            return dropFlashLight;
        }

        public String getScanNoticeText() {
            return scanNoticeText;
        }

        public List<Integer> getScanLineColors() {
            return scanLineColors;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.cornerColor);
            dest.writeInt(this.borderColor);
            dest.writeInt(this.maskColor);
            dest.writeInt(this.borderSize);
            dest.writeInt(this.borderWidth);
            dest.writeInt(this.borderHeight);
            dest.writeInt(this.scanMode);
            dest.writeString(this.title);
            dest.writeByte(this.showAlbum ? (byte) 1 : (byte) 0);
            dest.writeByte(this.scanHorizontal ? (byte) 1 : (byte) 0);
            dest.writeByte(this.continuousScan ? (byte) 1 : (byte) 0);
            dest.writeInt(this.flashLightOnDrawable);
            dest.writeInt(this.flashLightOffDrawable);
            dest.writeString(this.flashLightOnText);
            dest.writeString(this.flashLightOffText);
            dest.writeByte(this.dropFlashLight ? (byte) 1 : (byte) 0);
            dest.writeString(this.scanNoticeText);
            dest.writeList(this.scanLineColors);
        }

        public ScanOption() {
        }

        protected ScanOption(Parcel in) {
            this.cornerColor = in.readInt();
            this.borderColor = in.readInt();
            this.maskColor = in.readInt();
            this.borderSize = in.readInt();
            this.borderWidth = in.readInt();
            this.borderHeight = in.readInt();
            this.scanMode = in.readInt();
            this.title = in.readString();
            this.showAlbum = in.readByte() != 0;
            this.scanHorizontal = in.readByte() != 0;
            this.continuousScan = in.readByte() != 0;
            this.flashLightOnDrawable = in.readInt();
            this.flashLightOffDrawable = in.readInt();
            this.flashLightOnText = in.readString();
            this.flashLightOffText = in.readString();
            this.dropFlashLight = in.readByte() != 0;
            this.scanNoticeText = in.readString();
            this.scanLineColors = new ArrayList<>();
            in.readList(this.scanLineColors, Integer.class.getClassLoader());
        }

        public static final Creator<ScanOption> CREATOR = new Creator<ScanOption>() {
            @Override
            public ScanOption createFromParcel(Parcel source) {
                return new ScanOption(source);
            }

            @Override
            public ScanOption[] newArray(int size) {
                return new ScanOption[size];
            }
        };
    }
}
