package net.innit.drugbug.util;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.innit.drugbug.R;
import net.innit.drugbug.data.DatabaseDAO;
import net.innit.drugbug.model.DoseItem;
import net.innit.drugbug.model.MedicationItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            mViewHolder.dateLabel = (TextView) convertView.findViewById(R.id.tvMedListDoseLabel);
            mViewHolder.date = (TextView) convertView.findViewById(R.id.tvMedListDose);
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

        DoseItem dose;
        DatabaseDAO db = new DatabaseDAO(context);
        db.open();
        Date date = null;

        int defaultColor = 0;
        if (mViewHolder.date.getTextColors().getDefaultColor() != Color.RED) {
            defaultColor = mViewHolder.date.getTextColors().getDefaultColor();
        }
        if (medicationItem.isActive()) {
            dose = db.getFirstFutureDose(medicationItem);
            date = dose.getDate();
            Date now = new Date();

            if ((!dose.isTaken()) && (now.after(dose.getDate()))) {
                mViewHolder.date.setTextColor(Color.RED);
            } else {
                mViewHolder.date.setTextColor(defaultColor);
            }
        } else if (medicationItem.isArchived()) {
            // med is inactive and archived
            date = medicationItem.getArchiveDate();
            mViewHolder.dateLabel.setText("Archived:");
        } else {
            // med is inactive and not archived
            dose = db.getLatestTakenDose(medicationItem);
            if (dose != null) {
                date = dose.getDate();
            }
            mViewHolder.dateLabel.setText("Last taken:");
        }
        db.close();

        SimpleDateFormat sdf = new SimpleDateFormat(convertView.getResources().getString(R.string.date_format), Locale.getDefault());
        if (date != null) {
            mViewHolder.date.setText(sdf.format(date));
        } else {
            mViewHolder.date.setText("No doses taken");
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    private static class ViewHolder {
        private TextView name;
        private TextView date;
        private TextView dateLabel;
        private TextView frequency;
        private ImageView image;
    }

    public void updateList(List<MedicationItem> list){
        this.data = list;
        notifyDataSetChanged();
    }
}

