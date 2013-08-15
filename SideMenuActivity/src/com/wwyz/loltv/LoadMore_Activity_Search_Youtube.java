package com.wwyz.loltv;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

public class LoadMore_Activity_Search_Youtube extends LoadMore_Activity_Search
		implements SearchView.OnQueryTextListener {

	@Override
	public void Initializing() {
		// Give a title for the action bar
		abTitle = "Search Youtube";
		
		ab.setTitle(abTitle);

		// Get the query
		Intent i = getIntent();
		mQuery = i.getStringExtra("query");
		
		Log.i("debug: 1", mQuery);
		    
		// encoding the query
		try {
			recentAPI = "https://gdata.youtube.com/feeds/api/videos?q="+URLEncoder.encode(mQuery,"UTF-8")+"&orderby=relevance&start-index=1&max-results=10&v=2&alt=json";
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		// Give API URLs
		API.clear();
		API.add(recentAPI);
		

		// set a feed manager
		feedManager = new FeedManager_Search_Youtube();
		
		// No header view in the list
		hasHeader = false;
		
		// set text in search field
		queryHint = "Search videos";

	}



	@Override
	public boolean onQueryTextSubmit(String query) {
		
		// encoding the query
		try {
			recentAPI = "https://gdata.youtube.com/feeds/api/videos?q="+URLEncoder.encode(query,"UTF-8")+"&orderby=relevance&start-index=1&max-results=10&v=2&alt=json";
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

		redoRequest(recentAPI, new FeedManager_Search_Youtube());
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		Intent i = new Intent(this, YoutubeActionBarActivity.class);
		i.putExtra("video", videolist.get(position));
		startActivity(i);

	}
}
