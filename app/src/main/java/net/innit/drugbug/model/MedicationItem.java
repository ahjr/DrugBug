package net.innit.drugbug.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.Toast;

import net.innit.drugbug.R;
import net.innit.drugbug.data.DatabaseDAO;
import net.innit.drugbug.util.BitmapHelper;
import net.innit.drugbug.util.ImageStorage;
import net.innit.drugbug.util.OnListUpdatedListener;

import java.io.File;
import java.util.Comparator;
import java.util.Date;

public class MedicationItem implements Comparable<MedicationItem> {
    private long id;
    private String name;    // Name of drug taken
    private String frequency; // Frequency of doses
    private String imagePath; // Picture of the pill or label
    private boolean active = true;
    private boolean archived;
    private Date archiveDate;

    public MedicationItem() {
    }

    // Unused constructors - keeping them here in case they're needed in the future
//    public MedicationItem(String name, String frequency) {
//        this.name = name;
//        this.frequency = frequency;
//    }
//
//    public MedicationItem(String name, String frequency, String imagePath) {
//        this.name = name;
//        this.frequency = frequency;
//        this.imagePath = imagePath;
//    }
//
//    public MedicationItem(long id, String name, String frequency, String imagePath) {
//        this.id = id;
//        this.name = name;
//        this.frequency = frequency;
//        this.imagePath = imagePath;
//    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        if (archived) {
            this.setArchiveDate(new Date());
        } else {
            this.setArchiveDate(null);
        }
        this.archived = archived;
    }

    public Date getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(Date archiveDate) {
        this.archiveDate = archiveDate;
    }

    public boolean hasTaken(Context context) {
        DatabaseDAO db = new DatabaseDAO(context);
        db.open();
        if (db.getLatestTakenDose(MedicationItem.this) != null) {
            db.close();
            return true;
        } else {
            db.close();
            return false;
        }
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean deleteImageFile(Context context) {
        return imagePath != null && new File(ImageStorage.getInstance(context).getAbsDir(), imagePath).delete();
    }

    public boolean hasImage() {
        return (imagePath != null);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns the image associated with this medication as a Bitmap
     *
     * @param context Context for this bitmap
     * @param width Width of the image
     * @param height Height of the image
     * @return the medication image as a bitmap
     */
    public Bitmap getBitmap(Context context, int width, int height) {
        ImageStorage imageStorage = ImageStorage.getInstance(context);
        String imageAbsPath = imageStorage.getAbsDir() + "/" + imagePath;
        return BitmapHelper.decodeSampledBitmapFromFile(imageAbsPath, width, height);
    }

    /**
     * Loads the medication image into given ImageView in the background
     *
     * @param context Context for this bitmap
     * @param imageView ImageView to update when bitmap is loaded
     * @param width Width of the image
     * @param height Height of the image
     */
    public void getBitmap(Context context, ImageView imageView, int width, int height) {
        ImageStorage imageStorage = ImageStorage.getInstance(context);
        String imageAbsPath = imageStorage.getAbsDir() + "/" + imagePath;
        new BitmapHelper.BitmapWorkerTask(imageView, imageAbsPath, width, height).execute(context);
    }

    /**
     * Default sort - by name alphabetically
     *
     * @param another MedicationItem to compare this medication to
     * @return a negative integer if this instance is less than another; a positive integer if this instance is greater than another; 0 if this instance has the same order as another.
     */
    @Override
    public int compareTo(@NonNull MedicationItem another) {
        return name.compareTo(another.getName());
    }

    /**
     * A comparator so we can sort dosages by name, reverse alphabetically
     */
    public static class ReverseNameComparator implements Comparator<MedicationItem> {

        @Override
        public int compare(MedicationItem lhs, MedicationItem rhs) {
            return rhs.getName().compareTo(lhs.getName());
        }
    }

    public static class CreationComparator implements Comparator<MedicationItem> {

        @Override
        public int compare(MedicationItem lhs, MedicationItem rhs) {
            return (int) (lhs.getId() - rhs.getId());
        }
    }

    public static class ReverseCreationComparator implements Comparator<MedicationItem> {

        @Override
        public int compare(MedicationItem lhs, MedicationItem rhs) {
            return (int) (rhs.getId() - lhs.getId());
        }
    }

//    /**
//     * A comparator so we can sort dosages by date, descending
//     */
//    public static class ReverseDateComparator implements Comparator<DoseItem> {
//
//        @Override
//        public int compare(DoseItem lhs, DoseItem rhs) {
//            return rhs.getDate().compareTo(lhs.getDate());
//        }
//    }

    /**
     * Deletes doses for medication & medication after confirmation from user
     *
     */
    public void confirmSetInactive(final Context context, final OnListUpdatedListener listener) {
        final DatabaseDAO db = new DatabaseDAO(context);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(R.string.alert_deactivate_med_title);
        alertDialogBuilder.setMessage(R.string.alert_deactivate_med_message);
        alertDialogBuilder.setPositiveButton(R.string.alert_deactivate_med_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                int numDeleted = db.removeAllFutureDosesForMed(MedicationItem.this);
                MedicationItem.this.setActive(false);
                db.updateMedication(MedicationItem.this);
                db.close();
                if (listener != null) {
                    listener.onListUpdated();
                }
                Toast.makeText(context, "" + numDeleted + context.getString(R.string.alert_med_toast_num_deleted), Toast.LENGTH_SHORT).show();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.alert_delete_doses_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public void confirmSetInactive(final Context context) {
        confirmSetInactive(context, null);
    }

    /**
     * Deletes doses for medication & medication after confirmation from user
     *
     */
    public void confirmArchive(final Context context, final OnListUpdatedListener listener) {
        final DatabaseDAO db = new DatabaseDAO(context);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(R.string.alert_archive_med_title);
        alertDialogBuilder.setMessage(R.string.alert_archive_med_message);
        alertDialogBuilder.setPositiveButton(R.string.alert_archive_med_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                int numDeleted = db.removeAllDosesForMed(MedicationItem.this);
                MedicationItem.this.setActive(false);
                MedicationItem.this.setArchived(true);
                db.updateMedication(MedicationItem.this);
                db.close();
                if (listener != null) {
                    listener.onListUpdated();
                }
                Toast.makeText(context, "" + numDeleted + context.getString(R.string.alert_med_toast_num_deleted), Toast.LENGTH_SHORT).show();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.alert_delete_doses_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public void confirmArchive(final Context context) {
        confirmArchive(context, null);
    }

    public void confirmDeleteMed(final Context context, final OnListUpdatedListener listener) {
        final DatabaseDAO db = new DatabaseDAO(context);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(R.string.alert_delete_med_title);
        alertDialogBuilder.setMessage(R.string.alert_delete_med_message);
        alertDialogBuilder.setPositiveButton(R.string.alert_delete_med_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                db.removeMedication(context, MedicationItem.this);
                db.close();
                if (listener != null) {
                    listener.onListUpdated();
                }
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.alert_delete_med_negative, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public void confirmDeleteMed(final Context context) {
        confirmDeleteMed(context, null);
    }

}
