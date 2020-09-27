package com.eschava.firenotify;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.bosphere.filelogger.FL;
import com.bosphere.filelogger.FLConfig;
import com.bosphere.filelogger.FLConst;

public class Log {
    public static void init(Context context) {
        FL.init(new FLConfig.Builder(context)
                .minLevel(FLConst.Level.V)
                .logToFile(isWriteToFile(context))
//                .dir(new File(Environment.getExternalStorageDirectory(), "FireNotify.log"))
                .dir(Environment.getExternalStorageDirectory())
                .formatter(new FLConfig.DefaultFormatter() {
                    @Override public String formatFileName(long timeInMillis) { return "FireNotify.log";}
                })
                .retentionPolicy(FLConst.RetentionPolicy.NONE)
                .build());
        FL.setEnabled(true);
    }

    public static boolean isWriteToFile(Context context) {
        SharedPreferences settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        return settings.getBoolean("logToFile", false);
    }

    public static void setWriteToFile(Context context, boolean value) {
        SharedPreferences settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("logToFile", value);
        editor.apply();

        init(context);
    }

    public static void d(String tag, String text) {
        FL.d(tag, text);
    }

    public static void e(String tag, String text, Throwable e) {
        FL.e(tag, e, text);
    }
}
