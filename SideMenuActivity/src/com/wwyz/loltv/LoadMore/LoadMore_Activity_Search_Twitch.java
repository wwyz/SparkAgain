package com.wwyz.loltv.LoadMore;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.wwyz.loltv.FlashInstallerActivity;
import com.wwyz.loltv.TwitchPlayer;
import com.wwyz.loltv.FeedManager.FeedManager_Twitch;

public class LoadMore_Activity_Search_Twitch extends LoadMore_Activity_Search
		implements SearchView.OnQueryTextListener {

	@Override
	public void Initializing() {
		// Give a title for the action bar
		abTitle = "Search Lives";
		
		ab.setTitle(abTitle);

		// Get the query
		Intent i = getIntent();
		mQuery = i.getStringExtra("query");
		
		    
		// encoding the query
		try {
			recentAPI = "https://api.twitch.tv/kraken/search/streams?q="+URLEncoder.encode(mQuery,"UTF-8")+"&limit=10";
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		// Give API URLs
		API.clear();
		API.add(recentAPI);
		

		// set a feed manager
		feedManager = new FeedManager_Twitch();
		
		// No header view in the list
		hasHeader = false;
		
		// set text in search field
		queryHint = "Search streams";

	}



	@Override
	public boolean onQueryTextSubmit(String query) {
		
		// encoding the query
		try {
			recentAPI = "https://api.twitch.tv/kraken/search/streams?q="+URLEncoder.encode(query,"UTF-8")+"&limit=10";
			refreshActivity();
			
		} catch (UnsupportedEncodingException e) {

		}
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void refreshActivity() {

		redoRequest(recentAPI, new FeedManager_Twitch());
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		if (check()){
			Intent i = new Intent(this, TwitchPlayer.class);
			i.putExtra("video", videolist.get(position).getVideoId());
			startActivity(i);

		}else{
			Intent i = new Intent(this, FlashInstallerActivity.class);
			startActivity(i);
		}
	}
	
	private boolean check() {
		PackageManager pm = this.getPackageManager();
		List<PackageInfo> infoList = pm
				.getInstalledPackages(PackageManager.GET_SERVICES);
		for (PackageInfo info : infoList) {
			if ("com.adobe.flashplayer".equals(info.packageName)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void forceSet() {
		isMoreVideos = false;
		
	}
}
