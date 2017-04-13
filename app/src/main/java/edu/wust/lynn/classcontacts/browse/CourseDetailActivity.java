package edu.wust.lynn.classcontacts.browse;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.MainActivity;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.edit.CheckActivity;
import edu.wust.lynn.classcontacts.edit.EditCourseActivity;

public class CourseDetailActivity extends Activity {

    public static final String NORMAL_COUNT = "NORMAL_COUNT";
    public static final String LEAVE_COUNT = "LEAVE_COUNT";
    public static final String ABSENT_COUNT = "ABSENT_COUNT";

    private RefreshEditReceiver refreshEditReceiver;
    private View mCustomView;
    private Cursor cursor;

    private ImageView photoView;
    private TextView courseNameView;
    private TextView idView;
    private TextView teacherNameViw;
    private TextView phoneView;
    private ListView statisticsView;

    private String photoName;
    private String courseName;
    private String courseID;
    private String teacherName;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        mCustomView = getLayoutInflater().inflate(R.layout.custom_course_detail_view, null);

        refreshEditReceiver = new RefreshEditReceiver();
        IntentFilter filter = new IntentFilter(EditCourseActivity.EDIT_COURSE);
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshEditReceiver, filter);
        filter = new IntentFilter(CheckActivity.CHECK_DATA_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshEditReceiver, filter);

        photoView = (ImageView) findViewById(R.id.show_course_detail_photo);
        courseNameView = (TextView) findViewById(R.id.show_course_detail_name);
        idView = (TextView) findViewById(R.id.show_course_detail_id);
        teacherNameViw = (TextView) findViewById(R.id.show_course_detail_teacher_name);
        phoneView = (TextView) findViewById(R.id.show_course_detail_phone);
        statisticsView = (ListView) findViewById(R.id.show_course_detail_statistics_list);

        courseID = getIntent().getStringExtra(College.Course._ID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);

        SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
        cursor = College.Course.findCourse(db, courseID);
        cursor.moveToFirst();

        photoName = cursor.getString(cursor.getColumnIndex(College.Course.COLUMN_NAME_PHOTO_NAME));
        courseName = cursor.getString(cursor.getColumnIndex(College.Course.COLUMN_NAME_COURSE_NAME));
        teacherName = cursor.getString(cursor.getColumnIndex(College.Course.COLUMN_NAME_TEACHER_NAME));
        phone = cursor.getString(cursor.getColumnIndex(College.Course.COLUMN_NAME_PHONE));

        new ShowPhotoTask().execute();
        courseNameView.setText(courseName);
        idView.setText(courseID);
        teacherNameViw.setText(teacherName);
        phoneView.setText(phone);

        setAdapter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshEditReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.course_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_course:
                editCourse();
                break;
            case R.id.action_delete_course:
                deleteCourse();
                break;
            case R.id.action_check_list:
                showCheckList();
                break;
            case R.id.action_add_check:
                addCheck();
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

    public void showStudentDetail(View view) {
        TextView nameView = (TextView)view.findViewById(R.id.show_student_statistics_id);
        String name = nameView.getText().toString();
        Intent intent = new Intent(this, StudentDetailActivity.class);
        intent.putExtra(College.Student._ID, name);
        startActivity(intent);
    }

    private class RefreshEditReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }
    }

    private Cursor refreshCursor() {
        SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
        String sql = "SELECT " + College.Student.TABLE_NAME + "." + College.Student.COLUMN_NAME_NAME + ", "
                + College.Student.TABLE_NAME + "." + College.Student._ID + ", "

                + " (SELECT COUNT(*) FROM " + College.State.TABLE_NAME
                + " WHERE " + College.State._ID
                + " IN (SELECT " + College.Record._ID + " FROM " + College.Record.TABLE_NAME
                + " WHERE " + College.Record.COLUMN_NAME_COURSE_ID + " = ?)"
                + " AND " + College.State.COLUMN_NAME_STUDENT_ID  + " = " + College.Student.TABLE_NAME + "." + College.Student._ID
                + " AND " + College.State.COLUMN_NAME_STATE + " = ?) " + NORMAL_COUNT + ", "

                + " (SELECT COUNT(*) FROM " + College.State.TABLE_NAME
                + " WHERE " + College.State._ID
                + " IN (SELECT " + College.Record._ID + " FROM " + College.Record.TABLE_NAME
                + " WHERE " + College.Record.COLUMN_NAME_COURSE_ID + " = ?)"
                + " AND " + College.State.COLUMN_NAME_STUDENT_ID  + " = " + College.Student.TABLE_NAME + "." + College.Student._ID
                + " AND " + College.State.COLUMN_NAME_STATE + " = ?) " + LEAVE_COUNT + ", "

                + " (SELECT COUNT(*) FROM " + College.State.TABLE_NAME
                + " WHERE " + College.State._ID
                + " IN (SELECT " + College.Record._ID + " FROM " + College.Record.TABLE_NAME
                + " WHERE " + College.Record.COLUMN_NAME_COURSE_ID + " = ?)"
                + " AND " + College.State.COLUMN_NAME_STUDENT_ID  + " = " + College.Student.TABLE_NAME + "." + College.Student._ID
                + " AND " + College.State.COLUMN_NAME_STATE + " = ?) " + ABSENT_COUNT

                + " FROM " + College.Student.TABLE_NAME + " INNER JOIN " +  College.Optional.TABLE_NAME
                + " ON " + College.Student.TABLE_NAME + "." + College.Student._ID + " = "
                + College.Optional.TABLE_NAME + "." + College.Optional.COLUMN_NAME_STUDENT_ID
                + " WHERE " + College.Optional.TABLE_NAME + "." + College.Optional._ID + " = ?;";
        String[] selectionArgs = {courseID, College.State.VALUE_STATE_NORMAL, courseID, College.State.VALUE_STATE_LEAVE, courseID, College.State.VALUE_STATE_ABSENT, courseID};
        return db.rawQuery(sql, selectionArgs);
    }

    private void setAdapter() {
        String[] from = {
                College.Student.COLUMN_NAME_NAME,
                College.Student._ID,
                NORMAL_COUNT,
                LEAVE_COUNT,
                ABSENT_COUNT
        };
        int[] to = {
                R.id.show_student_statistics_name,
                R.id.show_student_statistics_id,
                R.id.show_student_statistics_normal,
                R.id.show_student_statistics_leave,
                R.id.show_student_statistics_absent
        };
        StaticticsAdapter adapter = new StaticticsAdapter(this, R.layout.show_student_statistics, refreshCursor(), from, to, 0);
        statisticsView.setAdapter(adapter);
    }

    private void editCourse() {
        Intent intent = new Intent(this, EditCourseActivity.class);
        intent.putExtra(College.Course._ID, courseID);
        startActivity(intent);
    }

    private void deleteCourse() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.message_delete_course);
        builder.setPositiveButton(R.string.button_message_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                new DeleteCourseTask().execute();
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

    private void showCheckList() {
        Intent intent = new Intent(this, CheckListActivity.class);
        intent.putExtra(College.Course._ID, courseID);
        startActivity(intent);
    }

    private void addCheck() {
        Intent intent = new Intent(this, CheckActivity.class);
        intent.putExtra(College.Optional._ID, courseID);
        startActivity(intent);
    }

    private class StaticticsAdapter extends SimpleCursorAdapter {

        public StaticticsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
        }
    }

    private class DeleteCourseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase db = MainActivity.collegeData.getWritableDatabase();
            College.Course.deleteCourse(db, courseID);
            Context context = getApplicationContext();
            context.deleteFile(photoName);

            Intent intent = new Intent(EditCourseActivity.COURSE_DATA_CHANGED);
            LocalBroadcastManager.getInstance(CourseDetailActivity.this).sendBroadcast(intent);
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
