package net.innit.drugbug;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import net.innit.drugbug.fragment.DoseListFragment;
import net.innit.drugbug.fragment.HelpFragment;

import static net.innit.drugbug.data.Constants.ACTION;
import static net.innit.drugbug.data.Constants.ACTION_ADD;
import static net.innit.drugbug.data.Constants.FILTER_DOSE;
import static net.innit.drugbug.data.Constants.FILTER_FUTURE;
import static net.innit.drugbug.data.Constants.FILTER_NONE;
import static net.innit.drugbug.data.Constants.FILTER_TAKEN;
import static net.innit.drugbug.data.Constants.INTENT_MED_ID;
import static net.innit.drugbug.data.Constants.SORT;
import static net.innit.drugbug.data.Constants.SORT_DATE_ASC;
import static net.innit.drugbug.data.Constants.SORT_DATE_DESC;
import static net.innit.drugbug.data.Constants.SORT_NAME;
import static net.innit.drugbug.data.Constants.SOURCE_LIST_FUTURE;
import static net.innit.drugbug.data.Constants.SOURCE_LIST_REMINDERS;
import static net.innit.drugbug.data.Constants.SOURCE_LIST_SINGLE_MED;
import static net.innit.drugbug.data.Constants.SOURCE_LIST_TAKEN;
import static net.innit.drugbug.data.Constants.SOURCE_MAIN;
import static net.innit.drugbug.data.Constants.TYPE;
import static net.innit.drugbug.data.Constants.TYPE_FUTURE;
import static net.innit.drugbug.data.Constants.TYPE_REMINDER;
import static net.innit.drugbug.data.Constants.TYPE_SINGLE;
import static net.innit.drugbug.data.Constants.TYPE_TAKEN;

public class DoseListActivity extends Activity {
    private String type;

    private String sortOrder;
    private String filter;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bundle = getIntent().getExtras();
        type = bundle.getString(TYPE, TYPE_FUTURE);
        sortOrder = bundle.getString(SORT);
        filter = bundle.getString(FILTER_DOSE);

        setContentView(R.layout.activity_list_dose);

        // Set the sortOrder if it wasn't passed in
        if (sortOrder == null) {
            switch (type) {
                case TYPE_TAKEN:
                    sortOrder = SORT_DATE_DESC;
                    break;
                default:
                    sortOrder = SORT_DATE_ASC;
            }
            bundle.putString(SORT, sortOrder);
        }

        if (filter == null) {
            filter = FILTER_NONE;
            bundle.putString(FILTER_DOSE, filter);
        }
    }

    @Override
    protected void onResume() {

        refreshDisplay();

        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);

        if (!type.equals(TYPE_SINGLE)) {
            menu.findItem(R.id.menu_filter).setVisible(false);
        } else {
            menu.findItem(R.id.menu_list_add).setVisible(false);
            menu.findItem(R.id.menu_sort_order_name).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setCurrentSortOrder(menu);
        setCurrentFilter(menu);

        return super.onPrepareOptionsMenu(menu);
    }

    private void setMenuItem(Menu menu, int menuItemRes, int stringRes) {
        menu.findItem(menuItemRes).setTitle(getString(stringRes) + " (" + getString(R.string.list_item_current) + ")");
    }

    private void setCurrentFilter(Menu menu) {
        switch (filter) {
            case FILTER_TAKEN:
                setMenuItem(menu, R.id.menu_filter_taken, R.string.menu_list_filter_taken);
                break;
            case FILTER_FUTURE:
                setMenuItem(menu, R.id.menu_filter_future, R.string.menu_list_filter_future);
                break;
            default:
                setMenuItem(menu, R.id.menu_filter_none, R.string.menu_list_filter_none);
        }
    }

    private void setCurrentSortOrder(Menu menu) {
        switch (sortOrder) {
            case SORT_DATE_DESC:
                setMenuItem(menu, R.id.menu_sort_order_date_dsc, R.string.menu_list_sort_date_dsc);
                break;
            case SORT_NAME:
                setMenuItem(menu, R.id.menu_sort_order_name, R.string.menu_list_sort_name);
                break;
            default:
                setMenuItem(menu, R.id.menu_sort_order_date_asc, R.string.menu_list_sort_date_asc);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        invalidateOptionsMenu();

        switch (item.getItemId()) {
            case R.id.menu_list_add:
                Intent intent = new Intent(DoseListActivity.this, AddDoseActivity.class);
                intent.putExtra(ACTION, ACTION_ADD);
                intent.putExtra(TYPE, type);
                intent.putExtra(INTENT_MED_ID, bundle.getLong(INTENT_MED_ID));
                intent.putExtra(SORT, sortOrder);
                intent.putExtra(FILTER_DOSE, filter);
                startActivity(intent);
                return true;
            case R.id.menu_list_help:
                switch (type) {
                    case TYPE_FUTURE:
                        HelpFragment.showHelp(getFragmentManager(), SOURCE_LIST_FUTURE);
                        break;
                    case TYPE_TAKEN:
                        HelpFragment.showHelp(getFragmentManager(), SOURCE_LIST_TAKEN);
                        break;
                    case TYPE_SINGLE:
                        HelpFragment.showHelp(getFragmentManager(), SOURCE_LIST_SINGLE_MED);
                        break;
                    case TYPE_REMINDER:
                        HelpFragment.showHelp(getFragmentManager(), SOURCE_LIST_REMINDERS);
                        break;
                    default:
                        HelpFragment.showHelp(getFragmentManager(), SOURCE_MAIN);
                }
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_sort_order_date_asc:
                sortOrder = SORT_DATE_ASC;
                bundle.putString(SORT, sortOrder);
                break;
            case R.id.menu_sort_order_date_dsc:
                sortOrder = SORT_DATE_DESC;
                bundle.putString(SORT, sortOrder);
                break;
            case R.id.menu_sort_order_name:
                sortOrder = SORT_NAME;
                bundle.putString(SORT, sortOrder);
                break;
            case R.id.menu_filter_future:
                filter = FILTER_FUTURE;
                bundle.putString(FILTER_DOSE, filter);
                break;
            case R.id.menu_filter_taken:
                filter = FILTER_TAKEN;
                bundle.putString(FILTER_DOSE, filter);
                break;
        }
        refreshDisplay();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent;
        switch (type) {
            case TYPE_SINGLE:
                intent = new Intent(this, MedicationListActivity.class);
                startActivity(intent);
                break;
        }
        finish();
    }

    private void refreshDisplay() {
        Fragment fragment = new DoseListFragment();
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();

    }
}
