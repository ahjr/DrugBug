package net.innit.drugbug.util;

import android.content.Context;
import android.widget.Toast;

import java.io.File;

/**
 * Created by alissa on 3/2/16.
 */
public class InternalStorage {
    private final Context context;

    private static InternalStorage instance;

    private String displayText = "Internal";     // Text for display
    private File rootDir;           // Root directory for the storage type
    private File absDir;            // Full directory - rootDir + IMAGE_DIR
    private boolean active;
    private ExternalStorage externalStorage;

    private InternalStorage(Context context, String subDir) {
        this.context = context;
        this.rootDir = context.getFilesDir();
        this.absDir = new File(rootDir, subDir);
        this.externalStorage = ExternalStorage.getInstance(context, subDir);

        // Check SharedPreferences
    }

    public static InternalStorage getInstance(Context context, String subDir) {
        if (instance == null) {
            instance = new InternalStorage(context, subDir);
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

    protected synchronized void setActive() {
        externalStorage.setInactive();
        active = true;
    }

    protected void setInactive() {
        active = false;
    }

    public void setInternal() {
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

        // Make this atomic
        synchronized (this) {
            setActive();
            externalStorage.setInactive();
        }
    }
}
