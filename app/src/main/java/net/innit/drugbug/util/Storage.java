package net.innit.drugbug.util;

import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static net.innit.drugbug.data.Constants.LOG;

abstract class Storage {
    private final File rootDir;           // Root directory for the storage type
    private final File absDir;            // Full directory - rootDir + subDir
    private final String displayText;
    private final String type;

    Storage(File rootDir, String subDir, String displayText, String type) {
        this.rootDir = rootDir;
        this.displayText = displayText;
        this.type = type;
        this.absDir = new File(rootDir, subDir);
    }

    private static void copyAllFiles(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                boolean dirsCreated = target.mkdir();
            }

            String[] children = source.list();
            for (int i = 0; i < source.listFiles().length; i++) {
                copyAllFiles(new File(source, children[i]), new File(target, children[i]));
            }
        } else {
            InputStream in = new FileInputStream(source);
            OutputStream out = new FileOutputStream(target);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }

    public File getRootDir() {
        return rootDir;
    }

    public File getAbsDir() {
        return absDir;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getType() {
        return type;
    }

    protected abstract void setStorageLocation(Storage oldStorage);

    public abstract boolean isAvailable();

    void prepareDirectory(Storage oldStorage) {
        if (!this.getAbsDir().exists()) {
            boolean created = absDir.mkdirs();
        } else {
            // empty it if location has changed
            if (oldStorage != null) {
                for (File file : absDir.listFiles()) { //noinspection ResultOfMethodCallIgnored
                    final boolean deleted = file.delete();
                }
            }
        }
    }

    // Archived for future use
//    public void revertToDefault(Context context, String key, String defaultLocation) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(key, defaultLocation);
//        editor.apply();
//    }

    boolean moveFiles(Storage oldStorage) {
        if (oldStorage.getAbsDir().canRead()) {
            // move files from old to new
            try {
                copyAllFiles(oldStorage.getAbsDir(), absDir);
                return true;
            } catch (IOException e) {
                Log.e(LOG, "moveFiles: Error copying files from " + oldStorage.getDisplayText() + " to " + this.getDisplayText());
            }

        }
        return false;
    }

    protected abstract Uri getStorageUri(File file);
}
