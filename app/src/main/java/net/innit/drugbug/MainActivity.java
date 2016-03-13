package net.innit.drugbug;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.innit.drugbug.data.DBDataSource;
import net.innit.drugbug.data.Settings;
import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;

import java.util.Date;
import java.util.List;

// future todo (After archiving/deleting of meds complete) Delete image file when medication gets removed
// future todo swipe right goes back
// -- This is more complicated than I anticipated
// -- http://androidexample.com/Swipe_screen_left__right__top_bottom/index.php?view=article_discription&aid=95&aaid=118
// future todo Handle missed doses better
// -- Show only one entry in list "Med Name - # missed"/"Last missed dose: Date/time"
// -- Clicking shows list of all missed doses
// future todo Handle reminder changes better
// -- Currently removing reminder from one dose changes all
// -- Should probably give the option to remove all or one

public class MainActivity extends Activity {
    public static final String LOGTAG = "DrugBug";

    private TextView mFutureDueToday;
    private TextView mFutureMissed;
    private TextView mFutureNumDoses;
    private TextView mMedActive;
//    private TextView mMedInactive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set SharedPreferences to default if setting is not set yet
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Settings setting : Settings.values()) {
            if (!sharedPreferences.contains(setting.getKey())) {
                if (setting.equals(Settings.NUM_DOSES)) {
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
        Log.d(MainActivity.LOGTAG, "onResume: start");

        DBDataSource db = new DBDataSource(this);

        db.open();
        List<DoseItem> doses = db.getAllDosesForDate(this, new Date());
        List<MedicationItem> medications = db.getAllMedications();
        db.close();

        setupViews();

        Date now = new Date();

        // Calculate # doses due today
        int count = 0;
        for (DoseItem dose : doses) {
            if (!dose.isTaken() && now.compareTo(dose.getDate())<0) {
                count++;
            }
        }
        mFutureDueToday.setText(String.format("%,d", count));

        // Calculate # missed doses today
        count = 0;
        for (DoseItem dose : doses) {
            if (!dose.isTaken() && now.compareTo(dose.getDate())>=0) {
                count++;
            }
        }
        mFutureMissed.setText(String.format("%,d", count));

        // Lookup number of doses from SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mFutureNumDoses.setText(String.format("%,d", sharedPreferences.getInt(Settings.NUM_DOSES.getKey(), Integer.parseInt(Settings.NUM_DOSES.getDefault(this)))));

        // Calculate # of active medications
        mMedActive.setText(String.format("%,d", medications.size()));

        // Calculate # of inactive medications (not implemented)
    }

    private void setupViews() {
        mFutureDueToday = (TextView) findViewById(R.id.tvMainFutureDueToday);
        mFutureMissed = (TextView) findViewById(R.id.tvMainFutureMissed);
        mFutureNumDoses = (TextView) findViewById(R.id.tvMainFutureNumDoses);
        mMedActive = (TextView) findViewById(R.id.tvMainMedActive);
//        mMedInactive = (TextView) findViewById(R.id.tvMainMedInactive);
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
                intent.putExtra("action", AddDoseActivity.ACTION_ADD);
                startActivity(intent);
                return true;
            case R.id.menu_main_prefs:
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_main_help:
                Bundle bundle = new Bundle();
                bundle.putInt("source", HelpFragment.SOURCE_MAIN);

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
        intent.putExtra("type", DoseItem.TYPE_FUTURE);
        startActivity(intent);
    }

    /**
     * Click handler to launch taken list activity
     *
     * @param view The view that was clicked
     */
    public void onClickMainTaken(View view) {
        Intent intent = new Intent(this, DoseListActivity.class);
        intent.putExtra("type", DoseItem.TYPE_TAKEN);
        startActivity(intent);
    }

    /**
     * Click handler to launch reminder list
     *
     * @param view The view that was clicked
     */
    public void onClickMainReminders(View view) {
        Intent intent = new Intent(this, DoseListActivity.class);
        intent.putExtra("type", DoseItem.TYPE_REMINDER);
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
}
