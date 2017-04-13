package edu.wust.lynn.classcontacts.dialog;



import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.MainActivity;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.edit.EditStudentActivity;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class AddRoomFragment extends DialogFragment {
    public static final String ROOM_DATA_CHANGED = "edu.wust.lynn.classcontacts.edit.ROOM_DATA_CHANGED";
    private SQLiteDatabase db;
    private String address;
    private String type;
    private boolean added;

    private EditText addressField;
    private RadioButton boyButton;

    public AddRoomFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        added = false;

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_room, null);
        addressField = (EditText)view.findViewById(R.id.add_room_edit_address);
        boyButton = (RadioButton)view.findViewById(R.id.add_room_boy_button);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_add_room)
                .setView(view)
                .setPositiveButton(R.string.button_message_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveRoom();
                    }
                })
                .setNegativeButton(R.string.button_message_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    private void saveRoom() {
        address = addressField.getText().toString();
        if (address.equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.message_blank_address);
            builder.setPositiveButton(R.string.button_message_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            db = MainActivity.collegeData.getWritableDatabase();
            Cursor cursor = College.Room.findRoom(db, address);
            if (cursor.getCount() != 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.message_address_exist);
                builder.setPositiveButton(R.string.button_message_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                new SaveRoomTask().execute();
            }
        }
    }

    public boolean hasAddedRoom() {
        return added;
    }

    public String getAddress() {
        return address;
    }

    public String getType() {
        return type;
    }

    private class SaveRoomTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            Intent intent = new Intent(ROOM_DATA_CHANGED);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }

        @Override
        protected Void doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            values.put(College.Room._ID, address);
            type = boyButton.isChecked() ? College.Student.VALUE_GENDER_BOY : College.Student.VALUE_GENDER_GIRL;
            values.put(College.Room.COLUMN_NAME_GENDER, type);
            db.insert(College.Room.TABLE_NAME, null, values);
            added = true;
            return null;
        }
    }
}
