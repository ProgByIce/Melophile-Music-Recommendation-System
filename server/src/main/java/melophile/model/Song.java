package melophile.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity //indicates that the class is a persistent Java class
@Table(name = "song")   //provides the table that maps this entity. Additionally, we set the table's name to "users"
public class Song {

    @Id //annotation for the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //annotation is used to define generation strategy for the primary key. GenerationType.IDENTITY assigns ids by a schema that you can explore in postgres database (in short, starts with id 1, and every next entity id is equal to (largest id + 1)
    private int id;

    @Column
    private String name;    //the name of the song

    @Column
    private String artist;  //the name of the artist performing the song

    @Column(unique = true)
    private String spotifyId;   //The Spotify ID for the song

    @Column
    private String externalUrl; //The url linking to the track's spotify page

    @ManyToMany(mappedBy = "songs")
    private Set<PlaylistOwn> playlistOwns;

    @Column
    private int popularity; //The popularity of the song; value between 0 (least popular) and 100 (most popular)

    @Column
    private float acousticness;

    @Column
    private float danceability;

    @Column
    private float energy;

    @Column
    private float instrumentalness;

    @Column
    private int key;

    @Column
    private float liveness;

    @Column
    private float loudness;

    @Column
    private int mode;

    @Column
    private float speechiness;

    @Column
    private float tempo;

    @Column
    private int timeSignature;

    @Column
    private float valence;

    //constructors
    public Song() {
    }

    //getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public Set<PlaylistOwn> getPlaylists() {
        return playlistOwns;
    }

    public int getPopularity() {
        return popularity;
    }

    public float getAcousticness() {
        return acousticness;
    }

    public float getDanceability() {
        return danceability;
    }

    public float getEnergy() {
        return energy;
    }

    public float getInstrumentalness() {
        return instrumentalness;
    }

    public int getKey() {
        return key;
    }

    public float getLiveness() {
        return liveness;
    }

    public float getLoudness() {
        return loudness;
    }

    public int getMode() {
        return mode;
    }

    public float getSpeechiness() {
        return speechiness;
    }

    public float getTempo() {
        return tempo;
    }

    public int getTimeSignature() {
        return timeSignature;
    }

    public float getValence() {
        return valence;
    }

    //setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public void setPlaylists(Set<PlaylistOwn> playlistOwns) {
        this.playlistOwns = playlistOwns;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public void setAcousticness(float acousticness) {
        this.acousticness = acousticness;
    }

    public void setDanceability(float danceability) {
        this.danceability = danceability;
    }

    public void setEnergy(float energy) {
        this.energy = energy;
    }

    public void setInstrumentalness(float instrumentalness) {
        this.instrumentalness = instrumentalness;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setLiveness(float liveness) {
        this.liveness = liveness;
    }

    public void setLoudness(float loudness) {
        this.loudness = loudness;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setSpeechiness(float speechiness) {
        this.speechiness = speechiness;
    }

    public void setTempo(float tempo) {
        this.tempo = tempo;
    }

    public void setTimeSignature(int timeSignature) {
        this.timeSignature = timeSignature;
    }

    public void setValence(float valence) {
        this.valence = valence;
    }

    //special

    //return the value of a specific feature, based on the name of the input feature
    public float getFeature(String featureName) {
        switch (featureName) {
            case "popularity":
                return this.getPopularity();
            case "acousticness":
                return this.getAcousticness();
            case "danceability":
                return this.getDanceability();
            case "energy":
                return this.getEnergy();
            case "instrumentalness":
                return this.getInstrumentalness();
            case "key":
                return this.getKey();
            case "liveness":
                return this.getLiveness();
            case "loudness":
                return this.getLoudness();
            case "mode":
                return this.getMode();
            case "speechiness":
                return this.getSpeechiness();
            case "tempo":
                return this.getTempo();
            case "time signature":
                return this.getTimeSignature();
            case "valence":
                return this.getValence();
            default:
                return -99;
        }
    }

    //return true if the other song has same spotify id as this song
    public boolean hasSameSpotifyId(Song other) {

        //check if either song is null or has missing spotifyId attribute
        if (this == null || other == null || this.getSpotifyId() == null || other.getSpotifyId() == null) {
            return false;
        }

        //compare the spotifyId attributes lexicographically
        if (this.getSpotifyId().compareTo(other.getSpotifyId()) == 0) {
            return true;
        }

        return false;
    }
}
