package com.wwyz.loltv;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;


public class Forum_official_na extends Forum_fragment {

	@Override
	public void Initializing() {
		// Give a title for the action bar
		abTitle = "Forums";

		// Give API URLs

		// initialize the fragments in the Menu
		FragmentAll = new Forum_reddit();
		FragmentUploader = new Forum_official_na();
//		FragmentPlaylist = new LoadMore_H_New_Playlist();

		// set a feed manager

		// Show menu		
		setHasOptionsMenu(true);
		setOptionMenu(true, true);

		currentPosition = 1;
		// Set retry button listener		

	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		if (getPhoneAndroidSDK() >= 14) {
			sfa.getWindow().setFlags(0x1000000, 0x1000000);
		}

		mWebView = (WebView) sfa.findViewById(R.id.forumView);
		WebSettings streamSettings = mWebView.getSettings();
		streamSettings.setJavaScriptEnabled(true);
		streamSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		streamSettings.setPluginState(PluginState.ON);
		streamSettings.setPluginsEnabled(true);
		streamSettings.setAllowFileAccess(true);
		streamSettings.setLoadWithOverviewMode(true);
		streamSettings.setUseWideViewPort(true);
		streamSettings.setBuiltInZoomControls(true);
		streamSettings.setSupportZoom(true);

		mWebView.setWebChromeClient(new WebChromeClient());
		mWebView.setWebViewClient(new WebViewClient());
		
		mWebView.loadUrl("http://forums.na.leagueoflegends.com/board/");


	}

}
