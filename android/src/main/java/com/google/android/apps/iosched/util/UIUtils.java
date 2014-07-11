package com.google.android.apps.iosched.util;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.widget.TextView;

import com.google.android.apps.iosched.BuildConfig;
import com.google.android.apps.iosched.R;

import java.util.Formatter;
import java.util.TimeZone;

/**
 * Created by vihaan on 29/6/14.
 */
public class UIUtils {
    public static final TimeZone CONFERENCE_TIME_ZONE = TimeZone.getTimeZone("America/Los_Angeles");
    private static final int TIME_FLAGS = DateUtils.FORMAT_SHOW_TIME
            | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY;
    public static final long CONFERENCE_END_MILLIS = ParserUtils.parseTime(
            "2013-05-17T16:00:00.000-07:00");

    private static CharSequence sNowPlayingText;
    private static CharSequence sLivestreamNowText;
    private static CharSequence sLivestreamAvailableText;


    public static String formatBlockTimeString(long blockStart, long blockEnd, StringBuilder recyle,Context context)
    {
        if(recyle == null)
        {
            recyle = new StringBuilder();
        }
        else
        {
            recyle.setLength(0);
        }

        Formatter formatter = new Formatter(recyle);
        return DateUtils.formatDateRange(context, formatter, blockStart, blockEnd, TIME_FLAGS,
                PrefUtils.getDisplayTimeZone(context).getID()).toString();
    }


    private static final long sAppLoadTime = System.currentTimeMillis();

    public static long getCurrentTime(final Context context) {
        if (BuildConfig.DEBUG) {
            return context.getSharedPreferences("mock_data", Context.MODE_PRIVATE)
                    .getLong("mock_current_time", System.currentTimeMillis())
                    + System.currentTimeMillis() - sAppLoadTime;
//            return ParserUtils.parseTime("2012-06-27T09:44:45.000-07:00")
//                    + System.currentTimeMillis() - sAppLoadTime;
        } else {
            return System.currentTimeMillis();
        }
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static void updateTimeAndLivestreamBlockUI(final Context context,
                                                      long blockStart, long blockEnd, boolean hasLivestream, TextView titleView,
                                                      TextView subtitleView, CharSequence subtitle) {
        long currentTimeMillis = getCurrentTime(context);

        boolean conferenceEnded = currentTimeMillis > CONFERENCE_END_MILLIS;
        boolean blockEnded = currentTimeMillis > blockEnd;
        boolean blockNow = (blockStart <= currentTimeMillis && currentTimeMillis <= blockEnd);

        if (titleView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                titleView.setTypeface(Typeface.create((blockEnded && !conferenceEnded)
                        ? "sans-serif-light" : "sans-serif", Typeface.NORMAL));
            } else {
                titleView.setTypeface(Typeface.SANS_SERIF,
                        (blockEnded && !conferenceEnded) ? Typeface.NORMAL : Typeface.BOLD);
            }
        }

        if (subtitleView != null) {
            boolean empty = true;
            SpannableStringBuilder sb = new SpannableStringBuilder(); // TODO: recycle
            if (subtitle != null) {
                sb.append(subtitle);
                empty = false;
            }

            if (blockNow) {
                if (sNowPlayingText == null) {
                    sNowPlayingText = Html.fromHtml(context.getString(R.string.now_playing_badge));
                }
                if (!empty) {
                    sb.append("  ");
                }
                sb.append(sNowPlayingText);

                if (hasLivestream) {
                    if (sLivestreamNowText == null) {
                        sLivestreamNowText = Html.fromHtml("&nbsp;&nbsp;" +
                                context.getString(R.string.live_now_badge));
                    }
                    sb.append(sLivestreamNowText);
                }
            } else if (hasLivestream) {
                if (sLivestreamAvailableText == null) {
                    sLivestreamAvailableText = Html.fromHtml(
                            context.getString(R.string.live_available_badge));
                }
                if (!empty) {
                    sb.append("  ");
                }
                sb.append(sLivestreamAvailableText);
            }

            subtitleView.setText(sb);
        }
    }


}
