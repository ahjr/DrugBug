package net.innit.drugbug.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.innit.drugbug.R;
import net.innit.drugbug.model.MedicationItem;

import java.util.List;

import static net.innit.drugbug.data.Constants.IMAGE_HEIGHT_LIST;
import static net.innit.drugbug.data.Constants.IMAGE_WIDTH_LIST;

public class MedicationArrayAdapter extends ArrayAdapter<MedicationItem> {
    private final Context context;
    private List<MedicationItem> data;

    public MedicationArrayAdapter(Context context, List<MedicationItem> medications) {
        super(context, R.layout.list_item_medication, medications);

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

        String title = medicationItem.getName();
        if (medicationItem.isArchived())
            title += context.getString(R.string.medication_adapter_archived);
        else if (!medicationItem.isActive())
            title += context.getString(R.string.medication_adapter_inactive);
        mViewHolder.name.setText(title);
        mViewHolder.frequency.setText(medicationItem.getFrequency());

        if (medicationItem.hasImage()) {
            medicationItem.getBitmap(context, mViewHolder.image, IMAGE_WIDTH_LIST, IMAGE_HEIGHT_LIST);
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

    public void updateList(List<MedicationItem> list){
        this.data = list;
        notifyDataSetChanged();
    }
}

