package com.jetmedialib.picker.image.rxjava;
import com.jetmedialib.picker.image.ImagePicker;
import com.jetmedialib.picker.image.rxjava.observable.ImagePickerObservable;

import java.util.List;
import rx.Observable;

/**
 * Created by akshay.kumar
 * @JetSynthesys
 */
public class ImagePickerHelper {
    private ImagePicker.Builder mBuilder;
    private ImagePickerObservable imagePickerObservable;
    public ImagePickerHelper(ImagePicker.Builder builder){
        this.mBuilder = builder;
    }

    public Observable<List<String>> getObservable(){
        imagePickerObservable=new ImagePickerObservable(mBuilder);
        return Observable.create(imagePickerObservable);
    }

    public ImagePickerObservable getImagePickerObservable() {
        return imagePickerObservable;
    }

    public void setImagePickerObservable(ImagePickerObservable imagePickerObservable) {
        this.imagePickerObservable = imagePickerObservable;
    }
}
