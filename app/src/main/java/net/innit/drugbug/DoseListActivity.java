package net.innit.drugbug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import net.innit.drugbug.data.DBDataSource;
import net.innit.drugbug.fragment.DetailFragment;
import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.DoseArrayAdapter;
import net.innit.drugbug.util.ReminderArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DoseListActivity extends Activity {
    private static final int CONTEXT_EDIT = 10001;
    private static final int CONTEXT_DELETE = 10002;
    private static final int CONTEXT_REMINDER_SET = 10003;
    private static final int CONTEXT_TAKEN = 10004;
    private static final int CONTEXT_ONLY_THIS_MED = 10005;

    private final DBDataSource db = new DBDataSource(this);
    private List<DoseItem> doses = new ArrayList<>();
    private String type;

    private String sortOrder;
    private Long medId;
    private MedicationItem medication;
    private int listItemPressedPos;
    private String filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        type = bundle.getString("type", DoseItem.TYPE_FUTURE);
        medId = bundle.getLong("med_id");
        sortOrder = bundle.getString("sort_order");
        filter = bundle.getString("filter");

        setContentView(R.layout.activity_list_dose);

        // Set the sortOrder if it wasn't passed in
        if (sortOrder == null) {
            switch (type) {
                case DoseItem.TYPE_TAKEN:
                    sortOrder = "dateDsc";
                    break;
                default:
                    sortOrder = "dateAsc";
            }
        }

        if (filter == null) {
            filter = "none";
        }

        if (bundle.getBoolean("fromReminder", false) && bundle.getLong("dose_id") > 0) {
            showDetailFragment(bundle);
        }
    }

    @Override
    protected void onResume() {
        doses = getDoses();

        refreshDisplay();

        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        if (!type.equals(DoseItem.TYPE_SINGLE)) {
            MenuItem menuItem = menu.findItem(R.id.menu_filter);
            menuItem.setVisible(false);
        } else {
            MenuItem menuItem = menu.findItem(R.id.menu_list_add);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        invalidateOptionsMenu();

        switch (item.getItemId()) {
            case R.id.menu_list_add:
                Intent intent = new Intent(DoseListActivity.this, AddDoseActivity.class);
                intent.putExtra("action", AddDoseActivity.ACTION_ADD);
                intent.putExtra("type", type);
                intent.putExtra("med_id", medId);
                intent.putExtra("sort_order", sortOrder);
                intent.putExtra("filter", filter);
                startActivity(intent);
                return true;
            case R.id.menu_list_help:
                Bundle bundle = new Bundle();
                switch (type) {
                    case DoseItem.TYPE_FUTURE:
                        bundle.putInt("source", HelpFragment.SOURCE_LIST_FUTURE);
                        break;
                    case DoseItem.TYPE_TAKEN:
                        bundle.putInt("source", HelpFragment.SOURCE_LIST_TAKEN);
                        break;
                    case DoseItem.TYPE_SINGLE:
                        bundle.putInt("source", HelpFragment.SOURCE_LIST_SINGLE_MED);
                        break;
                    case DoseItem.TYPE_REMINDER:
                        bundle.putInt("source", HelpFragment.SOURCE_LIST_REMINDERS);
                        break;
                    default:
                        bundle.putInt("source", HelpFragment.SOURCE_MAIN);
                }

                HelpFragment fragment = new HelpFragment();
                fragment.setArguments(bundle);
                fragment.show(getFragmentManager(), "Help Fragment");
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_sort_order_date_asc:
                sortOrder = "dateAsc";
                break;
            case R.id.menu_sort_order_date_dsc:
                sortOrder = "dateDsc";
                break;
            case R.id.menu_sort_order_name:
                sortOrder = "name";
                break;
            case R.id.menu_filter_future:
                filter = "future";
                break;
            case R.id.menu_filter_taken:
                filter = "taken";
                break;
        }
        doses = getDoses();
        refreshDisplay();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem;
        // Indicate which sort order is currently in use
        switch (sortOrder) {
            case "dateDsc":
                menuItem = menu.findItem(R.id.menu_sort_order_date_dsc);
                break;
            case "name":
                menuItem = menu.findItem(R.id.menu_sort_order_name);
                break;
            default:
                menuItem = menu.findItem(R.id.menu_sort_order_date_asc);
        }
        menuItem.setTitle(menuItem.getTitle() + " (" + getString(R.string.list_item_current) + ")");
        if (type.equals(DoseItem.TYPE_SINGLE))
            menu.findItem(R.id.menu_sort_order_name).setVisible(false);

        switch (filter) {
            case "taken":
                menuItem = menu.findItem(R.id.menu_filter_taken);
                break;
            case "future":
                menuItem = menu.findItem(R.id.menu_filter_future);
                break;
            default:
                menuItem = menu.findItem(R.id.menu_filter_none);
        }
        menuItem.setTitle(menuItem.getTitle() + " (" + getString(R.string.list_item_current) + ")");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        DoseItem doseItem = doses.get(listItemPressedPos);

        switch (type) {
            case DoseItem.TYPE_FUTURE:
            case DoseItem.TYPE_REMINDER:
            case DoseItem.TYPE_SINGLE:
                if (doseItem.isTaken()) break;      // no context options for taken items as yet
                menu.add(0, CONTEXT_TAKEN, 0, getString(R.string.context_menu_taken));
                menu.add(0, CONTEXT_EDIT, 1, getString(R.string.doselist_context_edit));
                menu.add(0, CONTEXT_DELETE, 2, getString(R.string.doselist_context_delete));
                Date now = new Date();
                if (now.before(doseItem.getDate()))
                    menu.add(0, CONTEXT_REMINDER_SET, 3, getString((doseItem.isReminderSet()) ? R.string.context_menu_reminder_unset : R.string.context_menu_reminder_set));
            case DoseItem.TYPE_TAKEN:
                if (!type.equals(DoseItem.TYPE_SINGLE))
                    menu.add(0, CONTEXT_ONLY_THIS_MED, 4, getString(R.string.context_menu_only_med));
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        DoseItem doseItem = doses.get(info.position);

        switch (item.getItemId()) {
            case CONTEXT_EDIT:
                Intent intent = new Intent(this, AddDoseActivity.class);
                intent.putExtra("dose_id", doseItem.getId());
                intent.putExtra("action", AddDoseActivity.ACTION_EDIT);
                intent.putExtra("type", type);
                intent.putExtra("sort_order", sortOrder);
                intent.putExtra("med_id", medId);
                startActivity(intent);
                return true;
            case CONTEXT_DELETE:
                intent = new Intent(getApplication(), DoseListActivity.class);
                intent.putExtra("type", type);
                intent.putExtra("sort_order", sortOrder);
                if (medId != null) intent.putExtra("med_id", medId);
                doseItem.confirmDelete(this, intent);
                return true;
            case CONTEXT_REMINDER_SET:
                doseItem.toggleReminder();
                db.open();
                if (db.updateDose(doseItem)) {
                    String message = ((doseItem.isReminderSet()) ? getString(R.string.doselist_toast_reminder_set) : getString(R.string.doselist_toast_reminder_unset));
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.doselist_toast_db_error_update, Toast.LENGTH_SHORT).show();
                }
                db.close();

                doses = getDoses();
                refreshDisplay();
                return true;
            case CONTEXT_TAKEN:
                intent = new Intent(getApplication(), DoseListActivity.class);
                intent.putExtra("type", DoseItem.TYPE_TAKEN);
                intent.putExtra("sort_order", sortOrder);
                doseItem.confirmTaken(this, intent);
                return true;
            case CONTEXT_ONLY_THIS_MED:
                intent = new Intent(getApplication(), DoseListActivity.class);
                intent.putExtra("type", DoseItem.TYPE_SINGLE);
                intent.putExtra("med_id", doseItem.getMedication().getId());
                intent.putExtra("sort_order", sortOrder);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent;
        switch (type) {
            case DoseItem.TYPE_SINGLE:
                intent = new Intent(this, MedicationListActivity.class);
                startActivity(intent);
                break;
        }
        finish();
    }

    /**
     * Redisplay the list of doses
     */
    private void refreshDisplay() {
        ListView listView = (ListView) findViewById(R.id.lvDoseList);
        ArrayAdapter<?> adapter;
        switch (type) {
            case DoseItem.TYPE_REMINDER:
                setTitle(getString(R.string.doselist_title_reminders));
                adapter = new ReminderArrayAdapter(getBaseContext(), doses);
                Log.d(MainActivity.LOGTAG, "refreshDisplay: adapter is " + adapter.toString());
                break;
            case DoseItem.TYPE_SINGLE:
                setTitle(medication.getName());
                adapter = new DoseArrayAdapter(getBaseContext(), doses);
                Log.d(MainActivity.LOGTAG, "refreshDisplay: adapter is " + adapter.toString());
                break;
            default:
                String title = (type.equals(DoseItem.TYPE_TAKEN)) ? getString(R.string.doselist_title_doses_taken) : getString(R.string.doselist_title_doses_future);
                setTitle(title);
                adapter = new DoseArrayAdapter(getBaseContext(), doses);
        }

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DoseItem doseItem = doses.get(position);
                Bundle bundle = new Bundle();
                bundle.putString("type", type);
                bundle.putLong("dose_id", doseItem.getId());
                bundle.putString("sort_order", sortOrder);

                showDetailFragment(bundle);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View v, int pos, long id) {
                listItemPressedPos = pos;
                return false;
            }
        });

        registerForContextMenu(listView);

    }

    private void showDetailFragment(Bundle bundle) {
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(bundle);
        fragment.show(getFragmentManager(), "Detail Fragment");
    }

    /**
     * Returns the list of sorted doses
     *
     * @return sorted list of doses
     */
    private List<DoseItem> getDoses() {
        db.open();

        List<DoseItem> doses;
        switch (type) {
            case DoseItem.TYPE_TAKEN:
                doses = db.getAllTaken(this);
                break;
            case DoseItem.TYPE_REMINDER:
                doses = db.getAllFutureWithReminder(this);
                break;
            case DoseItem.TYPE_SINGLE:
                medication = db.getMedication(medId);
                switch (filter) {
                    case "taken":
                        doses = db.getAllTakenForMed(this, medication);
                        break;
                    case "future":
                        doses = db.getAllFutureForMed(this, medication);
                        break;
                    default:
                        doses = db.getAllDosesForMed(this, medication);
                }
                break;
            default:
                doses = db.getAllFuture(this);

        }
        db.close();

        switch (sortOrder) {
            case "dateAsc":
                break;
            case "dateDsc":
                Collections.sort(doses, new DoseItem.ReverseDateComparator());
                break;
            case "name":
                Collections.sort(doses, new DoseItem.NameComparator());
                break;
        }

        return doses;
    }

}
