package com.jni;

public class FFmpegJni {

    static {
        System.loadLibrary("avutil-55");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("swscale-4");
        System.loadLibrary("swresample-2");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("ffmpeg");
    }
    public static native int run(String[] commands);
}
