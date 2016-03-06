package net.innit.drugbug.util;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;

public class ExternalStorage extends Storage {
    private static ExternalStorage instance;

    private final Context context;

    private ExternalStorage(Context context, String subDir) {
        super(new File(Environment.getExternalStorageDirectory(), context.getPackageName()), subDir, "SD Card", "EXTERNAL");
        this.context = context;
    }

    public static ExternalStorage getInstance(Context context, String subDir) {
        if (instance == null) {
            instance = new ExternalStorage(context, subDir);
        }
        return instance;
    }

    public void setStorageLocation(Storage oldStorage) {
        // if sd card is read/write
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // if external directory doesnt exist
            this.prepareDirectory();

            // Move files from old storage location to here
            if (oldStorage != null) {
                boolean success = this.moveFiles(oldStorage);
            }

        } else {
            Toast.makeText(context, "SD card is not available", Toast.LENGTH_SHORT).show();
            //revertToDefault(context);
        }
    }

    @Override
    public boolean isAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
