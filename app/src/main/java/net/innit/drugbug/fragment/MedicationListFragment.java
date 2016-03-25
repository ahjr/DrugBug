package net.innit.drugbug.fragment;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import net.innit.drugbug.AddDoseActivity;
import net.innit.drugbug.DoseListActivity;
import net.innit.drugbug.R;
import net.innit.drugbug.data.DatabaseDAO;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.MedicationArrayAdapter;
import net.innit.drugbug.util.OnListUpdatedListener;

import java.util.Collections;
import java.util.List;

import static net.innit.drugbug.data.Constants.ACTION;
import static net.innit.drugbug.data.Constants.ACTION_RESTORE;
import static net.innit.drugbug.data.Constants.FILTER_ACTIVE;
import static net.innit.drugbug.data.Constants.FILTER_ALL;
import static net.innit.drugbug.data.Constants.FILTER_ARCHIVED;
import static net.innit.drugbug.data.Constants.FILTER_DOSE;
import static net.innit.drugbug.data.Constants.FILTER_INACTIVE;
import static net.innit.drugbug.data.Constants.INTENT_MED_ID;
import static net.innit.drugbug.data.Constants.SORT;
import static net.innit.drugbug.data.Constants.SORT_NAME_ASC;
import static net.innit.drugbug.data.Constants.SORT_NAME_DESC;
import static net.innit.drugbug.data.Constants.TYPE;
import static net.innit.drugbug.data.Constants.TYPE_SINGLE;

/**
 * Created by alissa on 3/24/16.
 */
public class MedicationListFragment extends ListFragment {
    //    private static final int CONTEXT_DELETE_ALL = 1001;
    private DatabaseDAO db; 
    private MedicationArrayAdapter adapter;
    private String sortOrder;
    private String filter;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = getActivity();
        db = new DatabaseDAO(context);

        Bundle bundle = getArguments();
        if (bundle != null) {
            sortOrder = bundle.getString(SORT, SORT_NAME_ASC);
            filter = bundle.getString(FILTER_DOSE, FILTER_ACTIVE);
        } else {
            sortOrder = SORT_NAME_ASC;
            filter = FILTER_ACTIVE;
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
     * Refreshes the listview display.  Used when the data list has changed.
     */
    private void refreshDisplay() {
        final List<MedicationItem> medicationItems = updateMedications();
        if (adapter == null) {
            adapter = new MedicationArrayAdapter(context, medicationItems);
        }

        getListView().setAdapter(adapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MedicationItem medicationItem = medicationItems.get(position);

                if (medicationItem.isArchived() || (!medicationItem.isActive() && !medicationItem.hasTaken(context))) {
                    Intent intent = new Intent(context, AddDoseActivity.class);
                    intent.putExtra(ACTION, ACTION_RESTORE);
                    intent.putExtra(TYPE, TYPE_SINGLE);
                    intent.putExtra(INTENT_MED_ID, medicationItem.getId());
                    intent.putExtra(SORT, sortOrder);
                    intent.putExtra(FILTER_DOSE, filter);
                    startActivity(intent);
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
                MedicationItem medicationItem = medicationItems.get(position);
                OnListUpdatedListener listener = new OnListUpdatedListener() {
                    @Override
                    public void onListUpdated() {
                        adapter.updateList(updateMedications());
                    }
                };

                if (medicationItem.isActive()) {
                    medicationItem.confirmSetInactive(context, listener);
                } else {
                    if (!medicationItem.isArchived()) {
                        medicationItem.confirmArchive(context, listener);
                    } else {
                        medicationItem.confirmDeleteMed(context, listener);
                    }
                }
                return true;
            }
        });
    }

    /**
     * Gets a List of all medications in the database sorted by the current setting of sortOrder
     */
    private List<MedicationItem> updateMedications() {
        List<MedicationItem> medicationItems;
        db.open();
        switch (filter) {
            case FILTER_ALL:
                medicationItems = db.getAllMedications();
                break;
            case FILTER_INACTIVE:
                medicationItems = db.getAllMedicationsInactive();
                break;
            case FILTER_ARCHIVED:
                medicationItems = db.getAllMedicationsArchived();
                break;
            default:
                medicationItems = db.getAllMedicationsActive();
        }
        db.close();

        switch (sortOrder) {

            case SORT_NAME_ASC:
                Collections.sort(medicationItems);
                break;
            case SORT_NAME_DESC:
                Collections.sort(medicationItems, new MedicationItem.ReverseNameComparator());
                break;
        }

        return medicationItems;
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        // Not overly happy with this solution but ...
//        // Because AlertDialogs are called from MedicationItem class, updating the ListView is problematic.
//        // The calling method finishes before the dialog returns, so updating medications field happens before the db is updated.
//        // This solution updates the ListView when focus changes back to it (as from the dialogs) but it updates in every case, including on cancel
//        updateMedications();
//        adapter.updateList(medications);
//    }

}
