package com.wwyz.loltv;

import com.wwyz.loltv.feedManager.FeedManager_Subscription;


public class Forum_reddit extends Forum_fragment {

	@Override
	public void Initializing() {
		// Give a title for the action bar
		abTitle = "Reddit";

		// Give API URLs

		// initialize the fragments in the Menu
		FragmentAll = new Forum_reddit();
		FragmentUploader = new Forum_official_na();
//		FragmentPlaylist = new LoadMore_H_New_Playlist();

		// set a feed manager

		// Show menu		
		setHasOptionsMenu(true);
		setOptionMenu(true, true);

		currentPosition = 0;
		// Set retry button listener
		

	}

}
