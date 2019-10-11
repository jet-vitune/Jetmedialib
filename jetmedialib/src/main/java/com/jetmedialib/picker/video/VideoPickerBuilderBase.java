package com.jetmedialib.picker.video;

/**
 * Created by akshay.kumar
 * @JetSynthesys
 */
public interface VideoPickerBuilderBase {
//    VideoPicker.Builder compressLevel(VideoPicker.ComperesLevel compressLevel);

    VideoPicker.Builder mode(VideoPicker.Mode mode);

    VideoPicker.Builder directory(String directory);

    VideoPicker.Builder directory(VideoPicker.Directory directory);

    VideoPicker.Builder extension(VideoPicker.Extension extension);

//    VideoPicker.Builder scale(int minWidth, int minHeight);
//
//    VideoPicker.Builder allowMultipleImages(boolean allowMultiple);
    VideoPicker.Builder maxVideoDuration(int duration);

    VideoPicker.Builder enableDebuggingMode(boolean debug);

    VideoPicker build();

}
