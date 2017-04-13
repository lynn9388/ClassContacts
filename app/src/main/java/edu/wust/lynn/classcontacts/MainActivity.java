package edu.wust.lynn.classcontacts;

import android.app.FragmentTransaction;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

import edu.wust.lynn.classcontacts.dialog.AboutFragment;
import edu.wust.lynn.classcontacts.edit.EditStudentActivity;
import edu.wust.lynn.classcontacts.edit.EditCourseActivity;
import edu.wust.lynn.classcontacts.tab.ContactsActivity;
import edu.wust.lynn.classcontacts.tab.SearchActivity;


public class MainActivity extends TabActivity {
    public static CollegeHelper collegeData;

    private final static int TAB_INDEX_CONTACTS = 0;
    private final static int TAB_INDEX_SEARCH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec("contacts").setIndicator("contacts")
                .setContent(new Intent(this, ContactsActivity.class)));
        tabHost.addTab(tabHost.newTabSpec("search").setIndicator("search")
                .setContent(new Intent(this, SearchActivity.class)));
        getTabWidget().setVisibility(View.GONE);

        collegeData = new CollegeHelper(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (getTabHost().getCurrentTab()) {
            case TAB_INDEX_CONTACTS:
                getMenuInflater().inflate(R.menu.contacts, menu);
                break;
            case TAB_INDEX_SEARCH:
                getMenuInflater().inflate(R.menu.search, menu);
                break;
            default:
                break;
        }
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void refreshTabIcon(MenuItem item, int currentTab) {
        if (item.getGroupId() == R.id.tab_group) {
            switch (item.getItemId()) {
                case R.id.tab_menu_contacts:
                    item.setIcon(currentTab == TAB_INDEX_CONTACTS ? R.drawable.ic_tab_selected_contacts
                            : R.drawable.ic_tab_unselected_contacts);
                    break;
                case R.id.tab_menu_search:
                    item.setIcon(currentTab == TAB_INDEX_SEARCH ? R.drawable.ic_tab_selected_search
                            : R.drawable.ic_tab_unselected_search);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            refreshTabIcon(menu.getItem(i), getTabHost().getCurrentTab());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getGroupId() == R.id.tab_group) {
            switch (item.getItemId()) {
                case R.id.tab_menu_contacts:
                    getTabHost().setCurrentTab(TAB_INDEX_CONTACTS);
                    break;
                case R.id.tab_menu_search:
                    getTabHost().setCurrentTab(TAB_INDEX_SEARCH);
                    break;
                default:
                    break;
            }
            invalidateOptionsMenu();
        } else {
            Intent intent = null;
            switch(item.getItemId()) {
                case R.id.action_new_contact:
                    intent = new Intent(this, EditStudentActivity.class);
                    startActivity(intent);
                    break;
                case R.id.action_new_course:
                    intent = new Intent(this, EditCourseActivity.class);
                    startActivity(intent);
                    break;
                case R.id.action_settings:
                    intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                    break;
                case R.id.action_about:
                    new AboutFragment().show(getFragmentManager(), null);
                    break;
                default:
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
