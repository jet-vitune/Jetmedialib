package com.jetmedialib.picker.video.rxjava.observable;

import android.content.Context;
import android.util.Log;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by akshay.kumar
 * @JetSynthesys
 */
abstract class VideoPickerBaseObservable implements Observable.OnSubscribe<List<String>> {

    private static final String TAG = "ImagePicker";
    public Context context;

    public VideoPickerBaseObservable(Context context) {
        this.context = context;
    }

    @Override
    public void call(final Subscriber subscriber) {
        Log.d(TAG, "call() called with: " + "subscriber = [" + subscriber + "]");
        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                subscriber.unsubscribe();
                onUnsubscribed();
            }
        }));
    }

    public abstract void registerImagePickerObservable();


    abstract protected void onUnsubscribed();



}