package com.google.android.apps.iosched.sync;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;

import com.google.android.apps.iosched.provider.ScheduleContract;

/**
 * Created by vihaan on 12/7/14.
 */
public class SyncHelper {

    public static void requestManualSync(Account mChosenAccount)
    {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(mChosenAccount, ScheduleContract.CONTENT_AUTHORITY, b);

    }
}
