package net.innit.drugbug.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import net.innit.drugbug.util.ExternalStorage;
import net.innit.drugbug.util.ImageStorage;

import static net.innit.drugbug.util.Constants.DEFAULT_IMAGE_STORAGE_EXTERNAL;
import static net.innit.drugbug.util.Constants.DEFAULT_IMAGE_STORAGE_INTERNAL;
import static net.innit.drugbug.util.Constants.DEFAULT_KEEP_TIME_MISSED;
import static net.innit.drugbug.util.Constants.DEFAULT_KEEP_TIME_TAKEN;
import static net.innit.drugbug.util.Constants.DEFAULT_NUM_DOSES;

public class Settings {
    private static Settings sSharedPrefs;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private boolean mBulkUpdate = false;
    private Context context;

    private Settings(Context context) {
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context.getApplicationContext();
    }

    public static Settings getInstance(Context context) {
        if (sSharedPrefs == null) {
            sSharedPrefs = new Settings(context);
        }
        return sSharedPrefs;
    }

    public static Settings getInstance() {
        if (sSharedPrefs != null) {
            return sSharedPrefs;
        }

        throw new IllegalArgumentException("Should use getInstance(Context) at least once before using this method.");
    }

    public void put(Key key) {
        put(key, key.getDefault(context));
    }

    public void put(Key key, String val) {
        doEdit();
        mEditor.putString(key.name(), val);
        doApply();
    }

    public void put(Key key, int val) {
        doEdit();
        mEditor.putInt(key.name(), val);
        doApply();
    }

//    public void put(Key key, boolean val) {
//        doEdit();
//        mEditor.putBoolean(key.name(), val);
//        doCommit();
//    }
//
//    public void put(Key key, float val) {
//        doEdit();
//        mEditor.putFloat(key.name(), val);
//        doCommit();
//    }
//
//    /**
//     * Convenience method for storing doubles.
//     * <p/>
//     * There may be instances where the accuracy of a double is desired.
//     * SharedPreferences does not handle doubles so they have to
//     * cast to and from String.
//     *
//     * @param key The enum of the preference to store.
//     * @param val The new value for the preference.
//     */
//    public void put(Key key, double val) {
//        doEdit();
//        mEditor.putString(key.name(), String.valueOf(val));
//        doCommit();
//    }
//
//    public void put(Key key, long val) {
//        doEdit();
//        mEditor.putLong(key.name(), val);
//        doCommit();
//    }

    public String getString(Key key, String defaultValue) {
        return mPref.getString(key.name(), defaultValue);
    }

    public String getString(Key key) {
        return mPref.getString(key.name(), key.getDefault(context));
    }

    public int getInt(Key key) {
        return mPref.getInt(key.name(), Integer.parseInt(key.getDefault()));
    }

    public int getInt(Key key, int defaultValue) {
        return mPref.getInt(key.name(), defaultValue);
    }

//    public long getLong(Key key) {
//        return mPref.getLong(key.name(), 0);
//    }
//
//    public long getLong(Key key, long defaultValue) {
//        return mPref.getLong(key.name(), defaultValue);
//    }
//
//    public float getFloat(Key key) {
//        return mPref.getFloat(key.name(), 0);
//    }
//
//    public float getFloat(Key key, float defaultValue) {
//        return mPref.getFloat(key.name(), defaultValue);
//    }
//
//    /**
//     * Convenience method for retrieving doubles.
//     * <p/>
//     * There may be instances where the accuracy of a double is desired.
//     * SharedPreferences does not handle doubles so they have to
//     * cast to and from String.
//     *
//     * @param key The enum of the preference to fetch.
//     */
//    public double getDouble(Key key) {
//        return getDouble(key, 0);
//    }
//
//    /**
//     * Convenience method for retrieving doubles.
//     * <p/>
//     * There may be instances where the accuracy of a double is desired.
//     * SharedPreferences does not handle doubles so they have to
//     * cast to and from String.
//     *
//     * @param key The enum of the preference to fetch.
//     */
//    public double getDouble(Key key, double defaultValue) {
//        try {
//            return Double.valueOf(mPref.getString(key.name(), String.valueOf(defaultValue)));
//        } catch (NumberFormatException nfe) {
//            return defaultValue;
//        }
//    }
//
//    public boolean getBoolean(Key key, boolean defaultValue) {
//        return mPref.getBoolean(key.name(), defaultValue);
//    }
//
//    public boolean getBoolean(Key key) {
//        return mPref.getBoolean(key.name(), false);
//    }

    /**
     * Remove keys from SharedPreferences.
     *
     * @param keys The enum of the key(s) to be removed.
     */
    public void remove(Key... keys) {
        doEdit();
        for (Key key : keys) {
            mEditor.remove(key.name());
        }
        doApply();
    }

    /**
     * Remove all keys from SharedPreferences.
     */
    public void clear() {
        doEdit();
        mEditor.clear();
        doApply();
    }

    public void edit() {
        mBulkUpdate = true;
        mEditor = mPref.edit();
    }

    public void apply() {
        mBulkUpdate = false;
        mEditor.apply();
        mEditor = null;
    }

    private void doEdit() {
        if (!mBulkUpdate && mEditor == null) {
            mEditor = mPref.edit();
        }
    }

    private void doApply() {
        if (!mBulkUpdate && mEditor != null) {
            mEditor.apply();
            mEditor = null;
        }
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mPref.registerOnSharedPreferenceChangeListener(listener);
    }

    public boolean contains(Settings.Key key) {
        return mPref.contains(key.name());
    }

    /**
     * Enum representing your setting names or key for your setting.
     */
    public enum Key {
        NUM_DOSES(DEFAULT_NUM_DOSES),
        KEEP_TIME_TAKEN(DEFAULT_KEEP_TIME_TAKEN),
        KEEP_TIME_MISSED(DEFAULT_KEEP_TIME_MISSED),
        IMAGE_STORAGE(null);

        private String defaultValue;

        Key (String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDefault(Context context) {
            if (this == IMAGE_STORAGE) {
                if (ExternalStorage.getInstance(context, ImageStorage.IMAGE_DIR).isAvailable()) {
                    defaultValue = DEFAULT_IMAGE_STORAGE_EXTERNAL;
                } else {
                    defaultValue = DEFAULT_IMAGE_STORAGE_INTERNAL;
                }
            }
            return defaultValue;
        }

        public String getDefault() {
            if (!(this == IMAGE_STORAGE)) {
                return defaultValue;
            }

            throw new IllegalArgumentException("SettingsEnum.IMAGE_STORAGE.getDefault() must be called with Context parameter");
        }
    }

}