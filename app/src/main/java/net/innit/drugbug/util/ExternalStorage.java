package net.innit.drugbug.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import net.innit.drugbug.data.SettingsHelper;

import java.io.File;

/**
 * Created by alissa on 3/2/16.
 */
public class ExternalStorage {
    private final Context context;

    private static ExternalStorage instance;

    private String displayText = "SD Card";     // Text for display
    private File rootDir;           // Root directory for the storage type
    private File absDir;            // Full directory - rootDir + IMAGE_DIR
    private boolean active;
    private InternalStorage internalStorage;

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

    public String getDisplayText() {
        return displayText;
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

    protected void setInactive() {
        active = false;
    }

    public void setExternal() {
//        rootDir = LOCATION_EXTERNAL;
//        File externalDir = new File(rootDir, IMAGE_DIR);
//
//        // if sd card is read/write
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            // if external directory doesnt exist
//            if (!externalDir.exists()) {
//                boolean created = externalDir.mkdirs();
//            } else {
//                // empty it
//                for (File file : externalDir.listFiles()) file.delete();
//            }
//
//            // if locationType == INTERNAL
//            if (locationType.equals("INTERNAL")) {
//                // move files from internal to external
//                // if internal absDir exists
//                if (absDir.exists()) {
//                    // rename internal full absDir to external full absDir
//                    boolean moved = absDir.renameTo(externalDir);
//                    // if successful
//                    if (moved) {
//                        Toast.makeText(context, "Files moved from external to internal", Toast.LENGTH_SHORT).show();
//                    }
//                } // else do nothing
//            } // else it's already external so we'll change nothing
//            this.rootDir = rootDir;
//            this.absDir = externalDir;
//            this.locationType = "EXTERNAL";
//            this.displayText = "SD Card";
//        } else {
//            Toast.makeText(context, "SD card is not available", Toast.LENGTH_SHORT).show();
//            // change sharedPref back to internal
//            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putString(SettingsHelper.KEY_IMAGE_STORAGE, "INTERNAL");
//            editor.apply();
//
//        }

    }

}
