package com.jetmedialib.picker.video;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.jetmedialib.R;
import com.jetmedialib.picker.utility.FileProcessing;
import com.jetmedialib.picker.utility.Utility;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Created by akshay.kumar
 * @JetSynthesys
 */
public class VideoActivity extends AppCompatActivity {


    private static final String TAG = VideoActivity.class.getName();


    private File destination;
    private Uri mVideoUri;
    private VideoConfig mVideoConfig;
    private List<String> mListOfVideos;

    public static Intent getCallingIntent(Context activity, VideoConfig videoConfig) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra(VideoTags.Tags.IMG_CONFIG, videoConfig);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            try {
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            } catch (Exception e) {
                Log.e(TAG, "onCreate: ", e);
            }

            Intent intent = getIntent();
            if (intent != null) {
                mVideoConfig = (VideoConfig) intent.getSerializableExtra(VideoTags.Tags.IMG_CONFIG);
            }

            if (savedInstanceState == null) {
                pickVideoWrapper();
                mListOfVideos = new ArrayList<>();
            }
            if (mVideoConfig.debug)
                Log.d(VideoTags.Tags.TAG, mVideoConfig.toString());
        }catch (Exception e){
            Log.e(TAG, "onCreate: ", e);
        }
    }

    private void pickVideo() {
        Utility.createFolder(mVideoConfig.directory);
        destination = new File(mVideoConfig.directory, Utility.getRandomString() + mVideoConfig.extension.getValue());
        switch (mVideoConfig.mode) {
            case CAMERA:
                startActivityFromCamera();
                break;
            case GALLERY:
                if (mVideoConfig.allowMultiple)
                    startActivityFromGalleryMultiImg();
                else
                    startActivityFromGallery();
                break;
            case CAMERA_AND_GALLERY:
                showFromCameraOrGalleryAlert();
                break;
            default:
                break;
        }
    }

    private void showFromCameraOrGalleryAlert() {
        if(this!=null) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.media_picker_select_from))
                    .setPositiveButton(getString(R.string.media_picker_camera), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (mVideoConfig.debug)
                                Log.d(VideoTags.Tags.TAG, "Alert Dialog - Start From Camera");
                            startActivityFromCamera();
                        }
                    })
                    .setNegativeButton(getString(R.string.media_picker_gallery), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (mVideoConfig.debug)
                                Log.d(VideoTags.Tags.TAG, "Alert Dialog - Start From Gallery");
                            if (mVideoConfig.allowMultiple)
                                startActivityFromGalleryMultiImg();
                            else
                                startActivityFromGallery();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            if (mVideoConfig.debug)
                                Log.d(VideoTags.Tags.TAG, "Alert Dialog - Canceled");
                            finish();
                        }
                    })
                    .show();
        }
    }

    private void startActivityFromGallery() {
        mVideoConfig.isImgFromCamera = false;
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        photoPickerIntent.setType("video/*");
        startActivityForResult(photoPickerIntent, VideoTags.IntentCode.REQUEST_CODE_SELECT_PHOTO);
        if (mVideoConfig.debug)
            Log.d(VideoTags.Tags.TAG, "Gallery Start with Single video mode");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void startActivityFromGalleryMultiImg() {
        mVideoConfig.isImgFromCamera = false;
        Intent photoPickerIntent = new Intent();
        photoPickerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("video/*");
        startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Picture"), VideoTags.IntentCode.REQUEST_CODE_SELECT_MULTI_PHOTO);
        if (mVideoConfig.debug)
            Log.d(VideoTags.Tags.TAG, "Gallery Start with Multiple videos mode");
    }

    private void startActivityFromCamera() {
        mVideoConfig.isImgFromCamera = true;
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_DURATION_LIMIT, 30);
        mVideoUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", destination);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mVideoUri);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), VideoTags.IntentCode.CAMERA_REQUEST);
        if (mVideoConfig.debug)
            Log.d(VideoTags.Tags.TAG, "Camera Start");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mVideoUri != null) {
            outState.putString(VideoTags.Tags.CAMERA_IMAGE_URI, mVideoUri.toString());
            outState.putSerializable(VideoTags.Tags.IMG_CONFIG, mVideoConfig);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(VideoTags.Tags.CAMERA_IMAGE_URI)) {
            mVideoUri = Uri.parse(savedInstanceState.getString(VideoTags.Tags.CAMERA_IMAGE_URI));
            destination = new File(mVideoUri.getPath());
            mVideoConfig = (VideoConfig) savedInstanceState.getSerializable(VideoTags.Tags.IMG_CONFIG);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mVideoConfig.debug)
            Log.d(VideoTags.Tags.TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case VideoTags.IntentCode.CAMERA_REQUEST:
                    CompresVideoTask(destination.getAbsolutePath(), mVideoConfig,VideoActivity.this);
                    break;
                case VideoTags.IntentCode.REQUEST_CODE_SELECT_PHOTO:
                    processOneVideo(data);
                    break;
                case VideoTags.IntentCode.REQUEST_CODE_SELECT_MULTI_PHOTO:
                    //Check if the intent contain only one image
                    if (data.getClipData() == null) {
                        processOneVideo(data);
                    } else {

                      /*//intent has multi images
                        mListOfVideos = VideoProcessing.processMultiVideos(this, data);
                        new VideoActivity.CompresVideoTask(mListOfVideos, mVideoConfig, VideoActivity.this).execute();
                      */
                    }
                    break;
                default:
                    break;
            }
        } else {
            Intent intent = new Intent();
            intent.setAction(VideoTags.Action.SERVICE_ACTION);
            intent.putExtra(VideoTags.Tags.PICK_ERROR, "user did not select any videos");
            sendBroadcast(intent);
            finish();
        }
    }

    private void processOneVideo(Intent data) {
        try {
            Uri selectedVideo = data.getData();
            // OI FILE Manager
            String filemanagerstring = selectedVideo.getPath();
            // MEDIA GALLERY
            String path = FileProcessing.getVideoPath(selectedVideo, VideoActivity.this);
            if (path== null) {
               path=filemanagerstring;
            }
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            //use one of overloaded setDataSource() functions to set your data source
            retriever.setDataSource(VideoActivity.this, Uri.parse(path));
            String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);

            if (path!=null) {
                CompresVideoTask(path, mVideoConfig, VideoActivity.this);
            }else {
                Intent intent = new Intent();
                intent.setAction(VideoTags.Action.SERVICE_ACTION);
                intent.putExtra(VideoTags.Tags.PICK_ERROR, "Issue with video path: ");
                sendBroadcast(intent);
                setResult(RESULT_CANCELED, intent);
                finish();
            }

        } catch (Exception ex) {
            Intent intent = new Intent();
            intent.setAction(VideoTags.Action.SERVICE_ACTION);
            intent.putExtra(VideoTags.Tags.PICK_ERROR, "Issue with video path: " + ex.getMessage());
            sendBroadcast(intent);
            setResult(RESULT_CANCELED, intent);
            finish();
        }

    }

    private void finishActivity(List<String> path) {
        Intent intent = new Intent();
        intent.setAction(VideoTags.Action.SERVICE_ACTION);
        intent.putExtra(VideoTags.Tags.VIDEO_PATH, (Serializable) path);
        sendBroadcast(intent);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(VideoPicker.EXTRA_VIDEO_PATH, (Serializable) path);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void pickVideoWrapper() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissionsNeeded = new ArrayList<String>();

            final List<String> permissionsList = new ArrayList<String>();
            if ((mVideoConfig.mode == VideoPicker.Mode.CAMERA || mVideoConfig.mode == VideoPicker.Mode.CAMERA_AND_GALLERY) && !addPermission(permissionsList, Manifest.permission.CAMERA))
                permissionsNeeded.add(getString(R.string.media_picker_camera));
            if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
                permissionsNeeded.add(getString(R.string.media_picker_read_Write_external_storage));

            if (permissionsList.size() > 0) {
                if (permissionsNeeded.size() > 0) {
                    // Need Rationale
                    String message = getString(R.string.media_picker_you_need_to_grant_access_to) +" "+ permissionsNeeded.get(0);
                    for (int i = 1; i < permissionsNeeded.size(); i++)
                        message = message + ", " + permissionsNeeded.get(i);
                    showMessageOKCancel(message,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(VideoActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                                            VideoTags.IntentCode.REQUEST_CODE_ASK_PERMISSIONS);
                                }
                            },new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                               finish();
                        }
                    });
                    return;
                }
                ActivityCompat.requestPermissions(VideoActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                        VideoTags.IntentCode.REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }

            pickVideo();
        } else {
            pickVideo();
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
     if(VideoActivity.this!=null) {
         new AlertDialog.Builder(VideoActivity.this)
                 .setMessage(message)
                 .setCancelable(false)
                 .setPositiveButton(getString(R.string.media_picker_ok), okListener)
                 .setNegativeButton(getString(R.string.media_picker_cancel), cancelListener)
                 .create()
                 .show();
     }
     }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(VideoActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(VideoActivity.this, permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case VideoTags.IntentCode.REQUEST_CODE_ASK_PERMISSIONS:
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    pickVideo();
                } else {
                    // Permission Denied
                    if(VideoActivity.this!=null) {
                        Toast.makeText(VideoActivity.this, getString(R.string.media_picker_some_permission_is_denied), Toast.LENGTH_SHORT)
                                .show();
                    }
                      finish();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void CompresVideoTask(String absolutePath, VideoConfig videoConfig, VideoActivity context) {

        Log.e(TAG, "CompresVideoTask");


        List<String> destinationPaths = new ArrayList<>();

        File file = new File(absolutePath);

        destinationPaths.add(file.getAbsolutePath());

        if (context != null) {
            finishActivity(destinationPaths);
        }
    }

   /* private static class CompresVideoTask extends AsyncTask<Void, Void, Void> {

        private final VideoConfig mVideoConfig;
        private final List<String> listOfImgs;
        private List<String> destinationPaths;
        private WeakReference<VideoActivity> mContext;
        private ProgressDialog mProgressDialog;


       *//* public CompresVideoTask(List<String> listOfImgs, VideoConfig videoConfig, VideoActivity context) {
            this.listOfImgs = listOfImgs;
            this.mContext = new WeakReference<>(context);
            this.mVideoConfig = videoConfig;
            this.destinationPaths = new ArrayList<>();

            try {
                mProgressDialog = new ProgressDialog(mContext.get());
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage("Processing");
            } catch (Exception e) {
               Log.e(TAG, "", e);
            }
        }*//*

        public CompresVideoTask(String absolutePath, VideoConfig videoConfig, VideoActivity context) {

            Log.e(TAG, "CompresVideoTask");

            List<String> list = new ArrayList<>();
            list.add(absolutePath);
            this.listOfImgs = list;
            this.mContext = new WeakReference<>(context);
            this.destinationPaths = new ArrayList<>();
            this.mVideoConfig = videoConfig;

            File file = new File(absolutePath);

            destinationPaths.add(file.getAbsolutePath());

            if (context != null) {
                context.finishActivity(destinationPaths);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {

                Log.e(TAG, "CompresVideoTask onPreExecute");
                Log.e(TAG, "CompresVideoTask listOfImgs: "+listOfImgs.size());

                if (mProgressDialog!=null)
                    mProgressDialog.show();

            } catch (Exception e) {
               Log.e(TAG,"", e);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                Log.e(TAG, "CompresVideoTask doInBackground");


                for (String mPath : listOfImgs) {

                    Log.e(TAG, "File Path: "+mPath);

                    File file = new File(mPath);
                    File destinationFile;

                    if (mVideoConfig.isImgFromCamera) {
                        Log.e(TAG, "isImgFromCamera true FilePath: " +mPath);
                        destinationFile = file;
                    } else {
                        Log.e(TAG, "isImgFromCamera false FilePath: " +mPath);
                        destinationFile = file;

                        //new File(mVideoConfig.directory, Utility.getRandomString() + mVideoConfig.extension.getValue());
                        //FileProcessing.copyDirectory(file, destinationFile);
//                        destinationFile = file;
                    }
                    destinationPaths.add(destinationFile.getAbsolutePath());
    //
                }
            } catch (Exception e) {
               Log.e(TAG,"", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

           *//* try {
                Log.e(TAG, "CompresVideoTask onPostExecute");
                if (mProgressDialog!=null)
                    mProgressDialog.dismiss();
            } catch (Exception e) {
               Log.e(TAG, "", e);
            }*//*

          *//*  VideoActivity context = mContext.get();
            if (context != null) {
                context.finishActivity(destinationPaths);
            }*//*
        }
    }*/


}