package net.innit.drugbug.util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.innit.drugbug.MainActivity;
import net.innit.drugbug.R;
import net.innit.drugbug.model.MedicationItem;

import java.util.List;

public class MedicationArrayAdapter extends ArrayAdapter<MedicationItem> {
    private final Context context;
    private final List<MedicationItem> data;

    public MedicationArrayAdapter(Context context, List<MedicationItem> medications) {
        super(context, R.layout.list_item_medication, medications);

        Log.d(MainActivity.LOGTAG, "MedicationArrayAdapter: adapter created");
        this.context = context;
        data = medications;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        MedicationItem medicationItem = data.get(position);

        if (convertView == null) {
            mViewHolder = new ViewHolder();

            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.list_item_medication, parent, false);

            mViewHolder.name = (TextView) convertView.findViewById(R.id.tvListItemName);
            mViewHolder.frequency = (TextView) convertView.findViewById(R.id.tvMedListFreq);
            mViewHolder.image = (ImageView) convertView.findViewById(R.id.ivMedListImage);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        Log.d(MainActivity.LOGTAG, "getView: medication name is " + medicationItem.getName());

        String title = medicationItem.getName();
        if (medicationItem.isArchived())
            title += " (archived)";
        mViewHolder.name.setText(title);
        mViewHolder.frequency.setText(medicationItem.getFrequency());

        if (medicationItem.hasImage()) {
//            mViewHolder.image.setImageBitmap(medicationItem.getBitmap(context, 50, 50));
            medicationItem.new BitmapWorkerTask(mViewHolder.image, 50, 50).execute(context);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    private static class ViewHolder {
        private TextView name;
        private TextView frequency;
        private ImageView image;
    }
}

