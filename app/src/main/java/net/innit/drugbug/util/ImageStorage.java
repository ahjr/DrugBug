package net.innit.drugbug.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.ArrayMap;

import net.innit.drugbug.data.Settings;

import java.io.File;
import java.util.Map;

/**
 * Object for manipulating the app image storage
 */
public class ImageStorage {
    public static final String IMAGE_DIR = "images/medications";
    private static ImageStorage instance;
    private String locationType;    // File storage location type - INTERNAL or EXTERNAL
    private final Map<String, Storage> locations = new ArrayMap<>();

    /**
     * @param context Context for this object
     */
    private ImageStorage(Context context) {
        locations.put("INTERNAL", InternalStorage.getInstance(context, IMAGE_DIR));
        locations.put("EXTERNAL", ExternalStorage.getInstance(context, IMAGE_DIR));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.locationType = sharedPreferences.getString(Settings.IMAGE_STORAGE.getKey(), Settings.IMAGE_STORAGE.getDefault(context));

        setLocationType(locationType);
    }

    public static ImageStorage getInstance(Context context) {
        if (instance == null) {
            instance = new ImageStorage(context);
        }
        return instance;
    }

    public String getDefault() {
        if (locations.get("EXTERNAL").isAvailable()) {
            return "EXTERNAL";
        } else {
            return "INTERNAL";
        }
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

    public Map<String, String> getAllLocations() {
        Map<String, String> returnMap = new ArrayMap<>();
        for (Storage location : locations.values()) {
            if (location.isAvailable()) returnMap.put(location.getType(), location.getDisplayText());
        }
        return returnMap;
    }

    public Map<String, String> getAvailableLocations() {
        Map<String, String> returnMap = new ArrayMap<>();
        for (Storage location : locations.values()) {
            if (location.isAvailable()) returnMap.put(location.getType(), location.getDisplayText());
        }
        return returnMap;
    }

    public Uri getStorageUri(File file) {
        return locations.get(locationType).getStorageUri(file);
    }

}
