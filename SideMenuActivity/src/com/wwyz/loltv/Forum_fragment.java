package com.wwyz.loltv;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
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
import com.wwyz.loltv.TwitchPlayer.MyWebChromeClient;
import com.wwyz.loltv.TwitchPlayer.MyWebViewClient;
import com.wwyz.loltv.adapters.VideoArrayAdapter;
import com.wwyz.loltv.data.Video;
import com.wwyz.loltv.feedManager.FeedManager_Base;

@SuppressLint("SetJavaScriptEnabled")
public class Forum_fragment extends SherlockFragment implements
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
	protected Button mRetryButton;
	protected View mRetryView;
	protected boolean needFilter;
	protected FragmentManager fm;
	protected View fullscreenLoadingView;
	protected boolean hasRefresh;
	protected boolean hasDropDown = false;
	public boolean isBusy = false;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	protected ActionBar mActionBar;
	protected boolean firstTime = true;
	protected int currentPosition = 0;
	protected AdView adView;
	protected WebView mWebView;

	@SuppressWarnings("deprecation")
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
		view = inflater.inflate(R.layout.forum, null);

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

			final String[] catagory = { "Reddit", "Official" };

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
//		String firstApi = API.get(0);
//		API.clear();
//		API.add(firstApi);
//		isMoreVideos = true;
//		titles.clear();
//		videolist.clear();
		mWebView.reload();
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		if (getPhoneAndroidSDK() >= 14) {
			sfa.getWindow().setFlags(0x1000000, 0x1000000);
		}

		mWebView = (WebView) sfa.findViewById(R.id.forumView);
		WebSettings streamSettings = mWebView.getSettings();
		streamSettings.setJavaScriptEnabled(true);
		streamSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		streamSettings.setPluginState(PluginState.ON);
		streamSettings.setPluginsEnabled(true);
		streamSettings.setAllowFileAccess(true);
		streamSettings.setLoadWithOverviewMode(true);

		mWebView.setWebChromeClient(new WebChromeClient());
		mWebView.setWebViewClient(new WebViewClient());
		
		mWebView.loadUrl("http://i.reddit.com/r/leagueoflegends");


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
	
    public boolean backWebView() {
    	
    	if(mWebView == null) return false;

                if(mWebView.canGoBack() == true){
                    mWebView.goBack();
                    return true;
                }else return false;
    }
       


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
		hideAllViews();

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
			((SideMenuActivity)sfa).setCurrentFragment(FragmentAll);
			ft.replace(R.id.content_frame, FragmentAll);
			break;

		case 1:
			// Menu option 2
			((SideMenuActivity)sfa).setCurrentFragment(FragmentUploader);
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
	
	@SuppressWarnings("deprecation")
	public static int getPhoneAndroidSDK() {
		// TODO Auto-generated method stub
		int version = 0;
		try {
			version = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {
			// e.printStackTrace();
		}
		return version;

	}
}
