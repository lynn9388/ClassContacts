package edu.wust.lynn.classcontacts.browse;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.other.CustomScrollView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.MainActivity;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.dialog.DormitoryDetailFragment;
import edu.wust.lynn.classcontacts.edit.EditStudentActivity;

public class StudentDetailActivity extends FragmentActivity {

    private RefreshEditReceiver refreshEditReceiver;
    private View mCustomView;
    private Cursor cursor;

    private CustomScrollView mScrollView;
    private ImageView photoView;
    private TextView nameView;
    private TextView idView;
    private TextView phoneView;
    private TextView roomView;
    private TextView notesView;

    private String photoName;
    private String studentName;
    private String studentID;
    private String phone;
    private String roomID;
    private String notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        mCustomView = getLayoutInflater().inflate(R.layout.custom_student_detail_view, null);

        refreshEditReceiver = new RefreshEditReceiver();
        IntentFilter filter = new IntentFilter(EditStudentActivity.EDIT_CONTACT);
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshEditReceiver, filter);

        mScrollView = (CustomScrollView) findViewById(R.id.student_detail_scroll_view);

        photoView = (ImageView) findViewById(R.id.student_detail_photo);
        mScrollView.setImageView(photoView);


        nameView = (TextView) findViewById(R.id.student_detail_name);
        idView = (TextView) findViewById(R.id.student_detail_id);
        phoneView = (TextView) findViewById(R.id.student_detail_phone);
        roomView = (TextView) findViewById(R.id.student_detail_room);
        notesView = (TextView) findViewById(R.id.student_detail_notes);

        studentID = getIntent().getStringExtra(College.Student._ID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);

        SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
        cursor = College.Student.findStudentByStudentID(db, studentID);
        cursor.moveToFirst();

        photoName = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_PHOTO_NAME));
        studentName = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_NAME));
        phone = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_PHONE));
        roomID = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_ROOM_ID));
        notes = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_NOTES));

        new ShowPhotoTask().execute();
        nameView.setText(studentName);
        idView.setText(studentID);
        phoneView.setText(phone);
        roomView.setText(roomID);
        notesView.setText(notes);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshEditReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.student_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_contact:
                editContact();
                break;
            case R.id.action_delete_contact:
                deleteContact();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void callPhone(View view) {
        Uri number = Uri.parse("tel:" + phone);
        Intent intent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(intent);
    }

    public void sendMessage(View view) {
        Uri number = Uri.parse("smsto:" + phone);
        Intent intent = new Intent(Intent.ACTION_SENDTO, number);
        startActivity(intent);
    }

    public void showDormitory(View view) {
        if ("".equals(roomID)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.message_add_dormitory_info)
                    .setTitle(R.string.title_no_room_info)
                    .setPositiveButton(R.string.button_message_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editContact();
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
        } else {
            DormitoryDetailFragment fragment = DormitoryDetailFragment.newInstance(roomID);
            fragment.show(getSupportFragmentManager(), null);
        }
    }

    private class RefreshEditReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }
    }

    private void editContact() {
        Intent intent = new Intent(this, EditStudentActivity.class);
        intent.putExtra(College.Student._ID, studentID);
        startActivity(intent);
    }

    private void deleteContact() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.message_delete_contact);
        builder.setPositiveButton(R.string.button_message_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteContactTask().execute();
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

    private class DeleteContactTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase db = MainActivity.collegeData.getWritableDatabase();
            College.Student.deleteStudent(db, studentID);
            Context context = getApplicationContext();
            context.deleteFile(photoName);

            Intent intent = new Intent(EditStudentActivity.CONTACT_DATA_CHANGED);
            LocalBroadcastManager.getInstance(StudentDetailActivity.this).sendBroadcast(intent);
            return null;
        }
    }

    private class ShowPhotoTask extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            photoView.setImageBitmap(bitmap);
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            FileInputStream stream = null;
            try {
                stream = openFileInput(photoName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return BitmapFactory.decodeStream(stream);
        }
    }
}
