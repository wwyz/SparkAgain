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
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.costum.android.widget.LoadMoreListView;
import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
import com.google.ads.AdView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.wwyz.loltv.MyAsyncTask;
import com.wwyz.loltv.R;
import com.wwyz.loltv.YoutubeActionBarActivity;
import com.wwyz.loltv.R.drawable;
import com.wwyz.loltv.R.id;
import com.wwyz.loltv.R.layout;
import com.wwyz.loltv.adapters.VideoArrayAdapter;
import com.wwyz.loltv.data.Video;
import com.wwyz.loltv.feedManager.FeedManager_Base;

public class LoadMore_Base extends SherlockListFragment implements
		ActionBar.OnNavigationListener {
	protected LoadMoreListView myLoadMoreListView;
	protected ArrayList<String> titles;
	protected ArrayList<Video> videolist;

	protected boolean isMoreVideos;
	protected SherlockFragmentActivity sfa;
	protected ActionBar ab;
	protected String abTitle;
	protected FeedManager_Base feedManager;
	protected Fragment nextFragment;
	protected Fragment FragmentAll;
	protected Fragment FragmentUploader;
	protected Fragment FragmentPlaylist;
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
	protected ActionBar mActionBar;
	protected boolean firstTime = true;
	protected int currentPosition = 0;
	protected AdView adView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Get the current activity
		sfa = this.getSherlockActivity();

		// Get loading view
		fullscreenLoadingView = sfa
				.findViewById(R.id.fullscreen_loading_indicator);

		// Get ads view
		adView = (AdView) sfa.findViewById(R.id.ad);
		
		// default no filter for videos
		needFilter = false;

		mInflater = inflater;

		// set the layout
		view = inflater.inflate(R.layout.loadmore_list, null);

		// Initial fragment manager
		fm = sfa.getSupportFragmentManager();

		// Get the button view in retry.xml
		mRetryButton = (Button) sfa.findViewById(R.id.mRetryButton);

		// Get Retry view
		mRetryView = sfa.findViewById(R.id.mRetry);

		// get action bar
		ab = sfa.getSupportActionBar();

		// Initilizing the empty arrays
		titles = new ArrayList<String>();
		videolist = new ArrayList<Video>();
		// thumbList = new ArrayList<String>();

		// set adapter
		// vaa = new VideoArrayAdapter(inflater.getContext(), titles, videolist,
		// this);

		API = new ArrayList<String>();

		// Initializing important variables
		Initializing();

		// Set action bar title
		ab.setTitle(abTitle);

		// check whether there are more videos in the playlist
		if (API.isEmpty())
			isMoreVideos = false;
		else if (API.get(0) != null)
			isMoreVideos = true;

		// set the adapter
		// setListAdapter(vaa);

		mActionBar = sfa.getSupportActionBar();
		setDropdown();


		return view;

	}
	
	public void setDropdown(){
		if (hasDropDown) {

			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

			final String[] catagory = { "General", "Channels" };

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					mActionBar.getThemedContext(),
					R.layout.sherlock_spinner_item, android.R.id.text1,
					catagory);

			adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

			mActionBar.setListNavigationCallbacks(adapter, this);

			mActionBar.setSelectedNavigationItem(currentPosition);

		} else {
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		}
	}

	public void setOptionMenu(boolean hasRefresh, boolean hasDropDown) {
		this.hasRefresh = hasRefresh;
		this.hasDropDown = hasDropDown;
	}

	public void refreshFragment() {
		String firstApi = API.get(0);
		API.clear();
		API.add(firstApi);
		isMoreVideos = true;
		titles.clear();
		videolist.clear();
		setListView();
	}

	public void setListView() {
		myLoadMoreListView = (LoadMoreListView) this.getListView();
		myLoadMoreListView.setDivider(null);		
		
		setBannerInHeader();
		
		vaa = new VideoArrayAdapter(sfa, titles, videolist, imageLoader);
		setListAdapter(vaa);
		


		// Why check internet here?
		// if (ic.checkConnection(sfa)) {
		if (isMoreVideos) {
			// there are more videos in the list
			// set the listener for loading need
			myLoadMoreListView.setOnLoadMoreListener(new OnLoadMoreListener() {
				public void onLoadMore() {
					// Do the work to load more items at the end of
					// list

					if (isMoreVideos == true) {
						// new LoadMoreTask().execute(API.get(0));
						LoadMoreTask newTask = (LoadMoreTask) new LoadMoreTask(
								LoadMoreTask.LOADMORETASK, myLoadMoreListView,
								fullscreenLoadingView, mRetryView);
						newTask.execute(API.get(API.size()-1));
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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		setListView();

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		if (hasRefresh)
			menu.add(0, 0, 0, "Refresh")
					.setIcon(R.drawable.ic_refresh)
					.setShowAsAction(
							MenuItem.SHOW_AS_ACTION_IF_ROOM
									| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		// do nothing if no network
		FragmentTransaction ft = getFragmentManager().beginTransaction();

		switch (item.getItemId()) {

		case 0:
			// Menu option 1
//			Toast.makeText(sfa, "Refreshing", Toast.LENGTH_SHORT).show();
			refreshFragment();
//			ft.replace(R.id.content_frame, currentFragment);
			break;

		case 11:
			// Menu option 1
			ft.replace(R.id.content_frame, FragmentAll);
			break;

		case 12:
			// Menu option 2
			ft.replace(R.id.content_frame, FragmentUploader);
			break;

//		case 13:
//			// Menu option 3
//			ft.replace(R.id.content_frame, FragmentPlaylist);
//			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		ft.commit();

		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		// check network first
		// if (ic.checkConnection(this.getSherlockActivity())) {
		// get selected items

//		Toast.makeText(this.getSherlockActivity(), videos.get(position),
//				Toast.LENGTH_SHORT).show();

		Intent i = new Intent(this.getSherlockActivity(),
				YoutubeActionBarActivity.class);
		i.putExtra("video", videolist.get(position-1));
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
					newTask.execute(API.get(API.size()-1));
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
						videolist.add(v);
					}
				}
				try {
					// put the next API in the first place of the array
					API.add(feedManager.getNextApi());
					// nextAPI = feedManager.getNextApi();
					if (API.get(API.size()-1) == null) {
						// No more videos left
						isMoreVideos = false;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
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

	// public void handleCancelView(LoadMoreTask mTask,boolean isException) {
	//
	// ((LoadMoreListView) myLoadMoreListView).onLoadMoreComplete();
	//
	// if (isException){
	//
	// DisplayView(mRetryView, myLoadMoreListView, fullscreenLoadingView) ;
	// }
	// }

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Destroy ads when the view is destroyed
		if(adView != null){
			adView.destroy();
		}
//		Log.d("UniversalImageLoader", "cleared!");
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
	public void clearFragmentStack() {
		fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}

	public void hideAllViews() {
		if (fullscreenLoadingView != null)
			fullscreenLoadingView.setVisibility(View.GONE);
		if (myLoadMoreListView != null)
			myLoadMoreListView.setVisibility(View.GONE);
		if (mRetryView != null)
			mRetryView.setVisibility(View.GONE);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		if (firstTime) {
			firstTime = false;

			// System.out.println("First time!!!!!!!!!!!!!!!!!!");
			return true;
		}

		// System.out.println("Wo shi " + itemPosition);

		FragmentTransaction ft = getFragmentManager().beginTransaction();

		switch (itemPosition) {

		case 0:
			// Menu option 1
			ft.replace(R.id.content_frame, FragmentAll);
			break;

		case 1:
			// Menu option 2
			ft.replace(R.id.content_frame, FragmentUploader);
			break;

		case 2:
			// Menu option 3
			ft.replace(R.id.content_frame, FragmentPlaylist);
			break;

		}

		ft.commit();

		// TODO Auto-generated method stub
		return true;
	}

	public void setBannerInHeader(){
		 if (myLoadMoreListView.getHeaderViewsCount() == 0){
	           View header = (View) sfa.getLayoutInflater().inflate(R.layout.banner, null);
	           myLoadMoreListView.addHeaderView(header,null,false);
	           
	       
	     }
	}
}
