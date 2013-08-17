package com.wwyz.loltv.feedManager;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wwyz.loltv.data.Video;

public class FeedManager_Search_Youtube extends FeedManager_Base{
	@Override
	public ArrayList<Video> getVideoPlaylist() {
		try {
			if (mJSON != null)
				processJSON(mJSON);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

		ArrayList<Video> videos = new ArrayList<Video>();

		try {
			// System.out.println(plTitle);
			// get the playlist
			JSONArray playlist = feed.getJSONArray("entry");
			// System.out.println("Length: "+ playlist.length());

			for (int i = 0; i < playlist.length(); i++) {
				// get a video in the playlist
				JSONObject oneVideo = playlist.getJSONObject(i);
				// get the title of this video
				String videoTitle = oneVideo.getJSONObject("title").getString(
						"$t");
				String videoLink = null;
				String videoId = null;
				videoLink = oneVideo.getJSONObject("id").getString("$t");
				videoId = videoLink.substring(videoLink.indexOf("video:") + 6,
						videoLink.length());

				// System.out.println("Working 2: "+videoLink);
				String videoDesc = oneVideo.getJSONObject("media$group")
						.getJSONObject("media$description").getString("$t");
				String thumbUrl = oneVideo.getJSONObject("media$group")
						.getJSONArray("media$thumbnail").getJSONObject(2)
						.getString("url");
				String updateTime = oneVideo.getJSONObject("published")
						.getString("$t");
				// System.out.println("Working 4");
				String author = oneVideo.getJSONArray("author")
						.getJSONObject(0).getJSONObject("name").getString("$t");
				String vCount = oneVideo.getJSONObject("yt$statistics")
						.getString("viewCount") + " views";
				String inSecs = oneVideo.getJSONObject("media$group")
						.getJSONObject("yt$duration").getString("seconds");
				String convertedDuration = formatSecondsAsTime(inSecs) + " HD";

				updateTime = handleDate(updateTime);

				Video video = new Video();

				// System.out.println("converted duration: " +
				// convertedDuration);
				// System.out.println(videoDesc);
				// store title and link

				video.setTitle(videoTitle);
				video.setVideoId(videoId);
				video.setThumbnailUrl(thumbUrl);
				video.setVideoDesc(videoDesc);
				video.setUpdateTime(updateTime);
				video.setAuthor(author);
				video.setViewCount(vCount);
				video.setDuration(convertedDuration);
				// System.out.println(video.getTitle());
				// push it to the list
				videos.add(video);
				// System.out.println(videoTitle+"***"+videoLink);

			}

		} catch (JSONException ex) {

			//ex.printStackTrace();
		}

		return videos;
	}
}
