package net.innit.drugbug.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import net.innit.drugbug.MainActivity;
import net.innit.drugbug.PrefScreenActivity;
import net.innit.drugbug.R;
import net.innit.drugbug.data.DataSource;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.ImageStorage;

import java.util.List;

public class PrefScreenFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private DataSource db;
    private int oldNumDoses;
    private ImageStorage imageStorage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity().getApplicationContext();
        db = new DataSource(context);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        // Set summary for number of untaken doses to keep
        oldNumDoses = sharedPreferences.getInt(PrefScreenActivity.KEY_NUM_DOSES, PrefScreenActivity.DEFAULT_NUM_DOSES);
        findPreference(PrefScreenActivity.KEY_NUM_DOSES).setSummary("Current: " + oldNumDoses);

        // Set summary for taken dose keep time
        findPreference(PrefScreenActivity.KEY_KEEP_TIME_TAKEN)
                .setSummary("Current: " +
                        convertString(sharedPreferences.getString(PrefScreenActivity.KEY_KEEP_TIME_TAKEN, convertString(PrefScreenActivity.DEFAULT_KEEP_TIME_TAKEN))));

        // Set summary for untaken dose keep time
        findPreference(PrefScreenActivity.KEY_KEEP_TIME_MISSED)
                .setSummary("Current: " +
                        convertString(sharedPreferences.getString(PrefScreenActivity.KEY_KEEP_TIME_MISSED, convertString(PrefScreenActivity.DEFAULT_KEEP_TIME_MISSED))));

        // Set summary for image storage location
        imageStorage = new ImageStorage(context);
        if (!imageStorage.LOCATION_EXTERNAL.exists()) {
            // Hide external choice
            ListPreference listPreference = (ListPreference) findPreference(PrefScreenActivity.KEY_IMAGE_STORAGE);
            listPreference.setEntries(new String[]{"Internal"});
            listPreference.setEntryValues(new String[]{"2"});

        }
        findPreference(PrefScreenActivity.KEY_IMAGE_STORAGE).setSummary("Current: " + imageStorage.getDisplayText());
        // if external storage

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PrefScreenActivity.KEY_NUM_DOSES:
                int maxNumDoses = sharedPreferences.getInt(key, PrefScreenActivity.DEFAULT_NUM_DOSES);
                db.open();
                List<MedicationItem> medications = db.getAllMedications();
                for (MedicationItem medication : medications) {
                    int difference = maxNumDoses - oldNumDoses;
                    if (difference > 0) {
                        // number of doses has increased
                        // add $DIFFERENCE doses based on lastDose
                        // Get last dose
                        Log.d(MainActivity.LOGTAG, "onSharedPreferenceChanged: medId " + medication.getId());
                        DoseItem lastFutureDose = db.getLastDose(medication);
                        if (lastFutureDose != null) {
                            int doseCount = (int) db.getFutureDoseCount(medication);
                            while (maxNumDoses > doseCount) {
                                db.generateNextFuture(medication);
                                doseCount++;
                            }
                        }

                    } else if (difference < 0) {
                        // number of doses has decreased
                        // Get current number of future doses and remove doses until the number equals new max number
                        int doseCount = (int) db.getFutureDoseCount(medication);
                        while (doseCount > maxNumDoses) {
                            // Get last future dose for this medication
                            DoseItem lastFutureDose = db.getLastDose(medication);
                            // getLastDose returns null if there isn't one
                            if (lastFutureDose != null) {
                                db.removeDose(lastFutureDose.getId());
                            }
                            doseCount--;
                        }
                    } // if difference is 0, we'll do nothing
                }

                db.close();
                findPreference(key).setSummary("Current: " + maxNumDoses);

                break;
            case PrefScreenActivity.KEY_KEEP_TIME_TAKEN:
                String keepTimeString = sharedPreferences.getString(PrefScreenActivity.KEY_KEEP_TIME_TAKEN, convertString(PrefScreenActivity.DEFAULT_KEEP_TIME_TAKEN));
                findPreference(key).setSummary("Current: " + convertString(keepTimeString));
                db.open();
                int numRemoved = db.removeOldDoses(DoseItem.TYPE_TAKEN, keepTimeString);
                Log.d(MainActivity.LOGTAG, "onSharedPreferenceChanged: " + numRemoved + " taken doses removed");
                db.close();
                break;
            case PrefScreenActivity.KEY_KEEP_TIME_MISSED:
                keepTimeString = sharedPreferences.getString(PrefScreenActivity.KEY_KEEP_TIME_MISSED, convertString(PrefScreenActivity.DEFAULT_KEEP_TIME_MISSED));
                findPreference(key).setSummary("Current: " + convertString(keepTimeString));
                db.open();
                numRemoved = db.removeOldDoses(DoseItem.TYPE_MISSED, keepTimeString);
                Log.d(MainActivity.LOGTAG, "onSharedPreferenceChanged: " + numRemoved + " missed doses removed");
                db.close();
                break;
            case PrefScreenActivity.KEY_IMAGE_STORAGE:
                imageStorage.setLocationType(sharedPreferences.getString(PrefScreenActivity.KEY_IMAGE_STORAGE, PrefScreenActivity.DEFAULT_IMAGE_STORAGE));
                findPreference(key).setSummary("Current: " + imageStorage.getDisplayText());
                Log.d(MainActivity.LOGTAG, "onSharedPreferenceChanged: Image storage location changed to " + imageStorage.getDisplayText());

                break;

        }

    }

    private String convertString(String input) {
        String[] splitString = input.split(":");
        String output = "";
        if ((Integer.parseInt(splitString[0]) > 0)) {
            if (Integer.parseInt(splitString[0]) == 1) {
                output += splitString[0] + " year ";
            } else {
                output += splitString[0] + " years ";
            }
        }
        if ((Integer.parseInt(splitString[1]) > 0)) {
            if (Integer.parseInt(splitString[1]) == 1) {
                output += splitString[1] + " month ";
            } else {
                output += splitString[1] + " months ";
            }
        }
        if ((Integer.parseInt(splitString[2]) > 0)) {
            if (Integer.parseInt(splitString[2]) == 1) {
                output += splitString[2] + " day";
            } else {
                output += splitString[2] + " days";
            }
        }

        return output;
    }
}
