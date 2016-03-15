package net.innit.drugbug;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import net.innit.drugbug.data.DBDataSource;
import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.MedicationArrayAdapter;

import java.util.Collections;
import java.util.List;

/**
 * Activity to create a medication list
 */
public class MedicationListActivity extends Activity {
//    private static final int CONTEXT_DELETE_ALL = 1001;
    private DBDataSource db = new DBDataSource(this);
    private List<MedicationItem> medications;
    private String sortOrder;
    private MedicationArrayAdapter adapter;
    private String filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_medication);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            sortOrder = bundle.getString("sort_order", "dateDsc");
            filter = bundle.getString("filter", "active");
        } else {
            sortOrder = "dateDsc";
            filter = "active";
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
                intent.putExtra("action", AddDoseActivity.ACTION_ADD);
                intent.putExtra("type", "medication");
                intent.putExtra("sort_order", sortOrder);
                intent.putExtra("filter", filter);
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
                sortOrder = "nameAsc";
                break;
            case R.id.menu_med_sort_order_name_dsc:
                sortOrder = "nameDsc";
                break;
            case R.id.menu_med_filter_all:
                filter = "all";
                break;
            case R.id.menu_med_filter_active:
                filter = "active";
                break;
            case R.id.menu_med_filter_inactive:
                filter = "inactive";
                break;
        }
        refreshDisplay();
//        return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem;
        // Indicate which sort order is currently in use
        switch (sortOrder) {
            case "nameDsc":
                menuItem = menu.findItem(R.id.menu_med_sort_order_name_dsc);
                break;
            default:
                menuItem = menu.findItem(R.id.menu_med_sort_order_name_asc);
        }
        menuItem.setTitle(menuItem.getTitle() + " (" + getString(R.string.list_item_current) + ")");

        switch (filter) {
            case "all":
                menuItem = menu.findItem(R.id.menu_med_filter_all);
                break;
            case "active":
                menuItem = menu.findItem(R.id.menu_med_filter_active);
                break;
            case "inactive":
                menuItem = menu.findItem(R.id.menu_med_filter_inactive);
                break;
        }
        menuItem.setTitle(menuItem.getTitle() + " (" + getString(R.string.list_item_current) + ")");

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
//        confirmDeleteAllDoses(info.position);
//        return true;
//    }

    /**
     * Refreshes the listview display.  Used when the data list has changed.
     */
    private void refreshDisplay() {
        Log.d(MainActivity.LOGTAG, "refreshDisplay: Refreshing display");
        medications = getMedications();

        ListView listView = (ListView) findViewById(R.id.lvMedList);
        adapter = new MedicationArrayAdapter(getBaseContext(), medications);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MedicationItem medicationItem = medications.get(position);

                if (medicationItem.isArchived()) {
                    // Reactivate
                    Intent intent = new Intent(getBaseContext(), AddDoseActivity.class);
                    intent.putExtra("action", "reactivate");
                    intent.putExtra("med_id", medicationItem.getId());
                    intent.putExtra("sort_order", sortOrder);
                    intent.putExtra("filter", filter);
                    startActivity(intent);
                } else {
                    // View only this med
                    Intent intent = new Intent(getBaseContext(), DoseListActivity.class);
                    intent.putExtra("type", DoseItem.TYPE_SINGLE);
                    intent.putExtra("med_id", medicationItem.getId());
                    startActivity(intent);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MedicationItem medicationItem = medications.get(position);

                if (medicationItem.isArchived()) {
                    // Delete permanently
                    confirmDeleteMed(position);
                } else {
                    // Delete doses
                    confirmDeleteAllDoses(position);
                }
                return true;
            }
        });

//        registerForContextMenu(listView);
    }

    /**
     * Gets a List of all medications in the database sorted by the current setting of sortOrder
     *
     * @return Sorted list of medications
     */
    private List<MedicationItem> getMedications() {
        List<MedicationItem> medications;
        db.open();
        switch (filter) {
            case "all":
                medications = db.getAllMedications();
                break;
            case "inactive":
                medications = db.getAllMedicationsArchived();
                break;
            default:
                medications = db.getAllMedicationsUnarchived();
        }
        db.close();

        switch (sortOrder) {
            case "nameAsc":
                break;
            case "nameDsc":
                Collections.sort(medications, new MedicationItem.ReverseNameComparator());
                break;
        }

        return medications;
    }

    /**
     * Deletes doses for medication & medication after confirmation from user
     *
     * @param pos Position in the array of the medication to delete
     */
    private void confirmDeleteAllDoses(final int pos) {
        // todo Give choice to keep taken doses
        final Context context = this;
        final MedicationItem medicationItem = medications.get(pos);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.alert_delete_doses_title);
        alertDialogBuilder.setMessage(R.string.alert_delete_doses_message);
        alertDialogBuilder.setPositiveButton(R.string.alert_delete_doses_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                int numDeleted = db.removeAllDosesForMed(medicationItem);
                db.close();
                Toast.makeText(context, "" + numDeleted + " doses deleted", Toast.LENGTH_SHORT).show();
                medications.remove(pos);
                adapter.notifyDataSetChanged();
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
                medications.remove(pos);
                adapter.notifyDataSetChanged();
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
