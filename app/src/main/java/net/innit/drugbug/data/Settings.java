package net.innit.drugbug.data;

import android.content.Context;
import android.util.ArrayMap;

import net.innit.drugbug.util.ExternalStorage;
import net.innit.drugbug.util.ImageStorage;

import java.util.Map;

public enum Settings {
    NUM_DOSES("NumFutureDoses", "5"),
    KEEP_TIME_TAKEN("KeepTimeTaken", "1:0:0"),
    KEEP_TIME_MISSED("KeepTimeMissed", "0:1:0"),
    IMAGE_STORAGE("StorageLoc", "EXTERNAL");

    private final String key;
    private String defaultValue;

    private static final Map<String, Settings> keyToEnumMap = new ArrayMap<>();
    static {
        keyToEnumMap.put("NumFutureDoses", NUM_DOSES);
        keyToEnumMap.put("KeepTimeTaken", KEEP_TIME_TAKEN);
        keyToEnumMap.put("KeepTimeMissed", KEEP_TIME_MISSED);
        keyToEnumMap.put("StorageLoc", IMAGE_STORAGE);
    }

    public static Settings keyToEnum(String key) {
        return keyToEnumMap.get(key);
    }

    Settings(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefault(Context context) {
        if (this.getKey().equals("StorageLoc")) {
            if (ExternalStorage.getInstance(context, ImageStorage.IMAGE_DIR).isAvailable()) {
                defaultValue = "EXTERNAL";
            } else {
                defaultValue = "INTERNAL";
            }
        }
        return defaultValue;
    }
}
