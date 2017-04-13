package edu.wust.lynn.classcontacts.browse;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.MainActivity;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.edit.CheckActivity;

public class CheckListActivity extends Activity {

    private RefreshListReceiver refreshListReceiver;
    private View mCustomView;
    private ListView listView;
    private Cursor cursor;

    private String courseID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);

        mCustomView = getLayoutInflater().inflate(R.layout.custom_course_list_view, null);

        refreshListReceiver = new RefreshListReceiver();
        IntentFilter filter = new IntentFilter(CheckActivity.EDIT_CHECK);
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshListReceiver, filter);
        filter = new IntentFilter(CheckActivity.CHECK_DATA_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshListReceiver, filter);

        courseID = getIntent().getStringExtra(College.Course._ID);

        listView = (ListView) findViewById(R.id.show_check_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int recoredID = (Integer)view.getTag();
                Intent intent = new Intent(getApplicationContext(), CheckActivity.class);
                intent.putExtra(College.Record.COLUMN_NAME_COURSE_ID, courseID);
                intent.putExtra(College.Record._ID, recoredID);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(mCustomView);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);

        setAdapter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshListReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.check_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private class RefreshListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }
    }

    private Cursor refreshCursor() {
        SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
        String sql = "SELECT " + College.Record.TABLE_NAME + "." + College.Record._ID + ", "
                + College.Record.TABLE_NAME + "." + College.Record.COLUMN_NAME_TIME + ", "

                + " (SELECT COUNT(*) FROM " + College.State.TABLE_NAME
                + " WHERE " + College.State.TABLE_NAME + "." + College.State._ID + " = "
                + College.Record.TABLE_NAME + "." + College.Record._ID
                + " AND " + College.State.COLUMN_NAME_STATE + " = ?) " + CourseDetailActivity.NORMAL_COUNT + ", "

                + " (SELECT COUNT(*) FROM " + College.State.TABLE_NAME
                + " WHERE " + College.State.TABLE_NAME + "." + College.State._ID + " = "
                + College.Record.TABLE_NAME + "." + College.Record._ID
                + " AND " + College.State.COLUMN_NAME_STATE + " = ?) " + CourseDetailActivity.LEAVE_COUNT + ", "

                + " (SELECT COUNT(*) FROM " + College.State.TABLE_NAME
                + " WHERE " + College.State.TABLE_NAME + "." + College.State._ID + " = "
                + College.Record.TABLE_NAME + "." + College.Record._ID
                + " AND " + College.State.COLUMN_NAME_STATE + " = ?) " + CourseDetailActivity.ABSENT_COUNT

                + " FROM " + College.Record.TABLE_NAME
                + " WHERE " + College.Record.COLUMN_NAME_COURSE_ID + " = ?;";
        String[] selectionArgs = {College.State.VALUE_STATE_NORMAL, College.State.VALUE_STATE_LEAVE, College.State.VALUE_STATE_ABSENT, courseID};
        return db.rawQuery(sql, selectionArgs);
    }

    public void setAdapter() {
        cursor = refreshCursor();
        String[] from = {
                College.Record.COLUMN_NAME_TIME,
                CourseDetailActivity.NORMAL_COUNT,
                CourseDetailActivity.LEAVE_COUNT,
                CourseDetailActivity.ABSENT_COUNT
        };
        int[] to = {
                R.id.show_course_statistics_time,
                R.id.show_course_statistics_normal,
                R.id.show_course_statistics_leave,
                R.id.show_course_statistics_absent
        };
        CourseStatisticsAdapter adapter = new CourseStatisticsAdapter(this, R.layout.show_course_statistics, refreshCursor(), from, to, 0);
        listView.setAdapter(adapter);
    }

    private class CourseStatisticsAdapter extends SimpleCursorAdapter {

        public CourseStatisticsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            view.setTag(cursor.getInt(cursor.getColumnIndex(College.Record._ID)));
        }
    }
}
