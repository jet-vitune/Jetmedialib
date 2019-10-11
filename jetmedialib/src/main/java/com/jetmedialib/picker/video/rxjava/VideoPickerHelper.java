package com.jetmedialib.picker.video.rxjava;


import com.jetmedialib.picker.video.VideoPicker;
import com.jetmedialib.picker.video.rxjava.observable.VideoPickerObservable;

import java.util.List;

import rx.Observable;

/**
 * Created by akshay.kumar on 7/24/16.
 * MediaPicker
 */
public class VideoPickerHelper {
    private VideoPicker.Builder mBuilder;
    private VideoPickerObservable videoPickerObservable;
    public VideoPickerHelper(VideoPicker.Builder builder){
        this.mBuilder = builder;
    }

    public Observable<List<String>> getObservable(){
        videoPickerObservable=new VideoPickerObservable(mBuilder);
        return Observable.create(videoPickerObservable);
    }

    public VideoPickerObservable getVideoPickerObservable() {
        return videoPickerObservable;
    }

    public void setVideoPickerObservable(VideoPickerObservable videoPickerObservable) {
        this.videoPickerObservable = videoPickerObservable;
    }
}
