package net.innit.drugbug.model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.Toast;

import net.innit.drugbug.R;
import net.innit.drugbug.data.DBDataSource;
import net.innit.drugbug.util.BitmapHelper;
import net.innit.drugbug.util.ImageStorage;

import java.io.File;
import java.util.Comparator;

public class MedicationItem implements Comparable<MedicationItem> {
    private long id;
    private String name;    // Name of drug taken
    private String frequency; // Frequency of doses
    private String imagePath; // Picture of the pill or label
    private boolean active = true;
    private boolean archived;

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
        this.archived = archived;
    }

    public boolean hasTaken(Context context) {
        DBDataSource db = new DBDataSource(context);
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

    public Bitmap getBitmap(Context context, int width, int height) {
        ImageStorage imageStorage = ImageStorage.getInstance(context);
        String imageAbsPath = imageStorage.getAbsDir() + "/" + imagePath;
        return BitmapHelper.decodeSampledBitmapFromFile(imageAbsPath, width, height);
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

    // todo add sort list by last taken dose
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
    public void confirmSetInactive(final Context context) {
        final DBDataSource db = new DBDataSource(context);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Deactivate medication?");
        alertDialogBuilder.setMessage("All untaken doses will be removed.");
        alertDialogBuilder.setPositiveButton("Yes, deactivate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                int numDeleted = db.removeAllFutureDosesForMed(MedicationItem.this);
                MedicationItem.this.setActive(false);
                db.updateMedication(MedicationItem.this);
                db.close();
                Toast.makeText(context, "" + numDeleted + " doses deleted", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.alert_delete_doses_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    /**
     * Deletes doses for medication & medication after confirmation from user
     *
     */
    public void confirmArchive(final Context context) {
        final DBDataSource db = new DBDataSource(context);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Archive medication?");
        alertDialogBuilder.setMessage("All doses taken and untaken doses will be removed.");
        alertDialogBuilder.setPositiveButton("Yes, archive", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                int numDeleted = db.removeAllDosesForMed(MedicationItem.this);
                MedicationItem.this.setActive(false);
                MedicationItem.this.setArchived(true);
                db.updateMedication(MedicationItem.this);
                db.close();
                Toast.makeText(context, "" + numDeleted + " doses deleted", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.alert_delete_doses_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    public void confirmDeleteMed(final Context context) {
        final DBDataSource db = new DBDataSource(context);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(R.string.alert_delete_med_title);
        alertDialogBuilder.setMessage(R.string.alert_delete_med_message);
        alertDialogBuilder.setPositiveButton(R.string.alert_delete_med_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.open();
                db.removeMedication(context, MedicationItem.this);
                db.close();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.alert_delete_med_negative, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }


}
