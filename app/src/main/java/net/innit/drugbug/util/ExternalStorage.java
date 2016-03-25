package net.innit.drugbug.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import net.innit.drugbug.R;

import java.io.File;

public class ExternalStorage extends Storage {
    public static final String TYPE = "EXTERNAL";

    private static ExternalStorage instance;

    private final Context context;

    private ExternalStorage(Context context, String subDir) {
        super(new File(Environment.getExternalStorageDirectory(), context.getPackageName()), subDir, context.getString(R.string.storage_external_display), TYPE);
        this.context = context;
    }

    protected static ExternalStorage getInstance(Context context, String subDir) {
        if (instance == null) {
            instance = new ExternalStorage(context, subDir);
        }
        return instance;
    }

    protected void setStorageLocation(Storage oldStorage) {
        // if sd card is read/write
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                // if external directory doesnt exist
                this.prepareDirectory(oldStorage);
                if (oldStorage != null) {
                    boolean success = this.moveFiles(oldStorage);
                }
            } else {
                Toast.makeText(context, R.string.storage_external_not_available, Toast.LENGTH_SHORT).show();
                //revertToDefault(context);
            }
    }

    @Override
    public boolean isAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    protected Uri getStorageUri(File file) {
        return Uri.fromFile(file);
    }
}
