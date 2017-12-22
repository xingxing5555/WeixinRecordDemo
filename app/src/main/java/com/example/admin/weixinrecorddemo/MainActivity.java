package com.example.admin.weixinrecorddemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.admin.weixinrecorddemo.adapter.RecordAdapter;
import com.example.admin.weixinrecorddemo.audio.AudioManager;
import com.example.admin.weixinrecorddemo.constants.Constant;
import com.example.admin.weixinrecorddemo.dialog.DialogManager;
import com.example.admin.weixinrecorddemo.view.ButtonTouchView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Xinxin Shi
 *         1、1秒误触处理
 *         2、上滑取消，下滑发送
 *         3、获取权限时的录音资源及UI处理
 */
public class MainActivity extends AppCompatActivity implements View.OnTouchListener, AudioManager.OnVolumeChangeListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private List<String> fileList = new ArrayList<>();
    private AlertDialog recordDialogShow;
    private DialogManager dialogManager;
    private AudioManager audioManager;
    private MainHander mainHander;
    private long time;
    private String url = null;
    private RecordAdapter recordAdapter;
    private float downY;
    private boolean isCanceled = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initView();

    }

    private void init() {
        dialogManager = DialogManager.getInstance();
        audioManager = AudioManager.getInstance();
//        录音的音量监听
        audioManager.setOnVolumeChangeListener(this);
//        初始化dialog
        recordDialogShow = dialogManager.recordDialogShow(this);
        mainHander = new MainHander(this);
//        动态申请权限
        ActivityCompat.requestPermissions(this, permissions, Constant.REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private void initView() {
        ButtonTouchView btnRecord = findViewById(R.id.btn_record);
        btnRecord.setOnTouchListener(this);
        RecyclerView recyRecord = findViewById(R.id.recy_record);
        recyRecord.setLayoutManager(new LinearLayoutManager(this));
        recordAdapter = new RecordAdapter(fileList, this);
        recyRecord.setAdapter(recordAdapter);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
//                解决点击无效问题
                view.performClick();
                handlerActionDown(motionEvent);
                break;
            case MotionEvent.ACTION_UP:
                if (handlerActionUp()) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                handlerActionMove(motionEvent);
                break;
            case MotionEvent.ACTION_CANCEL:
                handlerActionCancel();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 处理点击操作
     *
     * @param motionEvent 移动事件
     */
    private void handlerActionDown(MotionEvent motionEvent) {
        Log.i(TAG, "  ACTION_DOWN ");
        downY = motionEvent.getY();
        audioManager.stopPlaying();
        dialogManager.updateUI(R.mipmap.listener00, getString(R.string.speaking));
        recordDialogShow.show();
        time = System.currentTimeMillis();
        url = getFlieUrl();
        audioManager.startRecording(url);
    }

    /**
     * 处理抬手操作
     *
     * @return 语音是否超过1秒
     */
    private boolean handlerActionUp() {
        Log.i(TAG, " ACTION_UP ");
        audioManager.stopRecording();
//                1秒误按处理
        if (System.currentTimeMillis() - time < Constant.DELAY_TIME_SHORT) {
            dialogManager.updateUI(R.mipmap.no_voice, getString(R.string.speak_short));
            mainHander.sendEmptyMessageDelayed(Constant.WHAT_DIALOG_CLOSE, Constant.DELAY_TIME_SHORT);
            url = null;
            return true;
        }
        recordDialogShow.dismiss();
//                是否取消发送
        if (!isCanceled) {
            fileList.add(url);
            recordAdapter.notifyDataSetChanged();
        }
        return false;
    }

    /**
     * 处理上滑取消，下滑发送
     *
     * @param motionEvent 移动事件2
     */
    private void handlerActionMove(MotionEvent motionEvent) {
        float moveY = motionEvent.getY();
        Log.i(TAG, " ACTION_MOVE downY=" + downY + " moveY=" + moveY);
        if (downY - moveY > Constant.VALUE_100) {
            Log.i(TAG, "上滑ing....");
            isCanceled = true;
            dialogManager.updateUI(R.mipmap.no_voice, getString(R.string.cancle_speaking));
        }
        if (downY - moveY < Constant.VALUE_20) {
            Log.i(TAG, "下滑ing....");
            isCanceled = false;
            dialogManager.updateUI(R.mipmap.listener00, getString(R.string.speaking));
        }
    }

    /**
     * 处理权限申请时的弹出框问题
     */
    private void handlerActionCancel() {
        Log.i(TAG, " ACTION_CANCEL ");
        recordDialogShow.dismiss();
        audioManager.cancelRecord();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            default:
                break;
        }
        if (!permissionToRecordAccepted) {
            finish();
        }
    }

    /**
     * 录音音量监听的回调
     *
     * @param value 分贝值
     */
    @Override
    public void onVolumeChange(double value) {
        Log.i(TAG, "当时获取到的音量为：" + value);
        int volume = (int) value;
        int resId = R.mipmap.listener00;
        if (volume > Constant.VALUE_0 && volume < Constant.VALUE_10) {
            resId = R.mipmap.listener01;
        } else if (volume < Constant.VALUE_20) {
            resId = R.mipmap.listener02;
        } else if (volume < Constant.VALUE_30) {
            resId = R.mipmap.listener03;
        } else if (volume < Constant.VALUE_40) {
            resId = R.mipmap.listener04;
        } else if (volume < Constant.VALUE_50) {
            resId = R.mipmap.listener05;
        } else if (volume < Constant.VALUE_60) {
            resId = R.mipmap.listener07;
        } else if (volume < Constant.VALUE_100) {
            resId = R.mipmap.listener08;
        }
        if (!isCanceled) {
            dialogManager.updateUI(resId, getString(R.string.speaking));
        }
    }


    /**
     * 防止handler的内存泄漏问题
     */
    private static class MainHander extends Handler {
        private final WeakReference<MainActivity> weakReference;

        private MainHander(MainActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = weakReference.get();
            if (null == activity) {
                return;
            }
            activity.handlerMessgae(msg);
        }
    }

    /**
     * handler的事件处理
     *
     * @param msg 消息
     */
    public void handlerMessgae(Message msg) {
        switch (msg.what) {
            case Constant.WHAT_DIALOG_CLOSE:
                if (recordDialogShow.isShowing()) {
                    recordDialogShow.dismiss();
                }
                break;
            default:
                break;
        }
    }


    /**
     * 文件保存的本地位置
     *
     * @return 资源本地位置 url: /data/data/com.example.admin.weixinrecorddemo/cache/1513913361991.3gp
     */
    public String getFlieUrl() {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            //外部存储可用
            cachePath = getExternalCacheDir().getAbsolutePath();
        } else {
            //外部存储不可用
            cachePath = getCacheDir().getAbsolutePath();
        }
        return String.format(Locale.getDefault(), "%1$s%2$s%3$d%4$s", cachePath, File.separator, System.currentTimeMillis(), ".mp3");
    }
}
