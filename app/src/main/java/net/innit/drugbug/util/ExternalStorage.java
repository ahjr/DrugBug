package net.innit.drugbug.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import net.innit.drugbug.data.SettingsHelper;

import java.io.File;

public class ExternalStorage implements Storage {
    public final static String DISPLAY_TEXT = "SD Card";     // Text for display

    private static ExternalStorage instance;
    private InternalStorage internalStorage;

    private final Context context;
    private final File rootDir;           // Root directory for the storage type
    private final File absDir;            // Full directory - rootDir + IMAGE_DIR
    private boolean active;

    private ExternalStorage(Context context, String subDir) {
        this.context = context;
        this.rootDir = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
        this.absDir = new File(rootDir, subDir);
        this.internalStorage = InternalStorage.getInstance(context, subDir);
    }

    public static ExternalStorage getInstance(Context context, String subDir) {
        if (instance == null) {
            instance = new ExternalStorage(context, subDir);
        }
        return instance;
    }

    public File getRootDir() {
        return rootDir;
    }

    public File getAbsDir() {
        return absDir;
    }

    public boolean isActive() {
        return active;
    }

    public synchronized void setActive() {
        internalStorage.setInactive();
        active = true;
    }

    public void setInactive() {
        active = false;
    }

    public void setStorageLocation () {
        // if sd card is read/write
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // if external directory doesnt exist
            if (!absDir.exists()) {
                boolean created = absDir.mkdirs();
            } else {
                // empty it
                for (File file : absDir.listFiles()) file.delete();
            }

            // if locationType == INTERNAL
            if (internalStorage.isActive() && internalStorage.getAbsDir().canWrite()) {
                // move files from internal to external
                boolean moved = internalStorage.getAbsDir().renameTo(absDir);
                // if successful
                if (moved) {
                    Toast.makeText(context, "Files moved from external to internal", Toast.LENGTH_SHORT).show();
                }
            } // else it's already external so we'll change nothing

        } else {
            Toast.makeText(context, "SD card is not available", Toast.LENGTH_SHORT).show();

            // change sharedPref back to internal
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SettingsHelper.KEY_IMAGE_STORAGE, "INTERNAL");
            editor.apply();

        }

        setActive();
    }

}
