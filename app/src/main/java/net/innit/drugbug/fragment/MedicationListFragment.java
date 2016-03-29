package net.innit.drugbug.fragment;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import static net.innit.drugbug.data.Constants.TAG_ADD;
import static net.innit.drugbug.data.Constants.TYPE;
import static net.innit.drugbug.data.Constants.TYPE_SINGLE;

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
     * Refreshes the ListView display.  Used when the data list has changed.
     */
    private void refreshDisplay() {
        if (adapter == null) {
            adapter = new MedicationArrayAdapter(context, getMedications());
        }

        getListView().setAdapter(adapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MedicationItem medicationItem = getMedications().get(position);

                if (medicationItem.isArchived() || (!medicationItem.isActive() && !medicationItem.hasTaken(context))) {
                    Bundle b = new Bundle();
                    b.putString(ACTION, ACTION_RESTORE);
                    b.putString(TYPE, TYPE_SINGLE);
                    b.putLong(INTENT_MED_ID, medicationItem.getId());
                    b.putString(SORT, sortOrder);
                    b.putString(FILTER_DOSE, filter);

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
    private List<MedicationItem> getMedications() {
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

}
