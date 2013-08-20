package com.wwyz.loltv.loadMore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.costum.android.widget.LoadMoreListView;
import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
import com.wwyz.loltv.R;
import com.wwyz.loltv.adapters.NewsArrayAdapter;
import com.wwyz.loltv.adapters.NewsArrayAdapter_Official;
import com.wwyz.loltv.data.News;

public class LoadMore_Official_News extends LoadMore_Base {
	private ArrayList<News> mNews = new ArrayList<News>();

	private NewsArrayAdapter_Official mArrayAdatper;
	private getMatchInfo mgetMatchInfo;
	private int pageNum;
	private final String baseUri = "http://beta.na.leagueoflegends.com";

	@Override
	public void Initializing() {
		// Inflating view

		// Give a title for the action bar
		abTitle = "News";

		// Give API URLs
		API.add("http://beta.na.leagueoflegends.com/en/news");

		pageNum = 0;

		// Show menu
		setHasOptionsMenu(true);
		setOptionMenu(true, true);

		currentPosition = 0;

	}

	@Override
	public void setDropdown() {
		if (hasDropDown) {

			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

			final String[] catagory = { "Official", "Gosu" };

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

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		if (firstTime) {
			firstTime = false;
			return true;
		}

		FragmentTransaction ft = getFragmentManager().beginTransaction();

		switch (itemPosition) {

		case 0:
			// Menu option 1
			ft.replace(R.id.content_frame, new LoadMore_Official_News());
			break;

		case 1:
			// Menu option 2
			ft.replace(R.id.content_frame, new LoadMore_Gosu_News());
			break;

		}

		ft.commit();

		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void refreshFragment() {
		String firstApi = API.get(0);
		API.clear();
		API.add(firstApi);
		isMoreVideos = true;
		pageNum = 0;
		titles.clear();
		mNews.clear();
		setListView();
	}

	@Override
	public void setListView() {

		myLoadMoreListView = (LoadMoreListView) this.getListView();
		// myLoadMoreListView.setDivider(null);

		setBannerInHeader();

		mArrayAdatper = new NewsArrayAdapter_Official(sfa, titles, mNews,
				imageLoader);
		setListAdapter(mArrayAdatper);

		if (isMoreVideos) {
			// there are more videos in the list
			// set the listener for loading need
			myLoadMoreListView.setOnLoadMoreListener(new OnLoadMoreListener() {
				public void onLoadMore() {
					// Do the work to load more items at the end of
					// list
					// network ok
					if (isMoreVideos) {

						mgetMatchInfo = new getMatchInfo(
								getMatchInfo.LOADMORETASK, myLoadMoreListView,
								fullscreenLoadingView, mRetryView);
						mgetMatchInfo.execute(API.get(API.size() - 1));
					} else {

						// ic.networkToast(sfa);

						((LoadMoreListView) myLoadMoreListView)
								.onLoadMoreComplete();
					}

				}
			});

		} else {
			myLoadMoreListView.setOnLoadMoreListener(null);
		}

		// sending Initial Get Request to Youtube
		if (!API.isEmpty()) {
			// show loading screen
			doRequest();
		}

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		// Toast.makeText(this.getSherlockActivity(),
		// matchArray.get(position).getGosuLink(), Toast.LENGTH_SHORT)
		// .show();

		// Intent i = new Intent(this.getSherlockActivity(),
		// MatchDetailsActivity.class);
		// i.putExtra("match", matchArray.get(position - 1));
		// startActivity(i);

		String url = mNews.get(position - 1).getLink();
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		// startActivity(i);
		startActivity(Intent.createChooser(i, "Choose a browser"));

	}

	@Override
	protected void doRequest() {
		// TODO Auto-generated method stub

		// System.out.println("DO!!!!!");
		for (String s : API) {
			mgetMatchInfo = new getMatchInfo(getMatchInfo.INITTASK,
					myLoadMoreListView, fullscreenLoadingView, mRetryView);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				mgetMatchInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						s);
			} else {
				mgetMatchInfo.execute(s);
			}
		}
	}

	private class getMatchInfo extends LoadMoreTask {

		public getMatchInfo(int type, View contentView, View loadingView,
				View retryView) {
			super(type, contentView, loadingView, retryView);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void setRetryListener(final int type) {
			mRetryButton = (Button) retryView.findViewById(R.id.mRetryButton);

			mRetryButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					mgetMatchInfo = (getMatchInfo) new getMatchInfo(type,
							contentView, loadingView, retryView);
					mgetMatchInfo.DisplayView(loadingView, contentView,
							retryView);
					mgetMatchInfo.execute(API.get(API.size() - 1));

				}
			});

		}

		@Override
		public String doInBackground(String... uri) {

			super.doInBackground(uri[0]);

			if (!taskCancel && responseString != null) {
				pullNews(responseString);
			}
			// pullNews();
			return responseString;
		}

		private void pullNews(String responseString) {
			Document doc = Jsoup.parse(responseString);
			// get all links
			Elements links = new Elements();
			links = doc.select("div.white-stone");

			if (!links.isEmpty()) {
				String imageUri = "";
				String newsUri = "";
				String newsTitle = "";
				String newsSubtitle = "";
				String date = "";
				System.out.println("Number of News: " + links.size());
				for (Element link : links) {

					// get the value from href attribute
					imageUri = link.select("img").first().attr("src");
					newsUri = link.select("a").first().attr("href");
					newsTitle = link.select("a").first().attr("title");
					newsSubtitle = link.select("div.teaser-content").first()
							.select("div").first().text();
					date = link.select("span").first().text();

					// System.out.println("\nImage uri: " + baseUri + imageUri);
					// System.out.println("News href: " + baseUri + newsUri);
					// System.out.println("News Title: " + newsTitle);
					// System.out.println("News Subtitle uri: " + newsSubtitle);
					// System.out.println("Date: " + date);

					titles.add(newsTitle);

					News aNews = new News();
					aNews.setLink(baseUri + newsUri);
					aNews.setImageUri(baseUri + imageUri);
					aNews.setTitle(newsTitle);
					aNews.setSubTitle(newsSubtitle);
					aNews.setDate(date);
					mNews.add(aNews);

				}

			}

			Elements pages = new Elements();
			pages = doc.select("a[class=next disabled]");
			if (pages != null && pages.size() == 0) {

				isMoreVideos = true;
				pageNum++;
				API.add("http://beta.na.leagueoflegends.com/en/news?page="
						+ pageNum);

			} else {
				isMoreVideos = false;
			}
		}

		@Override
		protected void onPostExecute(String result) {

			// Log.d("AsyncDebug", "Into onPostExecute!");

			if (!taskCancel && result != null) {

				mArrayAdatper.notifyDataSetChanged();

				// Call onLoadMoreComplete when the LoadMore task has finished
				((LoadMoreListView) myLoadMoreListView).onLoadMoreComplete();

				// loading done
				DisplayView(contentView, retryView, loadingView);

				if (!isMoreVideos) {
					((LoadMoreListView) myLoadMoreListView).onNoMoreItems();

					myLoadMoreListView.setOnLoadMoreListener(null);
				}

			} else {

				handleCancelView();
			}

		}
	}

	@Override
	public void onDestroy() {

		super.onDestroy();

		if (mgetMatchInfo != null
				&& mgetMatchInfo.getStatus() == Status.RUNNING)
			mgetMatchInfo.cancel(true);

	}

	@SuppressLint("SimpleDateFormat")
	public String processDate(String s) {

		Date date = new Date();
		// Calendar c = new Calendar();
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));

		try {
			date = sdf.parse(s);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		sdf = new SimpleDateFormat("MMMM d");
		sdf.setTimeZone(TimeZone.getDefault());
		// date.setHours(Integer.parseInt(hourInString));
		return sdf.format(date);

	}

}
