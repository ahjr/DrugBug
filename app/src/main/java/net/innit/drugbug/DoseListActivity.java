package net.innit.drugbug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import static net.innit.drugbug.util.Constants.ACTION;
import static net.innit.drugbug.util.Constants.ACTION_ADD;
import static net.innit.drugbug.util.Constants.ACTION_EDIT;
import static net.innit.drugbug.util.Constants.FILTER_DOSE;
import static net.innit.drugbug.util.Constants.FILTER_FUTURE;
import static net.innit.drugbug.util.Constants.FILTER_NONE;
import static net.innit.drugbug.util.Constants.FILTER_TAKEN;
import static net.innit.drugbug.util.Constants.FROM_REMINDER;
import static net.innit.drugbug.util.Constants.INTENT_DOSE_ID;
import static net.innit.drugbug.util.Constants.INTENT_MED_ID;
import static net.innit.drugbug.util.Constants.SORT;
import static net.innit.drugbug.util.Constants.SORT_DATE_ASC;
import static net.innit.drugbug.util.Constants.SORT_DATE_DESC;
import static net.innit.drugbug.util.Constants.SORT_NAME;
import static net.innit.drugbug.util.Constants.SOURCE;
import static net.innit.drugbug.util.Constants.SOURCE_LIST_FUTURE;
import static net.innit.drugbug.util.Constants.SOURCE_LIST_REMINDERS;
import static net.innit.drugbug.util.Constants.SOURCE_LIST_SINGLE_MED;
import static net.innit.drugbug.util.Constants.SOURCE_LIST_TAKEN;
import static net.innit.drugbug.util.Constants.SOURCE_MAIN;
import static net.innit.drugbug.util.Constants.TYPE;
import static net.innit.drugbug.util.Constants.TYPE_FUTURE;
import static net.innit.drugbug.util.Constants.TYPE_REMINDER;
import static net.innit.drugbug.util.Constants.TYPE_SINGLE;
import static net.innit.drugbug.util.Constants.TYPE_TAKEN;

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
        type = bundle.getString(TYPE, TYPE_FUTURE);
        medId = bundle.getLong(INTENT_MED_ID);
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
        }

        if (filter == null) {
            filter = FILTER_NONE;
        }

        if (bundle.getBoolean(FROM_REMINDER, false) && (bundle.getLong(INTENT_DOSE_ID) > 0)) {
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
        if (!type.equals(TYPE_SINGLE)) {
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
                intent.putExtra(ACTION, ACTION_ADD);
                intent.putExtra(TYPE, type);
                intent.putExtra(INTENT_MED_ID, medId);
                intent.putExtra(SORT, sortOrder);
                intent.putExtra(FILTER_DOSE, filter);
                startActivity(intent);
                return true;
            case R.id.menu_list_help:
                Bundle bundle = new Bundle();
                switch (type) {
                    case TYPE_FUTURE:
                        bundle.putInt(SOURCE, SOURCE_LIST_FUTURE);
                        break;
                    case TYPE_TAKEN:
                        bundle.putInt(SOURCE, SOURCE_LIST_TAKEN);
                        break;
                    case TYPE_SINGLE:
                        bundle.putInt(SOURCE, SOURCE_LIST_SINGLE_MED);
                        break;
                    case TYPE_REMINDER:
                        bundle.putInt(SOURCE, SOURCE_LIST_REMINDERS);
                        break;
                    default:
                        bundle.putInt(SOURCE, SOURCE_MAIN);
                }

                HelpFragment fragment = new HelpFragment();
                fragment.setArguments(bundle);
                fragment.show(getFragmentManager(), "Help Fragment");
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_sort_order_date_asc:
                sortOrder = SORT_DATE_ASC;
                break;
            case R.id.menu_sort_order_date_dsc:
                sortOrder = SORT_DATE_DESC;
                break;
            case R.id.menu_sort_order_name:
                sortOrder = SORT_NAME;
                break;
            case R.id.menu_filter_future:
                filter = FILTER_FUTURE;
                break;
            case R.id.menu_filter_taken:
                filter = FILTER_TAKEN;
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
            case SORT_DATE_DESC:
                menuItem = menu.findItem(R.id.menu_sort_order_date_dsc);
                break;
            case SORT_NAME:
                menuItem = menu.findItem(R.id.menu_sort_order_name);
                break;
            default:
                menuItem = menu.findItem(R.id.menu_sort_order_date_asc);
        }
        menuItem.setTitle(menuItem.getTitle() + " (" + getString(R.string.list_item_current) + ")");
        if (type.equals(TYPE_SINGLE))
            menu.findItem(R.id.menu_sort_order_name).setVisible(false);

        switch (filter) {
            case FILTER_TAKEN:
                menuItem = menu.findItem(R.id.menu_filter_taken);
                break;
            case FILTER_FUTURE:
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
            case TYPE_FUTURE:
            case TYPE_REMINDER:
            case TYPE_SINGLE:
                if (doseItem.isTaken()) break;      // no context options for taken items as yet
                menu.add(0, CONTEXT_TAKEN, 0, getString(R.string.context_menu_taken));
                menu.add(0, CONTEXT_EDIT, 1, getString(R.string.dose_list_context_edit));
                menu.add(0, CONTEXT_DELETE, 2, getString(R.string.dose_list_context_delete));
                Date now = new Date();
                if (now.before(doseItem.getDate()))
                    menu.add(0, CONTEXT_REMINDER_SET, 3, getString((doseItem.isReminderSet()) ? R.string.context_menu_reminder_unset : R.string.context_menu_reminder_set));
            case TYPE_TAKEN:
                if (!type.equals(TYPE_SINGLE))
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
                intent.putExtra(INTENT_DOSE_ID, doseItem.getId());
                intent.putExtra(ACTION, ACTION_EDIT);
                intent.putExtra(TYPE, type);
                intent.putExtra(SORT, sortOrder);
                intent.putExtra(INTENT_MED_ID, medId);
                startActivity(intent);
                return true;
            case CONTEXT_DELETE:
                intent = new Intent(getApplication(), DoseListActivity.class);
                intent.putExtra(TYPE, type);
                intent.putExtra(SORT, sortOrder);
                if (medId != null) intent.putExtra(INTENT_MED_ID, medId);
                doseItem.confirmDelete(this, intent);
                return true;
            case CONTEXT_REMINDER_SET:
                doseItem.toggleReminder();
                db.open();
                if (db.updateDose(doseItem)) {
                    String message = ((doseItem.isReminderSet()) ? getString(R.string.dose_list_toast_reminder_set) : getString(R.string.dose_list_toast_reminder_unset));
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.dose_list_toast_db_error_update, Toast.LENGTH_SHORT).show();
                }
                db.close();

                doses = getDoses();
                refreshDisplay();
                return true;
            case CONTEXT_TAKEN:
                intent = new Intent(getApplication(), DoseListActivity.class);
                intent.putExtra(TYPE, TYPE_TAKEN);
                intent.putExtra(SORT, sortOrder);
                doseItem.confirmTaken(this, intent);
                return true;
            case CONTEXT_ONLY_THIS_MED:
                intent = new Intent(getApplication(), DoseListActivity.class);
                intent.putExtra(TYPE, TYPE_SINGLE);
                intent.putExtra(INTENT_MED_ID, doseItem.getMedication().getId());
                intent.putExtra(SORT, sortOrder);
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
            case TYPE_SINGLE:
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
            case TYPE_REMINDER:
                setTitle(getString(R.string.dose_list_title_reminders));
                adapter = new ReminderArrayAdapter(this, doses);
                break;
            case TYPE_SINGLE:
                setTitle(medication.getName());
                adapter = new DoseArrayAdapter(this, doses);
                break;
            default:
                String title = (type.equals(TYPE_TAKEN)) ? getString(R.string.dose_list_title_doses_taken) : getString(R.string.dose_list_title_doses_future);
                setTitle(title);
                adapter = new DoseArrayAdapter(this, doses);
        }

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DoseItem doseItem = doses.get(position);
                Bundle bundle = new Bundle();
                bundle.putString(TYPE, type);
                bundle.putLong(INTENT_DOSE_ID, doseItem.getId());
                bundle.putString(SORT, sortOrder);

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
            case TYPE_TAKEN:
                doses = db.getAllTaken(this);
                break;
            case TYPE_REMINDER:
                doses = db.getAllFutureWithReminder(this);
                break;
            case TYPE_SINGLE:
                medication = db.getMedication(medId);
                switch (filter) {
                    case FILTER_TAKEN:
                        doses = db.getAllTakenForMed(this, medication);
                        break;
                    case FILTER_FUTURE:
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
            case SORT_DATE_ASC:
                break;
            case SORT_DATE_DESC:
                Collections.sort(doses, new DoseItem.ReverseDateComparator());
                break;
            case SORT_NAME:
                Collections.sort(doses, new DoseItem.NameComparator());
                break;
        }

        return doses;
    }

}
