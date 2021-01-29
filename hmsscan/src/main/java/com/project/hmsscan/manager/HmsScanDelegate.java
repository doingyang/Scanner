package com.project.hmsscan.manager;

import android.app.Activity;

import com.huawei.hms.ml.scan.HmsScan;

/**
 * 扫码代理
 */
public class HmsScanDelegate {

    public static HmsScanDelegate getInstance() {
        return Holder.instance;
    }

    private static final class Holder {
        private static final HmsScanDelegate instance = new HmsScanDelegate();
    }

    private OnScanResultDelegate mOnScanResultDelegate;

    public void setScanResultDelegate(OnScanResultDelegate onScanResultDelegate) {
        this.mOnScanResultDelegate = onScanResultDelegate;
    }

    public OnScanResultDelegate getScanResultDelegate() {
        return mOnScanResultDelegate;
    }

    public interface OnScanResultDelegate {
        void onScanResult(Activity activity, HmsScan hmsScan);
    }

    public enum HMS_SCAN_MODE {

        AREA(0),
        FULL(1);

        HMS_SCAN_MODE(int var) {
            this.scanMode = var;
        }

        private final int scanMode;

        public int getScanMode() {
            return 1 << this.scanMode;
        }
    }
}