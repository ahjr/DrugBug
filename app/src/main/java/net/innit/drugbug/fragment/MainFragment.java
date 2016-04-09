package net.innit.drugbug.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import net.innit.drugbug.DoseListActivity;
import net.innit.drugbug.MedicationListActivity;
import net.innit.drugbug.R;
import net.innit.drugbug.data.DatabaseDAO;
import net.innit.drugbug.data.Settings;
import net.innit.drugbug.model.DoseItem;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

import static net.innit.drugbug.data.Constants.TYPE;
import static net.innit.drugbug.data.Constants.TYPE_FUTURE;
import static net.innit.drugbug.data.Constants.TYPE_REMINDER;
import static net.innit.drugbug.data.Constants.TYPE_TAKEN;

public class MainFragment extends Fragment {

    private TextView mFutureDueToday;
    private TextView mFutureMissed;
    private TextView mFutureNumDoses;
    private TextView mMedActive;
    private TextView mMedInactive;
    private TextView mMedArchived;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        context = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        setupViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        new FutureWorkerTask(mFutureDueToday, mFutureMissed).execute();

        // Lookup number of doses from SharedPreferences
        Settings settings = Settings.getInstance(context.getApplicationContext());
        mFutureNumDoses.setText(settings.getString(Settings.Key.NUM_DOSES));

        new MedicationsWorkerTask(mMedActive, mMedInactive, mMedArchived).execute();
    }

    private void setupViews(View view) {
        mFutureDueToday = (TextView) view.findViewById(R.id.tvMainFutureDueToday);
        mFutureMissed = (TextView) view.findViewById(R.id.tvMainFutureMissed);
        mFutureNumDoses = (TextView) view.findViewById(R.id.tvMainFutureNumDoses);
        mMedActive = (TextView) view.findViewById(R.id.tvMainMedActive);
        mMedInactive = (TextView) view.findViewById(R.id.tvMainMedInactive);
        mMedArchived = (TextView) view.findViewById(R.id.tvMainMedArchived);

        TableLayout tableLayout = (TableLayout) view.findViewById(R.id.tlMainFuture);
        tableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMainFuture(v);
            }
        });

        tableLayout = (TableLayout) view.findViewById(R.id.tlMainTaken);
        tableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMainTaken(v);
            }
        });

        tableLayout = (TableLayout) view.findViewById(R.id.btnMainReminders);
        tableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMainReminders(v);
            }
        });

        tableLayout = (TableLayout) view.findViewById(R.id.tlMainMedications);
        tableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMainMedications(v);
            }
        });
    }

    /**
     * Click handler to launch future list activity
     *
     * @param view The view that was clicked
     */
    private void onClickMainFuture(View view) {
        Intent intent = new Intent(context, DoseListActivity.class);
        intent.putExtra(TYPE, TYPE_FUTURE);
        startActivity(intent);
    }

    /**
     * Click handler to launch taken list activity
     *
     * @param view The view that was clicked
     */
    private void onClickMainTaken(View view) {
        Intent intent = new Intent(context, DoseListActivity.class);
        intent.putExtra(TYPE, TYPE_TAKEN);
        startActivity(intent);
    }

    /**
     * Click handler to launch reminder list
     *
     * @param view The view that was clicked
     */
    private void onClickMainReminders(View view) {
        Intent intent = new Intent(context, DoseListActivity.class);
        intent.putExtra(TYPE, TYPE_REMINDER);
        startActivity(intent);
    }

    /**
     * Click handler to launch medication list
     *
     * @param view The view that was clicked
     */
    private void onClickMainMedications(View view) {
        Intent intent = new Intent(context, MedicationListActivity.class);
        startActivity(intent);
    }

    private class FutureWorkerTask extends AsyncTask<Void, Void, String[]> {
        private final WeakReference<TextView> dueTodayReference, missedReference;

        public FutureWorkerTask(TextView mFutureDueToday, TextView mFutureMissed) {
            dueTodayReference = new WeakReference<>(mFutureDueToday);
            missedReference = new WeakReference<>(mFutureMissed);
        }

        @Override
        protected String[] doInBackground(Void... params) {
            DatabaseDAO db = new DatabaseDAO(context);
            db.open();
            List<DoseItem> doses = db.getAllDosesForDate(context, new Date());
            db.close();

            Date now = new Date();

            // Calculate # doses due today
            int dueToday = 0;
            int missed = 0;
            for (DoseItem dose : doses) {
                if (!dose.isTaken()) {
                    if (now.compareTo(dose.getDate())<0) {
                        dueToday++;
                    } else {
                        missed++;
                    }
                }
            }

            return new String[] {
                    String.format("%,d", dueToday),
                    String.format("%,d", missed)
            };
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                WeakReference[] refs = new WeakReference[] {
                        dueTodayReference,
                        missedReference
                };
                for (int i = 0; i < 2; i++) {
                    if (refs[i] != null) {
                        final TextView textView = (TextView) refs[i].get();
                        if (textView != null) {
                            textView.setText(strings[i]);
                        }
                    }
                }
            }
        }
    }

    private class MedicationsWorkerTask extends AsyncTask<Void, Void, String[]> {
        private final WeakReference<TextView> activeReference;
        private final WeakReference<TextView> inactiveReference;
        private final WeakReference<TextView> archivedReference;

        public MedicationsWorkerTask(TextView mMedActive, TextView mMedInactive, TextView mMedArchived) {
            activeReference = new WeakReference<>(mMedActive);
            inactiveReference = new WeakReference<>(mMedInactive);
            archivedReference = new WeakReference<>(mMedArchived);
        }

        @Override
        protected String[] doInBackground(Void... params) {
            DatabaseDAO db = new DatabaseDAO(context);

            db.open();
            String[] strings = new String[] {
                    String.format("%,d", db.getAllMedicationsActive().size()),
                    String.format("%,d", db.getAllMedicationsInactive().size()),
                    String.format("%,d", db.getAllMedicationsArchived().size())
            };
            db.close();

            return strings;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                WeakReference[] refs = new WeakReference[] {
                        activeReference,
                        inactiveReference,
                        archivedReference
                };
                for (int i = 0; i < 3; i++) {
                    if (refs[i] != null) {
                        final TextView textView = (TextView) refs[i].get();
                        if (textView != null) {
                            textView.setText(strings[i]);
                        }
                    }
                }
            }
        }
    }

}
