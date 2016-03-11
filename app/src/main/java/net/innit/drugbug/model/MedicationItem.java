package net.innit.drugbug.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import net.innit.drugbug.util.ImageStorage;

import java.io.File;
import java.util.Comparator;

public class MedicationItem implements Comparable<MedicationItem> {
    private long id;
    private String name;    // Name of drug taken
    private String frequency; // Frequency of doses
    private String imagePath; // Picture of the pill or label

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

    public Bitmap getBitmap(Context context) {
        ImageStorage imageStorage = ImageStorage.getInstance(context);
        return BitmapFactory.decodeFile(imageStorage.getAbsDir() + "/" + imagePath);
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

    // future todo add sort list by last taken dose
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

}
