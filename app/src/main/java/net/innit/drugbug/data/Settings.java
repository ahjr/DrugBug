package net.innit.drugbug.data;

import android.content.Context;

import net.innit.drugbug.util.ExternalStorage;
import net.innit.drugbug.util.ImageStorage;

import static net.innit.drugbug.util.Constants.DEFAULT_IMAGE_STORAGE_EXTERNAL;
import static net.innit.drugbug.util.Constants.DEFAULT_IMAGE_STORAGE_INTERNAL;
import static net.innit.drugbug.util.Constants.DEFAULT_KEEP_TIME_MISSED;
import static net.innit.drugbug.util.Constants.DEFAULT_KEEP_TIME_TAKEN;
import static net.innit.drugbug.util.Constants.DEFAULT_NUM_DOSES;

public enum Settings {
    NUM_DOSES(DEFAULT_NUM_DOSES),
    KEEP_TIME_TAKEN(DEFAULT_KEEP_TIME_TAKEN),
    KEEP_TIME_MISSED(DEFAULT_KEEP_TIME_MISSED),
    IMAGE_STORAGE(null);

    private final String key;
    private String defaultValue;

    Settings(String defaultValue) {
        this.key = this.name();
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefault(Context context) {
        if (defaultValue == null && this == IMAGE_STORAGE) {
            if (ExternalStorage.getInstance(context, ImageStorage.IMAGE_DIR).isAvailable()) {
                defaultValue = DEFAULT_IMAGE_STORAGE_EXTERNAL;
            } else {
                defaultValue = DEFAULT_IMAGE_STORAGE_INTERNAL;
            }
        }
        return defaultValue;
    }

    public String getDefault() {
        if (this == IMAGE_STORAGE) {
            throw new IllegalArgumentException("Settings.IMAGE_STORAGE.getDefault must be called with Context parameter");
        } else {
            return defaultValue;
        }
    }
}
