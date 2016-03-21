package net.innit.drugbug.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.innit.drugbug.R;
import net.innit.drugbug.model.DoseItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static net.innit.drugbug.util.Constants.IMAGE_HEIGHT_LIST;
import static net.innit.drugbug.util.Constants.IMAGE_WIDTH_LIST;

public class ReminderArrayAdapter extends ArrayAdapter<DoseItem> {
    private final Context context;
    private final List<DoseItem> data;

    public ReminderArrayAdapter(Context context, List<DoseItem> doseItems) {
        super(context, R.layout.list_item_reminder, doseItems);

        this.context = context;
        data = doseItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;
        DoseItem doseItem = data.get(position);

        if (convertView == null) {
            mViewHolder = new ViewHolder();

            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.list_item_reminder, parent, false);

            mViewHolder.name = (TextView) convertView.findViewById(R.id.tvListItemName);
            mViewHolder.date = (TextView) convertView.findViewById(R.id.tvListItemDate);
            mViewHolder.image = (ImageView) convertView.findViewById(R.id.ivDoseListImage);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        String display = doseItem.getMedication().getName() + " (" + doseItem.getDosage() + ")";
        mViewHolder.name.setText(display);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(R.string.date_format), Locale.getDefault());
        display = simpleDateFormat.format(doseItem.getDate());
        mViewHolder.date.setText(display);

        if (doseItem.getMedication().hasImage()) {
            doseItem.getMedication().getBitmap(context, mViewHolder.image, IMAGE_WIDTH_LIST, IMAGE_HEIGHT_LIST);
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
        private ImageView image;
    }
}
