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
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.costum.android.widget.LoadMoreListView;
import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
import com.wwyz.loltv.R;
import com.wwyz.loltv.adapters.NewsArrayAdapter;
import com.wwyz.loltv.data.News;

public class LoadMore_Gosu_News extends LoadMore_Base {
	private ArrayList<News> mNews = new ArrayList<News>();


	private NewsArrayAdapter mArrayAdatper;
	private getMatchInfo mgetMatchInfo;
	private int pageNum;
	private final String baseUri = "http://www.gosugamers.net";


	@Override
	public void Initializing() {
		// Inflating view

		// Give a title for the action bar
		abTitle = "Gosu News";

		// Give API URLs
		API.add("http://www.gosugamers.net/lol/news/archive");

		pageNum = 1;

		// Show menu
		setHasOptionsMenu(true);
		setOptionMenu(true, false);

	}

	@Override
	public void refreshFragment() {
		String firstApi = API.get(0);
		API.clear();
		API.add(firstApi);
		isMoreVideos = true;
		pageNum = 1;
		mNews.clear();
		setListView();
	}

	@Override
	public void setListView() {

		myLoadMoreListView = (LoadMoreListView) this.getListView();
		//myLoadMoreListView.setDivider(null);

		setBannerInHeader();

		mArrayAdatper = new NewsArrayAdapter(sfa, mNews);
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
		
		String url = mNews.get(position).getLink();
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
//		startActivity(i);
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
		protected void onPostExecute(String result) {

			// Log.d("AsyncDebug", "Into onPostExecute!");

			if (!taskCancel && result != null) {

				Document doc = Jsoup.parse(result);
				// get all links
				Elements links = doc.select("tr:has(td)");
				if (!links.isEmpty()) {
					String href = "";
					String newsTitle = "";
					String date = "";
					for (Element link : links) {

						// get the value from href attribute
						href = link.select("a").first().attr("href");
						newsTitle = link.select("a").first().text();
						date = link.select("td").get(1).text();
						if (href.contains("news")) {

							News aNews = new News();
							aNews.setLink(baseUri+href);
							aNews.setTitle(newsTitle);
							aNews.setDate(processDate(date));
							mNews.add(aNews);
						}
					}

				}

				Elements pages = doc.select("div.pages");
				if (pages != null) {
					Elements page_indicators = pages.select("a");
					if (page_indicators != null) {
						isMoreVideos = false;
						
						if((page_indicators.size() > 7) || (pageNum == 1)){
							isMoreVideos = true;
							pageNum++;
							API.add("http://www.gosugamers.net/news/archive?page="
									+ pageNum);
						}else{
							isMoreVideos = false;
						}
					}
				}

				mArrayAdatper.notifyDataSetChanged();

				// Call onLoadMoreComplete when the LoadMore task has
				// finished
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
