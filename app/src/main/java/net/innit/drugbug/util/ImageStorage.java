package net.innit.drugbug.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.ArrayMap;

import net.innit.drugbug.data.SettingsHelper;

import java.io.File;
import java.util.Map;

/**
 * Object for manipulating the app image storage
 */
public class ImageStorage {
    public static final String IMAGE_DIR = "images/medications";
    private static ImageStorage instance;
    private String locationType;    // File storage location type - INTERNAL or EXTERNAL
    private Map<String, Storage> locations = new ArrayMap<>();

    /**
     * @param context Context for this object
     */
    private ImageStorage(Context context) {
        locations.put("INTERNAL", InternalStorage.getInstance(context, IMAGE_DIR));
        locations.put("EXTERNAL", ExternalStorage.getInstance(context, IMAGE_DIR));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.locationType = sharedPreferences.getString(SettingsHelper.KEY_IMAGE_STORAGE, new SettingsHelper(context).DEFAULT_IMAGE_STORAGE);

        setLocationType(locationType);
    }

    public static ImageStorage getInstance(Context context) {
        if (instance == null) {
            instance = new ImageStorage(context);
        }
        return instance;
    }

    /**
     * @return Text to display
     */
    public String getDisplayText() {
        return locations.get(locationType).getDisplayText();
    }

    /**
     * @return Current image location root directory (not utilized - future: there may be need for another category of images)
     */
    public File getRootDir() {
        return locations.get(locationType).getRootDir();
    }

    /**
     * @return Current image location absolute directory
     */
    public File getAbsDir() {
        return locations.get(locationType).getAbsDir();
    }

    /**
     * @return Location type string for this object
     */
    public String getLocationType() {
        return locationType;
    }

    /**
     * @param type Location type string
     */
    public void setLocationType(String type) {
        String oldType = locationType;

        if (!oldType.equals(type)) {
            // location has changed, so need to do stuff
            locations.get(type).setStorageLocation(locations.get(oldType));
        } else {
            locations.get(type).setStorageLocation(null);
        }

        locationType = type;
    }

    public Map<Integer, String> getAllLocations() {
        Map<Integer, String> returnMap = new ArrayMap<>();
        int key = 0;
        for (Storage location : locations.values()) {
            if (location.isAvailable()) returnMap.put(++key, location.getDisplayText());
        }
        return returnMap;
    }

    public Map<String, String> getAvailableLocations() {
        Map<String, String> returnMap = new ArrayMap<>();
        int key = 0;
        for (Storage location : locations.values()) {
            if (location.isAvailable()) returnMap.put("" + ++key, location.getDisplayText());
        }
        return returnMap;
    }

}
