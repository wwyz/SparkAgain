package com.wwyz.loltv.loadMore;

import java.util.ArrayList;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.costum.android.widget.LoadMoreListView;
import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
import com.wwyz.loltv.R;
import com.wwyz.loltv.YoutubeActionBarActivity;
import com.wwyz.loltv.R.layout;
import com.wwyz.loltv.feedManager.FeedManager_Base;
import com.wwyz.loltv.feedManager.FeedManager_Playlist;

public class LoadMore_Activity_Channel extends LoadMore_Activity_Base implements
		OnNavigationListener {
	private boolean isFirstTimeLoading = true;

	@Override
	public void Initializing() {
		ab.setTitle("Channel");
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		final String[] catagory = { "Recent", "Playlists" };

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				ab.getThemedContext(), R.layout.sherlock_spinner_item,
				android.R.id.text1, catagory);

		adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		ab.setListNavigationCallbacks(adapter, this);

		ab.setSelectedNavigationItem(currentPosition);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		// First check it is under which section
		switch (section) {
		case 0:
			// In "Recent"
			Intent i = new Intent(this, YoutubeActionBarActivity.class);
			i.putExtra("video", videolist.get(position-1));
			startActivity(i);
			break;

		case 1:
			// In "Playlists"
			Intent i1 = new Intent(this, LoadMore_Activity_Base.class);

			i1.putExtra("API", videolist.get(position-1).getRecentVideoUrl());
			i1.putExtra("PLAYLIST_API", videolist.get(position-1)
					.getPlaylistsUrl());
			i1.putExtra("title", videolist.get(position-1).getTitle());
			i1.putExtra("thumbnail", videolist.get(position-1)
					.getThumbnailUrl());
			startActivity(i1);
			break;

		}

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		// set section indicator
		section = itemPosition;

		if (isFirstTimeLoading) {
			isFirstTimeLoading = false;
			return true;
		}

		
		oneStepRefresh();

		return true;
	}

	@Override
	public void refreshActivity() {

		oneStepRefresh();

	}

	public void oneStepRefresh() {
		if (section == 0) {
			// Section "Recent"

			redoRequest(recentAPI, new FeedManager_Base());
		}

		if (section == 1) {
			// Section "Playlists"
			redoRequest(playlistAPI, new FeedManager_Playlist());

		}
	}

}
