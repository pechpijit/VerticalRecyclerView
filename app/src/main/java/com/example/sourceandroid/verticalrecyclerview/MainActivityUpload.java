package com.example.sourceandroid.verticalrecyclerview;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivityUpload extends AppCompatActivity implements UploadStatusDelegate,View.OnClickListener {
    private int PICK_IMAGE_REQUEST = 1;

    private static final String TAG = "UploadServiceDemo";
    private static final String USER_AGENT = "UploadServiceDemo/" + BuildConfig.VERSION_NAME;
    private AndroidPermissions mPermissions;
    ViewGroup container;

    private Button buttonChoose;
    private Button buttonUpload;
    private Button buttonCancel;

    private ImageView imageView;
    private EditText editText;
    private Uri filePath;
    private Bitmap bitmap;
    private Map<String, UploadProgressViewHolder> uploadProgressHolders = new HashMap<>();

    private static final int STORAGE_PERMISSION_CODE = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        imageView = (ImageView) findViewById(R.id.imageView);
        editText = (EditText) findViewById(R.id.editText);
        container = (ViewGroup) findViewById(R.id.container);

        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
//        mPermissions = new AndroidPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        requestStoragePermission();
    }

    private void logSuccessfullyUploadedFiles(List<String> files) {
        for (String file : files) {
            Log.e(TAG, "Success: " + file);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private UploadNotificationConfig getNotificationConfig(String filename) {
//        if (!displayNotification.isChecked()) return null;

        return new UploadNotificationConfig()
                .setIcon(R.drawable.ic_upload)
                .setCompletedIcon(R.drawable.ic_upload_success)
                .setErrorIcon(R.drawable.ic_upload_error)
                .setTitle(filename)
                .setInProgressMessage(getString(R.string.uploading))
                .setCompletedMessage(getString(R.string.upload_success))
                .setErrorMessage(getString(R.string.upload_error))
                .setAutoClearOnSuccess(true)
                .setClickIntent(new Intent(this, MainActivity.class))
                .setClearOnAction(true)
                .setRingToneEnabled(true);
    }

    private void addUploadToList(String uploadID, String filename) {
        View uploadProgressView = getLayoutInflater().inflate(R.layout.view_upload_progress, null);
        UploadProgressViewHolder viewHolder = new UploadProgressViewHolder(uploadProgressView, filename);
        viewHolder.uploadId = uploadID;
        container.addView(viewHolder.itemView, 0);
        uploadProgressHolders.put(uploadID, viewHolder);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            Toast.makeText(MainActivityUpload.this, ""+filePath, Toast.LENGTH_SHORT).show();
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//                imageView.setImageBitmap(bitmap);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    public void onMultipartUploadClick() {

//        String path = getPath(filePath); // android < 6.0
//        String path = getRealPathFromURI(MainActivityUpload.this,filePath); // android < 6.0

            try {
                MultipartUploadRequest req = new MultipartUploadRequest(this, "http://aof.commsk.com/upimg/index.php")
                        .addFileToUpload(Getpath.getPath(MainActivityUpload.this,filePath), "pic") // android >= 6.0
                        .setNotificationConfig(getNotificationConfig("เทพ"))
                        .setCustomUserAgent(USER_AGENT)
                        .setMaxRetries(3);

                String uploadID = req.setDelegate(this).startUpload();

                addUploadToList(uploadID,"เทพ");

                // these are the different exceptions that may be thrown
            } catch (FileNotFoundException exc) {
                showToast(exc.getMessage());
            } catch (IllegalArgumentException exc) {
                showToast("Missing some arguments. " + exc.getMessage());
            } catch (MalformedURLException exc) {
                showToast(exc.getMessage());
            }
    }


    public void onCancelAllUploadsButtonClick() {
        UploadService.stopAllUploads();
    }

    //method to get the file path from uri
    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonChoose:
                showFileChooser();
                break;
            case R.id.buttonUpload:
                onMultipartUploadClick();
                break;
            case R.id.buttonCancel:
                onCancelAllUploadsButtonClick();
                break;
        }
    }

    class UploadProgressViewHolder implements View.OnClickListener{
        View itemView;

        TextView uploadTitle;
        ProgressBar progressBar;
        Button btnCancle;

        String uploadId;

        UploadProgressViewHolder(View view, String filename) {
            itemView = view;

            uploadTitle = (TextView) view.findViewById(R.id.uploadTitle);
            progressBar = (ProgressBar) view.findViewById(R.id.uploadProgress);
            btnCancle = (Button) view.findViewById(R.id.cancelUploadButton);
            btnCancle.setOnClickListener(this);
            progressBar.setMax(100);
            progressBar.setProgress(0);

            uploadTitle.setText(getString(R.string.upload_progress, filename));
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.cancelUploadButton) {
                if (uploadId == null)
                    return;

                UploadService.stopUpload(uploadId);
            }
        }
    }

    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
        Log.i(TAG, String.format(Locale.getDefault(), "ID: %1$s (%2$d%%) at %3$.2f Kbit/s",
                uploadInfo.getUploadId(), uploadInfo.getProgressPercent(),
                uploadInfo.getUploadRate()));
        logSuccessfullyUploadedFiles(uploadInfo.getSuccessfullyUploadedFiles());

        if (uploadProgressHolders.get(uploadInfo.getUploadId()) == null)
            return;

        uploadProgressHolders.get(uploadInfo.getUploadId())
                .progressBar.setProgress(uploadInfo.getProgressPercent());
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, Exception exception) {
        Log.e(TAG, "Error with ID: " + uploadInfo.getUploadId() + ": "
                + exception.getLocalizedMessage(), exception);
        logSuccessfullyUploadedFiles(uploadInfo.getSuccessfullyUploadedFiles());

        if (uploadProgressHolders.get(uploadInfo.getUploadId()) == null)
            return;

        container.removeView(uploadProgressHolders.get(uploadInfo.getUploadId()).itemView);
        uploadProgressHolders.remove(uploadInfo.getUploadId());
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        Log.i(TAG, String.format(Locale.getDefault(),
                "ID %1$s: completed in %2$ds at %3$.2f Kbit/s. Response code: %4$d, body:[%5$s]",
                uploadInfo.getUploadId(), uploadInfo.getElapsedTime() / 1000,
                uploadInfo.getUploadRate(), serverResponse.getHttpCode(),
                serverResponse.getBodyAsString()));
        logSuccessfullyUploadedFiles(uploadInfo.getSuccessfullyUploadedFiles());
        for (Map.Entry<String, String> header : serverResponse.getHeaders().entrySet()) {
            Log.i("Header", header.getKey() + ": " + header.getValue());
        }

        Log.e(TAG, "Printing response body bytes");
        byte[] ba = serverResponse.getBody();
        for (int j = 0; j < ba.length; j++) {
            Log.e(TAG, String.format("%02X ", ba[j]));
        }

        if (uploadProgressHolders.get(uploadInfo.getUploadId()) == null)
            return;

        container.removeView(uploadProgressHolders.get(uploadInfo.getUploadId()).itemView);
        uploadProgressHolders.remove(uploadInfo.getUploadId());
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        Log.i(TAG, "Upload with ID " + uploadInfo.getUploadId() + " is cancelled");
        logSuccessfullyUploadedFiles(uploadInfo.getSuccessfullyUploadedFiles());

        if (uploadProgressHolders.get(uploadInfo.getUploadId()) == null)
            return;

        container.removeView(uploadProgressHolders.get(uploadInfo.getUploadId()).itemView);
        uploadProgressHolders.remove(uploadInfo.getUploadId());
    }
}
