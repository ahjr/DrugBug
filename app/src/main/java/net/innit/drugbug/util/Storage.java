package net.innit.drugbug.util;

import java.io.File;

/**
 * Created by alissa on 3/2/16.
 */
public interface Storage {
    File getRootDir();

    File getAbsDir();

    boolean isActive();

    void setActive();

    void setInactive();

    void setStorageLocation();

}
