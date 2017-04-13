package edu.wust.lynn.classcontacts.dialog;



import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.android.displayingbitmaps.util.ImageResizer;

import org.w3c.dom.Text;

import java.io.File;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.MainActivity;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.browse.StudentDetailActivity;
import edu.wust.lynn.classcontacts.tab.SearchActivity;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class DormitoryDetailFragment extends DialogFragment {
    private TextView roomIDView;
    private GridView gridView;
    private ImageResizer mImageResizer;

    private String roomID;


    public static DormitoryDetailFragment newInstance(String roomID) {
        DormitoryDetailFragment fragment = new DormitoryDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(College.Room._ID, roomID);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageResizer = new ImageResizer(getActivity(), 400, 400);
        mImageResizer.addImageCache(getActivity(), "DormitoryDetailFragment");
        mImageResizer.setImageFadeIn(false);

        roomID = getArguments().getString(College.Room._ID);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dormitory_detail, null);
        roomIDView = (TextView)view.findViewById(R.id.show_dormitory_detail_room_id);
        gridView = (GridView)view.findViewById(R.id.show_dormitory_detail_grid_view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String studentID = (String)view.getTag();
                Intent intent = new Intent(getActivity(), StudentDetailActivity.class);
                intent.putExtra(College.Student._ID, studentID);
                startActivity(intent);
            }
        });

        roomIDView.setText(roomID);
        setAdapter();

        builder.setView(view)
                .setPositiveButton(R.string.button_message_send_messages, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendMessages();
                    }
                });
        return builder.create();
    }


    public void sendMessages() {
        String phone = "";
        Cursor cursor = refreshCusor();
        cursor.moveToFirst();
        String str;
        for (int i = 0; !cursor.isAfterLast(); i++) {
            str = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_PHONE));
            phone = phone + str;
            while(!cursor.isAfterLast()) {
                str = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_PHONE));
                if (!"".equals(str)) {
                    phone = phone + ";";
                    cursor.moveToPosition(i);
                    break;
                } else {
                    cursor.moveToNext();
                }
            }
            cursor.moveToNext();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra("address", phone);
        intent.setType("vnd.android-dir/mms-sms");
        startActivity(intent);
    }

    private Cursor refreshCusor() {
        SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
        String[] projection = {
                College.Student._ID,
                College.Student.COLUMN_NAME_NAME,
                College.Student.COLUMN_NAME_PHOTO_NAME,
                College.Student.COLUMN_NAME_PHONE
        };
        String selection = College.Student.COLUMN_NAME_ROOM_ID + " = ?";
        String[] selectionArgs = {roomID};
        String sortOrder = College.Student._ID + " ASC";
        return db.query(
                College.Student.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    public void setAdapter() {
        Cursor cursor = refreshCusor();
        String[] from = {
                College.Student.COLUMN_NAME_NAME
        };
        int[] to = {
                R.id.show_dormitory_detail_student_name
        };
        DormitoryDetailAdapter adapter = new DormitoryDetailAdapter(
                getActivity(),
                R.layout.show_dormitory_detail,
                cursor,
                from,
                to,
                0
        );
        gridView.setAdapter(adapter);
    }

    private class DormitoryDetailAdapter extends SimpleCursorAdapter {

        public DormitoryDetailAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String photoName = cursor.getString(cursor.getColumnIndex(College.Student.COLUMN_NAME_PHOTO_NAME));
            String studentID = cursor.getString(cursor.getColumnIndex(College.Student._ID));
            File file = getActivity().getFileStreamPath(photoName);
            mImageResizer.loadImage(file.getAbsolutePath(), (ImageView)view.findViewById(R.id.show_dormitory_detail_student_photo));
            view.setTag(studentID);
            super.bindView(view, context, cursor);
        }
    }
}
