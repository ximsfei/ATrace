package com.ximsfei.atracedemo;

import android.app.Application;

import com.ximsfei.atrace.ATrace;

public class ATraceApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ATrace.init(this, true, true);
        ATrace.traceBegin();
    }
}
