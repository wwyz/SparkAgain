package com.wwyz.loltv.Adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.wwyz.loltv.R;
import com.wwyz.loltv.Data.Video;
import com.wwyz.loltv.R.drawable;
import com.wwyz.loltv.R.id;
import com.wwyz.loltv.R.layout;

public class VideoArrayAdapter extends ArrayAdapter<String> {

	private final ArrayList<String> values;
	private ArrayList<Video> videos;
	private LayoutInflater inflater;

	private DisplayImageOptions options;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	private ImageLoader imageLoader;

	public VideoArrayAdapter(Context context, ArrayList<String> values,
			ArrayList<Video> videos, ImageLoader imageLoader) {
		super(context, R.layout.videolist, values);

		this.values = values;
		this.videos = videos;
		this.imageLoader = imageLoader;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (!this.imageLoader.isInited()){
			this.imageLoader.init(ImageLoaderConfiguration.createDefault(context));
		}
		// imageLoader=new ImageLoader(context.getApplicationContext());

		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.loading)
				.showImageForEmptyUri(R.drawable.loading)
				.showImageOnFail(R.drawable.loading).cacheInMemory(true)
				.cacheOnDisc(true)
				// .displayer(new RoundedBitmapDisplayer(20))
				.build();
		//
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.videolist, parent, false);

			holder = new ViewHolder();

			holder.titleView = (TextView) convertView
					.findViewById(R.id.videotitle);
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.thumbnail);
			holder.countView = (TextView) convertView.findViewById(R.id.Desc);
			holder.videoLength = (TextView) convertView
					.findViewById(R.id.videolength);

			// set the author
			holder.authorView = (TextView) convertView
					.findViewById(R.id.videouploader);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.titleView.setText(values.get(position));
		holder.authorView.setText(videos.get(position).getAuthor());

		// values for time and view counts should not be null
		if (videos.get(position).getUpdateTime() != null
				&& videos.get(position).getViewCount() != null) {

			// For Youtube videos, showing update date and views
			holder.countView.setText(videos.get(position).getUpdateTime()
					+ " | " + videos.get(position).getViewCount());
		} else if (videos.get(position).getViewCount() != null) {

			// For Twitch, only showing number of viewers
			holder.watchingIcon = (ImageView) convertView
					.findViewById(R.id.watching);
			holder.watchingIcon.setVisibility(View.VISIBLE);
			holder.countView.setText(videos.get(position).getViewCount());
			
		} else {

			holder.countView.setText(null);
		}
		holder.videoLength.setText(videos.get(position).getDuration());

		imageLoader.displayImage(videos.get(position).getThumbnailUrl(),
				holder.imageView, options, animateFirstListener);

		return convertView;
	}

	static class ViewHolder {
		TextView titleView;
		TextView authorView;
		TextView countView;
		TextView videoLength;
		ImageView imageView;
		ImageView watchingIcon;

	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

}