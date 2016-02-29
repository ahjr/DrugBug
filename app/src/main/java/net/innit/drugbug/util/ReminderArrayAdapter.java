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
import net.innit.drugbug.model.DoseItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReminderArrayAdapter extends ArrayAdapter<DoseItem> {
    private final Context context;
    private final List<DoseItem> data;

    public ReminderArrayAdapter(Context context, List<DoseItem> doseItems) {
        super(context, R.layout.list_item_reminder, doseItems);

        Log.d(MainActivity.LOGTAG, "ReminderArrayAdapter: adapter created");
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

        Log.d(MainActivity.LOGTAG, "getView: dose name is " + doseItem.getMedication().getName());

        String display = doseItem.getMedication().getName() + " (" + doseItem.getDosage() + ")";
        mViewHolder.name.setText(display);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(R.string.date_format), Locale.getDefault());
        display = simpleDateFormat.format(doseItem.getDate());
        mViewHolder.date.setText(display);

        if (doseItem.getMedication().hasImage()) {
            mViewHolder.image.setImageBitmap(doseItem.getMedication().getBitmap(context));
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
