package com.sharebunk.sharebunkapp;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class SongsManager {
	// SDCard Path
	final String MEDIA_PATH = new String("/sdcard/");
	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	private final Activity context;
	// Constructor
	public SongsManager(Activity context){
		this.context = context;
	}
//
//	/**
//	 * Function to read all mp3 files from sdcard
//	 * and store the details in ArrayList
//	 * */
//	public ArrayList<HashMap<String, String>> getPlayList(){
//		File home = new File(MEDIA_PATH);
//
//		if (home.listFiles(new FileExtensionFilter()).length > 0) {
//			for (File file : home.listFiles(new FileExtensionFilter())) {
//				HashMap<String, String> song = new HashMap<String, String>();
//				song.put("songTitle", file.getName().substring(0, (file.getName().length() - 4)));
//				song.put("songPath", file.getPath());
//
//				// Adding each song to SongList
//				songsList.add(song);
//			}
//		}
//		// return songs list array
//		return songsList;
//	}

	public ArrayList<HashMap<String, String>> getPlayList() {
		//retrieve song info
		ContentResolver musicResolver = context.getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
		if(musicCursor!=null && musicCursor.moveToFirst()){
			//get columns
			int titleColumn = musicCursor.getColumnIndex
					(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex
					(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex
					(android.provider.MediaStore.Audio.Media.ARTIST);
			//add songs to list
			do {
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				//songsList.add(new Song(thisId, thisTitle, thisArtist));
				HashMap<String, String> song = new HashMap<String, String>();
				song.put("songTitle", thisTitle);
				song.put("songArtist", thisArtist);
				song.put("songPath", musicUri.toString());

				// Adding each song to SongList
				songsList.add(song);
			}
			while (musicCursor.moveToNext());
		}
		return songsList;

	}


	/**
	 * Class to filter files which are having .mp3 extension
	 * */
	class FileExtensionFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".mp3") || name.endsWith(".MP3"));
		}
	}
}
