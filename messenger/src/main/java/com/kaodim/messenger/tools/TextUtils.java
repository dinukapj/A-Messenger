package com.kaodim.messenger.tools;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;

/**
 * Created by Kanskiy on 12/10/2016.
 */

public class TextUtils {
    public static String getDateString(Long date, Context mContext){
        if (date==null){
            return "";
        }
        return DateUtils.getRelativeDateTimeString(mContext,
                date,
                DateUtils.DAY_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL)
                .toString();
    }
    public static Spanned fromHtml(String htmlText) {
        Spanned result;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY, null, new HtmlTagHandler());
            return result;
        } else {
            result = Html.fromHtml(htmlText, null, new HtmlTagHandler());
            return result;
        }
    }
}
