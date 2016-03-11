package net.innit.drugbug;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
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

// future todo Add edit medication - set reminder for all doses?

/**
 * Activity to create a medication list
 */
public class MedicationListActivity extends Activity {
    private static final int CONTEXT_DELETE_ALL = 1001;
    private DBDataSource db;
    private List<MedicationItem> medications;
    private String sortOrder;
    private MedicationArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_medication);

        db = new DBDataSource(this);
        db.open();
        medications = db.getAllMedications();
        db.close();
        sortOrder = "dateDsc";

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
            case R.id.menu_med_sort_order_name_asc:
                sortOrder = "nameAsc";
                medications = getMedications();
                refreshDisplay();
                return true;
            case R.id.menu_med_sort_order_name_dsc:
                sortOrder = "nameDsc";
                medications = getMedications();
                refreshDisplay();
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
        }

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
        menuItem.setTitle(menuItem.getTitle() + " (" + getString(R.string.dose_list_sort_order_current) + ")");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, CONTEXT_DELETE_ALL, 0, "Delete all doses for this medication");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        confirmDelete(info.position);
        return true;
    }

    /**
     * Refreshes the listview display.  Used when the data list has changed.
     */
    private void refreshDisplay() {
        Log.d(MainActivity.LOGTAG, "refreshDisplay: Refreshing display");
        ListView listView = (ListView) findViewById(R.id.lvMedList);
        adapter = new MedicationArrayAdapter(getBaseContext(), medications);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MedicationItem medicationItem = medications.get(position);

                Intent intent = new Intent(getBaseContext(), DoseListActivity.class);
                intent.putExtra("type", DoseItem.TYPE_SINGLE);
                intent.putExtra("med_id", medicationItem.getId());
                startActivity(intent);
            }
        });

        registerForContextMenu(listView);
    }

    /**
     * Gets a List of all medications in the database sorted by the current setting of sortOrder
     *
     * @return Sorted list of medications
     */
    private List<MedicationItem> getMedications() {
        db.open();

        List<MedicationItem> medications = db.getAllMedications();

        switch (sortOrder) {
            case "nameAsc":
                break;
            case "nameDsc":
                Collections.sort(medications, new MedicationItem.ReverseNameComparator());
                break;
        }
        db.close();

        return medications;
    }

    /**
     * Deletes doses for medication & medication after confirmation from user
     *
     * @param pos Position in the array of the medication to delete
     */
    public void confirmDelete(final int pos) {
        final Context context = this;
        final MedicationItem medicationItem = medications.get(pos);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.alert_delete_med_title);
        alertDialogBuilder.setMessage(R.string.alert_delete_med_message);
        alertDialogBuilder.setPositiveButton(R.string.alert_delete_med_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                int numDeleted = db.removeAllDosesForMed(context, medicationItem);
                db.close();
                Toast.makeText(context, "" + numDeleted + " doses deleted", Toast.LENGTH_SHORT).show();
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
