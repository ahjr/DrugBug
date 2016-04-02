package net.innit.drugbug.fragment;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import net.innit.drugbug.DoseListActivity;
import net.innit.drugbug.R;
import net.innit.drugbug.data.DatabaseDAO;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.MedicationArrayAdapter;
import net.innit.drugbug.util.OnListUpdatedListener;

import java.util.Collections;
import java.util.List;

import static net.innit.drugbug.data.Constants.ACTION;
import static net.innit.drugbug.data.Constants.ACTION_REACTIVATE;
import static net.innit.drugbug.data.Constants.ACTION_RESTORE;
import static net.innit.drugbug.data.Constants.FILTER_ACTIVE;
import static net.innit.drugbug.data.Constants.FILTER_ALL;
import static net.innit.drugbug.data.Constants.FILTER_ARCHIVED;
import static net.innit.drugbug.data.Constants.FILTER_INACTIVE;
import static net.innit.drugbug.data.Constants.FILTER_MED;
import static net.innit.drugbug.data.Constants.INTENT_MED_ID;
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
import static net.innit.drugbug.data.Constants.TAG_ADD;
import static net.innit.drugbug.data.Constants.TYPE;
import static net.innit.drugbug.data.Constants.TYPE_MEDICATION;
import static net.innit.drugbug.data.Constants.TYPE_SINGLE;

public class MedicationListFragment extends ListFragment {
    public static final int MENU_NEXT_FUTURE_ASC = 1001;
    public static final int MENU_NEXT_FUTURE_DSC = 1002;
    public static final int MENU_LAST_TAKEN_ASC = 1003;
    public static final int MENU_LAST_TAKEN_DSC = 1004;
    public static final int MENU_ARCHIVED_ASC = 1005;
    public static final int MENU_ARCHIVED_DSC = 1006;

    //    private static final int CONTEXT_DELETE_ALL = 1001;
    private static final int CONTEXT_REACTIVATE = 1002;
    private static final int CONTEXT_ARCHIVE = 1003;

    private DatabaseDAO db; 
    private MedicationArrayAdapter adapter;
    private String medSortOrder;
    private String medFilter;
    private Context context;
    private int listItemPressedPos;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, CONTEXT_REACTIVATE, 0, "Reactivate medication");
        menu.add(0, CONTEXT_ARCHIVE, 1, "Archive medication");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        MedicationItem med = getMedications().get(listItemPressedPos);

        switch (item.getItemId()) {
            case CONTEXT_REACTIVATE:
                Bundle b = new Bundle();
                b.putString(ACTION, ACTION_REACTIVATE);
                b.putString(TYPE, TYPE_MEDICATION);
                b.putString(SORT_MED, medSortOrder);
                b.putString(FILTER_MED, medFilter);
                b.putLong(INTENT_MED_ID, med.getId());

                Fragment fragment = new AddDoseFragment();
                fragment.setArguments(b);
                getFragmentManager().beginTransaction().add(fragment, TAG_ADD).commit();
                return true;
            case CONTEXT_ARCHIVE:
                OnListUpdatedListener listener = new OnListUpdatedListener() {
                    @Override
                    public void onListUpdated() {
                        adapter.updateList(getMedications());
                    }
                };

                med.confirmArchive(context, listener);
                return true;
            default:
                return false;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        fixSortOrder();
        addOptionsMenuItems(menu);
        setCurrentSortIndicator(menu);
        setCurrentFilterIndicator(menu);
    }

    private void setCurrentFilterIndicator(Menu menu) {
        switch (medFilter) {
            case FILTER_ALL:
                menu.findItem(R.id.menu_med_filter_all).setTitle(">> " + getString(R.string.menu_med_filter_all) + " <<");
                break;
            case FILTER_INACTIVE:
                menu.findItem(R.id.menu_med_filter_inactive).setTitle(">> " + getString(R.string.menu_med_filter_inactive) + " <<");
                break;
            case FILTER_ARCHIVED:
                menu.findItem(R.id.menu_med_filter_archived).setTitle(">> " + getString(R.string.menu_med_filter_archived) + " <<");
                break;
            default:
                menu.findItem(R.id.menu_med_filter_active).setTitle(">> " + getString(R.string.menu_med_filter_active) + " <<");
        }
    }

    private void setCurrentSortIndicator(Menu menu) {
        // Indicate which sort order is currently in use
        switch (medSortOrder) {
            case SORT_NEXT_FUTURE_ASC:
                menu.findItem(MENU_NEXT_FUTURE_ASC).setTitle(">> " + getString(R.string.menu_med_sort_next_asc) + " <<");
                break;
            case SORT_NEXT_FUTURE_DESC:
                menu.findItem(MENU_NEXT_FUTURE_DSC).setTitle(">> " + getString(R.string.menu_med_sort_next_dsc) + " <<");
                break;
            case SORT_LAST_TAKEN_ASC:
                menu.findItem(MENU_LAST_TAKEN_ASC).setTitle(">> " + getString(R.string.menu_med_sort_taken_asc) + " <<");
                break;
            case SORT_LAST_TAKEN_DESC:
                menu.findItem(MENU_LAST_TAKEN_DSC).setTitle(">> " + getString(R.string.menu_med_sort_taken_dsc) + " <<");
                break;
            case SORT_ARCHIVED_ASC:
                menu.findItem(MENU_ARCHIVED_ASC).setTitle(">> " + getString(R.string.menu_med_sort_archived_asc) + " <<");
                break;
            case SORT_ARCHIVED_DESC:
                menu.findItem(MENU_ARCHIVED_DSC).setTitle(">> " + getString(R.string.menu_med_sort_archived_dsc) + " <<");
                break;
            case SORT_NAME_DESC:
                menu.findItem(R.id.menu_med_sort_order_name_dsc).setTitle(">> " + getString(R.string.menu_list_sort_name_dsc) + " <<");
                break;
            case SORT_CREATION_ASC:
                menu.findItem(R.id.menu_med_sort_order_creation_asc).setTitle(">> " + getString(R.string.menu_med_sort_creation_asc) + " <<");
                break;
            case SORT_CREATION_DESC:
                menu.findItem(R.id.menu_med_sort_order_creation_dsc).setTitle(">> " + getString(R.string.menu_med_sort_creation_dsc) + " <<");
                break;
            default:
                menu.findItem(R.id.menu_med_sort_order_name_asc).setTitle(">> " + getString(R.string.menu_list_sort_name_asc) + " <<");
        }
    }

    private void addOptionsMenuItems(Menu menu) {
        switch (medFilter) {
            case FILTER_ACTIVE:
                menu.findItem(R.id.menu_med_sort_order).getSubMenu().add(0, MENU_NEXT_FUTURE_ASC, 4, R.string.menu_med_sort_next_asc);
                menu.findItem(R.id.menu_med_sort_order).getSubMenu().add(0, MENU_NEXT_FUTURE_DSC, 5, R.string.menu_med_sort_next_dsc);
                break;
            case FILTER_INACTIVE:
                menu.findItem(R.id.menu_med_sort_order).getSubMenu().add(0, MENU_LAST_TAKEN_ASC, 4, R.string.menu_med_sort_taken_asc);
                menu.findItem(R.id.menu_med_sort_order).getSubMenu().add(0, MENU_LAST_TAKEN_DSC, 5, R.string.menu_med_sort_taken_dsc);
                break;
            case FILTER_ARCHIVED:
                menu.findItem(R.id.menu_med_sort_order).getSubMenu().add(0, MENU_ARCHIVED_ASC, 4, R.string.menu_med_sort_archived_asc);
                menu.findItem(R.id.menu_med_sort_order).getSubMenu().add(0, MENU_ARCHIVED_DSC, 5, R.string.menu_med_sort_archived_dsc);
                break;
        }
    }

    private void fixSortOrder() {
        String origSO = medSortOrder;
        switch (medSortOrder) {
            case SORT_NEXT_FUTURE_ASC:
                switch (medFilter) {
                    case FILTER_ALL:
                        medSortOrder = SORT_NAME_ASC;
                        break;
                    case FILTER_INACTIVE:
                        medSortOrder = SORT_LAST_TAKEN_ASC;
                        break;
                    case FILTER_ARCHIVED:
                        medSortOrder = SORT_ARCHIVED_ASC;
                        break;
                }
                break;

            case SORT_NEXT_FUTURE_DESC:
                switch (medFilter) {
                    case FILTER_ALL:
                        medSortOrder = SORT_NAME_ASC;
                        break;
                    case FILTER_INACTIVE:
                        medSortOrder = SORT_LAST_TAKEN_DESC;
                        break;
                    case FILTER_ARCHIVED:
                        medSortOrder = SORT_ARCHIVED_DESC;
                        break;
                }
                break;

            case SORT_LAST_TAKEN_ASC:
                switch (medFilter) {
                    case FILTER_ALL:
                        medSortOrder = SORT_NAME_ASC;
                        break;
                    case FILTER_ACTIVE:
                        medSortOrder = SORT_NEXT_FUTURE_ASC;
                        break;
                    case FILTER_ARCHIVED:
                        medSortOrder = SORT_ARCHIVED_ASC;
                        break;
                }
                break;

            case SORT_LAST_TAKEN_DESC:
                switch (medFilter) {
                    case FILTER_ALL:
                        medSortOrder = SORT_NAME_ASC;
                        break;
                    case FILTER_ACTIVE:
                        medSortOrder = SORT_NEXT_FUTURE_DESC;
                        break;
                    case FILTER_ARCHIVED:
                        medSortOrder = SORT_ARCHIVED_DESC;
                        break;
                }
                break;

            case SORT_ARCHIVED_ASC:
                switch (medFilter) {
                    case FILTER_ALL:
                        medSortOrder = SORT_NAME_ASC;
                        break;
                    case FILTER_ACTIVE:
                        medSortOrder = SORT_NEXT_FUTURE_ASC;
                        break;
                    case FILTER_INACTIVE:
                        medSortOrder = SORT_LAST_TAKEN_ASC;
                        break;
                }
                break;

            case SORT_ARCHIVED_DESC:
                switch (medFilter) {
                    case FILTER_ALL:
                        medSortOrder = SORT_NAME_ASC;
                        break;
                    case FILTER_ACTIVE:
                        medSortOrder = SORT_NEXT_FUTURE_DESC;
                        break;
                    case FILTER_INACTIVE:
                        medSortOrder = SORT_LAST_TAKEN_DESC;
                        break;
                }
                break;

        }
        if (!origSO.equals(medSortOrder)) {
            refreshDisplay();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getActivity().invalidateOptionsMenu();
        switch (item.getItemId()) {
            case MENU_NEXT_FUTURE_ASC:
                medSortOrder = SORT_NEXT_FUTURE_ASC;
                refreshDisplay();
                return true;
            case MENU_NEXT_FUTURE_DSC:
                medSortOrder = SORT_NEXT_FUTURE_DESC;
                refreshDisplay();
                return true;
            case MENU_LAST_TAKEN_ASC:
                medSortOrder = SORT_LAST_TAKEN_ASC;
                refreshDisplay();
                return true;
            case MENU_LAST_TAKEN_DSC:
                medSortOrder = SORT_LAST_TAKEN_DESC;
                refreshDisplay();
                return true;
            case MENU_ARCHIVED_ASC:
                medSortOrder = SORT_ARCHIVED_ASC;
                refreshDisplay();
                return true;
            case MENU_ARCHIVED_DSC:
                medSortOrder = SORT_ARCHIVED_DESC;
                refreshDisplay();
                return true;
            case R.id.menu_med_sort_order_name_asc:
                medSortOrder = SORT_NAME_ASC;
                refreshDisplay();
                return true;
            case R.id.menu_med_sort_order_name_dsc:
                medSortOrder = SORT_NAME_DESC;
                refreshDisplay();
                return true;
            case R.id.menu_med_sort_order_creation_asc:
                medSortOrder = SORT_CREATION_ASC;
                refreshDisplay();
                return true;
            case R.id.menu_med_sort_order_creation_dsc:
                medSortOrder = SORT_CREATION_DESC;
                refreshDisplay();
                return true;
            case R.id.menu_med_filter_all:
                medFilter = FILTER_ALL;
                refreshDisplay();
                return true;
            case R.id.menu_med_filter_active:
                medFilter = FILTER_ACTIVE;
                refreshDisplay();
                return true;
            case R.id.menu_med_filter_inactive:
                medFilter = FILTER_INACTIVE;
                refreshDisplay();
                return true;
            case R.id.menu_med_filter_archived:
                medFilter = FILTER_ARCHIVED;
                refreshDisplay();
                return true;

        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        context = getActivity();
        db = new DatabaseDAO(context);

        Bundle bundle = getArguments();
        if (bundle != null) {
            medSortOrder = bundle.getString(SORT_MED, SORT_NAME_ASC);
            medFilter = bundle.getString(FILTER_MED, FILTER_ACTIVE);
        } else {
            medSortOrder = SORT_NAME_ASC;
            medFilter = FILTER_ACTIVE;
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_list_medication, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshDisplay();
    }

    /**
     * Refreshes the ListView display.  Used when the data list has changed.
     */
    private void refreshDisplay() {
        if (adapter == null) {
            adapter = new MedicationArrayAdapter(context, getMedications());
        } else {
            adapter.updateList(getMedications());
        }

        getListView().setAdapter(adapter);

        registerForContextMenu(getListView());

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MedicationItem medicationItem = getMedications().get(position);

                if (medicationItem.isArchived() || (!medicationItem.isActive() && !medicationItem.hasTaken(context))) {
                    Bundle b = new Bundle();
                    b.putString(ACTION, ACTION_RESTORE);
                    b.putString(TYPE, TYPE_SINGLE);
                    b.putLong(INTENT_MED_ID, medicationItem.getId());
                    b.putString(SORT_MED, medSortOrder);
                    b.putString(FILTER_MED, medFilter);

                    Fragment fragment = new AddDoseFragment();
                    fragment.setArguments(b);
                    getFragmentManager().beginTransaction().add(fragment, TAG_ADD).commit();
                } else {
                    Intent intent = new Intent(context, DoseListActivity.class);
                    intent.putExtra(TYPE, TYPE_SINGLE);
                    intent.putExtra(INTENT_MED_ID, medicationItem.getId());
                    startActivity(intent);
                }
            }
        });

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                MedicationItem medicationItem = getMedications().get(position);
                listItemPressedPos = position;

                OnListUpdatedListener listener = new OnListUpdatedListener() {
                    @Override
                    public void onListUpdated() {
                        adapter.updateList(getMedications());
                    }
                };

                if (medicationItem.isActive()) {
                    medicationItem.confirmSetInactive(context, listener);
                } else {
                    if (!medicationItem.isArchived()) {
                        getListView().showContextMenu();
                    } else {
                        medicationItem.confirmDeleteMed(context, listener);
                    }
                }
                return true;
            }
        });

    }

    /**
     * Gets a List of all medications in the database sorted by the current setting of medSortOrder
     */
    private List<MedicationItem> getMedications() {
        List<MedicationItem> medicationItems;
        db.open();
        switch (medFilter) {
            case FILTER_ALL:
//                if (medSortOrder.equals(SORT_NEXT_FUTURE_ASC) || medSortOrder.equals(SORT_NEXT_FUTURE_DESC)) {
//                    medicationItems = db.getAllMedicationsByDose(medSortOrder);
//                } else {
                    medicationItems = db.getAllMedications();
//                }
                break;
            case FILTER_INACTIVE:
                if (medSortOrder.equals(SORT_LAST_TAKEN_ASC) || medSortOrder.equals(SORT_LAST_TAKEN_DESC)) {
                    medicationItems = db.getAllMedicationsByDose(medSortOrder);
                } else {
                    medicationItems = db.getAllMedicationsInactive();
                }
                break;
            case FILTER_ARCHIVED:
                if (medSortOrder.equals(SORT_ARCHIVED_ASC) || medSortOrder.equals(SORT_ARCHIVED_DESC)) {
                    medicationItems = db.getAllMedicationsByDose(medSortOrder);
                } else {
                    medicationItems = db.getAllMedicationsArchived();
                }
                break;
            default:
                if (medSortOrder.equals(SORT_NEXT_FUTURE_ASC) || medSortOrder.equals(SORT_NEXT_FUTURE_DESC)) {
                    medicationItems = db.getAllMedicationsByDose(medSortOrder);
                } else {
                    medicationItems = db.getAllMedicationsActive();
                }
        }
        db.close();

        switch (medSortOrder) {

            case SORT_NAME_ASC:
                Collections.sort(medicationItems);
                break;
            case SORT_NAME_DESC:
                Collections.sort(medicationItems, new MedicationItem.ReverseNameComparator());
                break;
            case SORT_CREATION_ASC:
                Collections.sort(medicationItems, new MedicationItem.CreationComparator());
                break;
            case SORT_CREATION_DESC:
                Collections.sort(medicationItems, new MedicationItem.ReverseCreationComparator());
                break;
        }

        return medicationItems;
    }

}
