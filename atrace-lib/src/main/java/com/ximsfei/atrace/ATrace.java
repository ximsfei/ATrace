package com.ximsfei.atrace;

import android.content.Context;
import android.os.Process;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
`import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

final public class ATrace {
    private static final int TYPE_ENTER = 0;
    private static final int TYPE_EXIT = 1;
    private static AtomicInteger sIndex = new AtomicInteger();
    private static ThreadLocal<Integer> sLocalIndex = new ThreadLocal<>();
    private static boolean isInit = false;
    private static boolean isBegin = false;
    private static boolean isDebug = false;
    private static boolean isPersistent = false;
    private static String mPackageName;
    private static String mProcessName;
    private static ConcurrentHashMap<Long, List<String>> mThreadTraceBuffer = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Long, String> mThreadArray = new ConcurrentHashMap<>();

    private ATrace() {
    }

    public static void init(Context context, boolean persistent, boolean debug) {
        mPackageName = context.getPackageName();
        mProcessName = getProcessName();
        isPersistent = persistent;
        isDebug = debug;
        isInit = true;
    }

    private static String getProcessName() {
        String processCmdPath = String.format(Locale.getDefault(), "/proc/%d/cmdline", Process.myPid());
        BufferedReader inputStream = null;
        try {
            inputStream = new BufferedReader(new FileReader(processCmdPath));
            return inputStream.readLine().trim();
        } catch (Exception e) {
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
            }
        }
        return mPackageName;
    }

    public static void traceBegin() {
        if (isInit) {
            isBegin = true;
        }
    }

    public static void traceEnd() {
        if (isInit) {
            if (isPersistent) {
                // ToDo: Write sdCard.
            }
            isBegin = false;
        }
    }

    public static void enterMethod(String method, Object... args) {
        trace(TYPE_ENTER, method, args);
    }

    public static void exitMethod(String method, Object result) {
        trace(TYPE_EXIT, method, result);
    }

    private static void trace(int type, String method, Object... args) {
        if (isInit && isBegin) {
            Thread thread = Thread.currentThread();
            long threadId = thread.getId();
            String threadName = thread.getName();
            if (!mThreadArray.containsKey(threadId)) {
                mThreadArray.put(threadId, threadName);
            }
            if (sLocalIndex.get() == null) {
                sLocalIndex.set(0);
            }
            List<String> traces = mThreadTraceBuffer.get(threadId);
            if (!mThreadTraceBuffer.containsKey(threadId)) {
                traces = new ArrayList<>();
                mThreadTraceBuffer.put(threadId, traces);
            }
            if (isPersistent || isDebug) {
                StringBuffer sb = new StringBuffer();
                String argsType = "params";
                if (type == 0) {
                    sb.append("enterMethod");
                } else if (type == 1) {
                    sb.append("exitMethod");
                    argsType = "result";
                }
                sb.append(" [gIndex, ").append(sIndex).append("],")
                        .append(" [localIndex, ").append(sLocalIndex.get()).append("],")
                        .append(" [proc, ").append(mProcessName).append("],")
                        .append(" [tid, ").append(threadId).append("],")
                        .append(" [tname, ").append(threadName).append("],")
                        .append(" [time, ").append(System.currentTimeMillis()).append("],")
                        .append(" [method, ").append(method).append("]");

                if (localArgs != null && localArgs.length > 0) {
                    for (int i = 0; i < localArgs.length; i++) {
//                        sb.append(", [").append(argsType).append(i).append(", ").append(localArgs[i]).append("]");
                    }
                }
                String msg = sb.toString();
                if (isPersistent) {
                    traces.add(msg);
                }
                if (isDebug) {
                    print(msg);
                }
            }
            sIndex.incrementAndGet();
            sLocalIndex.set(sLocalIndex.get() + 1);
        }
    }

    private static void print(String msg) {
        Log.d("ATrace", msg);
    }
}
