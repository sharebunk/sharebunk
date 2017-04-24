package com.sharebunk.sharebunkapp;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class MusicListActivity extends ListActivity  implements  MediaPlayer.OnCompletionListener {
    // Songs list
    public ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private  MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();;
    private SongsManager songManager;
    private Utilities utils;
    private int currentSongIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();
        // Mediaplayer
        mp = new MediaPlayer();
        //get play list
        getPlayList();
        Collections.sort(songsList, new Comparator<HashMap<String, String>>(){
            public int compare(HashMap<String, String> a, HashMap<String, String>  b){
                return a.get("songTitle").compareTo(b.get("songTitle"));
            }
        });
        utils = new Utilities();
        // Listeners
        //songProgressBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important
        // looping through playlist
        for (int i = 0; i < songsList.size(); i++) {
            // creating new HashMap
            HashMap<String, String> song = songsList.get(i);

            // adding HashList to ArrayList
            songsListData.add(song);
        }

        // Adding menuItems to ListView
        ListAdapter adapter = new SimpleAdapter(this, songsListData,
                R.layout.playlist_item, new String[] { "songTitle", "songArtist", "songDuration" }, new int[] {
                R.id.songTitle, R.id.songArtist, R.id.songDuration });

        setListAdapter(adapter);
        //get instance of utilities
        utils = new Utilities();

        // selecting single ListView item
        ListView lv = getListView();
        // listening to single listitem click
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting listitem index

               // view.setBackgroundColor(ResourcesCompat.getColor(getResources(),R.color.colorAccent, null));
                // Starting new intent
//                Intent in = new Intent(getApplicationContext(),
//                        MainActivity.class);
                // Sending songIndex to PlayerActivity
//                in.putExtra("songIndex", songIndex);
                int songIndex = position;
                playSong(songIndex);
//                setResult(100, in);
//                // Closing PlayListView
//                finish();
            }
        });

    }

    public ArrayList<HashMap<String, String>> getPlayList() {
//retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int dataColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int dataDuration= musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            //add songs to list
            do {
                utils = new Utilities();
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String songPath = musicCursor.getString(dataColumn);
                long songDuration = musicCursor.getLong(dataDuration);
                String duration = utils.milliSecondsToTimer(songDuration);

                //songsList.add(new Song(thisId, thisTitle, thisArtist));
                HashMap<String, String> song = new HashMap<String, String>();
                song.put("songTitle", thisTitle);
                song.put("songArtist", thisArtist);
                song.put("songPath", songPath);
                song.put("songDuration",duration );


                // Adding each song to SongList
                songsList.add(song);
            }
            while (musicCursor.moveToNext());
        }
        return songsList;

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
