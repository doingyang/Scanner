package com.project.hmsscan.custom;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

public class ScanCodeConfig {

    public static final int QUESTCODE = 0x001;
    public static final String CODE_KEY = "code";
    public static final String MODEL_KEY = "model";

    private Activity mActivity;
    private Fragment mFragment;
    private ScanCodeModel model;

    public ScanCodeConfig(ScanCodeModel model) {
        this.mActivity = model.mActivity;
        this.mFragment = model.mFragment;
        this.model = model;
    }

    public static ScanCodeModel create(Activity mActivity) {
        return new ScanCodeModel(mActivity);
    }

    public static ScanCodeModel create(Activity mActivity, Fragment mFragment) {
        return new ScanCodeModel(mActivity, mFragment);
    }

    public void start(Class mClass) {
        if (mFragment != null) {
            Intent intent = new Intent(mActivity, mClass);
            intent.putExtra(MODEL_KEY, model);
            mFragment.startActivityForResult(intent, QUESTCODE);
        } else {
            Intent intent = new Intent(mActivity, mClass);
            intent.putExtra(MODEL_KEY, model);
            mActivity.startActivityForResult(intent, QUESTCODE);
        }
    }
}