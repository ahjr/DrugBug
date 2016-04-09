package net.innit.drugbug.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

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
    private Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsHelper = new SettingsHelper(getActivity());

        settings = Settings.getInstance(getActivity());
        settings.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        oldNumDoses = Integer.parseInt(settings.getString(Settings.Key.NUM_DOSES));

        imageStorage = ImageStorage.getInstance(getActivity());

        ListPreference listPreference = (ListPreference) findPreference(Settings.Key.IMAGE_STORAGE.name());
        Map<String, String> locations = imageStorage.getAvailableLocations();
        Collection<String> values = locations.values();
        listPreference.setEntries(values.toArray(new String[values.size()]));
        values = locations.keySet();
        listPreference.setEntryValues(values.toArray(new String[values.size()]));

        imageStorage.setLocationType(settings.getString(Settings.Key.IMAGE_STORAGE));

        setSummaries(settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String keyString) {
        if (isAdded()) {
            // Since we're only using the default shared preferences here, we'll ignore the SharedPreferences
            // parameter and just use the Settings instance we created in onCreate
            Settings.Key key = Settings.Key.valueOf(keyString);
            switch (key) {
                case NUM_DOSES:
                    settingsHelper.numDosesChanged(getActivity(), Integer.parseInt(settings.getString(key)), oldNumDoses);
                    break;
                case KEEP_TIME_TAKEN:
                    settingsHelper.keepTimeTakenChanged(settings.getString(key));
                    break;
                case KEEP_TIME_MISSED:
                    settingsHelper.keepTimeMissedChanged(settings.getString(key));
                    break;
                case IMAGE_STORAGE:
                    imageStorage.setLocationType(settings.getString(key));
                    settingsHelper.imageStorageChanged(imageStorage);
                    break;
                case TIME_WAKE:
                case TIME_BREAKFAST:
                case TIME_LUNCH:
                case TIME_DINNER:
                case TIME_BED:
                    settingsHelper.timeChanged(key, settings.getString(key));
                    break;
            }
            setSummaries(settings);
        }
    }

    /**
     *  Set the summaries for all preferences
     *
     * @param sharedPreferences Shared preferences object containing the preferences to set summaries for
     */
    private void setSummaries(Settings sharedPreferences) {
        for (Settings.Key key : Settings.Key.values()) {
            String currentString = "";
            switch (key) {
                case NUM_DOSES:
                    currentString = sharedPreferences.getString(Settings.Key.NUM_DOSES);
                    break;
                case IMAGE_STORAGE:
                    currentString = imageStorage.getDisplayText();
                    break;
                case KEEP_TIME_TAKEN:
                case KEEP_TIME_MISSED:
                    currentString = SettingsHelper.convertString(getActivity(), sharedPreferences.getString(key));
                    break;
                case TIME_WAKE:
                case TIME_BREAKFAST:
                case TIME_LUNCH:
                case TIME_DINNER:
                case TIME_BED:
                    currentString = SettingsHelper.convertTime(sharedPreferences.getString(key));
                    break;
            }

            findPreference(key.name()).setSummary(getString(R.string.preference_current) + currentString);
        }

    }
}
