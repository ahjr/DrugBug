package net.innit.drugbug;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.innit.drugbug.data.DBDataSource;
import net.innit.drugbug.data.Settings;
import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.util.Constants;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

import static net.innit.drugbug.util.Constants.ACTION;
import static net.innit.drugbug.util.Constants.ACTION_ADD;
import static net.innit.drugbug.util.Constants.SOURCE;
import static net.innit.drugbug.util.Constants.SOURCE_MAIN;
import static net.innit.drugbug.util.Constants.TYPE;
import static net.innit.drugbug.util.Constants.TYPE_FUTURE;
import static net.innit.drugbug.util.Constants.TYPE_REMINDER;
import static net.innit.drugbug.util.Constants.TYPE_TAKEN;

public class MainActivity extends Activity {

    private TextView mFutureDueToday;
    private TextView mFutureMissed;
    private TextView mFutureNumDoses;
    private TextView mMedActive;
    private TextView mMedInactive;
    private TextView mMedArchived;

    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializePrefs();
    }

    private void initializePrefs() {
        settings = Settings.getInstance(getApplicationContext());

        // Clean SharedPreferences if constant is true
        if (Constants.CLEAR_SHARED_PREFS) {
            settings.clear();
            settings.apply();
        }

        // Set SharedPreferences to default if setting is not set yet
        settings.edit();
        for (Settings.Key setting : Settings.Key.values()) {
            if (!settings.contains(setting)) {
                if (setting == Settings.Key.NUM_DOSES) {
                    settings.put(setting);
                } else {
                    settings.put(setting);
                }
            }
        }
        settings.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupViews();

        new FutureWorkerTask(mFutureDueToday, mFutureMissed).execute();

        // Lookup number of doses from SharedPreferences
        mFutureNumDoses.setText(settings.getString(Settings.Key.NUM_DOSES));

        new MedicationsWorkerTask(mMedActive, mMedInactive, mMedArchived).execute();

    }

    private void setupViews() {
        mFutureDueToday = (TextView) findViewById(R.id.tvMainFutureDueToday);
        mFutureMissed = (TextView) findViewById(R.id.tvMainFutureMissed);
        mFutureNumDoses = (TextView) findViewById(R.id.tvMainFutureNumDoses);
        mMedActive = (TextView) findViewById(R.id.tvMainMedActive);
        mMedInactive = (TextView) findViewById(R.id.tvMainMedInactive);
        mMedArchived = (TextView) findViewById(R.id.tvMainMedArchived);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_add:
                Intent intent = new Intent(MainActivity.this, AddDoseActivity.class);
                intent.putExtra(ACTION, ACTION_ADD);
                startActivity(intent);
                return true;
            case R.id.menu_main_prefs:
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_main_help:
                Bundle bundle = new Bundle();
                bundle.putInt(SOURCE, SOURCE_MAIN);

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

    /**
     * Click handler to launch future list activity
     *
     * @param view The view that was clicked
     */
    public void onClickMainFuture(View view) {
        Intent intent = new Intent(this, DoseListActivity.class);
        intent.putExtra(TYPE, TYPE_FUTURE);
        startActivity(intent);
    }

    /**
     * Click handler to launch taken list activity
     *
     * @param view The view that was clicked
     */
    public void onClickMainTaken(View view) {
        Intent intent = new Intent(this, DoseListActivity.class);
        intent.putExtra(TYPE, TYPE_TAKEN);
        startActivity(intent);
    }

    /**
     * Click handler to launch reminder list
     *
     * @param view The view that was clicked
     */
    public void onClickMainReminders(View view) {
        Intent intent = new Intent(this, DoseListActivity.class);
        intent.putExtra(TYPE, TYPE_REMINDER);
        startActivity(intent);
    }

    /**
     * Click handler to launch medication list
     *
     * @param view The view that was clicked
     */
    public void onClickMainMedications(View view) {
        Intent intent = new Intent(this, MedicationListActivity.class);
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
            DBDataSource db = new DBDataSource(MainActivity.this);
            db.open();
            List<DoseItem> doses = db.getAllDosesForDate(MainActivity.this, new Date());
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
            DBDataSource db = new DBDataSource(MainActivity.this);

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
