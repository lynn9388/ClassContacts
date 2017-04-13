package edu.wust.lynn.classcontacts.tab;


import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import java.util.Timer;
import java.util.TimerTask;

import edu.wust.lynn.classcontacts.College;
import edu.wust.lynn.classcontacts.MainActivity;
import edu.wust.lynn.classcontacts.R;
import edu.wust.lynn.classcontacts.browse.CourseFragment;
import edu.wust.lynn.classcontacts.browse.DormitoryFragment;
import edu.wust.lynn.classcontacts.browse.StudentFragment;
import edu.wust.lynn.classcontacts.dialog.DormitoryDetailFragment;

public class SearchActivity extends FragmentActivity {
    public static final String SEARCH_TEXT = "edu.wust.lynn.classcontacts.tab.SearchActivity.SEARCH_TEXT";
    private int mActionBarOptions;
    private ActionBar actionBar;
    private View mCustomView;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mCustomView = getLayoutInflater().inflate(R.layout.custom_search_view, null);
        searchView = (SearchView) mCustomView.findViewById(R.id.searchView);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                StudentFragment fragment = StudentFragment.newInstance(newText);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(android.R.id.content, fragment);
                transaction.commit();
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        actionBar = getParent().getActionBar();
        actionBar.setCustomView(mCustomView);
        mActionBarOptions = actionBar.getDisplayOptions();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);

//        searchView.setFocusable(true);
//        searchView.setFocusableInTouchMode(true);
//        searchView.requestFocus();
//        final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(searchView, InputMethodManager.RESULT_SHOWN);
//
//        Timer timer = new Timer();
//        timer.schedule(new TimerTask(){
//            @Override
//            public void run() {
//                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
//            }
//        }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getParent().getActionBar().setDisplayOptions(mActionBarOptions,
                ActionBar.DISPLAY_SHOW_CUSTOM | mActionBarOptions);
    }

}
