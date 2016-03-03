package net.innit.drugbug.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import net.innit.drugbug.R;
import net.innit.drugbug.data.SettingsHelper;
import net.innit.drugbug.util.ImageStorage;

import java.util.List;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private int oldNumDoses;                // Holder field for initial number of doses to keep
    private ImageStorage imageStorage;      // Image storage object
    private SettingsHelper settingsHelper;  // Settings constants and methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity().getApplicationContext();
        settingsHelper = new SettingsHelper(context);

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        oldNumDoses = sharedPreferences.getInt(SettingsHelper.KEY_NUM_DOSES, SettingsHelper.DEFAULT_NUM_DOSES);

        imageStorage = new ImageStorage(context);
        // Hide external choice if SD card is not available
        if (!imageStorage.LOCATION_EXTERNAL.canWrite()) {
            ListPreference listPreference = (ListPreference) findPreference(SettingsHelper.KEY_IMAGE_STORAGE);
            listPreference.setEntries(new String[]{"Internal"});
            listPreference.setEntryValues(new String[]{"2"});
        }

        setSummaries(sharedPreferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case SettingsHelper.KEY_NUM_DOSES:
                settingsHelper.numDosesChanged(sharedPreferences.getInt(key, SettingsHelper.DEFAULT_NUM_DOSES), oldNumDoses);
                break;
            case SettingsHelper.KEY_KEEP_TIME_TAKEN:
                settingsHelper.keepTimeTakenChanged(sharedPreferences.getString(key, SettingsHelper.DEFAULT_KEEP_TIME_TAKEN));
                break;
            case SettingsHelper.KEY_KEEP_TIME_MISSED:
                settingsHelper.keepTimeMissedChanged(sharedPreferences.getString(key, SettingsHelper.DEFAULT_KEEP_TIME_MISSED));
                break;
            case SettingsHelper.KEY_IMAGE_STORAGE:
                imageStorage.setLocationType(sharedPreferences.getString(key, SettingsHelper.DEFAULT_IMAGE_STORAGE));
                settingsHelper.imageStorageChanged(imageStorage);
                break;
        }
        setSummaries(sharedPreferences);
    }

    /**
     *  Set the summaries for all preferences
     *
     * @param sharedPreferences Shared preferences object containing the preferences to set summaries for
     */
    private void setSummaries(SharedPreferences sharedPreferences) {
        // Set summary for number of untaken doses to keep
        findPreference(SettingsHelper.KEY_NUM_DOSES)
                .setSummary("Current: " +
                        sharedPreferences.getInt(SettingsHelper.KEY_NUM_DOSES, SettingsHelper.DEFAULT_NUM_DOSES));

        // Set summary for taken dose keep time
        findPreference(SettingsHelper.KEY_KEEP_TIME_TAKEN)
                .setSummary("Current: " +
                        SettingsHelper.convertString(sharedPreferences.getString(SettingsHelper.KEY_KEEP_TIME_TAKEN, SettingsHelper.DEFAULT_KEEP_TIME_TAKEN)));

        // Set summary for untaken dose keep time
        findPreference(SettingsHelper.KEY_KEEP_TIME_MISSED)
                .setSummary("Current: " +
                        SettingsHelper.convertString(sharedPreferences.getString(SettingsHelper.KEY_KEEP_TIME_MISSED, SettingsHelper.DEFAULT_KEEP_TIME_MISSED)));

        // Set summary for image storage location
        findPreference(SettingsHelper.KEY_IMAGE_STORAGE).setSummary("Current: " + imageStorage.getDisplayText());

    }

}
