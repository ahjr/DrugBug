package net.innit.drugbug;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import net.innit.drugbug.fragment.AddFrequencyFragment;

/**
 * Created by alissa on 4/8/16.
 */
public class FrequencyActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().add(new AddFrequencyFragment(), "Freq Fragment").commit();
    }
}
