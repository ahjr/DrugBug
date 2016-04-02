package net.innit.drugbug;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import static net.innit.drugbug.data.Constants.FILTER_INACTIVE;
import static net.innit.drugbug.data.Constants.FILTER_MED;
import static net.innit.drugbug.data.Constants.SORT_ARCHIVED_ASC;
import static net.innit.drugbug.data.Constants.SORT_ARCHIVED_DESC;
import static net.innit.drugbug.data.Constants.SORT_CREATION_ASC;
import static net.innit.drugbug.data.Constants.SORT_CREATION_DESC;
import static net.innit.drugbug.data.Constants.SORT_LAST_TAKEN_ASC;
import static net.innit.drugbug.data.Constants.SORT_LAST_TAKEN_DESC;
import static net.innit.drugbug.data.Constants.SORT_MED;
import static net.innit.drugbug.data.Constants.SORT_NAME_ASC;
import static net.innit.drugbug.data.Constants.SORT_NAME_DESC;
import static net.innit.drugbug.data.Constants.SORT_NEXT_FUTURE_ASC;
import static net.innit.drugbug.data.Constants.SORT_NEXT_FUTURE_DESC;
import static net.innit.drugbug.data.Constants.SOURCE_LIST_MEDICATIONS;
import static net.innit.drugbug.data.Constants.TAG_ADD;
import static net.innit.drugbug.data.Constants.TYPE;
import static net.innit.drugbug.data.Constants.TYPE_MEDICATION;

/**
 * Activity to create a medication list
 */
public class MedicationListActivity extends FragmentActivity {
    private String medSortOrder;
    private String medFilter;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = getIntent().getExtras();
        if (bundle == null) {
            bundle = new Bundle();
            medSortOrder = SORT_NAME_ASC;
            medFilter = FILTER_ACTIVE;
        } else {
            medSortOrder = bundle.getString(SORT_MED, SORT_NAME_ASC);
            medFilter = bundle.getString(FILTER_MED, FILTER_ACTIVE);
        }

        setTitle(getString(R.string.medication_list_title));
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshDisplay();
    }

    private void refreshDisplay() {
        bundle.putString(SORT_MED, medSortOrder);
        bundle.putString(FILTER_MED, medFilter);

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
        super.onOptionsItemSelected(item);
        invalidateOptionsMenu();

        switch (item.getItemId()) {
            case R.id.menu_med_list_add:
                Bundle b = new Bundle();
                b.putString(ACTION, ACTION_ADD);
                b.putString(TYPE, TYPE_MEDICATION);
                b.putString(SORT_MED, medSortOrder);
                b.putString(FILTER_MED, medFilter);

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
                medSortOrder = SORT_NAME_ASC;
                return false;
            case R.id.menu_med_sort_order_name_dsc:
                medSortOrder = SORT_NAME_DESC;
                return false;
            case R.id.menu_med_sort_order_creation_asc:
                medSortOrder = SORT_CREATION_ASC;
                return false;
            case R.id.menu_med_sort_order_creation_dsc:
                medSortOrder = SORT_CREATION_DESC;
                return false;
            case MedicationListFragment.MENU_NEXT_FUTURE_ASC:
                medSortOrder = SORT_NEXT_FUTURE_ASC;
                return false;
            case MedicationListFragment.MENU_NEXT_FUTURE_DSC:
                medSortOrder = SORT_NEXT_FUTURE_DESC;
                return false;
            case MedicationListFragment.MENU_LAST_TAKEN_ASC:
                medSortOrder = SORT_LAST_TAKEN_ASC;
                return false;
            case MedicationListFragment.MENU_LAST_TAKEN_DSC:
                medSortOrder = SORT_LAST_TAKEN_DESC;
                return false;
            case MedicationListFragment.MENU_ARCHIVED_ASC:
                medSortOrder = SORT_ARCHIVED_ASC;
                return false;
            case MedicationListFragment.MENU_ARCHIVED_DSC:
                medSortOrder = SORT_ARCHIVED_DESC;
                return false;
            case R.id.menu_med_filter_all:
                medFilter = FILTER_ALL;
                return false;
            case R.id.menu_med_filter_active:
                medFilter = FILTER_ACTIVE;
                return false;
            case R.id.menu_med_filter_inactive:
                medFilter = FILTER_INACTIVE;
                return false;
            case R.id.menu_med_filter_archived:
                medFilter = FILTER_ARCHIVED;
                return false;
        }

        return false;
    }
}
