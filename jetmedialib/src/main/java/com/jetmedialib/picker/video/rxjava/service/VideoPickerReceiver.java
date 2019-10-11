package com.jetmedialib.picker.video.rxjava.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jetmedialib.picker.video.VideoTags;

import java.util.List;

import rx.Observer;

/**
 * Created by akshay.kumar
 * @JetSynthesys
 */
public class VideoPickerReceiver extends BroadcastReceiver {

    private static final String TAG = "VideoPickerReceiver";
    private Observer<List<String>> observer;

    public VideoPickerReceiver(Observer<List<String>> observer) {
        this.observer = observer;
    }

    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "onReceive message " + intent);
        Log.e(TAG, "onReceive action " + intent.getAction());

        List<String> imagePath = (List<String>) intent.getSerializableExtra(VideoTags.Tags.VIDEO_PATH);

        Log.e(TAG, "onReceive imagePath: "+imagePath);

        if (imagePath != null && imagePath.size() > 0)
            observer.onNext(imagePath);
        else
            observer.onError(new Throwable(intent.getStringExtra(VideoTags.Tags.PICK_ERROR)));
    }
}
