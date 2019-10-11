package com.jetmedialib.picker.video.rxjava.observable;


import android.content.IntentFilter;
import android.util.Log;


import com.jetmedialib.picker.video.VideoPicker;
import com.jetmedialib.picker.video.VideoTags;
import com.jetmedialib.picker.video.rxjava.service.VideoPickerReceiver;

import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;
/**
 * Created by akshay.kumar
 * @JetSynthesys
 */
public class VideoPickerObservable extends VideoPickerBaseObservable {

    private static final String TAG = "VideoPickerObservable";
    private VideoPicker.Builder mVideoPicker;
    private VideoPickerReceiver mReceiver;

    public VideoPickerObservable(VideoPicker.Builder videoPicker) {
        super(videoPicker.getContext());
        this.mVideoPicker = videoPicker;

    }


    @Override
    public void call(final Subscriber subscriber) {
        super.call(subscriber);
        mReceiver = new VideoPickerReceiver(subscriber);
        mVideoPicker.build();
        registerImagePickerObservable();
        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                subscriber.unsubscribe();
                onUnsubscribed();
            }
        }));
    }

    @Override
    public void registerImagePickerObservable() {
        //PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(RECEIVER_ACTION), 0);
        context.registerReceiver(mReceiver, new IntentFilter(VideoTags.Action.SERVICE_ACTION));

    }

    @Override
    public void onUnsubscribed() {
        Log.d(TAG, "onUnsubscribed() called with: " + "");
        try {
            context.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException ignored) {
        }

    }


}
