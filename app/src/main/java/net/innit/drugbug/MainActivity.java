package net.innit.drugbug;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.innit.drugbug.data.DBDataSource;
import net.innit.drugbug.data.Settings;
import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;
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

// todo Swipe right goes back
// -- This is more complicated than I anticipated
// -- http://androidexample.com/Swipe_screen_left__right__top_bottom/index.php?view=article_discription&aid=95&aaid=118

// todo Handle missed doses better
// -- Show only one entry in list "Med Name - # missed"/"Last missed dose: Date/time"
// -- Clicking shows list of all missed doses

// todo Handle reminder changes better
// -- Currently removing reminder from one dose changes all
// -- Should probably give the option to remove all or one

// todo Refactor settings enum
// -- Change to singleton SharedPreferences access? https://gist.github.com/alphamu/8748537a3b73d9c58b4c

// todo Future dose list help screen is wrong --> Can't replicate this

// todo Back from Medication list(inactive) -> dose list returns to default filter
// -- Pretty sure the only way to change this is to have 2 different filter extras (med_filter and dose_filter?)

// todo Possible --> should taking dose shift all future doses?

// todo Implement Frequency customization

// todo Give user the ability to set meal and waking times for more accuracy

// todo Some Activity classes are doing too much.  Need to refactor.

// todo Normalize drawables (size/color/name).  Remove unused.

// todo Extract string resources from code (probably still some in xml too)
// -- Find in Path (regular expression) ".*"

public class MainActivity extends Activity {

    private TextView mFutureDueToday;
    private TextView mFutureMissed;
    private TextView mFutureNumDoses;
    private TextView mMedActive;
    private TextView mMedInactive;
    private TextView mMedArchived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set SharedPreferences to default if setting is not set yet
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (Constants.CLEAR_SHARED_PREFS) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Settings setting : Settings.values()) {
            if (!sharedPreferences.contains(setting.getKey())) {
                if (setting == Settings.NUM_DOSES) {
                    editor.putInt(setting.getKey(), Integer.parseInt(setting.getDefault(this)));
                } else {
                    editor.putString(setting.getKey(), setting.getDefault(this));
                }
            }
        }
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupViews();

        new FutureWorkerTask(mFutureDueToday, mFutureMissed).execute();

        // Lookup number of doses from SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mFutureNumDoses.setText(String.format("%,d", sharedPreferences.getInt(Settings.NUM_DOSES.getKey(), Integer.parseInt(Settings.NUM_DOSES.getDefault(this)))));

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
            String[] strings = new String[2];
            DBDataSource db = new DBDataSource(MainActivity.this);
            db.open();
            List<DoseItem> doses = db.getAllDosesForDate(MainActivity.this, new Date());
            db.close();

            Date now = new Date();

            // Calculate # doses due today
            int count = 0;
            for (DoseItem dose : doses) {
                if (!dose.isTaken() && now.compareTo(dose.getDate())<0) {
                    count++;
                }
            }
            strings[0] = String.format("%,d", count);

            // Calculate # missed doses today
            count = 0;
            for (DoseItem dose : doses) {
                if (!dose.isTaken() && now.compareTo(dose.getDate())>=0) {
                    count++;
                }
            }
            strings[1] = String.format("%,d", count);

            return strings;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                if (dueTodayReference != null) {
                    final TextView textView = dueTodayReference.get();
                    if (textView != null) {
                        textView.setText(strings[0]);
                    }
                }
                if (missedReference != null) {
                    final TextView textView = missedReference.get();
                    if (textView != null) {
                        textView.setText(strings[1]);
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
            String[] strings = new String[3];
            DBDataSource db = new DBDataSource(MainActivity.this);

            db.open();
            List<MedicationItem> medications = db.getAllMedicationsActive();
            strings[0] = String.format("%,d", medications.size());
            medications = db.getAllMedicationsInactive();
            strings[1] = String.format("%,d", medications.size());
            medications = db.getAllMedicationsArchived();
            strings[2] = String.format("%,d", medications.size());
            db.close();

            return strings;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                if (activeReference != null) {
                    final TextView textView = activeReference.get();
                    if (textView != null) {
                        textView.setText(strings[0]);
                    }
                }
                if (inactiveReference != null) {
                    final TextView textView = inactiveReference.get();
                    if (textView != null) {
                        textView.setText(strings[1]);
                    }
                }
                if (archivedReference != null) {
                    final TextView textView = archivedReference.get();
                    if (textView != null) {
                        textView.setText(strings[2]);
                    }
                }
            }
        }
    }
}
