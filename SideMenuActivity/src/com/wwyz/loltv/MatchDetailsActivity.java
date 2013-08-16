package com.wwyz.loltv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class MatchDetailsActivity extends SherlockListActivity {

	private ActionBar mActionBar;
	private Match match;
	private String baseUrl = "http://www.gosugamers.net";
	protected View fullscreenLoadingView;
	private Activity mActivity;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	DisplayImageOptions options;
	private ArrayList<String> lives = new ArrayList<String>();
	private ArrayList<String> videoIds = new ArrayList<String>();
	private View mRetryView;
	private TextView myTimer;
	private View contentLayout;
	private getMatchDetails mMatchDetails;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.matchdisplay);

		Intent intent = getIntent();
		match = intent.getParcelableExtra("match");

		mActionBar = getSupportActionBar();

		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);

		contentLayout = findViewById(R.id.contentLayout);

		fullscreenLoadingView = findViewById(R.id.fullscreen_loading_indicator);

		// imageLoader.init(ImageLoaderConfiguration.createDefault(activityContext));
		//
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.loading)
				.showImageForEmptyUri(R.drawable.loading)
				.showImageOnFail(R.drawable.loading).cacheInMemory(true)
				.cacheOnDisc(true).displayer(new RoundedBitmapDisplayer(20))
				.build();

		mActivity = this;

		// Get Retry view
		mRetryView = mActivity.findViewById(R.id.mRetry);

		// Set a listener for the button Retry
		mMatchDetails = new getMatchDetails(MyAsyncTask.INITTASK,
				contentLayout, fullscreenLoadingView, mRetryView);

		mMatchDetails.execute(match.getGosuLink());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, 0, 0, "Refresh")
				.setIcon(R.drawable.ic_refresh_inverse)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		getSupportMenuInflater().inflate(R.menu.share_action_provider, menu);

		MenuItem actionItem = menu
				.findItem(R.id.menu_item_share_action_provider_action_bar);
		ShareActionProvider actionProvider = (ShareActionProvider) actionItem
				.getActionProvider();
		actionProvider
				.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
		// Note that you can set/change the intent any time,
		// say when the user has selected an image.
		actionProvider.setShareIntent(createShareIntent());

		return true;

	}

	private Intent createShareIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		// shareIntent.setType("image/*");
		// Uri uri = Uri.fromFile(getFileStreamPath("shared.png"));
		// shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
		shareIntent.setAction(Intent.ACTION_SEND);

		if (match.getMatchStatus() == Match.NOTSTARTED) {
			shareIntent.putExtra(Intent.EXTRA_TEXT,
					match.getTeamName1() + " vs " + match.getTeamName2()
							+ " will start " + match.getTime()
							+ " later");
		} else if (match.getMatchStatus() == Match.LIVE) {
			shareIntent.putExtra(Intent.EXTRA_TEXT, match.getTeamName1()
					+ " vs " + match.getTeamName2()
					+ " is live now");
		} else if (match.getMatchStatus() == Match.ENDED){
			shareIntent.putExtra(Intent.EXTRA_TEXT, match.getTeamName1() + " "
					+ match.getScore() + " " + match.getTeamName2());

		}
		shareIntent.setType("text/plain");
		return shareIntent;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			finish();
		}

		if (item.getItemId() == 0) {
			refreshActivity();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);

		if (match.getMatchStatus() == Match.ENDED) {
			Intent i = new Intent(this, YoutubeActionBarActivity.class);
			// Toast.makeText(this, videoIds.get(position), Toast.LENGTH_SHORT)
			// .show();

			i.putExtra("isfullscreen", true);
			i.putExtra("videoId", videoIds.get(position));
			startActivity(i);
		} else {
			if (check()) {
				Intent i = new Intent(this, TwitchPlayer.class);
				i.putExtra("video", lives.get(position));
				startActivity(i);
			} else {
				Intent i = new Intent(this, FlashInstallerActivity.class);
				startActivity(i);
			}
		}

	}

	public void initMatchView() {

	}

	private class getMatchDetails extends MyAsyncTask {

		public getMatchDetails(int type, View contentView, View loadingView,
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

					mMatchDetails = new getMatchDetails(type, contentView,
							loadingView, retryView);

					mMatchDetails.execute(match.getGosuLink());

				}
			});

		}

		@Override
		protected void onPostExecute(String result) {
			// Checking network status first
			// Log.d("AsyncDebug", "Into onPostExecute!");

			if (!taskCancel && result != null) {

				Document doc = Jsoup.parse(result);

				Element opp_1 = null;

				opp_1 = doc.select("div.opponent").first();

				if (opp_1 != null) {

					Element opp_2 = doc.select("div.opponent").get(1);

					Element scoreDiv_1 = doc.select("div[class=center-column]")
							.first().select("div").get(1);

					Element scoreDiv_2 = doc.select("div[class=center-column]")
							.first().select("div").get(2);

					Element details = doc.select("table#match-details").first();

					Elements flash = doc.select("object");

					Elements videos = doc.select("div[class^=video]");

					if (match.getMatchStatus() != Match.ENDED) {
						if (!flash.isEmpty())
							for (Element f : flash) {
								if (!flash.isEmpty()) {
									String data = f.attr("data");
									String mData = data.substring(
											data.indexOf("=") + 1,
											data.length());
									// String pattern = "(.*?)=(.*?)";
									// data.replace(pattern, "$2");
									lives.add(mData);
								}
							}
					} else {
						if (!videos.isEmpty()) {
							for (Element v : videos) {
								String imgurl = v.select("img").first()
										.attr("src");
								String title = v.select("a").first()
										.attr("data-dialog-title");
								String mImgurl = imgurl.replaceAll(
										"https://i1.ytimg.com/vi/(.*?)/(.*)",
										"$1");
								// System.out.println("url: "+ mImgurl);
								lives.add(title);
								videoIds.add(mImgurl);
							}
						}

					}

					myTimer = (TextView) findViewById(R.id.myTimer);

					ImageView icon_1 = (ImageView) findViewById(R.id.icon1);
					ImageView icon_2 = (ImageView) findViewById(R.id.icon2);

					TextView teamName_1 = (TextView) findViewById(R.id.tName1);
					TextView teamName_2 = (TextView) findViewById(R.id.tName2);

					TextView team1score = (TextView) findViewById(R.id.team1score);
					TextView team2score = (TextView) findViewById(R.id.team2score);

					TextView tournamentName = (TextView) findViewById(R.id.tournamentName);
					TextView format = (TextView) findViewById(R.id.format);
					TextView startTime = (TextView) findViewById(R.id.startTime);
					TextView liveLabel = (TextView) findViewById(R.id.livelabel);
					TextView noLive = (TextView) findViewById(R.id.nolive);
					TextView liveStatus = (TextView) findViewById(R.id.liveStatus);

					teamName_1.setText(opp_1.select("a").first().text().trim());
					teamName_2.setText(opp_2.select("a").first().text().trim());

					if (scoreDiv_1.className().trim().endsWith("winner")) {
						team1score.setTextColor(Color.RED);
					}
					team1score.setText(scoreDiv_1.text().trim());

					if (scoreDiv_2.className().trim().endsWith("winner")) {
						team2score.setTextColor(Color.RED);
					}
					team2score.setText(scoreDiv_2.text().trim());

					imageLoader.displayImage(baseUrl
							+ opp_1.select("img").first().attr("src"), icon_1,
							options, animateFirstListener);

					imageLoader.displayImage(baseUrl
							+ opp_2.select("img").first().attr("src"), icon_2,
							options, animateFirstListener);

					Elements detailTd = details.select("td");

					if (detailTd.size() == 4) {

						tournamentName.setText("Tournament: "
								+ detailTd.get(2).text().trim());
						format.setText("Format: Best of "
								+ detailTd.get(3).text().trim());
						String dateInString = detailTd.first().text().trim();
						startTime.setText("Start Time: "
								+ processDate(dateInString) + " (Your place)");

					} else {
						tournamentName.setText("Tournament: "
								+ detailTd.get(1).text().trim());
						format.setText("Format: Best of "
								+ detailTd.get(2).text().trim());
						startTime.setText("Start Time: ");
					}

					if (match.getMatchStatus() == Match.LIVE) {
						liveStatus.setVisibility(View.VISIBLE);
					} else if (match.getMatchStatus() == Match.NOTSTARTED) {
						myTimer.setText(match.getTime());
						myTimer.setVisibility(View.VISIBLE);
					}

					if (match.getMatchStatus() == Match.ENDED) {
						noLive.setText("No Videos Released Yet.");
						liveLabel.setText("VODs");
					}

					if (lives.isEmpty()) {
						liveLabel.setVisibility(View.GONE);
						noLive.setVisibility(View.VISIBLE);
					} else {

						ArrayAdapter<String> adapter = new ArrayAdapter<String>(
								mActivity, android.R.layout.simple_list_item_1,
								lives);

						setListAdapter(adapter);
					}

					DisplayView(contentView, retryView, loadingView);

				} else {
					handleCancelView();
				}

			} else {

				handleCancelView();

			}
		}

		@Override
		protected void onCancelled() {
			// Notify the loading more operation has finished
			// Log.d("AsyncDebug", "Into OnCancelled!");
			handleCancelView();
		}

	}

	@SuppressLint("SimpleDateFormat")
	public String processDate(String s) {

		Date date = new Date();
		// Calendar c = new Calendar();
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy 'at' HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("CET"));

		try {
			date = sdf.parse(s);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

		sdf = new SimpleDateFormat("MMMM d 'at' hh:mm a");
		sdf.setTimeZone(TimeZone.getDefault());
		// date.setHours(Integer.parseInt(hourInString));
		return sdf.format(date);

	}

	public void refreshActivity() {
		// Destroy current activity
		finish();

		Toast.makeText(mActivity, "Refreshing", Toast.LENGTH_SHORT).show();

		// Start a new activity
		Intent i = new Intent(mActivity, MatchDetailsActivity.class);
		i.putExtra("match", match);
		startActivity(i);

	}

	// set a listener for "Retry" button

	public void hideAllViews() {
		if (fullscreenLoadingView != null)
			fullscreenLoadingView.setVisibility(View.GONE);
		if (contentLayout != null)
			contentLayout.setVisibility(View.GONE);
		if (mRetryView != null)
			mRetryView.setVisibility(View.GONE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Log.d("UniversalImageLoader", "clear from MatchDetail");
		imageLoader.clearDiscCache();
		imageLoader.clearMemoryCache();

		// check the state of the task
		cancelAllTask();
		hideAllViews();

	}

	public void cancelAllTask() {

		if (mMatchDetails != null
				&& mMatchDetails.getStatus() == Status.RUNNING) {
			mMatchDetails.cancel(true);

			// Log.d("AsyncDebug", "Task cancelled!!!!!!!!");
		} else {
			// Log.d("AsyncDebug", "Task cancellation failed!!!!");
		}
	}

	private boolean check() {
		PackageManager pm = getPackageManager();
		List<PackageInfo> infoList = pm
				.getInstalledPackages(PackageManager.GET_SERVICES);
		for (PackageInfo info : infoList) {
			if ("com.adobe.flashplayer".equals(info.packageName)) {
				return true;
			}
		}
		return false;
	}

}
