package net.innit.drugbug;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import net.innit.drugbug.data.DBDataSource;
import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.Constants;
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
import static net.innit.drugbug.util.Constants.TYPE;
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
                intent.putExtra(TYPE, "medication");
                intent.putExtra(SORT, sortOrder);
                intent.putExtra(FILTER_DOSE, filter);
                startActivity(intent);
                return true;
            case R.id.menu_list_med_help:
                Bundle bundle = new Bundle();
                bundle.putInt("source", HelpFragment.SOURCE_LIST_MEDICATIONS);

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
                    confirmSetInactive(position);
                } else {
                    if (!medicationItem.isArchived()) {
                        confirmArchive(position);
                    } else {
                        confirmDeleteMed(position);
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

    /**
     * Deletes doses for medication & medication after confirmation from user
     *
     * @param pos Position in the array of the medication to delete
     */
    private void confirmSetInactive(final int pos) {
        final Context context = this;
        final MedicationItem medicationItem = medications.get(pos);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Deactivate medication?");
        alertDialogBuilder.setMessage("All untaken doses will be removed.");
        alertDialogBuilder.setPositiveButton("Yes, deactivate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                int numDeleted = db.removeAllFutureDosesForMed(medicationItem);
                medicationItem.setActive(false);
                db.updateMedication(medicationItem);
                db.close();
                Toast.makeText(context, "" + numDeleted + " doses deleted", Toast.LENGTH_SHORT).show();
                updateMedications();
                adapter.updateList(medications);
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.alert_delete_doses_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    /**
     * Deletes doses for medication & medication after confirmation from user
     *
     * @param pos Position in the array of the medication to delete
     */
    private void confirmArchive(final int pos) {
        final Context context = this;
        final MedicationItem medicationItem = medications.get(pos);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Archive medication?");
        alertDialogBuilder.setMessage("All doses taken and untaken doses will be removed.");
        alertDialogBuilder.setPositiveButton("Yes, archive", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                int numDeleted = db.removeAllDosesForMed(medicationItem);
                medicationItem.setActive(false);
                medicationItem.setArchived(true);
                db.updateMedication(medicationItem);
                db.close();
                Toast.makeText(context, "" + numDeleted + " doses deleted", Toast.LENGTH_SHORT).show();
                updateMedications();
                adapter.updateList(medications);
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.alert_delete_doses_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    private void confirmDeleteMed(final int pos) {
        final Context context = this;
        final MedicationItem medicationItem = medications.get(pos);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.alert_delete_med_title);
        alertDialogBuilder.setMessage(R.string.alert_delete_med_message);
        alertDialogBuilder.setPositiveButton(R.string.alert_delete_med_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                db.removeMedication(context, medicationItem);
                db.close();
                updateMedications();
                adapter.updateList(medications);
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.alert_delete_med_negative, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

}
