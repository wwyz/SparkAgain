package com.wwyz.loltv.loadMore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.costum.android.widget.LoadMoreListView;
import com.costum.android.widget.LoadMoreListView.OnLoadMoreListener;
import com.wwyz.loltv.MatchDetailsActivity;
import com.wwyz.loltv.R;
import com.wwyz.loltv.adapters.MatchArrayAdapter;
import com.wwyz.loltv.data.Match;

public class LoadMore_UpcomingMatch extends LoadMore_Base {

	private Elements links;
	private ArrayList<Match> matchArray = new ArrayList<Match>();
	private MatchArrayAdapter mArrayAdatper;
	private getMatchInfo mgetMatchInfo;
	private int pageNum;
	private String baseUrl = "http://www.gosugamers.net";

	@Override
	public void Initializing() {
		// Inflating view

		// Give a title for the action bar
		abTitle = "Upcoming Matches";

		// Give API URLs
		API.add("http://www.in2lol.com/en/matches/");

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
		isMoreVideos = false;
		pageNum = 1;
		matchArray.clear();
		setListView();
	}

	@Override
	public void setListView() {

		myLoadMoreListView = (LoadMoreListView) this.getListView();
		myLoadMoreListView.setDivider(null);

		setBannerInHeader();

		mArrayAdatper = new MatchArrayAdapter(sfa, matchArray, imageLoader,
				false);
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

		Intent i = new Intent(this.getSherlockActivity(),
				MatchDetailsActivity.class);
		i.putExtra("match", matchArray.get(position - 1));
		startActivity(i);

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
			
//			System.out.println(responseString);

			if (!taskCancel && responseString != null) {
				try{
					pullResults(responseString);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			// pullNews();
			return responseString;
			
		}

		private void pullResults(String responseString) {
			Document doc = Jsoup.parse(responseString);

			Element box = null;
			box = doc.select("div.main").get(0);
			
//			System.out.println(box.toString());
			
			if (box != null) {

//				if (pageNum == 1) {
//					Element box_1 = doc.select("div#main_left").first();
//					links = box_1.select("tr:has(td.opp)");
//					Elements upcoming_links = box_2.select("tr:has(td.opp)");
//					links.addAll(upcoming_links);
//				} else {
//					links = box_2.select("tr:has(td.opp)");
//				}
//
//				Element paginator = box_2.select("div.paginator").first();
//
//				if (paginator == null) {
//					isMoreVideos = false;
//				} else {
//					if (paginator.select("a").last().hasAttr("class")) {
//						isMoreVideos = false;
//					} else {
//						isMoreVideos = true;
//						pageNum++;
//						API.add("http://www.gosugamers.net/lol/gosubet?u-page="
//								+ pageNum);
//					}
//				}
//
//				// Setting layout
				
				isMoreVideos = false;
				
				links = box.select("a.item");
				
//
				for (Element link : links) {
					
					System.out.println(link.toString());

					Match newMatch = new Match();

					Element opp_1 = link.select("span").get(0);
					Element opp_2 = link.select("span").get(2);

					newMatch.setTeamName1(opp_1.text().trim());
					newMatch.setTeamName2(opp_2.text().trim());

					newMatch.setTeamIcon1(opp_1.select("img").attr("src"));
					newMatch.setTeamIcon2(opp_2.select("img").attr("src"));

					newMatch.setTime(link.select("span").get(3).text().trim());
					
					if (newMatch.getTime().matches("[0-9]:[0-9]"))
							break;
					
					newMatch.setGosuLink(link.attr("href"));

					if (newMatch.getTime().toLowerCase().matches("live")) {
						newMatch.setMatchStatus(Match.LIVE);
					} else
						newMatch.setMatchStatus(Match.NOTSTARTED);
					
//					newMatch.setMatchStatus(Match.NOTSTARTED);

					matchArray.add(newMatch);

				}
				
				Collections.reverse(matchArray);

			} else {
				handleCancelView();
			}

		}

		@Override
		protected void onPostExecute(String result) {

			// Log.d("AsyncDebug", "Into onPostExecute!");

			if (!taskCancel && result != null) {
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

}
