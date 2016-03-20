package net.innit.drugbug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.innit.drugbug.data.DBDataSource;
import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.MedicationArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.innit.drugbug.util.Constants.ACTION;
import static net.innit.drugbug.util.Constants.ACTION_ADD;
import static net.innit.drugbug.util.Constants.ACTION_RESTORE;
import static net.innit.drugbug.util.Constants.FILTER_ACTIVE;
import static net.innit.drugbug.util.Constants.FILTER_ALL;
import static net.innit.drugbug.util.Constants.FILTER_ARCHIVED;
import static net.innit.drugbug.util.Constants.FILTER_DOSE;
import static net.innit.drugbug.util.Constants.FILTER_INACTIVE;
import static net.innit.drugbug.util.Constants.INTENT_MED_ID;
import static net.innit.drugbug.util.Constants.SORT;
import static net.innit.drugbug.util.Constants.SORT_DATE_DESC;
import static net.innit.drugbug.util.Constants.SORT_NAME_ASC;
import static net.innit.drugbug.util.Constants.SORT_NAME_DESC;
import static net.innit.drugbug.util.Constants.SOURCE;
import static net.innit.drugbug.util.Constants.SOURCE_LIST_MEDICATIONS;
import static net.innit.drugbug.util.Constants.TYPE;
import static net.innit.drugbug.util.Constants.TYPE_MEDICATION;
import static net.innit.drugbug.util.Constants.TYPE_SINGLE;

/**
 * Activity to create a medication list
 */
public class MedicationListActivity extends Activity {
    //    private static final int CONTEXT_DELETE_ALL = 1001;
    private final DBDataSource db = new DBDataSource(this);
    private List<MedicationItem> medications = new ArrayList<>();
    private MedicationArrayAdapter adapter;
    private String sortOrder;
    private String filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_medication);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            sortOrder = bundle.getString(SORT, SORT_DATE_DESC);
            filter = bundle.getString(FILTER_DOSE, FILTER_ACTIVE);
        } else {
            sortOrder = SORT_DATE_DESC;
            filter = FILTER_ACTIVE;
        }

        setTitle(getString(R.string.medication_list_title));
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshDisplay();
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
                Intent intent = new Intent(MedicationListActivity.this, AddDoseActivity.class);
                intent.putExtra(ACTION, ACTION_ADD);
                intent.putExtra(TYPE, TYPE_MEDICATION);
                intent.putExtra(SORT, sortOrder);
                intent.putExtra(FILTER_DOSE, filter);
                startActivity(intent);
                return true;
            case R.id.menu_list_med_help:
                Bundle bundle = new Bundle();
                bundle.putInt(SOURCE, SOURCE_LIST_MEDICATIONS);

                HelpFragment fragment = new HelpFragment();
                fragment.setArguments(bundle);
                fragment.show(getFragmentManager(), "Help Fragment");
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
        MenuItem menuItemSort;
        // Indicate which sort order is currently in use
        switch (sortOrder) {
            case SORT_NAME_DESC:
                menuItemSort = menu.findItem(R.id.menu_med_sort_order_name_dsc);
                break;
            default:
                menuItemSort = menu.findItem(R.id.menu_med_sort_order_name_asc);
        }
        menuItemSort.setTitle(menuItemSort.getTitle() + " (" + getString(R.string.list_item_current) + ")");

        MenuItem menuItemFilter;
        switch (filter) {
            case FILTER_ALL:
                menuItemFilter = menu.findItem(R.id.menu_med_filter_all);
                break;
            case FILTER_INACTIVE:
                menuItemFilter = menu.findItem(R.id.menu_med_filter_inactive);
                break;
            case FILTER_ARCHIVED:
                menuItemFilter = menu.findItem(R.id.menu_med_filter_archived);
                break;
            default:
                menuItemFilter = menu.findItem(R.id.menu_med_filter_active);
        }
        menuItemFilter.setTitle(menuItemFilter.getTitle() + " (" + getString(R.string.list_item_current) + ")");

        return super.onPrepareOptionsMenu(menu);
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        menu.add(0, CONTEXT_DELETE_ALL, 0, "Delete all doses for this medication");
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        confirmArchive(info.position);
//        return true;
//    }

    /**
     * Refreshes the listview display.  Used when the data list has changed.
     */
    private void refreshDisplay() {
        updateMedications();

        ListView listView = (ListView) findViewById(R.id.lvMedList);
        adapter = new MedicationArrayAdapter(getBaseContext(), medications);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MedicationItem medicationItem = medications.get(position);

                if (medicationItem.isArchived() || (!medicationItem.isActive() && !medicationItem.hasTaken(MedicationListActivity.this))) {
                    Intent intent = new Intent(getBaseContext(), AddDoseActivity.class);
                    intent.putExtra(ACTION, ACTION_RESTORE);
                    intent.putExtra(TYPE, TYPE_SINGLE);
                    intent.putExtra(INTENT_MED_ID, medicationItem.getId());
                    intent.putExtra(SORT, sortOrder);
                    intent.putExtra(FILTER_DOSE, filter);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getBaseContext(), DoseListActivity.class);
                    intent.putExtra(TYPE, TYPE_SINGLE);
                    intent.putExtra(INTENT_MED_ID, medicationItem.getId());
                    startActivity(intent);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MedicationItem medicationItem = medications.get(position);

                if (medicationItem.isActive()) {
                    medicationItem.confirmSetInactive(MedicationListActivity.this);
                } else {
                    if (!medicationItem.isArchived()) {
                        medicationItem.confirmArchive(MedicationListActivity.this);
                    } else {
                        medicationItem.confirmDeleteMed(MedicationListActivity.this);
                    }
                }
                return true;
            }
        });
    }

    /**
     * Gets a List of all medications in the database sorted by the current setting of sortOrder
     */
    private void updateMedications() {
        db.open();
        switch (filter) {
            case FILTER_ALL:
                medications = db.getAllMedications();
                break;
            case FILTER_INACTIVE:
                medications = db.getAllMedicationsInactive();
                break;
            case FILTER_ARCHIVED:
                medications = db.getAllMedicationsArchived();
                break;
            default:
                medications = db.getAllMedicationsActive();
        }
        db.close();

        switch (sortOrder) {
            case SORT_NAME_ASC:
                break;
            case SORT_NAME_DESC:
                Collections.sort(medications, new MedicationItem.ReverseNameComparator());
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // Not overly happy with this solution but ...
        // Because AlertDialogs are called from MedicationItem class, updating the ListView is problematic.
        // The calling method finishes before the dialog returns, so updating medications field happens before the db is updated.
        // This solution updates the ListView when focus changes back to it (as from the dialogs) but it updates in every case, including on cancel
        updateMedications();
        adapter.updateList(medications);
    }
}
