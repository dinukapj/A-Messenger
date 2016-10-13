package com.kaodim.messenger.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.kaodim.messenger.R;
import com.kaodim.messenger.tools.FileHelper;
import com.kaodim.messenger.tools.ImageHelper;

import java.io.File;
import java.io.IOException;

/**
 * Created by Kanskiy on 12/10/2016.
 */

public class PreviewActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    public static final int INTENT_TYPE_CAMERA = 0;
    public static final int INTENT_TYPE_SELECTION = 1;

    public static final int REQUEST_CODE_IMAGE_CAPTURE = 11;
    public static final int REQUEST_CODE_IMAGE_SELECTION = 111;

    private final int MY_PERMISSIONS_READ_GALLERY =1;
    private final int MY_PERMISSIONS_REQUEST_CAMERA =2;

    private int intentType;
    private String mCurrentPhotoPath;
    private EditText etCaption;
    AQuery aq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        setTitle(getString(R.string.messenger_file_preview));
        Intent receivedIntent  = getIntent();
        intentType = receivedIntent.getIntExtra("intentType",-1);
        aq = new AQuery(this);
        etCaption = (EditText)findViewById(R.id.etCaption);
        etCaption.clearComposingText();
        aq.id(R.id.llContainer).clicked(this,"closeKeyboard");
        switch (intentType){
            case INTENT_TYPE_CAMERA:
                captureImage();
                break;
            case INTENT_TYPE_SELECTION:
                openGallery();
                break;
            default:
                setResult(RESULT_CANCELED);
                finish();
        }
    }
    public void closeKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //CAMERA
    private void captureImage(){
        if (hasCameraHardware(this)) {
            boolean hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            boolean hasWriteStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (!hasCameraPermission || !hasWriteStoragePermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_CAMERA);

                return;
            }
            dispatchTakePictureIntent();
            return;
        }
        Toast.makeText(this, getString(R.string.messenger_no_camera), Toast.LENGTH_LONG).show();
    }

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = FileHelper.createEmptyFile(FileHelper.FILE_JPG, Environment.DIRECTORY_PICTURES);
                mCurrentPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent,REQUEST_CODE_IMAGE_CAPTURE);
            }
        }
    }

    //GALLERY
    private void openGallery(){
        boolean hasReadStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!hasReadStoragePermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_READ_GALLERY);

            return;
        }
        displatchOpenGalleryIntent();
    }
    private void displatchOpenGalleryIntent(){
        Intent intent = new Intent();
//        intent.setType("image/*|application/pdf|text/plain");
        intent.setType("*/*");

        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/*", "application/pdf","text/plain"});
        startActivityForResult(Intent.createChooser(intent, getString(R.string.messenger_select_file)),REQUEST_CODE_IMAGE_SELECTION);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                }
                return;
            }
            case MY_PERMISSIONS_READ_GALLERY:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displatchOpenGalleryIntent();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode!=RESULT_OK){
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        try {
            switch (requestCode){
                case REQUEST_CODE_IMAGE_CAPTURE:
                    ImageHelper.setupImageOrientation(mCurrentPhotoPath);
                    break;
                case REQUEST_CODE_IMAGE_SELECTION:
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri==null){
                        return;
                    }
                    mCurrentPhotoPath =FileHelper.getPath(selectedImageUri,this);
                    break;
                default:
                    mCurrentPhotoPath = null;
            }




        } catch (IOException e) {
            e.printStackTrace();
            displayNoFileExcceptionDialog();
            mCurrentPhotoPath = null;
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            displayNoFileExcceptionDialog();
            mCurrentPhotoPath = null;
        }


        displayFile();



    }
    private void displayFile(){
        switch (FileHelper.getFileType(mCurrentPhotoPath)){
            case FileHelper.FILE_TYPE_DOCUMENT:
                aq.id(R.id.cardDocument).visible();
                aq.id(R.id.ivPreview).gone();
                aq.id(R.id.tvDocName).text(FileHelper.getFileNameFromPath(mCurrentPhotoPath));
                break;
            case FileHelper.FILE_TYPE_IMAGE:
                aq.id(R.id.cardDocument).gone();
                aq.id(R.id.ivPreview).visible().image(new File(mCurrentPhotoPath),1016);
                break;
            default:
                Intent resultIntent = new Intent();
                resultIntent.putExtra("reason", getString(R.string.messenger_file_not_supported));
                setResult(RESULT_CANCELED,resultIntent);
                finish();
                break;
        }
    }
    private void displayNoFileExcceptionDialog(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.messenger_file_not_found))
                .setMessage(getString(R.string.messenger_unable_to_retrieve_file_required))
                .setPositiveButton(R.string.messenger_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
    private boolean hasCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item_send) {
            if (mCurrentPhotoPath!=null){
                String caption = etCaption.getText().toString();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("resultFilePath", mCurrentPhotoPath);
                resultIntent.putExtra("resultCaption", caption);
                setResult(RESULT_OK,resultIntent);
                finish();
            }else{
                Toast.makeText(this, getString(R.string.messenger_select_file),Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview_activity, menu);
        return true;
    }
}