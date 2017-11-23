package com.ximsfei.atrace;

import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

final public class ATrace {
    private static final ATrace INSTANCE = new ATrace();
    private boolean isInited = false;
    private String mPackageName;
    private String mProcessName;

    private ATrace() {
    }

    public static ATrace get() {
        return INSTANCE;
    }

    public void init(Context context) {
        mPackageName = context.getPackageName();
        try {
            mProcessName = getProcessName();
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(mProcessName)) {
            mProcessName = mPackageName;
        }
        isInited = true;
    }

    private String getProcessName() throws IOException {
        String processCmdPath = String.format(Locale.getDefault(), "/proc/%d/cmdline", Process.myPid());
        BufferedReader inputStream = null;
        try {
            inputStream = new BufferedReader(new FileReader(processCmdPath));
            return inputStream.readLine().trim();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public void beforeMethod(String method) {
        Thread thread = Thread.currentThread();
        long threadId = thread.getId();
        String threadName = thread.getName();
        Log.e("ATrace", "proc = " + mProcessName + ", thread id = " + threadId + ", thread name = " + threadName + ", method = " + method);
    }

    public void afterMethod(String method) {
        Thread thread = Thread.currentThread();
        long threadId = thread.getId();
        String threadName = thread.getName();
        Log.e("ATrace", "after = proc = " + mProcessName + ", thread id = " + threadId + ", thread name = " + threadName);
    }
}
