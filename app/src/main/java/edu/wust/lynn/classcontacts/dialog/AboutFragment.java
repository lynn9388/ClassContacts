package edu.wust.lynn.classcontacts.dialog;



import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.browse.StudentDetailActivity;

public class AboutFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_about, null);

        TextView versionView = (TextView) view.findViewById(R.id.show_about_version_view);
        PackageManager packageManager = getActivity().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getActivity().getPackageName(), 0);
            String version = packageInfo.versionName;
            versionView.setText("V." + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        builder.setView(view);
        return builder.create();
    }
}
