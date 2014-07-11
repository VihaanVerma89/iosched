package com.google.android.apps.iosched.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.apps.iosched.R;
import com.google.android.apps.iosched.provider.ScheduleContract;
import com.google.android.apps.iosched.util.PrefUtils;
import com.google.android.apps.iosched.util.UIUtils;

import java.util.Formatter;
import java.util.Locale;

import static com.google.android.apps.iosched.util.LogUtils.makeLogTag;

/**
 * Created by vihaan on 29/6/14.
 */
public class ScheduleFragment extends ListFragment
implements LoaderManager.LoaderCallbacks<Cursor>, ActionMode.Callback
{
    private static final String TAG = makeLogTag(ScheduleFragment.class);
    private static final String STATE_ACTION_MODE = "actionMode";

    private boolean mActionModeStarted = false;
    private SparseArray<String> mSelectedItemData;
    private View mLongClickedView;
    private ActionMode mActionMode;
    private StringBuilder mBuffer = new StringBuilder();

    private Formatter mFormatter = new Formatter(mBuffer, Locale.getDefault());








    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class MySchduleAdapter extends CursorAdapter
    {

        public MySchduleAdapter(Context context)
        {
            super(context ,null,0);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.list_item_schedule_block, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final String type = cursor.getString(BlocksQuery.BLOCK_TYPE);

            final String blockId = cursor.getString(BlocksQuery.BLOCK_ID);
            final String blockTitle = cursor.getString(BlocksQuery.BLOCK_TITLE);
            final long blockStart = cursor.getLong(BlocksQuery.BLOCK_START);
            final long blockEnd = cursor.getLong(BlocksQuery.BLOCK_END);
            final String blockMeta = cursor.getString(BlocksQuery.BLOCK_META);

            final String blockTimeString = UIUtils.formatBlockTimeString(blockStart, blockEnd, mBuffer, context);


            final TextView timeView = (TextView) view.findViewById(R.id.block_time);
            final TextView endtimeView = (TextView) view.findViewById(R.id.block_endtime);
            final TextView titleView = (TextView) view.findViewById(R.id.block_title);
            final TextView subtitleView = (TextView) view.findViewById(R.id.block_subtitle);
            final ImageButton extraButton = (ImageButton) view.findViewById(R.id.extra_button);
            final View primaryTouchTargetView = view.findViewById(R.id.list_item_middle_container);

            final Resources res = getResources();

            String subtitle;

            boolean isLiveStreamed = false;
            primaryTouchTargetView.setOnLongClickListener(null);
            primaryTouchTargetView.setSelected(false);

            endtimeView.setText(null);

            titleView.setTextColor(res.getColorStateList(R.color.body_text_1_stateful));
            subtitleView.setTextColor(res.getColorStateList(R.color.body_text_2_stateful));


            if (ScheduleContract.Blocks.BLOCK_TYPE_SESSION.equals(type)
                    || ScheduleContract.Blocks.BLOCK_TYPE_CODELAB.equals(type)
                    || ScheduleContract.Blocks.BLOCK_TYPE_OFFICE_HOURS.equals(type)) {
                final int numStarredSessions = cursor.getInt(BlocksQuery.NUM_STARRED_SESSIONS);
                final String starredSessionId = cursor.getString(BlocksQuery.STARRED_SESSION_ID);

                View.OnClickListener allSessionsListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mActionModeStarted) {
                            return;
                        }

                        final Uri sessionsUri = ScheduleContract.Blocks.buildSessionsUri(blockId);
                        final Intent intent = new Intent(Intent.ACTION_VIEW, sessionsUri);
                        intent.putExtra(Intent.EXTRA_TITLE, blockTimeString);
                        startActivity(intent);
                    }
                };


                if (numStarredSessions == 0) {
                    // 0 sessions starred
                    titleView.setText(getString(R.string.schedule_empty_slot_title_template,
                            TextUtils.isEmpty(blockTitle)
                                    ? ""
                                    : (" " + blockTitle.toLowerCase())));
                    titleView.setTextColor(res.getColorStateList(
                            R.color.body_text_1_positive_stateful));
                    subtitle = getString(R.string.schedule_empty_slot_subtitle);
                    extraButton.setVisibility(View.GONE);

                    primaryTouchTargetView.setOnClickListener(allSessionsListener);
                    primaryTouchTargetView.setEnabled(!mActionModeStarted);

                } else if (numStarredSessions == 1) {
                    // exactly 1 session starred
                    final String starredSessionTitle =
                            cursor.getString(BlocksQuery.STARRED_SESSION_TITLE);
                    final String starredSessionHashtags = cursor.getString(BlocksQuery.STARRED_SESSION_HASHTAGS);
                    final String starredSessionUrl = cursor.getString(BlocksQuery.STARRED_SESSION_URL);
                    final String starredSessionRoomId = cursor.getString(BlocksQuery.STARRED_SESSION_ROOM_ID);
                    titleView.setText(starredSessionTitle);
                    subtitle = cursor.getString(BlocksQuery.STARRED_SESSION_ROOM_NAME);
                    if (subtitle == null) {
                        subtitle = getString(R.string.unknown_room);
                    }

                    // Determine if the session is in the past
                    long currentTimeMillis = UIUtils.getCurrentTime(context);
                    boolean conferenceEnded = currentTimeMillis > UIUtils.CONFERENCE_END_MILLIS;
                    boolean blockEnded = currentTimeMillis > blockEnd;
                    if (blockEnded && !conferenceEnded) {
                        subtitle = getString(R.string.session_finished);
                    }

                    isLiveStreamed = !TextUtils.isEmpty(
                            cursor.getString(BlocksQuery.STARRED_SESSION_LIVESTREAM_URL));
                    extraButton.setVisibility(View.VISIBLE);
                    extraButton.setOnClickListener(allSessionsListener);
                    extraButton.setEnabled(!mActionModeStarted);
                    if (mSelectedItemData != null && mActionModeStarted
                            && mSelectedItemData.get(BlocksQuery.STARRED_SESSION_ID, "").equals(
                            starredSessionId)) {
                        primaryTouchTargetView.setSelected(true);
                        mLongClickedView = primaryTouchTargetView;
                    }

                    final Runnable restartActionMode = new Runnable() {
                        @Override
                        public void run() {
                            boolean currentlySelected = false;

                            if (mActionModeStarted
                                    && mSelectedItemData != null
                                    && starredSessionId.equals(mSelectedItemData.get(
                                    BlocksQuery.STARRED_SESSION_ID))) {
                                currentlySelected = true;
                            }

                            if (mActionMode != null) {
                                mActionMode.finish();
                                if (currentlySelected) {
                                    return;
                                }
                            }

                            mLongClickedView = primaryTouchTargetView;
                            mSelectedItemData = new SparseArray<String>();
                            mSelectedItemData.put(BlocksQuery.STARRED_SESSION_ID,
                                    starredSessionId);
                            mSelectedItemData.put(BlocksQuery.STARRED_SESSION_TITLE,
                                    starredSessionTitle);
                            mSelectedItemData.put(BlocksQuery.STARRED_SESSION_HASHTAGS,
                                    starredSessionHashtags);
                            mSelectedItemData.put(BlocksQuery.STARRED_SESSION_URL,
                                    starredSessionUrl);
                            mSelectedItemData.put(BlocksQuery.STARRED_SESSION_ROOM_ID,
                                    starredSessionRoomId);
                            mActionMode = ((ActionBarActivity) getActivity())
                                    .startSupportActionMode(ScheduleFragment.this);
                            primaryTouchTargetView.setSelected(true);
                        }
                    };

                    primaryTouchTargetView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mActionModeStarted) {
                                restartActionMode.run();
                                return;
                            }

//                            final Uri sessionUri = ScheduleContract.Sessions.buildSessionUri(
//                                    starredSessionId);
//                            final Intent intent = new Intent(Intent.ACTION_VIEW, sessionUri);
//                            intent.putExtra(SessionsSandboxMultiPaneActivity.EXTRA_MASTER_URI,
//                                    ScheduleContract.Blocks.buildSessionsUri(blockId));
//                            intent.putExtra(Intent.EXTRA_TITLE, blockTimeString);
//                            startActivity(intent);
                        }
                    });

                    primaryTouchTargetView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            restartActionMode.run();
                            return true;
                        }
                    });

                    primaryTouchTargetView.setEnabled(true);

                } else {
                    // 2 or more sessions starred
                    titleView.setText(getString(R.string.schedule_conflict_title,
                            numStarredSessions));
                    subtitle = getString(R.string.schedule_conflict_subtitle);
                    extraButton.setVisibility(View.VISIBLE);
                    extraButton.setOnClickListener(allSessionsListener);
                    extraButton.setEnabled(!mActionModeStarted);

                    primaryTouchTargetView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mActionModeStarted) {
                                return;
                            }

                            final Uri sessionsUri = ScheduleContract.Blocks
                                    .buildStarredSessionsUri(
                                            blockId);
                            final Intent intent = new Intent(Intent.ACTION_VIEW, sessionsUri);
                            intent.putExtra(Intent.EXTRA_TITLE, blockTimeString);
                            startActivity(intent);
                        }
                    });

                    primaryTouchTargetView.setEnabled(!mActionModeStarted);
                }

            } else if (ScheduleContract.Blocks.BLOCK_TYPE_KEYNOTE.equals(type)) {
                final String starredSessionId = cursor.getString(BlocksQuery.STARRED_SESSION_ID);
                final String starredSessionTitle =
                        cursor.getString(BlocksQuery.STARRED_SESSION_TITLE);

                long currentTimeMillis = UIUtils.getCurrentTime(context);
                boolean past = (currentTimeMillis > blockEnd
                        && currentTimeMillis < UIUtils.CONFERENCE_END_MILLIS);
                boolean present = !past && (currentTimeMillis >= blockStart);
                boolean canViewStream = present && UIUtils.hasHoneycomb();

                boolean enabled = canViewStream && !mActionModeStarted;

                isLiveStreamed = true;
                subtitle = getString(R.string.keynote_room);

                titleView.setText(starredSessionTitle);
                extraButton.setVisibility(View.GONE);
                primaryTouchTargetView.setEnabled(enabled);
                primaryTouchTargetView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        if (mActionModeStarted) {
//                            return;
//                        }
//
//                        final Uri sessionUri = ScheduleContract.Sessions.buildSessionUri(
//                                starredSessionId);
//                        Intent livestreamIntent = new Intent(Intent.ACTION_VIEW, sessionUri);
//                        livestreamIntent.setClass(getActivity(), SessionLivestreamActivity.class);
//                        startActivity(livestreamIntent);
                    }
                });

            } else {
                subtitle = blockMeta;
                titleView.setText(blockTitle);
                extraButton.setVisibility(View.GONE);
                primaryTouchTargetView.setEnabled(false);
                primaryTouchTargetView.setOnClickListener(null);

                mBuffer.setLength(0);
                endtimeView.setText(DateUtils.formatDateRange(context, mFormatter,
                        blockEnd, blockEnd,
                        DateUtils.FORMAT_SHOW_TIME,
                        PrefUtils.getDisplayTimeZone(context).getID()).toString());
            }

            mBuffer.setLength(0);
            timeView.setText(DateUtils.formatDateRange(context, mFormatter,
                    blockStart, blockStart,
                    DateUtils.FORMAT_SHOW_TIME,
                    PrefUtils.getDisplayTimeZone(context).getID()).toString());

            // Show past/present/future and livestream status for this block.
            UIUtils.updateTimeAndLivestreamBlockUI(context,
                    blockStart, blockEnd, isLiveStreamed,
                    titleView, subtitleView, subtitle);
        }

    }

    private interface BlocksQuery {
        String[] PROJECTION = {
                BaseColumns._ID,
                ScheduleContract.Blocks.BLOCK_ID,
                ScheduleContract.Blocks.BLOCK_TITLE,
                ScheduleContract.Blocks.BLOCK_START,
                ScheduleContract.Blocks.BLOCK_END,
                ScheduleContract.Blocks.BLOCK_TYPE,
                ScheduleContract.Blocks.BLOCK_META,
                ScheduleContract.Blocks.SESSIONS_COUNT,
                ScheduleContract.Blocks.NUM_STARRED_SESSIONS,
                ScheduleContract.Blocks.NUM_LIVESTREAMED_SESSIONS,
                ScheduleContract.Blocks.STARRED_SESSION_ID,
                ScheduleContract.Blocks.STARRED_SESSION_TITLE,
                ScheduleContract.Blocks.STARRED_SESSION_ROOM_NAME,
                ScheduleContract.Blocks.STARRED_SESSION_ROOM_ID,
                ScheduleContract.Blocks.STARRED_SESSION_HASHTAGS,
                ScheduleContract.Blocks.STARRED_SESSION_URL,
                ScheduleContract.Blocks.STARRED_SESSION_LIVESTREAM_URL,
        };

        int _ID = 0;
        int BLOCK_ID = 1;
        int BLOCK_TITLE = 2;
        int BLOCK_START = 3;
        int BLOCK_END = 4;
        int BLOCK_TYPE = 5;
        int BLOCK_META = 6;
        int SESSIONS_COUNT = 7;
        int NUM_STARRED_SESSIONS = 8;
        int NUM_LIVESTREAMED_SESSIONS = 9;
        int STARRED_SESSION_ID = 10;
        int STARRED_SESSION_TITLE = 11;
        int STARRED_SESSION_ROOM_NAME = 12;
        int STARRED_SESSION_ROOM_ID = 13;
        int STARRED_SESSION_HASHTAGS = 14;
        int STARRED_SESSION_URL = 15;
        int STARRED_SESSION_LIVESTREAM_URL = 16;

    }









































}
