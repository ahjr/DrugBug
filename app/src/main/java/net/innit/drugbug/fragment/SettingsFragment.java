package net.innit.drugbug.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import net.innit.drugbug.MainActivity;
import net.innit.drugbug.R;
import net.innit.drugbug.data.DBDataSource;
import net.innit.drugbug.data.SettingsHelper;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;
import net.innit.drugbug.util.ImageStorage;

import java.util.List;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private int oldNumDoses;
    private ImageStorage imageStorage;
    private SettingsHelper settingsHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity().getApplicationContext();
        settingsHelper = new SettingsHelper(context);

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Set summary for number of untaken doses to keep
        oldNumDoses = sharedPreferences.getInt(SettingsHelper.KEY_NUM_DOSES, SettingsHelper.DEFAULT_NUM_DOSES);
        findPreference(SettingsHelper.KEY_NUM_DOSES).setSummary("Current: " + oldNumDoses);

        // Set summary for taken dose keep time
        findPreference(SettingsHelper.KEY_KEEP_TIME_TAKEN)
                .setSummary("Current: " +
                        SettingsHelper.convertString(sharedPreferences.getString(SettingsHelper.KEY_KEEP_TIME_TAKEN, SettingsHelper.convertString(SettingsHelper.DEFAULT_KEEP_TIME_TAKEN))));

        // Set summary for untaken dose keep time
        findPreference(SettingsHelper.KEY_KEEP_TIME_MISSED)
                .setSummary("Current: " +
                        SettingsHelper.convertString(sharedPreferences.getString(SettingsHelper.KEY_KEEP_TIME_MISSED, SettingsHelper.convertString(SettingsHelper.DEFAULT_KEEP_TIME_MISSED))));

        // Set summary for image storage location
        imageStorage = new ImageStorage(context);
        if (!imageStorage.LOCATION_EXTERNAL.canWrite()) {
            // Hide external choice if SD card is not available
            ListPreference listPreference = (ListPreference) findPreference(SettingsHelper.KEY_IMAGE_STORAGE);
            listPreference.setEntries(new String[]{"Internal"});
            listPreference.setEntryValues(new String[]{"2"});

        }
        findPreference(SettingsHelper.KEY_IMAGE_STORAGE).setSummary("Current: " + imageStorage.getDisplayText());
        // if external storage

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case SettingsHelper.KEY_NUM_DOSES:
                int maxNumDoses = sharedPreferences.getInt(key, SettingsHelper.DEFAULT_NUM_DOSES);
                findPreference(key).setSummary("Current: " + maxNumDoses);
                settingsHelper.numDosesChanged(maxNumDoses, oldNumDoses);
                break;
            case SettingsHelper.KEY_KEEP_TIME_TAKEN:
                String keepTimeString = sharedPreferences.getString(SettingsHelper.KEY_KEEP_TIME_TAKEN, SettingsHelper.convertString(SettingsHelper.DEFAULT_KEEP_TIME_TAKEN));
                findPreference(key).setSummary("Current: " + SettingsHelper.convertString(keepTimeString));
                settingsHelper.keepTimeTakenChanged(keepTimeString);
                break;
            case SettingsHelper.KEY_KEEP_TIME_MISSED:
                keepTimeString = sharedPreferences.getString(SettingsHelper.KEY_KEEP_TIME_MISSED, SettingsHelper.convertString(SettingsHelper.DEFAULT_KEEP_TIME_MISSED));
                findPreference(key).setSummary("Current: " + SettingsHelper.convertString(keepTimeString));
                settingsHelper.keepTimeMissedChanged(keepTimeString);
                break;
            case SettingsHelper.KEY_IMAGE_STORAGE:
                imageStorage.setLocationType(sharedPreferences.getString(SettingsHelper.KEY_IMAGE_STORAGE, SettingsHelper.DEFAULT_IMAGE_STORAGE));
                findPreference(key).setSummary("Current: " + imageStorage.getDisplayText());
                settingsHelper.imageStorageChanged(imageStorage);
                break;

        }

    }

}
