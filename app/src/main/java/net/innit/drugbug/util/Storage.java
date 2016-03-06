package net.innit.drugbug.util;

import android.content.Context;
import android.content.SharedPreferences;
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

public abstract class Storage {
    private File rootDir;           // Root directory for the storage type
    private File absDir;            // Full directory - rootDir + subDir
    private String displayText;
    private String type;

    protected Storage(File rootDir, String subDir, String displayText, String type) {
        this.rootDir = rootDir;
        this.displayText = displayText;
        this.type = type;
        this.absDir = new File(rootDir, subDir);
    }

    public static void copyAllFiles(File source, File target) throws IOException {
        if (source.isDirectory()) {
            Log.d(MainActivity.LOGTAG, "copyAllFiles: " + source + " is a directory");
            if (!target.exists()) {
                target.mkdir();
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

    public abstract void setStorageLocation(Storage oldStorage);

    public abstract boolean isAvailable();

    public void prepareDirectory() {
        if (!this.getAbsDir().exists()) {
            boolean created = absDir.mkdirs();
            Log.d(MainActivity.LOGTAG, "prepareDirectory: Directory " + this.getAbsDir() + " (and any missing parents) have been created.");
        } else {
            // empty it
            for (File file : absDir.listFiles()) file.delete();
            Log.d(MainActivity.LOGTAG, "prepareDirectory: Directory " + this.getAbsDir() + " has been emptied.");
        }
    }

    public void revertToDefault(Context context, String key, String defaultLocation) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, defaultLocation);
        editor.apply();
        Log.d(MainActivity.LOGTAG, "revertToDefault: Storage location " + key + " reset to default: " + defaultLocation);
    }

    public boolean moveFiles(Storage oldStorage) {
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
}
