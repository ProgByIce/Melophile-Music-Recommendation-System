package melophile.controller;

import melophile.model.PlaylistOwn;
import melophile.model.Song;
import melophile.model.SpotifySongFeature;
import melophile.repository.PlaylistOwnRepository;
import melophile.repository.SongRepository;
import melophile.repository.SpotifySongFeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CommandLineController {

    @Autowired  //injects the repository, making it available for use
    SongRepository songRepository;
    @Autowired
    PlaylistOwnRepository playlistOwnRepository;
    @Autowired
    SpotifySongFeatureRepository spotifySongFeatureRepository;

    //save a song in the database
    public Song saveSongInDatabase(Song song) {
        try {

            Song saved = songRepository.save(song);
            System.out.println("Successfully created song: \"" + saved.getName() + "\" by " + saved.getArtist());
            return saved;

        } catch (DataIntegrityViolationException exception) {
            System.out.println("Failed to create: \"" + song.getName() + "\" by " + song.getArtist()  + ", as it already exists in the database.");
            return song;
        }
    }

    //iterate through all the songs associated with a playlist (ie, in the playlist's Set<Song> songs attribute) and save them in the database
    public void savePlaylistOwnSongsInDatabase(PlaylistOwn playlistOwn) {
        try {
            playlistOwn.getSongs().forEach(song -> {
                saveSongInDatabase(song);
            });
        } catch (Exception exception) {
            System.out.println("Error: " + exception);
        }
    }

    //save a playlist in the database
    public PlaylistOwn savePlaylistOwnInDatabase(PlaylistOwn playlistOwn) {
        try {
            PlaylistOwn saved = playlistOwnRepository.save(playlistOwn);
            System.out.println("Successfully created playlist: \"" + saved.getName() + "\"");
            return saved;
        } catch (DataIntegrityViolationException exception) {
            System.out.println("Failed to save playlist because the playlist contains a song that already exists in the database. More details: " + exception);
            return null;
        }catch (Exception exception) {
            System.out.println("Error: " + exception);
            return null;
        }
    }

    //return a list of all the spotify song features in the database
    public List<SpotifySongFeature> getAllSpotifySongFeatures() {
        try {
            return spotifySongFeatureRepository.findAll();
        } catch (Exception exception) {
            System.out.println("Error: " + exception);
            return null;
        }
    }

    //return a list of all the songs in the database
    public List<Song> getAllSongs() {
        try {
            return songRepository.findAll();
        } catch (Exception exception) {
            System.out.println("Error: " + exception);
            return null;
        }
    }

    //return a specific playlist, specified by its id
    public PlaylistOwn getPlaylistOwn(int id) {
        try {
            return playlistOwnRepository.findById(id);
        } catch (Exception exception) {
            System.out.println("Error: " + exception);
            return null;
        }
    }

    //return a list of all playlists in the database
    public List<PlaylistOwn> getAllPlaylistOwns(){
        try {
            return playlistOwnRepository.findAll();
        } catch (Exception exception) {
            System.out.println("Error: " + exception);
            return null;
        }
    }

    //return a specific spotify song feature, specified by its id
    public SpotifySongFeature getSpotifySongFeature(int id) {
        try {
            return spotifySongFeatureRepository.findById(id);
        } catch (Exception exception) {
            System.out.println("Error: " + exception);
            return null;
        }
    }
}
