package edu.wust.lynn.classcontacts.browse;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.MainActivity;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.dialog.ChoosePhotoSourceFragment;
import edu.wust.lynn.classcontacts.edit.EditStudentActivity;
import edu.wust.lynn.classcontacts.tab.SearchActivity;

import com.example.android.displayingbitmaps.util.ImageCache;
import com.example.android.displayingbitmaps.util.ImageResizer;
import com.example.android.displayingbitmaps.util.ImageWorker;

public class StudentFragment extends ListFragment {

    private ImageResizer mImageResizer;
    private RefreshListReceiver refreshListReceiver;
    private Cursor cursor;

    public static StudentFragment newInstance(String searchText) {
        StudentFragment fragment = new StudentFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SearchActivity.SEARCH_TEXT, searchText);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student, null);

        setAdapter();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageResizer = new ImageResizer(getActivity(), 400, 400);
        mImageResizer.addImageCache(getActivity(), "Student");
        mImageResizer.setImageFadeIn(false);

        cursor = null;
        Bundle bundle = getArguments();
        if (bundle != null) {
            SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
            cursor = College.StudentFTS3.findStudentFTS3(db, bundle.getString(SearchActivity.SEARCH_TEXT));
        }

        refreshListReceiver = new RefreshListReceiver();
        IntentFilter filter = new IntentFilter(EditStudentActivity.CONTACT_DATA_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshListReceiver, filter);
        filter = new IntentFilter(EditStudentActivity.EDIT_CONTACT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshListReceiver, filter);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(refreshListReceiver);
        super.onDestroy();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TextView idView = (TextView) v.findViewById(R.id.student_id);
        String studentID = idView.getText().toString();

        Intent intent = new Intent(getActivity(), StudentDetailActivity.class);
        intent.putExtra(College.Student._ID, studentID);
        startActivity(intent);
    }

    private Cursor refreshCursor() {
        SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
        String[] projection = {
                College.Student.COLUMN_NAME_PHOTO_NAME,
                College.Student.COLUMN_NAME_NAME,
                College.Student._ID,
                College.Student.COLUMN_NAME_PHONE
        };
        String sortOrder = College.Student._ID + " ASC";
        return db.query(
                College.Student.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    public void setAdapter() {
        if (cursor == null) {
            cursor = refreshCursor();
        }
        String[] from = {
                College.Student.COLUMN_NAME_NAME,
                College.Student._ID,
                College.Student.COLUMN_NAME_PHONE
        };
        int[] to = {
                R.id.student_name,
                R.id.student_id,
                R.id.student_phone
        };
        StudentAdapter adapter = new StudentAdapter(getActivity(), R.layout.show_student, cursor, from, to, 0);
        setListAdapter(adapter);
    }

    private class StudentAdapter extends SimpleCursorAdapter {

        public StudentAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView imageView = (ImageView) view.findViewById(R.id.student_photo);
            String photoName = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_PHOTO_NAME));
            File file = getActivity().getFileStreamPath(photoName);
            mImageResizer.loadImage(file.getAbsolutePath(), imageView);
            super.bindView(view, context, cursor);
        }
    }

    private class RefreshListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            cursor = refreshCursor();
            setAdapter();
        }
    }

}



