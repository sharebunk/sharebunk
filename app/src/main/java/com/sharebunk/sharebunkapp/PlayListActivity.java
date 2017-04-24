package com.sharebunk.sharebunkapp;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import custom.Song;

public class PlayListActivity extends ListActivity  implements  OnCompletionListener {
	// Songs list
	private ArrayList<Song> songList;
	private ListView songView;

	private ImageButton btnPlay;
	//    private ImageButton btnForward;
//    private ImageButton btnBackward;
	private ImageButton btnNext;
	private ImageButton btnPrevious;
	private ImageButton btnPlaylist;
	private ImageButton btnRepeat;
	//    private ImageButton btnShuffle;
//    private SeekBar songProgressBar;
	private Button swipeTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	// Media Player
	private  MediaPlayer mp;
	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();;
	private SongsManager songManager;
	private Utilities utils;
	private int seekForwardTime = 5000; // 5000 milliseconds
	private int seekBackwardTime = 5000; // 5000 milliseconds
	private int currentSongIndex = 0;
	private boolean isShuffle = false;
	private boolean isRepeat = false;
	private Button pop, party, rap, swipeButton;

	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	private int songIndex = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist);

		final ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();
		getPlayList();
		//sort music
		Collections.sort(songsList, new Comparator<HashMap<String, String>>(){
			public int compare(HashMap<String, String> a, HashMap<String, String>  b){
				return a.get("songTitle").compareTo(b.get("songTitle"));
			}
		});
		// looping through playlist
		for (int i = 0; i < songsList.size(); i++) {
			// creating new HashMap
			HashMap<String, String> song = songsList.get(i);
			// adding HashList to ArrayList
			songsListData.add(song);
		}
		// Adding menuItems to ListView
		ListAdapter adapter = new SimpleAdapter(this, songsListData,
				R.layout.playlist_item, new String[] { "songTitle", "songArtist" }, new int[] {
				R.id.songTitle, R.id.songArtist });

		setListAdapter(adapter);

		// selecting single ListView item
		ListView lv = getListView();
		// listening to single listitem click
		//initialize songIndex
		songIndex = 0;
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				// getting listitem index
				songIndex = position;
				// Starting new intent
				Intent in = new Intent(getApplicationContext(),
						MainActivity.class);
				playSong(songIndex);
			}
		});
// All player buttons
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
		swipeTitleLabel = (Button) findViewById(R.id.btnSwipe);
		swipeTitleLabel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(PlayListActivity.this, PlayerActivity.class);
				intent.putExtra("songIndex", String.format("%s",songIndex));
				startActivity(intent);
			}
		});

		// Mediaplayer
		mp = new MediaPlayer();
		utils = new Utilities();
		// Listeners
		mp.setOnCompletionListener(this); // Important
		// By default prepare first song for play
		playSong(currentSongIndex);
		if(mp != null){
			mp.pause();
			btnPlay.setImageResource(R.drawable.btn_play);
		}
		/**
		 * Play button click event
		 * plays a song and changes button to pause image
		 * pauses a song and changes button to play image
		 * */
		btnPlay.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				//play current song

				// check for already playing
				if(mp.isPlaying()){
					if(mp!=null){
						mp.pause();
						// Changing button image to play button
						btnPlay.setImageResource(R.drawable.btn_play);
					}
				}else{
					// Resume song
					if(mp!=null){
						mp.start();
						// Changing button image to pause button
						btnPlay.setImageResource(R.drawable.btn_pause);
					}
				}

			}
		});

		/**
		 * Button Click event for Play list click event
		 * Launches list activity which displays list of songs
		 * */
		btnPlaylist.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), MusicListActivity.class);
				startActivityForResult(i, 100);
			}
		});

	}

	/**
	 * Receiving song index from playlist view
	 * and play the song
	 * */
	@Override
	protected void onActivityResult(int requestCode,
									int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == 100){
			currentSongIndex = data.getExtras().getInt("songIndex");
			// play selected song
			playSong(currentSongIndex);
		}

	}
	/**
	 * Function to play a song
	 * @param songIndex - index of song
	 * */
	public void  playSong(int songIndex){
		// Play song
		try {
			mp.reset();
			mp.setDataSource(songsList.get(songIndex).get("songPath"));
			mp.prepare();
			mp.start();
			// Displaying Song title
			String songTitle = songsList.get(songIndex).get("songTitle");
			swipeTitleLabel.setText(songTitle);

			// Changing Button Image to pause image
			btnPlay.setImageResource(R.drawable.btn_pause);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * On Song Playing completed
	 * if repeat is ON play same song again
	 * if shuffle is ON play random song
	 * */
	@Override
	public void onCompletion(MediaPlayer arg0) {

		// check for repeat is ON or OFF
		if(isRepeat){
			// repeat is on play same song again
			playSong(currentSongIndex);
		} else if(isShuffle){
			// shuffle is on - play a random song
			Random rand = new Random();
			currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
			playSong(currentSongIndex);
		} else{
			// no repeat or shuffle ON - play next song
			if(currentSongIndex < (songsList.size() - 1)){
				playSong(currentSongIndex + 1);
				currentSongIndex = currentSongIndex + 1;
			}else{
				// play first song
				playSong(0);
				currentSongIndex = 0;
			}
		}
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		mp.release();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


	//get song list
	public ArrayList<HashMap<String, String>> getPlayList() {
		//retrieve song info
		ContentResolver musicResolver = getContentResolver();
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
			int dataColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DATA);
			//add songs to list
			do {
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				String songPath = musicCursor.getString(dataColumn);
				//songsList.add(new Song(thisId, thisTitle, thisArtist));
				HashMap<String, String> song = new HashMap<String, String>();
				song.put("songTitle", thisTitle);
				song.put("songArtist", thisArtist);
				song.put("songPath", songPath);

				// Adding each song to SongList
				songsList.add(song);
			}
			while (musicCursor.moveToNext());
		}
		return songsList;

	}
}
