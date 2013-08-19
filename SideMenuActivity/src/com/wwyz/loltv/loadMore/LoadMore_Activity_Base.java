package com.wwyz.loltv.loadMore;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.costum.android.widget.LoadMoreListView;
import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
import com.google.ads.AdView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.wwyz.loltv.MyAsyncTask;
import com.wwyz.loltv.R;
import com.wwyz.loltv.YoutubeActionBarActivity;
import com.wwyz.loltv.R.drawable;
import com.wwyz.loltv.R.id;
import com.wwyz.loltv.R.layout;
import com.wwyz.loltv.adapters.VideoArrayAdapter;
import com.wwyz.loltv.data.Video;
import com.wwyz.loltv.feedManager.FeedManager_Base;

public class LoadMore_Activity_Base extends SherlockListActivity {
	protected LoadMoreListView myLoadMoreListView;
	protected ArrayList<String> titles;
	protected ArrayList<String> videos;
	protected ArrayList<Video> videolist;

	protected boolean isMoreVideos;
	protected ActionBar ab;
	protected String abTitle;
	protected FeedManager_Base feedManager;
	protected Fragment nextFragment;
	protected Fragment FragmentAll;
	protected Fragment FragmentUploader;
	protected ArrayList<String> API;
	protected View view;
	protected LayoutInflater mInflater;
	protected VideoArrayAdapter vaa;
	protected ArrayList<LoadMoreTask> mLoadMoreTasks = new ArrayList<LoadMoreTask>();
	protected Button mRetryButton;
	protected View mRetryView;
	protected boolean needFilter;
	protected FragmentManager fm;
	protected View fullscreenLoadingView;
	protected boolean hasRefresh;
	protected boolean hasDropDown = false;
	protected Fragment currentFragment;
	public boolean isBusy = false;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	protected boolean firstTime = true;
	protected int currentPosition = 0;
	protected String title;
	protected String recentAPI;
	protected String playlistAPI;
	protected String thumbnailUrl;
	protected int section = 0;
	private DisplayImageOptions options;
	protected AdView adView;
	protected boolean hasHeader = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get the current activity
		setContentView(R.layout.loadmore_list);

		// Get loading view
		fullscreenLoadingView = findViewById(R.id.fullscreen_loading_indicator);
		adView = (AdView) findViewById(R.id.ad);
		// default no filter for videos

		Intent intent = getIntent();
		recentAPI = intent.getStringExtra("API");
		playlistAPI = intent.getStringExtra("PLAYLIST_API");
		title = intent.getStringExtra("title");
		thumbnailUrl = intent.getStringExtra("thumbnail");

		if (!this.imageLoader.isInited()) {
			this.imageLoader.init(ImageLoaderConfiguration.createDefault(this));
		}
		// imageLoader=new ImageLoader(context.getApplicationContext());

		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.loading)
				.showImageForEmptyUri(R.drawable.loading)
				.showImageOnFail(R.drawable.loading).cacheInMemory(true)
				.cacheOnDisc(true).displayer(new RoundedBitmapDisplayer(20))
				.build();

		// Get Retry view
		mRetryView = findViewById(R.id.mRetry);

		// get action bar
		ab = getSupportActionBar();

		// Initilizing the empty arrays
		titles = new ArrayList<String>();
		videos = new ArrayList<String>();
		videolist = new ArrayList<Video>();
		// thumbList = new ArrayList<String>();

		// set adapter
		// vaa = new VideoArrayAdapter(inflater.getContext(), titles, videolist,
		// this);

		API = new ArrayList<String>();

		// Initializing important variables
		API.add(recentAPI);
		// Set action bar title
		// System.out.println("My title: "+title);

		ab.setTitle("");

		feedManager = new FeedManager_Base();

		Initializing();
		// check whether there are more videos in the playlist
		if (API.isEmpty())
			isMoreVideos = false;
		else if (API.get(0) != null)
			isMoreVideos = true;

		// set the adapter
		// setListAdapter(vaa);

		ab.setHomeButtonEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);

		setListView();

	}

	public void setOptionMenu(boolean hasRefresh, boolean hasDropDown) {
		this.hasRefresh = hasRefresh;
		this.hasDropDown = hasDropDown;
	}

	public void setListView() {
		myLoadMoreListView = (LoadMoreListView) this.getListView();
		myLoadMoreListView.setDivider(null);

		if (myLoadMoreListView.getHeaderViewsCount() == 0 && hasHeader == true) {
			View header = (View) getLayoutInflater().inflate(
					R.layout.titleview, null);
			myLoadMoreListView.addHeaderView(header, null, false);

			ImageView channelImage = (ImageView) findViewById(R.id.thumbnail);
			TextView channelName = (TextView) findViewById(R.id.name);

			imageLoader.displayImage(thumbnailUrl, channelImage, options, null);
			channelName.setText(title);

		}

		vaa = new VideoArrayAdapter(this, titles, videolist, imageLoader);
		setListAdapter(vaa);

		forceSet();
		if (isMoreVideos) {

			// there are more videos in the list
			// set the listener for loading need
			myLoadMoreListView.setOnLoadMoreListener(new OnLoadMoreListener() {
				public void onLoadMore() {

					if (isMoreVideos == true) {
						// new LoadMoreTask().execute(API.get(0));
						LoadMoreTask newTask = (LoadMoreTask) new LoadMoreTask(
								LoadMoreTask.LOADMORETASK, myLoadMoreListView,
								fullscreenLoadingView, mRetryView);
						newTask.execute(API.get(API.size() - 1));
						mLoadMoreTasks.add(newTask);
					}

				}
			});

		} else {
			myLoadMoreListView.setOnLoadMoreListener(null);
		}

		// sending Initial Get Request to Youtube
		if (!API.isEmpty()) {
			// show loading screen
			// DisplayView(fullscreenLoadingView, myLoadMoreListView,
			// mRetryView) ;
			doRequest();
		}

	}

	// Used to force set isMoreVideos variable
	protected void forceSet() {
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, 0, 0, "Refresh")
				.setIcon(R.drawable.ic_refresh)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			finish();
		}

		if (item.getItemId() == 0) {
			refreshActivity();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		Intent i = new Intent(this, YoutubeActionBarActivity.class);
		i.putExtra("video", videolist.get(position - 1));
		startActivity(i);

	}

	class LoadMoreTask extends MyAsyncTask {

		public LoadMoreTask(int type, View contentView, View loadingView,
				View retryView) {
			super(type, contentView, loadingView, retryView);
		}

		@Override
		public void handleCancelView() {
			((LoadMoreListView) myLoadMoreListView).onLoadMoreComplete();

			if (isException) {

				DisplayView(retryView, contentView, loadingView);
			}

		}

		@Override
		public void setRetryListener(final int type) {
			mRetryButton = (Button) retryView.findViewById(R.id.mRetryButton);

			mRetryButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					LoadMoreTask newTask = (LoadMoreTask) new LoadMoreTask(
							type, contentView, loadingView, retryView);
					newTask.DisplayView(loadingView, contentView, retryView);
					newTask.execute(API.get(API.size() - 1));
					mLoadMoreTasks.add(newTask);

				}
			});

		}

		@Override
		protected void onPostExecute(String result) {
			// Do anything with response..
			// System.out.println(result);

			// Log.d("AsyncDebug", "Into onPostExecute!");

			if (!taskCancel && result != null) {
				// Do anything with response..

				feedManager.setmJSON(result);

				List<Video> newVideos = feedManager.getVideoPlaylist();

				// adding new loaded videos to our current video list
				for (Video v : newVideos) {
					// System.out.println("new id: " + v.getVideoId());
					if (needFilter) {
						filtering(v);
						// System.out.println("need filter!");
					} else {
						titles.add(v.getTitle());
						videos.add(v.getVideoId());
						videolist.add(v);
					}
				}
				try {
					// put the next API in the first place of the array
					API.add(feedManager.getNextApi());
					// nextAPI = feedManager.getNextApi();
					if (API.get(API.size() - 1) == null) {
						// No more videos left
						isMoreVideos = false;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				vaa.notifyDataSetChanged();

				// Call onLoadMoreComplete when the LoadMore task, has
				// finished
				((LoadMoreListView) myLoadMoreListView).onLoadMoreComplete();

				// loading done
				DisplayView(contentView, retryView, loadingView);
				if (!isMoreVideos) {
					((LoadMoreListView) myLoadMoreListView).onNoMoreItems();

					((LoadMoreListView) myLoadMoreListView)
							.setOnLoadMoreListener(null);
				}

			} else {
				handleCancelView();
			}

		}

	}

	// sending the http request
	protected void doRequest() {
		// TODO Auto-generated method stub
		for (String s : API) {
			LoadMoreTask newTask = new LoadMoreTask(LoadMoreTask.INITTASK,
					myLoadMoreListView, fullscreenLoadingView, mRetryView);
			mLoadMoreTasks.add(newTask);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s);
			} else {
				newTask.execute(s);
			}
		}
	}

	public void Initializing() {

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Destroy ads when the view is destroyed
		if (adView != null) {
			adView.destroy();
		}

		// Log.d("UniversalImageLoader", "It's task root!");
		imageLoader.clearDiscCache();
		imageLoader.clearMemoryCache();

		// check the state of the task
		cancelAllTask();
		hideAllViews();

	}

	public void cancelAllTask() {

		for (LoadMoreTask mTask : mLoadMoreTasks) {
			if (mTask != null && mTask.getStatus() == Status.RUNNING) {
				mTask.cancel(true);

				// Log.d("AsyncDebug", "Task cancelled!!!!!!!!");
			}
			// else
			// Log.d("AsyncDebug", "Task cancellation failed!!!!");
		}

	}

	protected void filtering(Video v) {
		// TODO Auto-generated method stub

	}

	// Clear fragment back stack
	public void refreshActivity() {

		redoRequest(recentAPI, new FeedManager_Base());
	}

	public void redoRequest(String api, FeedManager_Base fb) {
		// Clean the API array, titiles, videos, and videlist
		API = new ArrayList<String>();
		titles = new ArrayList<String>();
		videos = new ArrayList<String>();
		videolist = new ArrayList<Video>();

		// Set feed manager
		feedManager = fb;

		// Add playlist API
		API.add(api);

		// Reset isMoreVideos
		if (API.isEmpty())
			isMoreVideos = false;
		else if (API.get(0) != null)
			isMoreVideos = true;

		// Getting playlists from Youtube
		setListView();
	}

	public void hideAllViews() {
		if (fullscreenLoadingView != null)
			fullscreenLoadingView.setVisibility(View.GONE);
		if (myLoadMoreListView != null)
			myLoadMoreListView.setVisibility(View.GONE);
		if (mRetryView != null)
			mRetryView.setVisibility(View.GONE);
	}

}
