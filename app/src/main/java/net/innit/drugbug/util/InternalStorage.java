package net.innit.drugbug.util;

import android.content.Context;

public class InternalStorage extends Storage {
    private static InternalStorage instance;

    private InternalStorage(Context context, String subDir) {
        super(context.getFilesDir(), subDir, "Internal", "INTERNAL");
    }

    public static InternalStorage getInstance(Context context, String subDir) {
        if (instance == null) {
            instance = new InternalStorage(context, subDir);
        }
        return instance;
    }

    public void setStorageLocation(Storage oldStorage) {
        // if external directory doesnt exist
        this.prepareDirectory();

        // Move files from old storage location to here
        if (oldStorage != null) {
            boolean success = this.moveFiles(oldStorage);
        }
    }

    @Override
    public boolean isAvailable() {
        // Internal storage is always available
        return true;
    }
}
