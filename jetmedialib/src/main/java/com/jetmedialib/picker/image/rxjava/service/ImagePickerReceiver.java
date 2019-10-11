package com.jetmedialib.picker.image.rxjava.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jetmedialib.picker.image.ImageTags;

import java.util.List;
import rx.Observer;


/**
 * /**
 * Created by akshay.kumar
 * @JetSynthesys
 */
public class ImagePickerReceiver extends BroadcastReceiver {

    private static final String TAG = "ImagePickerReceiver";
    private Observer<List<String>> observer;

    public ImagePickerReceiver(Observer<List<String>> observer) {
        this.observer = observer;
    }

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received message " + intent);
        List<String> imagePath = (List<String>) intent.getSerializableExtra(ImageTags.Tags.IMAGE_PATH);
        if (imagePath != null && imagePath.size() > 0)
            observer.onNext(imagePath);
        else
            observer.onError(new Throwable(intent.getStringExtra(ImageTags.Tags.PICK_ERROR)));
    }
}
