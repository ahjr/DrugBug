package net.innit.drugbug;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.innit.drugbug.data.DBDataSource;
import net.innit.drugbug.data.SettingsHelper;
import net.innit.drugbug.fragment.HelpFragment;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;

import java.util.Date;
import java.util.List;

// TODO really need to add comments everywhere
// TODO Extract string resources
// todo delete image file when medication gets removed

public class MainActivity extends Activity {
    public static final String LOGTAG = "DrugBug";

    private TextView mFutureDueToday;
    private TextView mFutureMissed;
    private TextView mFutureNumDoses;
    private TextView mMedActive;
//    private TextView mMedInactive;

    private DBDataSource db;
    private List<DoseItem> doses;
    private List<MedicationItem> medications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBDataSource(this);

        setupViews();

        db.open();
        doses = db.getDosesForDate(new Date());
        medications = db.getAllMedications();
        db.close();

    }

    private void setupViews() {
        mFutureDueToday = (TextView) findViewById(R.id.tvMainFutureDueToday);
        mFutureMissed = (TextView) findViewById(R.id.tvMainFutureMissed);
        mFutureNumDoses = (TextView) findViewById(R.id.tvMainFutureNumDoses);
        mMedActive = (TextView) findViewById(R.id.tvMainMedActive);
//        mMedInactive = (TextView) findViewById(R.id.tvMainMedInactive);
    }

    @Override
    protected void onResume() {
        super.onResume();

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
        mFutureNumDoses.setText(String.format("%,d", sharedPreferences.getInt(SettingsHelper.KEY_NUM_DOSES, SettingsHelper.DEFAULT_NUM_DOSES)));

        // Calculate # of active medications
        mMedActive.setText(String.format("%,d", medications.size()));

        // Calculate # of inactive medications (not implemented)
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
