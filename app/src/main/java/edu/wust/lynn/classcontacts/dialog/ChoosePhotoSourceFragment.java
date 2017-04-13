package edu.wust.lynn.classcontacts.dialog;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.wust.lynn.classcontacts.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChoosePhotoSourceFragment extends DialogFragment {
    public static final String PHOTO_PATH = "edu.wust.lynn.classcontacts.edit.PHOTO_PATH";
    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_SELECT_PHOTO = 2;
    public static final int REQUEST_CROP_PHOTO = 3;


    public static int photoWidth = 1080;
    public static int photoHeight = 1080;

    private String photoPath;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_choose_photo_source)
                .setItems(R.array.choose_photo_array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                takePhoto();
                                getActivity().getIntent().putExtra(PHOTO_PATH, photoPath);
                                break;
                            case 1:
                                selectPhoto();
                                getActivity().getIntent().putExtra(PHOTO_PATH, photoPath);
                                break;
                        }
                    }
                });
        return builder.create();
    }

    public static String createFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "JPEG_" + timeStamp + "_";
        return fileName;
    }

    private File createImageFile() throws IOException {
        String imageFileName = createFileName();
        File storageDir;
        storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        photoPath = image.getAbsolutePath();
        return image;
    }



    public void selectPhoto() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.d("lynn", "Create File fail");
        }
        if (photoFile != null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            Uri uri = Uri.fromFile(photoFile);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", photoWidth);
            intent.putExtra("outputY", photoHeight);
            intent.putExtra("scale", true);
            intent.putExtra("scaleUpIfNeeded", true);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            getActivity().startActivityForResult(intent, REQUEST_SELECT_PHOTO);
        }
    }

    public void takePhoto() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.d("lynn", "Create File fail");
        }
        if (photoFile != null) {
            Uri uri = Uri.fromFile(photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            getActivity().startActivityForResult(intent , REQUEST_TAKE_PHOTO);
        }
    }
}
