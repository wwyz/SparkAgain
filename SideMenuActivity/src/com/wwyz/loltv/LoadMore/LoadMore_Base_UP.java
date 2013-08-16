package com.wwyz.loltv.LoadMore;

import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;

public class LoadMore_Base_UP extends LoadMore_Base {

	protected LoadMore_Base mLoadMore;
	protected String nextFragmentAPI;

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		// Putting the current fragment into stack for later call back

		// get the API corresponding to the item selected
		nextFragmentAPI = videolist.get(position-1).getRecentVideoUrl();
		String title = videolist.get(position-1).getTitle();
		String url = videolist.get(position-1).getThumbnailUrl();

		Intent i = new Intent(this.getSherlockActivity(),
				LoadMore_Activity_Channel.class);
		i.putExtra("API", nextFragmentAPI);
		i.putExtra("PLAYLIST_API", videolist.get(position-1).getPlaylistsUrl());
		i.putExtra("title", title);
		i.putExtra("thumbnail", url);
		startActivity(i);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			sfa.getSupportFragmentManager().popBackStack();
		}

		return super.onOptionsItemSelected(item);
	}

	public void InitializingNextFragment() {

	}

}
