package net.innit.drugbug.util;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

public class InternalStorage extends Storage {
    private static InternalStorage instance;

    private static final String MED_IMAGE_FILE_PROVIDER = "net.innit.drugbug.med_image.fileprovider";
    private final Context context;

    private InternalStorage(Context context, String subDir) {
        super(context.getFilesDir(), subDir, "Internal", "INTERNAL");
        this.context = context;
    }

    public static InternalStorage getInstance(Context context, String subDir) {
        if (instance == null) {
            instance = new InternalStorage(context, subDir);
        }
        return instance;
    }

    protected void setStorageLocation(Storage oldStorage) {
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

    protected Uri getStorageUri(File file) {
        return FileProvider.getUriForFile(context, MED_IMAGE_FILE_PROVIDER, file);
    }
}
