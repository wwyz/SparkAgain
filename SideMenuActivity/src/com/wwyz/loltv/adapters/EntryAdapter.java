package com.wwyz.loltv.adapters;

import java.util.ArrayList;

import com.wwyz.loltv.R;
import com.wwyz.loltv.R.id;
import com.wwyz.loltv.R.layout;
import com.wwyz.loltv.data.EntryItem;
import com.wwyz.loltv.data.Item;
import com.wwyz.loltv.data.SectionItem;

import android.R.color;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EntryAdapter extends ArrayAdapter<Item> {

	private ArrayList<Item> items;
	private LayoutInflater vi;

	public EntryAdapter(Context context, ArrayList<Item> items) {
		super(context, 0, items);
		this.items = items;
		vi = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		final Item i = items.get(position);
		if (i != null) {
			if (i.isSection()) {
				SectionItem si = (SectionItem) i;
				v = vi.inflate(R.layout.list_item_section, null);

				v.setOnClickListener(null);
				v.setOnLongClickListener(null);
				v.setLongClickable(false);

				final TextView sectionView = (TextView) v
						.findViewById(R.id.list_item_section_text);
				sectionView.setText(si.getTitle());
			} else {
				EntryItem ei = (EntryItem) i;
				v = vi.inflate(R.layout.list_item_entry, null);
				final TextView title = (TextView) v
						.findViewById(R.id.list_item_entry_title);
				final TextView subtitle = (TextView) v
						.findViewById(R.id.list_item_entry_summary);
				final ImageView icon = (ImageView) v
						.findViewById(R.id.list_item_entry_drawable);
				final ImageView header = (ImageView) v
						.findViewById(R.id.list_item_entry_header);

				if (title != null)
					title.setText(ei.title);
				if (subtitle != null)
					subtitle.setText(ei.subtitle);
				if (icon != null)
					icon.setImageResource(ei.icon);
				if (header != null)
					header.setImageResource(color.transparent);

				if (ei.isChecked()) {

					v.setBackgroundResource(R.color.grey);
					header.setImageResource(R.color.red);

				}
			}
		}

		return v;
	}

}
