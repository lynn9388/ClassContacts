package edu.wust.lynn.classcontacts.browse;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.android.displayingbitmaps.util.ImageResizer;

import java.io.File;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.MainActivity;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.dialog.AddRoomFragment;
import edu.wust.lynn.classcontacts.dialog.DormitoryDetailFragment;
import edu.wust.lynn.classcontacts.edit.EditStudentActivity;

public class DormitoryFragment extends Fragment {

    private GridView gridView;
    private ImageResizer mImageResizer;
    private RefreshListReceiver refreshListReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dormitory, container, false);
        gridView = (GridView) view.findViewById(R.id.dormitory_gridview);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView roomIDView = (TextView)view.findViewById(R.id.show_dormitory_room_id);
                String roomID = roomIDView.getText().toString();

                DormitoryDetailFragment fragment = DormitoryDetailFragment.newInstance(roomID);
                fragment.show(getFragmentManager(), null);
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
                PopupMenu menu = new PopupMenu(getActivity(), view);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_delete:
                                deleteDormitory(view);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                MenuInflater inflater = menu.getMenuInflater();
                inflater.inflate(R.menu.dormitory_popup, menu.getMenu());
                menu.show();
                return true;
            }
        });

        setAdapter();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageResizer = new ImageResizer(getActivity(), 400, 400);
        mImageResizer.addImageCache(getActivity(), "Student");
        mImageResizer.setImageFadeIn(false);

        refreshListReceiver = new RefreshListReceiver();
        IntentFilter filter = new IntentFilter(EditStudentActivity.CONTACT_DATA_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshListReceiver, filter);
        filter = new IntentFilter(EditStudentActivity.EDIT_CONTACT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshListReceiver, filter);
        filter = new IntentFilter(AddRoomFragment.ROOM_DATA_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshListReceiver, filter);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(refreshListReceiver);
        super.onDestroy();
    }

    private void deleteDormitory(View view) {
        TextView roomIDView = (TextView) view.findViewById(R.id.show_dormitory_room_id);
        final String roomID = roomIDView.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.message_delete_dormitory);
        builder.setPositiveButton(R.string.button_message_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteDormitoryTask().execute(roomID);
                dialog.dismiss();
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

    private Cursor refreshCusor() {
        SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
        String[] projection = {
                College.Room._ID,
                College.Room.COLUMN_NAME_GENDER
        };
        String sortOrder = College.Room._ID + " ASC";
        return db.query(
                College.Room.TABLE_NAME,
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
                College.Room._ID,
                College.Room.COLUMN_NAME_GENDER
        };
        int[] to = {
                R.id.show_dormitory_room_id
        };
        DormitoryAdapter adapter = new DormitoryAdapter(
                getActivity(),
                R.layout.show_dormitory,
                cursor,
                from,
                to,
                0
        );
        gridView.setAdapter(adapter);
    }

    public class DormitoryAdapter extends SimpleCursorAdapter {

        public DormitoryAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            TextView roomIDView = (TextView) view.findViewById(R.id.show_dormitory_room_id);
            TextView genderView = (TextView) view.findViewById(R.id.show_dormitory_gender);

            String gender = cursor.getString(cursor.getColumnIndex(College.Room.COLUMN_NAME_GENDER));
            if (gender.equals(College.Student.VALUE_GENDER_BOY)) {
                genderView.setText(R.string.edit_boy);
            } else {
                genderView.setText(R.string.edit_girl);
            }

            String roomID = roomIDView.getText().toString();
            SQLiteDatabase db = MainActivity.collegeData.getReadableDatabase();
            Cursor roomCursor = College.Student.findStudentByRoomID(db, roomID);
            roomCursor.moveToFirst();
            int countStudents = roomCursor.getCount();
            int photoNameIndex = roomCursor.getColumnIndex(College.Student.COLUMN_NAME_PHOTO_NAME);
            if (countStudents == 1) {
                String photoName = roomCursor.getString(photoNameIndex);
                File file = getActivity().getFileStreamPath(photoName);
                mImageResizer.loadImage(file.getAbsolutePath(), (ImageView) view.findViewById(R.id.show_dormitory_one_first));
            } else if (countStudents == 2) {
                String photoName = roomCursor.getString(photoNameIndex);
                File file = getActivity().getFileStreamPath(photoName);
                mImageResizer.loadImage(file.getAbsolutePath(), (ImageView) view.findViewById(R.id.show_dormitory_two_first));

                roomCursor.moveToNext();
                photoName = roomCursor.getString(photoNameIndex);
                file = getActivity().getFileStreamPath(photoName);
                mImageResizer.loadImage(file.getAbsolutePath(), (ImageView) view.findViewById(R.id.show_dormitory_two_second));
            } else if (countStudents >= 3) {
                String photoName = roomCursor.getString(photoNameIndex);
                File file = getActivity().getFileStreamPath(photoName);
                mImageResizer.loadImage(file.getAbsolutePath(), (ImageView) view.findViewById(R.id.show_dormitory_three_four_first));

                roomCursor.moveToNext();
                photoName = roomCursor.getString(photoNameIndex);
                file = getActivity().getFileStreamPath(photoName);
                mImageResizer.loadImage(file.getAbsolutePath(), (ImageView) view.findViewById(R.id.show_dormitory_three_four_second));

                if (countStudents == 3) {
                    roomCursor.moveToNext();
                    photoName = roomCursor.getString(photoNameIndex);
                    file = getActivity().getFileStreamPath(photoName);
                    mImageResizer.loadImage(file.getAbsolutePath(), (ImageView) view.findViewById(R.id.show_dormitory_three_third));
                } else {
                    roomCursor.moveToNext();
                    photoName = roomCursor.getString(photoNameIndex);
                    file = getActivity().getFileStreamPath(photoName);
                    mImageResizer.loadImage(file.getAbsolutePath(), (ImageView) view.findViewById(R.id.show_dormitory_four_third));

                    roomCursor.moveToNext();
                    photoName = roomCursor.getString(photoNameIndex);
                    file = getActivity().getFileStreamPath(photoName);
                    mImageResizer.loadImage(file.getAbsolutePath(), (ImageView) view.findViewById(R.id.show_dormitory_four_fourth));
                }
            }
        }
    }

    private class RefreshListReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            setAdapter();
        }
    }

    private class DeleteDormitoryTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = new Intent(EditStudentActivity.EDIT_CONTACT);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }

        @Override
        protected Void doInBackground(String... params) {
            SQLiteDatabase db = MainActivity.collegeData.getWritableDatabase();
            College.Room.deleteRoom(db, params[0]);
            return null;
        }
    }

}
