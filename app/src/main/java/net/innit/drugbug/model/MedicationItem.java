package net.innit.drugbug.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import net.innit.drugbug.MainActivity;
import net.innit.drugbug.util.ImageStorage;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Comparator;

public class MedicationItem implements Comparable<MedicationItem> {
    private long id;
    private String name;    // Name of drug taken
    private String frequency; // Frequency of doses
    private String imagePath; // Picture of the pill or label
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

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
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
        return decodeSampledBitmapFromFile(imageAbsPath, width, height);
    }

    private static int resolveBitmapOrientation(String path) throws IOException {
        Log.d(MainActivity.LOGTAG, "resolveBitmapOrientation: start");
        return new ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    }

    private static Bitmap applyOrientation(Bitmap bitmap, int orientation) {
        Log.d(MainActivity.LOGTAG, "applyOrientation: start");
        Log.d(MainActivity.LOGTAG, "applyOrientation: orientation is " + orientation);
        int rotate;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            default:
                return bitmap;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;

        if (reqHeight == 0) {
            reqHeight = height;
        }

        if (reqWidth == 0) {
            reqWidth = width;
        }

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String imagePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        // Reorient resized bitmap if necessary
        try {
            return applyOrientation(BitmapFactory.decodeFile(imagePath, options), resolveBitmapOrientation(imagePath));
        } catch (IOException e) {
            return BitmapFactory.decodeFile(imagePath, options);
        }
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

    public class BitmapWorkerTask extends AsyncTask<Context, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int width;
        private int height;

        public BitmapWorkerTask(ImageView imageView, int width, int height) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
            this.width = width;
            this.height = height;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Context... params) {
            ImageStorage imageStorage = ImageStorage.getInstance(params[0]);
            String imageAbsPath = imageStorage.getAbsDir() + "/" + imagePath;
            return decodeSampledBitmapFromFile(imageAbsPath, width, height);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

}
