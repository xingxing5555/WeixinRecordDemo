package com.example.admin.weixinrecorddemo.audio;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * @author Xinxin Shi
 *         录音播放工具类
 */

public class AudioManager {
    private final static String TAG = AudioManager.class.getSimpleName();
    private static AudioManager instance;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;


    public static AudioManager getInstance() {
        if (null == instance) {
            synchronized (AudioManager.class) {
                instance = new AudioManager();
            }
        }
        return instance;
    }

    /**
     * 开始录音
     *
     * @param url 录音地址
     */
    public void startRecording(String url) {
        if (TextUtils.isEmpty(url)) {
            Log.w(TAG, "录音保存地址不存在");
            return;
        }
        Log.i(TAG,"录音保存地址"+url);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(url);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        updateMicStatus();
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "MediaRecorder prepare() failed");
        }
        mRecorder.start();
    }

    /**
     * 停止录音
     */
    public void stopRecording() {
        if (null == mRecorder) {
            return;
        }
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    /**
     * 用于获取权限时，未开始录音情况下，仅用来释放录音资源
     */
    public void cancelRecord() {
        if (null == mRecorder) {
            return;
        }
        mRecorder.release();
        mRecorder = null;
    }

    private final Handler mHandler = new Handler();
    private Runnable mUpdateMicStatusTimer = new Runnable() {
        @Override
        public void run() {
            updateMicStatus();
        }
    };

    /**
     * 更新话筒状态
     */
    private void updateMicStatus() {
        int base = 1, space = 100;
        if (mRecorder != null) {
            double ratio = (double) mRecorder.getMaxAmplitude() / base;
            // 分贝
            double db = 0;
            if (ratio > 1) {
                db = 20 * Math.log10(ratio);
            }
            Log.i(TAG, "音量值：" + db);
            onVolumeChangeListener.onVolumeChange(db);
            mHandler.postDelayed(mUpdateMicStatusTimer, space);
        }
    }

    /**
     * 开始播放
     *
     * @param url 播放地址
     */
    public void startPlaying(String url) {
        if (TextUtils.isEmpty(url)) {
            Log.w(TAG, "资源地址不存在");
            return;
        }
        Log.i(TAG, "播放地址为：" + url);
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(url);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlaying();
            }
        });
    }

    /**
     * 停止播放
     */
    public void stopPlaying() {
        if (null == mPlayer) {
            return;
        }
        mPlayer.release();
        mPlayer = null;
    }


    /**
     * 获取录音的时长
     *
     * @param fileName 录音的文件
     * @return 时长的毫秒值
     */
    public int getTime(String fileName) {
        int duration = 1000;
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            duration = mPlayer.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getTime is error");
        }
        stopPlaying();
        return duration;
    }


    public interface OnVolumeChangeListener {

        /**
         * 音量变化的监听回调
         *
         * @param value 分贝值
         */
        void onVolumeChange(double value);
    }

    private OnVolumeChangeListener onVolumeChangeListener;

    public void setOnVolumeChangeListener(OnVolumeChangeListener onVolumeChangeListener) {
        this.onVolumeChangeListener = onVolumeChangeListener;
    }


}
