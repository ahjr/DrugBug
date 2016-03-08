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
import net.innit.drugbug.data.Settings;
import net.innit.drugbug.data.SettingsHelper;
import net.innit.drugbug.util.ImageStorage;

import java.util.Arrays;
import java.util.Map;

import static net.innit.drugbug.data.Settings.*;

// todo set default values on first run - probably in a method?

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

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

//        oldNumDoses = sharedPreferences.getInt(SettingsHelper.KEY_NUM_DOSES, Integer.parseInt(SettingsHelper.DEFAULT_NUM_DOSES));
        oldNumDoses = sharedPreferences.getInt(Settings.NUM_DOSES.getKey(), Integer.parseInt(Settings.NUM_DOSES.getDefault(context)));

        imageStorage = ImageStorage.getInstance(context);

        ListPreference listPreference = (ListPreference) findPreference(Settings.IMAGE_STORAGE.getKey());
        Map<String, String> locations = imageStorage.getAvailableLocations();
        listPreference.setEntries(locations.values().toArray(new String[0]));
        Log.d(MainActivity.LOGTAG, "SettingsFragment: Entries -> " + Arrays.toString(locations.values().toArray(new String[0])));
        listPreference.setEntryValues(locations.keySet().toArray(new String[0]));
        Log.d(MainActivity.LOGTAG, "SettingsFragment: Values -> " + Arrays.toString(locations.keySet().toArray(new String[0])));

        if (listPreference.getValue() == null) {
//            listPreference.setValue(settingsHelper.DEFAULT_IMAGE_STORAGE);
            listPreference.setValue(Settings.IMAGE_STORAGE.getDefault(context));
        }

//        imageStorage.setLocationType(sharedPreferences.getString(SettingsHelper.KEY_IMAGE_STORAGE, settingsHelper.DEFAULT_IMAGE_STORAGE));
        imageStorage.setLocationType(sharedPreferences.getString(Settings.IMAGE_STORAGE.getKey(), Settings.IMAGE_STORAGE.getDefault(context)));

    }

    @Override
    public void onResume() {
        super.onResume();
        setSummaries(sharedPreferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Settings changedSetting = Settings.keyToEnum(key);
//        switch (key) {
        switch (changedSetting) {
//            case SettingsHelper.KEY_NUM_DOSES:
            case NUM_DOSES:
                settingsHelper.numDosesChanged(sharedPreferences.getInt(key, Integer.parseInt(Settings.NUM_DOSES.getDefault(context))), oldNumDoses);
                break;
//            case SettingsHelper.KEY_KEEP_TIME_TAKEN:
            case KEEP_TIME_TAKEN:
                settingsHelper.keepTimeTakenChanged(sharedPreferences.getString(key, Settings.KEEP_TIME_TAKEN.getDefault(context)));
                break;
//            case SettingsHelper.KEY_KEEP_TIME_MISSED:
            case KEEP_TIME_MISSED:
                settingsHelper.keepTimeMissedChanged(sharedPreferences.getString(key, Settings.KEEP_TIME_MISSED.getDefault(context)));
                break;
//            case SettingsHelper.KEY_IMAGE_STORAGE:
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
//        findPreference(SettingsHelper.KEY_NUM_DOSES)
//                .setSummary("Current: " +
//                        sharedPreferences.getInt(SettingsHelper.KEY_NUM_DOSES, Integer.parseInt(SettingsHelper.DEFAULT_NUM_DOSES)));
        findPreference(Settings.NUM_DOSES.getKey())
                .setSummary("Current: " +
                        sharedPreferences.getInt(Settings.NUM_DOSES.getKey(), Integer.parseInt(Settings.NUM_DOSES.getDefault(context))));

        // Set summary for taken dose keep time
//        findPreference(SettingsHelper.KEY_KEEP_TIME_TAKEN)
//                .setSummary("Current: " +
//                        SettingsHelper.convertString(sharedPreferences.getString(SettingsHelper.KEY_KEEP_TIME_TAKEN, SettingsHelper.DEFAULT_KEEP_TIME_TAKEN)));
        findPreference(Settings.KEEP_TIME_TAKEN.getKey())
                .setSummary("Current: " +
                        SettingsHelper.convertString(sharedPreferences.getString(Settings.KEEP_TIME_TAKEN.getKey(), Settings.KEEP_TIME_TAKEN.getDefault(context))));

        // Set summary for untaken dose keep time
//        findPreference(SettingsHelper.KEY_KEEP_TIME_MISSED)
//                .setSummary("Current: " +
//                        SettingsHelper.convertString(sharedPreferences.getString(SettingsHelper.KEY_KEEP_TIME_MISSED, SettingsHelper.DEFAULT_KEEP_TIME_MISSED)));
        findPreference(Settings.KEEP_TIME_MISSED.getKey())
                .setSummary("Current: " +
                        SettingsHelper.convertString(sharedPreferences.getString(Settings.KEEP_TIME_MISSED.getKey(), Settings.KEEP_TIME_MISSED.getDefault(context))));

        // Set summary for image storage location
//        findPreference(SettingsHelper.KEY_IMAGE_STORAGE).setSummary("Current: " + imageStorage.getDisplayText());
        findPreference(Settings.IMAGE_STORAGE.getKey()).setSummary("Current: " + imageStorage.getDisplayText());

    }

}
