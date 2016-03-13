package net.innit.drugbug.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import net.innit.drugbug.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

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
            Log.d(MainActivity.LOGTAG, "copyAllFiles: " + source + " is a directory");
            if (!target.exists()) {
                if (target.mkdir()) {
                    Log.d(MainActivity.LOGTAG, "copyAllFiles: " + target + " was created");
                } else {
                    Log.d(MainActivity.LOGTAG, "copyAllFiles: " + target + " was not created");
                }
            }

            String[] children = source.list();
            Log.d(MainActivity.LOGTAG, "copyAllFiles: files: " + Arrays.toString(children));
            for (int i = 0; i < source.listFiles().length; i++) {
                copyAllFiles(new File(source, children[i]), new File(target, children[i]));
            }
        } else {
            Log.d(MainActivity.LOGTAG, "copyAllFiles: " + source + " is a file");
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
            Log.d(MainActivity.LOGTAG, "prepareDirectory: Directory " + this.getAbsDir() + " (and any missing parents) have been created.");
        } else {
            // empty it if location has changed
            if (oldStorage != null) {
                for (File file : absDir.listFiles()) //noinspection ResultOfMethodCallIgnored
                    file.delete();
                Log.d(MainActivity.LOGTAG, "prepareDirectory: Directory " + this.getAbsDir() + " has been emptied.");
            }
        }
    }

    public void revertToDefault(Context context, String key, String defaultLocation) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, defaultLocation);
        editor.apply();
        Log.d(MainActivity.LOGTAG, "revertToDefault: Storage location " + key + " reset to default: " + defaultLocation);
    }

    boolean moveFiles(Storage oldStorage) {
        if (oldStorage.getAbsDir().canRead()) {
            // move files from old to new
            try {
                copyAllFiles(oldStorage.getAbsDir(), absDir);
                Log.d(MainActivity.LOGTAG, "moveFiles: Files copied from " + oldStorage.getDisplayText() + " to " + this.getDisplayText());
                return true;
            } catch (IOException e) {
                Log.d(MainActivity.LOGTAG, "moveFiles: Error copying files from " + oldStorage.getDisplayText() + " to " + this.getDisplayText());
            }

        } else {
            Log.d(MainActivity.LOGTAG, "moveFiles: " + oldStorage.getDisplayText() + " directory not readable");
        }
        return false;
    }

    protected abstract Uri getStorageUri(File file);
}
