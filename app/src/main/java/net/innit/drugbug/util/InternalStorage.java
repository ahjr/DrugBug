package net.innit.drugbug.util;

import android.content.Context;
import android.widget.Toast;

import java.io.File;

public class InternalStorage implements Storage {
    public static final String DISPLAY_TEXT = "Internal";

    private static InternalStorage instance;
    private ExternalStorage externalStorage;

    private final Context context;
    private File rootDir;           // Root directory for the storage type
    private File absDir;            // Full directory - rootDir + IMAGE_DIR
    private boolean active;

    private InternalStorage(Context context, String subDir) {
        this.context = context;
        this.rootDir = context.getFilesDir();
        this.absDir = new File(rootDir, subDir);
        this.externalStorage = ExternalStorage.getInstance(context, subDir);

        // Check SharedPreferences and set active if internal is selected
    }

    public static InternalStorage getInstance(Context context, String subDir) {
        if (instance == null) {
            instance = new InternalStorage(context, subDir);
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
        externalStorage.setInactive();
        active = true;
    }

    public void setInactive() {
        active = false;
    }

    public void setStorageLocation () {
        // if internal directory doesn't exist
        if (!absDir.exists()) {
            boolean created = absDir.mkdirs();
        } else {
            // empty it
            for (File file : absDir.listFiles()) file.delete();
        }

        if (externalStorage.isActive() && externalStorage.getAbsDir().canWrite()) {
            // move files from external to internal
                boolean moved = externalStorage.getAbsDir().renameTo(absDir);
                // if successful
                if (moved) {
                    Toast.makeText(context, "Files moved from external to internal", Toast.LENGTH_SHORT).show();
                }
        } // else do nothing

        setActive();
    }
}
