package com.wwyz.loltv;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

//import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Handler;
//import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

@SuppressLint("HandlerLeak")
public class LoadMore_News extends LoadMore_Base implements
		SearchView.OnQueryTextListener {

	private ImageView[] imageViews = null;
	private ImageView imageView = null;
	private ViewPager advPager = null;
	private AtomicInteger what = new AtomicInteger(0);
	private boolean isContinue = true;
	private boolean isEnd = false;
	private ViewGroup group;
	private ArrayList<String> matches = new ArrayList<String>();
	private ArrayList<String> results = new ArrayList<String>();
	private Elements links = new Elements();
	boolean isPagerSet = false;
	private getMatchInfo mMatchInfo;

	private View pagerContent;
	private View pagerLoading;
	private View pagerRetry;
	private View listLoading;
	private View listRetry;
	private String url = "http://www.gosugamers.net/lol/gosubet";
	private int rand_1;
	private int rand_2;
	private AdvAdapter myAdvAdapter;

	private int position = 0;

	DisplayImageOptions options;

	private Random random;

	private SideMenuActivity sma;
	private final int[] myDrawables = new int[] { R.drawable.lol1,
			R.drawable.lol2, R.drawable.lol3, R.drawable.lol4,
			R.drawable.lol5, R.drawable.lol6, R.drawable.lol7,
			R.drawable.lol8, R.drawable.lol9, R.drawable.lol10,
			R.drawable.lol11, R.drawable.lol12, R.drawable.lol13,
			R.drawable.lol14, R.drawable.lol15, R.drawable.lol16,
			R.drawable.lol17, R.drawable.lol18, R.drawable.lol19,
			R.drawable.lol20, R.drawable.lol21 };

	private List<View> views = new ArrayList<View>();

	// private Thread myThread;

	@Override
	public void Initializing() {
		// Inflating view
		view = mInflater.inflate(R.layout.whatsnew, null);

		// Give a title for the action bar
		abTitle = "What's New";

		// Give API URLs
		API.add("https://gdata.youtube.com/feeds/api/users/ybJ_SpdJsJB5Ug8ibG6jWw/newsubscriptionvideos?max-results=10&alt=json");

		// set a feed manager
		feedManager = new FeedManager_Subscription();

		// Show menu
		setHasOptionsMenu(true);
		setOptionMenu(true, false);

		// Get sidemenuactivity
		sma = (SideMenuActivity) sfa;

		// if (!this.imageLoader.isInited()){
		// this.imageLoader.init(ImageLoaderConfiguration.createDefault(sfa));
		// }
		// // imageLoader=new ImageLoader(context.getApplicationContext());
		//
		// options = new DisplayImageOptions.Builder()
		// .showStubImage(R.drawable.loading)
		// .showImageForEmptyUri(R.drawable.loading)
		// .showImageOnFail(R.drawable.loading).cacheInMemory(true)
		// .cacheOnDisc(true)
		// // .displayer(new RoundedBitmapDisplayer(20))
		// .build();

		random = new Random();

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		SearchView searchView = new SearchView(sfa.getSupportActionBar()
				.getThemedContext());
		searchView.setQueryHint("Search Youtube");
		searchView.setOnQueryTextListener(this);

		menu.add(0,20,0,"Search")
				.setIcon(R.drawable.ic_search_inverse)
				.setActionView(searchView)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		menu.add(0,0,0,"Refresh")
				.setIcon(R.drawable.ic_refresh_inverse)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

	}

	@Override
	public void setListView() {

		pagerContent = sfa.findViewById(R.id.pageContent);
		pagerLoading = sfa.findViewById(R.id.pagerLoadingIndicator);
		pagerRetry = sfa.findViewById(R.id.pagerRetryView);
		listLoading = sfa.findViewById(R.id.listViewLoadingIndicator);
		listRetry = sfa.findViewById(R.id.ListViewRetryView);

		super.setListView();

	}

	private void setViewPager() {

		String[] matcharray = matches.toArray(new String[matches.size()]);
		String[] resultarray = results.toArray(new String[results.size()]);

		View v1 = new View(sfa);
		View v2 = new View(sfa);
		final LayoutInflater inflater = (LayoutInflater) sfa
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		v1 = inflater.inflate(R.layout.livetext, null, false);
		v1.setBackgroundResource(myDrawables[rand_1]);

		TextView liveTitle = (TextView) v1.findViewById(R.id.livetitle);
		TextView liveMatch1 = (TextView) v1.findViewById(R.id.lineup1);
		TextView liveMatch2 = (TextView) v1.findViewById(R.id.lineup2);
		TextView liveMatch3 = (TextView) v1.findViewById(R.id.lineup3);
		TextView live1 = (TextView) v1.findViewById(R.id.live1);
		TextView live2 = (TextView) v1.findViewById(R.id.live2);
		TextView live3 = (TextView) v1.findViewById(R.id.live3);

		liveTitle.setText("Upcoming Matches");

		if (matcharray.length >= 1) {
			if (matcharray[0].endsWith("Live")) {
				liveMatch1.setText(matcharray[0].substring(0,
						matcharray[0].length() - 4));
			} else {
				liveMatch1.setText(matcharray[0]);
				live1.setVisibility(View.GONE);
			}
		} else {
			liveMatch1.setVisibility(View.GONE);
			live1.setVisibility(View.GONE);
		}
		// System.out.println(matcharray[0]);

		if (matcharray.length >= 2) {
			if (matcharray[1].endsWith("Live")) {
				liveMatch2.setText(matcharray[1].substring(0,
						matcharray[1].length() - 4));
			} else {
				liveMatch2.setText(matcharray[1]);
				live2.setVisibility(View.GONE);
			}
		} else {
			liveMatch2.setVisibility(View.GONE);
			live2.setVisibility(View.GONE);
		}

		if (matcharray.length >= 3) {
			if (matcharray[2].endsWith("Live")) {
				liveMatch3.setText(matcharray[2].substring(0,
						matcharray[2].length() - 4));
			} else {
				liveMatch3.setText(matcharray[2]);
				live3.setVisibility(View.GONE);
			}
		} else {
			liveMatch3.setVisibility(View.GONE);
			live3.setVisibility(View.GONE);
		}

		v1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Set the drawer indicator in position "Upcoming Matches"
				sma.setDrawerIndicator(8);

				// Replacing the current fragment
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.replace(R.id.content_frame, new LoadMore_UpcomingMatch());
				ft.commit();
			}
		});

		views.add(v1);

		v2 = inflater.inflate(R.layout.livetext, null, false);
		v2.setBackgroundResource(myDrawables[rand_2]);

		liveTitle = (TextView) v2.findViewById(R.id.livetitle);
		liveMatch1 = (TextView) v2.findViewById(R.id.lineup1);
		liveMatch2 = (TextView) v2.findViewById(R.id.lineup2);
		liveMatch3 = (TextView) v2.findViewById(R.id.lineup3);
		live1 = (TextView) v2.findViewById(R.id.live1);
		live2 = (TextView) v2.findViewById(R.id.live2);
		live3 = (TextView) v2.findViewById(R.id.live3);

		liveTitle.setText("Resent Results");

		if (resultarray.length >= 1) {
			liveMatch1.setText(resultarray[0]);
		} else {
			liveMatch1.setVisibility(View.GONE);
		}
		live1.setVisibility(View.GONE);

		if (resultarray.length >= 2) {
			liveMatch2.setText(resultarray[1]);
		} else {
			liveMatch2.setVisibility(View.GONE);
		}
		live2.setVisibility(View.GONE);

		if (resultarray.length >= 3) {
			liveMatch3.setText(resultarray[2]);
		} else {
			liveMatch3.setVisibility(View.GONE);
		}
		live3.setVisibility(View.GONE);

		v2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// Set the drawer indicator in position "Recent Result"
				sma.setDrawerIndicator(9);

				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.replace(R.id.content_frame, new LoadMore_Result());
				ft.commit();
			}
		});

		views.add(v2);

	}

	private void initViewPager() {

		rand_1 = random.nextInt(myDrawables.length - 1);
		// System.out.println("New random:"+ rand_1);
		do {
			rand_2 = random.nextInt(myDrawables.length - 1);
		} while (rand_1 == rand_2);

		if (!isPagerSet) {
			advPager = (ViewPager) sfa.findViewById(R.id.adv_pager);
			group = (ViewGroup) sfa.findViewById(R.id.viewGroup);
		} else {
			views.clear();
		}

		setViewPager();

		if (!isPagerSet) {

			imageViews = new ImageView[views.size()];
			for (int i = 0; i < views.size(); i++) {
				imageView = new ImageView(sfa);
				imageView.setLayoutParams(new LayoutParams(20, 20));
				imageView.setPadding(5, 5, 5, 5);
				imageViews[i] = imageView;
				if (i == 0) {
					imageViews[i].setBackgroundResource(R.drawable.d2_selected);
				} else {
					imageViews[i]
							.setBackgroundResource(R.drawable.d2_unselected);
				}
				group.addView(imageViews[i]);
			}

			advPager.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_MOVE:
						isContinue = false;
						break;
					case MotionEvent.ACTION_UP:
						isContinue = true;
						break;
					default:
						isContinue = true;
						break;
					}
					return false;
				}
			});

			handler.postDelayed(runnable, 10000);

			// myThread = new Thread(new Runnable() {
			//
			// @Override
			// public void run() {
			// while (true) {
			// if (myThread.isInterrupted()) break;
			// if (isContinue) {
			// viewHandler.sendEmptyMessage(what.get());
			// whatOption();
			// }
			// }
			// }
			// });
			//
			// myThread.start();

			isPagerSet = true;
		}

		myAdvAdapter = new AdvAdapter();

		advPager.setAdapter(myAdvAdapter);

		advPager.setOnPageChangeListener(new GuidePageChangeListener());

		advPager.setCurrentItem(0);

		imageViews[0].setBackgroundResource(R.drawable.d2_selected);
		imageViews[1].setBackgroundResource(R.drawable.d2_unselected);

	}

	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		public void run() {
			if (position == 0) {
				position = 1;
			} else {
				position = 0;
			}
			advPager.setCurrentItem(position, true);
			// refreshFragment();
			handler.postDelayed(runnable, 10000);
		}
	};

	// private void whatOption() {
	// what.incrementAndGet();
	// if (what.get() > imageViews.length - 1) {
	// what.getAndAdd(-4);
	// }
	// try {
	// Thread.sleep(5000);
	// } catch (InterruptedException e) {
	//
	// }
	// }
	//
	// private final Handler viewHandler = new Handler() {
	//
	// @Override
	// public void handleMessage(Message msg) {
	// advPager.setCurrentItem(msg.what);
	// super.handleMessage(msg);
	// }
	//
	// };

	private final class GuidePageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int arg0) {
			what.getAndSet(arg0);
			for (int i = 0; i < imageViews.length; i++) {
				imageViews[arg0].setBackgroundResource(R.drawable.d2_selected);
				if (arg0 != i) {
					imageViews[i]
							.setBackgroundResource(R.drawable.d2_unselected);
				}
			}

		}

	}

	private final class AdvAdapter extends PagerAdapter {
		// private List<View> views = null;

		public AdvAdapter() {
			// this.views = views;
		}

		@Override
		public void destroyItem(ViewGroup collection, int position, Object view) {
			// ((ViewPager) arg0).removeView(views.get(arg1));
			collection.removeView((View) view);
			view = null;
		}

		@Override
		public void finishUpdate(View arg0) {

		}

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public Object instantiateItem(ViewGroup collection, int position) {

			collection.addView(views.get(position), 0);
			return views.get(position);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}

	}

	private class getMatchInfo extends MyAsyncTask {

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

					mMatchInfo = (getMatchInfo) new getMatchInfo(type,
							contentView, loadingView, retryView);
					mMatchInfo.execute(url);
				}
			});

		}

		@Override
		protected void onPostExecute(String result) {

			// Log.d("AsyncDebug", "Into onPostExecute!");

			if (!taskCancel && result != null) {
				// Do anything with response..
				Document doc = Jsoup.parse(result);
				links = doc.select("tr:has(td.opp)");

				if (!links.isEmpty()) {

					for (Element link : links) {

						String match;

						match = link.select("span").first().text().trim()
								+ " vs "
								+ link.select("span").get(2).text().trim()
								+ " ";
						if (link.getElementsByClass("results").isEmpty()) {
							match += link.select("td").get(3).text().trim();
							matches.add(match);
						} else {
							match += link.select("span.hidden").first().text()
									.trim();
							results.add(match);
						}
					}

					initViewPager();

					DisplayView(contentView, retryView, loadingView);
				} else {
					handleCancelView();
				}
			} else {
				handleCancelView();
			}

		}

	}

	// class LoadMoreTask_News extends LoadMoreTask {
	//
	// public LoadMoreTask_News(int type, View contentView, View loadingView,
	// View retryView) {
	// super(type, contentView, loadingView, retryView);
	// // TODO Auto-generated constructor stub
	// }
	//
	// @Override
	// protected void onPostExecute(String result) {
	// // Do anything with response..
	// // System.out.println(result);
	// //Log.d("AsyncDebug", "Into onPostExecute!");
	//
	// if (!taskCancel && result != null) {
	//
	// feedManager.setmJSON(result);
	//
	// List<Video> newVideos = feedManager.getVideoPlaylist();
	//
	// // adding new loaded videos to our current video list
	// for (Video v : newVideos) {
	// //System.out.println("new id: " + v.getVideoId());
	// if (needFilter) {
	// filtering(v);
	// // System.out.println("need filter!");
	// } else {
	// titles.add(v.getTitle());
	// videolist.add(v);
	// }
	// }
	// try {
	// // put the next API in the first place of the array
	// API.add(feedManager.getNextApi());
	// // nextAPI = feedManager.getNextApi();
	// if (API.get(API.size()-1) == null) {
	// // No more videos left
	// isMoreVideos = false;
	// }
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// //e.printStackTrace();
	// }
	// vaa.notifyDataSetChanged();
	//
	// ((LoadMoreListView) myLoadMoreListView).onLoadMoreComplete();
	//
	// DisplayView(contentView, retryView, loadingView);
	//
	// if (!isMoreVideos) {
	// ((LoadMoreListView) myLoadMoreListView).onNoMoreItems();
	//
	// myLoadMoreListView.setOnLoadMoreListener(null);
	// }
	//
	// } else {
	// handleCancelView();
	// }
	// }
	//
	// }

	@Override
	protected void doRequest() {
		// TODO Auto-generated method stub
		for (String s : API) {
			LoadMoreTask newTask = new LoadMoreTask(LoadMoreTask.INITTASK,
					myLoadMoreListView, listLoading, listRetry);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, s);
			} else {
				newTask.execute(s);
			}

			mLoadMoreTasks.add(newTask);

		}

		mMatchInfo = new getMatchInfo(getMatchInfo.INITTASK, pagerContent,
				pagerLoading, pagerRetry);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mMatchInfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
		} else {
			mMatchInfo.execute(url);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onDestroy() {

		super.onDestroy();

		views.clear();
		// advPager.removeAllViews();
		// group.removeAllViews();
		// advPager = null;
		isEnd = true;
		// group = null;
		// myThread.interrupt();

		if (handler != null) {
			handler.removeCallbacks(runnable);
		}

		if (mMatchInfo != null && mMatchInfo.getStatus() == Status.RUNNING)
			mMatchInfo.cancel(true);

	}

	@Override
	public boolean onQueryTextSubmit(String query) {
//		Toast.makeText(sfa, "You searched for: " + query, Toast.LENGTH_LONG)
//				.show();
		
		// starting search activity
		Intent intent = new Intent(sfa, LoadMore_Activity_Search_Youtube.class);
		intent.putExtra("query", query);
		startActivity(intent);
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		return false;
	}

}
