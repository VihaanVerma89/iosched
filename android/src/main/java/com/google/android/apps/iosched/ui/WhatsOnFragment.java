package com.google.android.apps.iosched.ui;

import android.annotation.SuppressLint;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.apps.iosched.R;
import com.google.android.apps.iosched.provider.ScheduleContract;
import com.google.android.apps.iosched.provider.ScheduleContract.Announcements;
import com.google.android.apps.iosched.util.UIUtils;

/**
 * Created by vihaan on 29/6/14.
 */
public class WhatsOnFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int ANNOUNCEMENTS_LOADER_ID = 0;

    private Handler mHandler = new Handler();

    private TextView mCountdownTextView;
    private ViewGroup mRootView;
    private View mAnnoucementView;
    private Cursor mAnnouncementCursor;
    private String mLatestAnnoucementId;
    private LayoutInflater mInflater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_whats_on,
                container);
        refresh();
        return mRootView;
    }

    private void refresh() {
        mHandler.removeCallbacksAndMessages(null);
        mRootView.removeAllViews();

        final long currentTimeMillis = UIUtils.getCurrentTime(getActivity());

        // Show Loading... and load the view corresponding to the current state
        if (currentTimeMillis < UIUtils.CONFERENCE_START_MILLIS) {
            setupBefore();
        } else if (currentTimeMillis > UIUtils.CONFERENCE_END_MILLIS) {
            setupAfter();
        } else {
            setupDuring();
        }
    }

    private void setupBefore() {
        // Before conference, show countdown.
        mCountdownTextView = (TextView) mInflater
                .inflate(R.layout.whats_on_countdown, mRootView, false);
        mRootView.addView(mCountdownTextView);
        mHandler.post(mCountdownRunnable);
    }


    private void setupAfter() {
        // After conference, show canned text.
        mInflater.inflate(R.layout.whats_on_thank_you,
                mRootView, true);
    }

    private void setupDuring() {
        // Start background query to load announcements
        getLoaderManager().initLoader(ANNOUNCEMENTS_LOADER_ID, null, this);
        getActivity().getContentResolver().registerContentObserver(
                ScheduleContract.Announcements.CONTENT_URI, true, mObserver);
    }





    private Runnable mCountdownRunnable = new Runnable() {
        @SuppressLint("StringFormatMatches")
        public void run() {
            int remainingSec = (int) Math.max(0,
                    (UIUtils.CONFERENCE_START_MILLIS - UIUtils
                            .getCurrentTime(getActivity())) / 1000);
            final boolean conferenceStarted = remainingSec == 0;

            if (conferenceStarted) {
                // Conference started while in countdown mode, switch modes and
                // bail on future countdown updates.
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        refresh();
                    }
                }, 100);
                return;
            }

            final int secs = remainingSec % 86400;
            final int days = remainingSec / 86400;
            final String str;
            if (days == 0) {
                str = getResources().getString(
                        R.string.whats_on_countdown_title_0,
                        DateUtils.formatElapsedTime(secs));
            } else {
                str = getResources().getQuantityString(
                        R.plurals.whats_on_countdown_title, days, days,
                        DateUtils.formatElapsedTime(secs));
            }
            mCountdownTextView.setText(str);

            // Repost ourselves to keep updating countdown
            mHandler.postDelayed(mCountdownRunnable, 1000);
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity(), Announcements.CONTENT_URI, AnnouncementsQuery.PROJECTION, null, null,
                Announcements.DEFAULT_SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if(getActivity() == null) {
                return;
            }
            getLoaderManager().restartLoader(ANNOUNCEMENTS_LOADER_ID, null, WhatsOnFragment.this);

        }

    };


    private interface AnnouncementsQuery {
        String[] PROJECTION = {
                ScheduleContract.Announcements.ANNOUNCEMENT_ID,
                ScheduleContract.Announcements.ANNOUNCEMENT_TITLE,
                ScheduleContract.Announcements.ANNOUNCEMENT_DATE,
                ScheduleContract.Announcements.ANNOUNCEMENT_URL,
        };

        int ANNOUNCEMENT_ID = 0;
        int ANNOUNCEMENT_TITLE = 1;
        int ANNOUNCEMENT_DATE = 2;
        int ANNOUNCEMENT_URL = 3;
    }
}
