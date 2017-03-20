package happyyoung.trashnetwork.cleaning.util;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

import happyyoung.trashnetwork.cleaning.R;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-20
 */
public class DateTimeUtil {
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd");
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd");

    public static String convertTimestamp(Context context, Date time, boolean showDate, boolean showTime){
        return convertTimestamp(context, time, showDate, showTime, false);
    }

    public static String convertTimestamp(Context context, Date time, boolean showDate, boolean showTime, boolean showSecond){
        Date currentDate = new Date();
        String timeStr = "";
        if(time.getYear() == currentDate.getYear() && time.getMonth() == currentDate.getMonth()
                && time.getDate() == currentDate.getDate()) {
            if(showDate && !showTime)
                timeStr = context.getString(R.string.today);
        }else if(time.getYear() == currentDate.getYear() && time.getMonth() == currentDate.getMonth()
                && time.getDate() == currentDate.getDate() - 1){
            if(showDate) {
                timeStr = context.getString(R.string.yesterday);
                if(showTime)
                    timeStr += ' ';
            }
        }else if(time.getYear() == currentDate.getYear()){
            if(showDate) {
                timeStr = sdf2.format(time);
                if(showTime)
                    timeStr += ' ';
            }
        }else if(showDate){
            timeStr = sdf3.format(time);
            if(showTime)
                timeStr += ' ';
        }
        if(showTime) {
            if(showSecond)
                timeStr += sdf1.format(time);
            else
                timeStr += sdf.format(time);
        }
        return timeStr;
    }

    @SuppressWarnings("deprecation")
    public static boolean isSameDay(Date d1, Date d2){
        return d1.getYear() == d2.getYear() && d1.getMonth() == d2.getMonth()
                && d1.getDate() == d2.getDate();
    }
}
