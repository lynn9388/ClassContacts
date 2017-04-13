package edu.wust.lynn.classcontacts.edit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.MainActivity;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.browse.CourseDetailActivity;

import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageResizer;
import com.example.android.displayingbitmaps.util.ImageWorker;

import org.w3c.dom.Text;

import java.io.File;

public class CheckActivity extends FragmentActivity {
    private static final String IMAGE_CACHE_DIR = "check_thumbs";
    public static final String CHECK_DATA_CHANGED = "edu.wust.lynn.classcontacts.edit.CHECK_DATA_CHANGED";
    public static final String EDIT_CHECK = "edu.wust.lynn.classcontacts.edit.EDIT_CHECK";

    private View mCustomView;
    private GridView gridView;
    private Boolean isAddCheck;
    private ImageResizer mImageResizer;

    private String normalState;
    private String leaveState;
    private String absentState;

    private String courseID;
    private int recordID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        mImageResizer = new ImageResizer(this, 400, 400);
        mImageResizer.addImageCache(this, "Student");
        mImageResizer.setImageFadeIn(false);

        normalState = getResources().getString(R.string.text_normal_state);
        leaveState = getResources().getString(R.string.text_leave_state);
        absentState = getResources().getString(R.string.text_absent_state);

        gridView = (GridView) findViewById(R.id.check_gridview);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView stateView = (TextView) view.findViewById(R.id.show_check_student_state);
                String state = stateView.getText().toString();
                if (state.equals(normalState)) {
                    setState(view, absentState);
                } else if (state.equals(leaveState)) {
                    setState(view, normalState);
                } else if (state.equals(absentState)) {
                    setState(view, leaveState);
                }
            }
        });

        isAddCheck = true;
        courseID = getIntent().getStringExtra(College.Optional._ID);
        recordID = getIntent().getIntExtra(College.Record._ID, -1);
        if (recordID >= 0) {
            isAddCheck = false;
            courseID = getIntent().getStringExtra(College.Record.COLUMN_NAME_COURSE_ID);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCustomView = getLayoutInflater().inflate(R.layout.custom_check_view, null);
        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);

        setAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem saveMenu = menu.add(getResources().getString(R.string.menu_save));
        saveMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        saveMenu.setIcon(R.drawable.ic_action_accept);
        saveMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                saveCheck();
                return true;
            }
        });

        if (!isAddCheck) {
            MenuItem deleteMenu = menu.add(getResources().getString(R.string.action_delete));
            deleteMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            deleteMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    deleteCheck();
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void setState(View view, String state) {
        TextView stateView = (TextView) view.findViewById(R.id.show_check_student_state);
        ImageView coverView = (ImageView) view.findViewById(R.id.show_check_student_cover);
        RelativeLayout bar = (RelativeLayout) view.findViewById(R.id.show_check_student_bar);
        if (normalState.equals(state)) {
            stateView.setText(normalState);
            stateView.setTag(College.State.VALUE_STATE_NORMAL);
            coverView.setBackgroundColor(getResources().getColor(R.color.transparent_normal));
            bar.setBackgroundColor(getResources().getColor(R.color.normal));
        } else if (leaveState.equals(state)) {
            stateView.setText(leaveState);
            stateView.setTag(College.State.VALUE_STATE_LEAVE);
            coverView.setBackgroundColor(getResources().getColor(R.color.transparent_leave));
            bar.setBackgroundColor(getResources().getColor(R.color.leave));
        } else if (absentState.equals(state)) {
            stateView.setText(absentState);
            stateView.setTag(College.State.VALUE_STATE_ABSENT);
            coverView.setBackgroundColor(getResources().getColor(R.color.transparent_absent));
            bar.setBackgroundColor(getResources().getColor(R.color.absent));
        }
    }

    private Cursor refreshCusor() {
        SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
        Cursor cursor = null;
        if (isAddCheck) {
            String sql = "SELECT " + College.Student.COLUMN_NAME_PHOTO_NAME + ", "
                    + College.Student.COLUMN_NAME_NAME + ", "
                    + College.Student.TABLE_NAME + "." + College.Student._ID
                    + " FROM " + College.Student.TABLE_NAME + " INNER JOIN " + College.Optional.TABLE_NAME
                    + " ON " + College.Student.TABLE_NAME + "." + College.Student._ID + " = "
                    + College.Optional.TABLE_NAME + "." + College.Optional.COLUMN_NAME_STUDENT_ID
                    + " WHERE " + College.Optional.TABLE_NAME + "." + College.Optional._ID + " = ?;";
            String[] selectionArgs = {courseID};
            cursor = db.rawQuery(sql, selectionArgs);
        } else {
            String sql = "SELECT " + College.Student.COLUMN_NAME_PHOTO_NAME + ", "
                    + College.Student.COLUMN_NAME_NAME + ", "
                    + College.Student.TABLE_NAME + "." + College.Student._ID + ", "

                    + " (SELECT " + College.State.COLUMN_NAME_STATE
                    + " FROM " + College.State.TABLE_NAME
                    + " WHERE " + College.State._ID + " = " + recordID
                    + " AND " + College.State.COLUMN_NAME_STUDENT_ID + " = "
                    + College.Student.TABLE_NAME + "." + College.Student._ID
                    + " ) " + College.State.COLUMN_NAME_STATE

                    + " FROM " + College.Student.TABLE_NAME + " INNER JOIN " + College.State.TABLE_NAME
                    + " ON " + College.Student.TABLE_NAME + "." + College.Student._ID + " = "
                    + College.State.TABLE_NAME + "." + College.State.COLUMN_NAME_STUDENT_ID
                    + " WHERE " + College.State.TABLE_NAME + "." + College.State._ID + " = " + recordID + ";";
            cursor = db.rawQuery(sql, null);
        }
        return cursor;
    }

    public void setAdapter() {
        Cursor cursor = refreshCusor();
        String[] from = {
                College.Student.COLUMN_NAME_NAME,
                College.Student._ID
        };
        int[] to = {
                R.id.show_check_student_name,
                R.id.show_check_student_id
        };
        CheckAdapter adapter = new CheckAdapter(this, R.layout.show_check_student, cursor, from, to, 0);
        gridView.setAdapter(adapter);
    }

    private void saveCheck() {
        new SaveCheckTask().execute();
        finish();
    }

    private void deleteCheck() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.message_delete_check);
        builder.setPositiveButton(R.string.button_message_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteCheckTask().execute();
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

    private class DeleteCheckTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase db = MainActivity.collegeData.getWritableDatabase();
            College.Record.deleteRecord(db, recordID);

            Intent intent = new Intent(CHECK_DATA_CHANGED);
            LocalBroadcastManager.getInstance(CheckActivity.this).sendBroadcast(intent);
            return null;
        }
    }

    private class CheckAdapter extends SimpleCursorAdapter {

        public CheckAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView imageView = (ImageView) view.findViewById(R.id.show_check_student_photo);
            String photoName = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_PHOTO_NAME));
            File file = getFileStreamPath(photoName);
            mImageResizer.loadImage(file.getAbsolutePath(), imageView);

            TextView stateView = (TextView) view.findViewById(R.id.show_check_student_state);
            stateView.setTag(College.State.VALUE_STATE_LEAVE);
            if (!isAddCheck) {
                String state = cursor.getString(cursor.getColumnIndex(College.State.COLUMN_NAME_STATE));
                setState(view, state);
            }
            super.bindView(view, context, cursor);
        }
    }

    private class SaveCheckTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = null;
            if (isAddCheck) {
                intent = new Intent(CHECK_DATA_CHANGED);
            } else {
                intent = new Intent(EDIT_CHECK);
            }
            LocalBroadcastManager.getInstance(CheckActivity.this).sendBroadcast(intent);

            Toast toast = Toast.makeText(getApplicationContext(), R.string.message_check_saved, Toast.LENGTH_SHORT);
            toast.show();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase db = MainActivity.collegeData.getWritableDatabase();
            ContentValues values;
            if (isAddCheck) {
                values = new ContentValues();
                values.put(College.Record.COLUMN_NAME_COURSE_ID, courseID);
                db.insert(College.Record.TABLE_NAME, null, values);
                String sql = "SELECT LAST_INSERT_ROWID() FROM " + College.Record.TABLE_NAME;
                Cursor cursor = db.rawQuery(sql, null);
                if (cursor.moveToFirst()) {
                    recordID = cursor.getInt(0);
                }
            } else {
                College.State.deleteRecordState(db, recordID);
            }

            for (int i = 0; i < gridView.getChildCount(); i++) {
                View view = gridView.getChildAt(i);
                TextView stateView = (TextView) view.findViewById(R.id.show_check_student_state);
                TextView idView = (TextView) view.findViewById(R.id.show_check_student_id);
                String studentID = idView.getText().toString();
                String state = (String)stateView.getTag();
                College.State.insertState(db, recordID, studentID, state);
            }
            return null;
        }
    }
}
