package com.google.android.apps.iosched.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.android.apps.iosched.util.AccountUtils;
import com.google.android.apps.iosched.util.LogUtils;
import com.google.android.apps.iosched.util.PrefUtils;

import static com.google.android.apps.iosched.util.LogUtils.makeLogTag;

/**
 * Created by vihaan on 22/6/14.
 */
public abstract class BaseActivity extends ActionBarActivity {
    private static final String TAG = makeLogTag(BaseActivity.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!AccountUtils.isAuthenticated(this) || !PrefUtils.isSetupDone(this))
        {
            LogUtils.LOGD(TAG, "exiting:"
                    + " isAuthenticated=" + AccountUtils.isAuthenticated(this)
                    + " isSetupDone=" + PrefUtils.isSetupDone(this));
            AccountUtils.startAuthenticationFlow(this, getIntent());

            finish();
        }
    }
}
