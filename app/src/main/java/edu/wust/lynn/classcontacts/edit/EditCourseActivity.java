package edu.wust.lynn.classcontacts.edit;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.MainActivity;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.dialog.ChoosePhotoSourceFragment;

public class EditCourseActivity extends ListActivity {
    public static final String COURSE_DATA_CHANGED = "edu.wust.lynn.classcontacts.edit.COURSE_DATA_CHANGED";
    public static final String EDIT_COURSE = "edu.wust.lynn.classcontacts.edit.EDIT_CONTACT";

    private View mCustomView;
    private Boolean isAddCourse;
    private Boolean hasChangedPhoto;
    private Cursor cursor;
    private Bitmap photo;
    private String photoPath;

    private ImageView photoView;
    private TextView idField;
    private TextView courseNameField;
    private TextView teacherNameField;
    private TextView phoneField;

    private String photoName;
    private String courseID;
    private String courseName;
    private String teacherName;
    private String phone;
    private List<Boolean> checked;

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
        setContentView(R.layout.activity_edit_course);

        checked = new ArrayList<Boolean>();

        photoView = (ImageView) findViewById(R.id.edit_course_photo);
        idField = (TextView) findViewById(R.id.edit_course_id);
        courseNameField = (TextView) findViewById(R.id.edit_course_course_name);
        teacherNameField = (TextView) findViewById(R.id.edit_course_teacher_name);
        phoneField = (TextView) findViewById(R.id.edit_course_phone);

        isAddCourse = true;
        hasChangedPhoto = false;
        photo = null;
        courseID = getIntent().getStringExtra(College.Student._ID);

        if (courseID != null) {
            isAddCourse = false;
            new RestoreCourseTask().execute();
        }

        setAdapter();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        CheckBox checkBox = (CheckBox)v.findViewById(R.id.optional_student_check_box);
        if (checkBox.isChecked()) {
            checkBox.setChecked(false);
            checked.set(position, false);
        } else {
            checkBox.setChecked(true);
            checked.set(position, true);
        }
        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCustomView = getLayoutInflater().inflate(R.layout.custom_course_edit_view, null);
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem saveMenu = menu.add(getResources().getString(R.string.menu_save));
        saveMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        saveMenu.setIcon(R.drawable.ic_action_accept);
        saveMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                saveCourse();
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

    private void setAdapter() {
        SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
        String[] projection = {
                College.Student.COLUMN_NAME_NAME,
                College.Student._ID
        };

        String sortOrder = College.Student._ID + " ASC";
        Cursor cursor = db.query(
                College.Student.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        String[] from = {
                College.Student.COLUMN_NAME_NAME,
                College.Student._ID
        };
        int[] to = {
                R.id.optional_student_name,
                R.id.optional_student_id
        };
        setListAdapter(new CourseAdapter(this, R.layout.show_student_and_choose_course, cursor, from, to, 0));
    }

    private void saveCourse() {
        SQLiteDatabase db = MainActivity.collegeData.getWritableDatabase();
        courseID = idField.getText().toString();
        if (courseID.equals("")) {
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
            if (isAddCourse) {
                cursor = College.Course.findCourse(db, courseID);
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
                    new SaveCourseTask().execute();
                    finish();
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.message_edit_course_and_delete_data);
                builder.setPositiveButton(R.string.button_message_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new SaveCourseTask().execute();
                        finish();
                    }
                })
                        .setNegativeButton(R.string.button_message_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    private class CourseAdapter extends SimpleCursorAdapter {

        public CourseAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (!isAddCourse) {
                SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.optional_student_check_box);
                String studentID = cursor.getString(cursor.getColumnIndex(College.Student._ID));
                if (College.Optional.findOptional(db, courseID, studentID).getCount() == 0) {
                    checkBox.setChecked(false);
                    checked.add(false);
                } else {
                    checked.add(true);
                }
            } else {
                checked.add(true);
            }
            super.bindView(view, context, cursor);
        }
    }

    private class RestoreCourseTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            photoView.setImageBitmap(bitmap);

            idField.setText(courseID);
            idField.setFocusable(false);
            idField.setFocusableInTouchMode(false);

            courseNameField.setText(courseName);
            teacherNameField.setText(teacherName);
            phoneField.setText(phone);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
            cursor = College.Course.findCourse(db, courseID);
            cursor.moveToFirst();

            photoName = cursor.getString(cursor.getColumnIndex(College.Course.COLUMN_NAME_PHOTO_NAME));
            courseName = cursor.getString(cursor.getColumnIndex(College.Course.COLUMN_NAME_COURSE_NAME));
            teacherName = cursor.getString(cursor.getColumnIndex(College.Course.COLUMN_NAME_TEACHER_NAME));
            phone = cursor.getString(cursor.getColumnIndex(College.Course.COLUMN_NAME_PHONE));

            FileInputStream stream = null;
            try {
                stream = openFileInput(photoName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            photo = BitmapFactory.decodeStream(stream);
            return photo;
        }
    }


    private class SaveCourseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent;
            if (isAddCourse) {
                intent = new Intent(COURSE_DATA_CHANGED);
            } else {
                intent = new Intent(EDIT_COURSE);
            }
            LocalBroadcastManager.getInstance(EditCourseActivity.this).sendBroadcast(intent);

            Toast toast = Toast.makeText(getApplicationContext(), R.string.message_course_saved, Toast.LENGTH_SHORT);
            toast.show();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            courseName = courseNameField.getText().toString();
            teacherName = teacherNameField.getText().toString();
            phone = phoneField.getText().toString();

            SQLiteDatabase db = MainActivity.collegeData.getWritableDatabase();
            if (isAddCourse) {
                photoName = ChoosePhotoSourceFragment.createFileName();
                new SavePhotoTask().execute();

                College.Course.insertCourse(
                        db,
                        courseID,
                        photoName + "temp",
                        courseName,
                        teacherName,
                        phone
                );
            } else {
                if (hasChangedPhoto) {
                    deleteFile(photoName);
                    photoName = ChoosePhotoSourceFragment.createFileName();
                    new SavePhotoTask().execute();
                }
                College.Course.updateCourse(
                        db,
                        courseID,
                        photoName + (hasChangedPhoto ? "temp" : ""),
                        courseName,
                        teacherName,
                        phone
                );
            }

            ListView listView = getListView();
            ListAdapter listAdapter = listView.getAdapter();
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View view = listAdapter.getView(i, null, null);
                TextView studentIDView = (TextView) view.findViewById(R.id.optional_student_id);
                String studentID = studentIDView.getText().toString();
                if (checked.get(i)) {
                    Cursor findCursor = College.Optional.findOptional(db, courseID, studentID);
                    if (findCursor.getCount() == 0) {
                        College.Optional.insertOptional(
                                db,
                                courseID,
                                studentID
                        );
                    }
                } else if (!isAddCourse) {
                    College.Optional.deleteOptional(db, courseID, studentID);
                }
            }
            return null;
        }
    }

    private class SavePhotoTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (photo == null) {
                Drawable drawable = getResources().getDrawable(R.drawable.course_photo);
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
            String sql = "UPDATE " + College.Course.TABLE_NAME
                    + " SET " + College.Course.COLUMN_NAME_PHOTO_NAME + " = ?"
                    + " WHERE " + College.Course.COLUMN_NAME_PHOTO_NAME + " = ?;";
            String[] selectionArgs = {photoName, photoName + "temp"};
            db.execSQL(sql, selectionArgs);

            Intent intent;
            if (isAddCourse) {
                intent = new Intent(COURSE_DATA_CHANGED);
            } else {
                intent = new Intent(EDIT_COURSE);
            }
            LocalBroadcastManager.getInstance(EditCourseActivity.this).sendBroadcast(intent);
            super.onPostExecute(aVoid);
        }
    }
}
