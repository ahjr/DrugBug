package net.innit.drugbug.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import net.innit.drugbug.R;
import net.innit.drugbug.data.SettingsHelper;
import net.innit.drugbug.util.ImageStorage;

import java.util.Map;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private int oldNumDoses;                // Holder field for initial number of doses to keep
    private ImageStorage imageStorage;      // Image storage object
    private SettingsHelper settingsHelper;  // Settings constants and methods
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity().getApplicationContext();
        settingsHelper = new SettingsHelper(context);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        oldNumDoses = sharedPreferences.getInt(SettingsHelper.KEY_NUM_DOSES, SettingsHelper.DEFAULT_NUM_DOSES);

        imageStorage = ImageStorage.getInstance(context);

        ListPreference listPreference = (ListPreference) findPreference(SettingsHelper.KEY_IMAGE_STORAGE);
        Map<String, String> locations = imageStorage.getAvailableLocations();
        listPreference.setEntries(locations.values().toArray(new String[0]));
        listPreference.setEntryValues(locations.keySet().toArray(new String[0]));

        imageStorage.setLocationType(sharedPreferences.getString(SettingsHelper.KEY_IMAGE_STORAGE, settingsHelper.DEFAULT_IMAGE_STORAGE));

    }

    @Override
    public void onResume() {
        super.onResume();
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
                imageStorage.setLocationType(sharedPreferences.getString(key, settingsHelper.DEFAULT_IMAGE_STORAGE));
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
