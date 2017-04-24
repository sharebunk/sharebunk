package custom;

/**
 * Created by HighStrit on 19/04/2017.
 */
public class Song {
    private long id;
    private String title;
    private String artist;
    //constructor to instantiate the instance variables
    public Song(long songID, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }
    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
}
