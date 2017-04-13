package edu.wust.lynn.classcontacts.edit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.MainActivity;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.dialog.AddRoomFragment;
import edu.wust.lynn.classcontacts.dialog.ChoosePhotoSourceFragment;

public class EditStudentActivity extends Activity {
    public static final String CONTACT_DATA_CHANGED = "edu.wust.lynn.classcontacts.edit.CONTACT_DATA_CHANGED";
    public static final String EDIT_CONTACT = "edu.wust.lynn.classcontacts.edit.EDIT_CONTACT";

    private View mCustomView;
    private Boolean isAddContact;
    private Boolean hasChangedPhoto;
    private Cursor cursor;
    private Bitmap photo;
    private String photoPath;

    private AddRoomFragment addRoomFragment;
    private RefreshSpinnerReceiver refreshSpinnerReceiver;
    private List<String> list;

    private ImageView photoView;
    private EditText nameField;
    private EditText idField;
    private RadioButton boyButton;
    private RadioButton girlButton;
    private EditText phoneField;
    private Spinner roomSpinner;
    private EditText notesField;

    private String photoName;
    private String studentName;
    private String studentID;
    private String gender;
    private String roomID;
    private String phone;
    private String notes;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ChoosePhotoSourceFragment.REQUEST_SELECT_PHOTO:
                    photoPath = getIntent().getStringExtra(ChoosePhotoSourceFragment.PHOTO_PATH);
                    photo = BitmapFactory.decodeFile(photoPath, null);
                    photoView.setImageBitmap(photo);
                    hasChangedPhoto = true;
                    break;
                case ChoosePhotoSourceFragment.REQUEST_TAKE_PHOTO:
                    photoPath = getIntent().getStringExtra(ChoosePhotoSourceFragment.PHOTO_PATH);
                    File photoFile = new File(photoPath);
                    Uri uri = Uri.fromFile(photoFile);
                    cropPhoto(uri);
                    break;
                case ChoosePhotoSourceFragment.REQUEST_CROP_PHOTO:
                    photo = BitmapFactory.decodeFile(photoPath, null);
                    photoView.setImageBitmap(photo);
                    hasChangedPhoto = true;
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        photoView = (ImageView) findViewById(R.id.edit_photo);
        nameField = (EditText) findViewById(R.id.edit_name);
        idField = (EditText) findViewById(R.id.edit_id);
        boyButton = (RadioButton) findViewById(R.id.boy_button);
        girlButton = (RadioButton) findViewById(R.id.girl_button);
        phoneField = (EditText) findViewById(R.id.edit_phone);
        roomSpinner = (Spinner) findViewById(R.id.edit_room);
        notesField = (EditText) findViewById(R.id.edit_notes);

        refreshSpinnerReceiver = new RefreshSpinnerReceiver();
        IntentFilter filter = new IntentFilter(AddRoomFragment.ROOM_DATA_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshSpinnerReceiver, filter);

        isAddContact = true;
        hasChangedPhoto = false;
        photo = null;
        studentID = getIntent().getStringExtra(College.Student._ID);
        gender = College.Student.VALUE_GENDER_BOY;

        if (studentID != null) {
            isAddContact = false;
            new RestoreContactTask().execute();
        }

        setSpinnerAdapter();
        roomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                roomID = ((TextView) view).getText().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCustomView = getLayoutInflater().inflate(R.layout.custom_contact_edit_view, null);
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshSpinnerReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem saveMenu = menu.add(getResources().getString(R.string.menu_save));
        saveMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        saveMenu.setIcon(R.drawable.ic_action_accept);
        saveMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                saveContact();
                return true;
            }
        });
        return true;
    }

    public void choosePhoto(View view) {
        ChoosePhotoSourceFragment dialog = new ChoosePhotoSourceFragment();
        dialog.show(getFragmentManager(), "missiles");
    }

    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", ChoosePhotoSourceFragment.photoWidth);
        intent.putExtra("outputY", ChoosePhotoSourceFragment.photoWidth);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, ChoosePhotoSourceFragment.REQUEST_CROP_PHOTO);
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.boy_button:
                if (checked) {
                    gender = College.Student.VALUE_GENDER_BOY;
                }
                break;
            case R.id.girl_button:
                if (checked) {
                    gender = College.Student.VALUE_GENDER_GIRL;
                }
                break;
        }
        setSpinnerAdapter();
    }

    public void addRoom(View view) {
        addRoomFragment = new AddRoomFragment();
        addRoomFragment.show(getFragmentManager(), null);
    }

    private void saveContact() {
        SQLiteDatabase db = MainActivity.collegeData.getWritableDatabase();
        studentID = idField.getText().toString();
        if (studentID.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.message_blank_id);
            builder.setPositiveButton(R.string.button_message_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            if (isAddContact) {
                cursor = College.Student.findStudentByStudentID(db, studentID);
                if (cursor.getCount() != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.message_id_exist);
                    builder.setPositiveButton(R.string.button_message_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    new SaveContactTask().execute();
                    finish();
                }
            } else {
                new SaveContactTask().execute();
                finish();
            }
        }
    }

    public void setSpinnerAdapter() {
        SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
        String[] projection = {
                College.Room._ID,
        };
        String selection = College.Room.COLUMN_NAME_GENDER + " = ?";
        String[] selectionArgs = {gender};
        String sortOrder = College.Room._ID + " ASC";
        Cursor cursor = db.query(
                College.Room.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        list = new ArrayList<String>();
        list.add("");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(cursor.getString(cursor.getColumnIndex(College.Room._ID)));
            cursor.moveToNext();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list);
        roomSpinner.setAdapter(adapter);
        int index = list.indexOf(roomID);
        roomSpinner.setSelection(index);
    }

    private class RefreshSpinnerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (addRoomFragment.hasAddedRoom()) {
                String type = addRoomFragment.getType();
                if (type.equals(gender)) {
                    roomID = addRoomFragment.getAddress();
                }
            }
            setSpinnerAdapter();
        }
    }

    private class RestoreContactTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... params) {
            SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
            cursor = College.Student.findStudentByStudentID(db, studentID);
            cursor.moveToFirst();

            FileInputStream stream = null;
            try {
                photoName = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_PHOTO_NAME));
                stream = openFileInput(photoName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            photo = BitmapFactory.decodeStream(stream);
            return photo;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            cursor.moveToFirst();
            photoView.setImageBitmap(bitmap);

            studentName = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_NAME));
            studentID = cursor.getString(cursor.getColumnIndex(College.Student._ID));
            gender = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_GENDER));
            roomID = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_ROOM_ID));
            phone = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_PHONE));
            notes = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_NOTES));

            nameField.setText(studentName);

            idField.setText(studentID);
            idField.setFocusable(false);
            idField.setFocusableInTouchMode(false);

            if (gender.equals(College.Student.VALUE_GENDER_BOY)) {
                boyButton.setChecked(true);
            } else {
                girlButton.setChecked(true);
            }

            phoneField.setText(phone);

            int index = list.indexOf(roomID);
            roomSpinner.setSelection(index);

            notesField.setText(notes);
        }
    }

    private class SaveContactTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent;
            if (isAddContact) {
                intent = new Intent(CONTACT_DATA_CHANGED);
            } else {
                intent = new Intent(EDIT_CONTACT);
            }
            LocalBroadcastManager.getInstance(EditStudentActivity.this).sendBroadcast(intent);

            Toast toast = Toast.makeText(getApplicationContext(), R.string.message_contact_saved, Toast.LENGTH_SHORT);
            toast.show();
            new SaveStudentFTS3Task().execute();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            studentName = nameField.getText().toString();
            phone = phoneField.getText().toString();
            notes = notesField.getText().toString();

            SQLiteDatabase db = MainActivity.collegeData.getWritableDatabase();
            if (isAddContact) {
                photoName = ChoosePhotoSourceFragment.createFileName();
                new SavePhotoTask().execute();
                College.Student.InsertStudent(
                        db,
                        studentID,
                        photoName + "temp",
                        studentName,
                        gender,
                        phone,
                        roomID,
                        notes
                );
            } else {
                if (hasChangedPhoto) {
                    deleteFile(photoName);
                    photoName = ChoosePhotoSourceFragment.createFileName();
                    new SavePhotoTask().execute();
                }
                College.Student.updateStudent(
                        db,
                        studentID,
                        photoName + (hasChangedPhoto ? "temp" : ""),
                        studentName,
                        gender,
                        phone,
                        roomID,
                        notes
                );
            }

            return null;
        }
    }

    private class SavePhotoTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (photo == null) {
                Drawable drawable = getResources().getDrawable(R.drawable.contact_photo);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                photo = bitmapDrawable.getBitmap();
            }
            FileOutputStream photoOutputStream = null;
            try {
                photoOutputStream = openFileOutput(photoName, Context.MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            photo.compress(Bitmap.CompressFormat.PNG, 100, photoOutputStream);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            SQLiteDatabase db = MainActivity.collegeData.getWritableDatabase();
            String sql = "UPDATE " + College.Student.TABLE_NAME
                    + " SET " + College.Student.COLUMN_NAME_PHOTO_NAME + " = ?"
                    + " WHERE " + College.Student.COLUMN_NAME_PHOTO_NAME + " = ?;";
            String[] selectionArgs = {photoName, photoName + "temp"};
            db.execSQL(sql, selectionArgs);

            Intent intent;
            if (isAddContact) {
                intent = new Intent(CONTACT_DATA_CHANGED);
            } else {
                intent = new Intent(EDIT_CONTACT);
            }
            LocalBroadcastManager.getInstance(EditStudentActivity.this).sendBroadcast(intent);
            super.onPostExecute(aVoid);
        }
    }

    private class SaveStudentFTS3Task extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase db = MainActivity.collegeData.getWritableDatabase();
            if (isAddContact) {
                College.StudentFTS3.insertStudentFTS3(
                        db,
                        studentID,
                        photoName,
                        studentName,
                        phone,
                        roomID
                );
            } else {
                College.StudentFTS3.updateStudentFTS3(
                        db,
                        studentID,
                        photoName,
                        studentName,
                        phone,
                        roomID
                );
            }
            return null;
        }
    }
}
