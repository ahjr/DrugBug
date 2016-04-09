package net.innit.drugbug.util;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;

/**
 * Created by alissa on 4/8/16.
 */
public class GestureListener extends GestureDetector.SimpleOnGestureListener {
    private Context ctx;

    public GestureListener(Context context) {
        ctx = context;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(ctx);
        int minSwipeDistance = viewConfiguration.getScaledPagingTouchSlop();
        int minSwipeVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        int maxSwipeOffPath = viewConfiguration.getScaledTouchSlop();

        if (Math.abs(e1.getY() - e2.getY()) > maxSwipeOffPath) {
            return false;
        }

        if (Math.abs(velocityX) > minSwipeVelocity) {
            // Right to left swipe
//            if (e1.getX() - e2.getX() > minSwipeDistance) {
//
//            }
//            // Left to right
//            else
            if (e2.getX() - e1.getX() > minSwipeDistance) {

                Toast.makeText(ctx, "Back swipe", Toast.LENGTH_SHORT).show();
            }

            // Call some app-related functions to update the display
//            displayDate(calendar.getMonth(), calendar.getYear());
        }

        return false;
    }
}
