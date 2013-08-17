package com.wwyz.loltv.loadMore;

import com.wwyz.loltv.feedManager.FeedManager_Subscription;


public class LoadMore_M_Subscription extends LoadMore_Base {

	@Override
	public void Initializing() {
		// Give a title for the action bar
		abTitle = "Matches";

		// Give API URLs
		API.add("https://gdata.youtube.com/feeds/api/users/15yOeOcyVOFKNSbzzLLiDQ/newsubscriptionvideos?max-results=10&alt=json");
		
			

		// initialize the fragments in the Menu
		FragmentAll = new LoadMore_M_Subscription();
		FragmentUploader = new LoadMore_M_Uploader();
//		FragmentPlaylist = new LoadMore_M_Playlist();

		// set a feed manager
		feedManager = new FeedManager_Subscription();

		// Show menu
		setHasOptionsMenu(true);
		setOptionMenu(true, true);
		
		// Set retry button listener
		currentPosition = 0;

	}
	
	
}
