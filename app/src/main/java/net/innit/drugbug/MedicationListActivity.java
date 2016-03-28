package net.innit.drugbug;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.innit.drugbug.fragment.AddDoseFragment;
import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.fragment.MedicationListFragment;

import static net.innit.drugbug.data.Constants.ACTION;
import static net.innit.drugbug.data.Constants.ACTION_ADD;
import static net.innit.drugbug.data.Constants.FILTER_ACTIVE;
import static net.innit.drugbug.data.Constants.FILTER_ALL;
import static net.innit.drugbug.data.Constants.FILTER_ARCHIVED;
import static net.innit.drugbug.data.Constants.FILTER_DOSE;
import static net.innit.drugbug.data.Constants.FILTER_INACTIVE;
import static net.innit.drugbug.data.Constants.SORT;
import static net.innit.drugbug.data.Constants.SORT_NAME_ASC;
import static net.innit.drugbug.data.Constants.SORT_NAME_DESC;
import static net.innit.drugbug.data.Constants.SOURCE_LIST_MEDICATIONS;
import static net.innit.drugbug.data.Constants.TAG_ADD;
import static net.innit.drugbug.data.Constants.TYPE;
import static net.innit.drugbug.data.Constants.TYPE_MEDICATION;

/**
 * Activity to create a medication list
 */
public class MedicationListActivity extends Activity {
    private String sortOrder;
    private String filter;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = new Bundle();
            sortOrder = SORT_NAME_ASC;
            filter = FILTER_ACTIVE;
        } else {
            sortOrder = bundle.getString(SORT, SORT_NAME_ASC);
            filter = bundle.getString(FILTER_DOSE, FILTER_ACTIVE);
        }

        setTitle(getString(R.string.medication_list_title));
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshDisplay();
    }

    private void refreshDisplay() {
        bundle.putString(SORT, sortOrder);
        bundle.putString(FILTER_DOSE, filter);

        Fragment fragment = new MedicationListFragment();
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_med, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        invalidateOptionsMenu();

        switch (item.getItemId()) {
            case R.id.menu_med_list_add:
                Bundle b = new Bundle();
                b.putString(ACTION, ACTION_ADD);
                b.putString(TYPE, TYPE_MEDICATION);
                b.putString(SORT, sortOrder);
                b.putString(FILTER_DOSE, filter);

                Fragment fragment = new AddDoseFragment();
                fragment.setArguments(b);
                getFragmentManager().beginTransaction().add(fragment, TAG_ADD).commit();
                return true;
            case R.id.menu_list_med_help:
                HelpFragment.showHelp(getFragmentManager(), SOURCE_LIST_MEDICATIONS);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_med_sort_order_name_asc:
                sortOrder = SORT_NAME_ASC;
                break;
            case R.id.menu_med_sort_order_name_dsc:
                sortOrder = SORT_NAME_DESC;
                break;
            case R.id.menu_med_filter_all:
                filter = FILTER_ALL;
                break;
            case R.id.menu_med_filter_active:
                filter = FILTER_ACTIVE;
                break;
            case R.id.menu_med_filter_inactive:
                filter = FILTER_INACTIVE;
                break;
            case R.id.menu_med_filter_archived:
                filter = FILTER_ARCHIVED;
                break;
        }
        refreshDisplay();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setCurrentSortOrder(menu);
        setCurrentFilter(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    private void setCurrentFilter(Menu menu) {
        MenuItem menuItem;
        String title;
        switch (filter) {
            case FILTER_ALL:
                menuItem = menu.findItem(R.id.menu_med_filter_all);
                title = getString(R.string.menu_med_filter_all) + " (" + getString(R.string.list_item_current) + ")";
                break;
            case FILTER_INACTIVE:
                menuItem = menu.findItem(R.id.menu_med_filter_inactive);
                title = getString(R.string.menu_med_filter_inactive) + " (" + getString(R.string.list_item_current) + ")";
                break;
            case FILTER_ARCHIVED:
                menuItem = menu.findItem(R.id.menu_med_filter_archived);
                title = getString(R.string.menu_med_filter_archived) + " (" + getString(R.string.list_item_current) + ")";
                break;
            default:
                menuItem = menu.findItem(R.id.menu_med_filter_active);
                title = getString(R.string.menu_med_filter_active) + " (" + getString(R.string.list_item_current) + ")";
        }
        menuItem.setTitle(title);
    }

    private void setCurrentSortOrder(Menu menu) {
        MenuItem menuItem;
        String title;
        // Indicate which sort order is currently in use
        switch (sortOrder) {
            case SORT_NAME_DESC:
                menuItem = menu.findItem(R.id.menu_med_sort_order_name_dsc);
                title = getString(R.string.sort_order_name_dsc) + " (" + getString(R.string.list_item_current) + ")";
                break;
            default:
                menuItem = menu.findItem(R.id.menu_med_sort_order_name_asc);
                title = getString(R.string.sort_order_name_asc) + " (" + getString(R.string.list_item_current) + ")";
        }
        menuItem.setTitle(title);
    }
}
