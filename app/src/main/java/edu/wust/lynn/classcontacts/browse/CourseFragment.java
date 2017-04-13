package edu.wust.lynn.classcontacts.browse;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageResizer;
import com.example.android.displayingbitmaps.util.ImageWorker;

import java.io.File;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.MainActivity;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.edit.EditCourseActivity;

public class CourseFragment extends Fragment {


    private GridView gridView;
    private ImageResizer mImageResizer;
    private RefreshListReceiver refreshListReceiver;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, null);
        gridView = (GridView)view.findViewById(R.id.show_course_gridview);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), CourseDetailActivity.class);
                TextView idView = (TextView) view.findViewById(R.id.show_course_id);
                String courseID = idView.getText().toString();
                intent.putExtra(College.Course._ID, courseID);
                startActivity(intent);
            }
        });
        setAdapter();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageResizer = new ImageResizer(getActivity(), 500, 500);
        mImageResizer.addImageCache(getActivity(), "Course");
        mImageResizer.setImageFadeIn(false);

        refreshListReceiver = new RefreshListReceiver();
        IntentFilter filter = new IntentFilter(EditCourseActivity.COURSE_DATA_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshListReceiver, filter);
        filter = new IntentFilter(EditCourseActivity.EDIT_COURSE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshListReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(refreshListReceiver);
    }

    private class RefreshListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            setAdapter();
        }
    }

    private Cursor refreshCusor() {
        SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
        String[] projection = {
                College.Course.COLUMN_NAME_PHOTO_NAME,
                College.Course.COLUMN_NAME_COURSE_NAME,
                College.Course._ID,
                College.Course.COLUMN_NAME_TEACHER_NAME
        };
        String sortOrder = College.Course._ID + " ASC";
        return db.query(
                College.Course.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    public void setAdapter() {
        Cursor cursor = refreshCusor();
        String[] from = {
                College.Course.COLUMN_NAME_COURSE_NAME,
                College.Course._ID,
                College.Course.COLUMN_NAME_TEACHER_NAME
        };
        int[] to = {
                R.id.show_course_name,
                R.id.show_course_id,
                R.id.show_course_detail_teacher_name
        };
        CourseAdapter adapter = new CourseAdapter(getActivity(), R.layout.show_course, cursor, from, to, 0);
        gridView.setAdapter(adapter);
    }

    private class CourseAdapter extends SimpleCursorAdapter {

        public CourseAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView imageView = (ImageView) view.findViewById(R.id.show_course_photo);
            String photoName = cursor.getString(cursor.getColumnIndex(College.Course.COLUMN_NAME_PHOTO_NAME));
            File file = getActivity().getFileStreamPath(photoName);
            mImageResizer.loadImage(file.getAbsolutePath(), imageView);
            super.bindView(view, context, cursor);
        }
    }

}
