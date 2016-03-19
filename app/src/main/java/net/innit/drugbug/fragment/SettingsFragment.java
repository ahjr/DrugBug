package net.innit.drugbug.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import net.innit.drugbug.R;
import net.innit.drugbug.data.Settings;
import net.innit.drugbug.data.SettingsHelper;
import net.innit.drugbug.util.ImageStorage;

import java.util.Collection;
import java.util.Map;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private int oldNumDoses;                // Holder field for initial number of doses to keep
    private ImageStorage imageStorage;      // Image storage object
    private SettingsHelper settingsHelper;  // Settings constants and methods
    private SharedPreferences sharedPreferences;
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getActivity().getApplicationContext();
        settingsHelper = new SettingsHelper(context);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        oldNumDoses = sharedPreferences.getInt(Settings.NUM_DOSES.getKey(), Integer.parseInt(Settings.NUM_DOSES.getDefault()));

        imageStorage = ImageStorage.getInstance(context);

        ListPreference listPreference = (ListPreference) findPreference(Settings.IMAGE_STORAGE.getKey());
        Map<String, String> locations = imageStorage.getAvailableLocations();
        Collection<String> values = locations.values();
        listPreference.setEntries(values.toArray(new String[values.size()]));
        values = locations.keySet();
        listPreference.setEntryValues(values.toArray(new String[values.size()]));

        imageStorage.setLocationType(sharedPreferences.getString(Settings.IMAGE_STORAGE.getKey(), Settings.IMAGE_STORAGE.getDefault(context)));

        setSummaries(sharedPreferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Settings changedSetting = Settings.valueOf(key);
        switch (changedSetting) {
            case NUM_DOSES:
                settingsHelper.numDosesChanged(sharedPreferences.getInt(key, Integer.parseInt(Settings.NUM_DOSES.getDefault())), oldNumDoses);
                break;
            case KEEP_TIME_TAKEN:
                settingsHelper.keepTimeTakenChanged(sharedPreferences.getString(key, Settings.KEEP_TIME_TAKEN.getDefault()));
                break;
            case KEEP_TIME_MISSED:
                settingsHelper.keepTimeMissedChanged(sharedPreferences.getString(key, Settings.KEEP_TIME_MISSED.getDefault()));
                break;
            case IMAGE_STORAGE:
                imageStorage.setLocationType(sharedPreferences.getString(key, Settings.IMAGE_STORAGE.getDefault(context)));
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
        findPreference(Settings.NUM_DOSES.getKey())
                .setSummary("Current: " +
                        sharedPreferences.getInt(Settings.NUM_DOSES.getKey(),
                                Integer.parseInt(Settings.NUM_DOSES.getDefault())));

        // Set summary for taken dose keep time
        findPreference(Settings.KEEP_TIME_TAKEN.getKey())
                .setSummary("Current: " +
                        SettingsHelper.convertString(sharedPreferences.getString(Settings.KEEP_TIME_TAKEN.getKey(),
                                Settings.KEEP_TIME_TAKEN.getDefault())));

        // Set summary for untaken dose keep time
        findPreference(Settings.KEEP_TIME_MISSED.getKey())
                .setSummary("Current: " +
                        SettingsHelper.convertString(sharedPreferences.getString(Settings.KEEP_TIME_MISSED.getKey(),
                                Settings.KEEP_TIME_MISSED.getDefault())));

        // Set summary for image storage location
        findPreference(Settings.IMAGE_STORAGE.getKey())
                .setSummary("Current: " +
                        imageStorage.getDisplayText());
    }
}
