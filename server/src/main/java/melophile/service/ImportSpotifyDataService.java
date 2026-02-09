package melophile.service;

import melophile.model.PlaylistOwn;
import melophile.model.Song;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetSeveralTracksRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.util.*;

@Service
public class ImportSpotifyDataService {

    @Autowired
    SpotifyAuthenticationService spotifyAuthenticationService;

    //method to return the spotify id of a spotify object, given the spotify url for the object
    public static String extractIdFromUrl(String objectUrl) {

        //possible url format one: https://open.spotify.com/{object-type}/{spotify-id}?si={referral-information}
        if (objectUrl.contains("?si=")) {
            String objectTypeAndId = StringUtils.substringBetween(objectUrl, "https://open.spotify.com/", "?");
            String objectId = StringUtils.substringAfter(objectTypeAndId, "/");
            return objectId;
        }
        //possible url format two: https://open.spotify.com/{object-type}/{spotify-id}
        else {
            String objectTypeAndId = StringUtils.substringAfter(objectUrl, "https://open.spotify.com/");
            String objectId = StringUtils.substringAfter(objectTypeAndId, "/");
            return objectId;
        }

    }

    public Song convertSpotifyTrackToSong(String trackId) {
        try {

            GetTrackRequest getTrackRequest = spotifyAuthenticationService.getSpotifyApi().getTrack(trackId).build();
            GetAudioFeaturesForTrackRequest getAudioFeaturesForTrackRequest = spotifyAuthenticationService
                    .getSpotifyApi()
                    .getAudioFeaturesForTrack(trackId)
                    .build();

            Track track = getTrackRequest.execute();
            AudioFeatures audioFeatures = getAudioFeaturesForTrackRequest.execute();

            Song song = mapSpotifyTrackToSong(track, audioFeatures);

            return song;

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public Set<Song> convertSeveralSpotifyTracksToSongs(String[] trackIds) {
        try {

            GetSeveralTracksRequest getSeveralTracksRequest = spotifyAuthenticationService.getSpotifyApi().getSeveralTracks(trackIds).build();
            GetAudioFeaturesForSeveralTracksRequest getAudioFeaturesForSeveralTracksRequest = spotifyAuthenticationService
                    .getSpotifyApi()
                    .getAudioFeaturesForSeveralTracks(trackIds)
                    .build();

            Track[] tracks = getSeveralTracksRequest.execute();
            AudioFeatures[] audioFeatures = getAudioFeaturesForSeveralTracksRequest.execute();
            Set<Song> songs = new HashSet<>();

            //iterate through all tracks, creating new Song object and adding it to songs
            for (int i = 0; i < tracks.length; i++) {

                //only process non-null tracks
                if(tracks[i]!=null){
                    songs.add(mapSpotifyTrackToSong(tracks[i], audioFeatures[i]));
                }

            }

            return songs;

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    //return a Song object, given a Spotify Track and track's AudioFeatures
    private Song mapSpotifyTrackToSong(Track track, AudioFeatures audioFeatures) {

        //build new Song object, using necessary data retrieved from its Track and AudioAnalysis record in Spotify
        Song song = new Song();
        song.setName(track.getName());
        song.setArtist(track.getArtists()[0].getName());
        song.setSpotifyId(track.getId());
        song.setExternalUrl(track.getExternalUrls().get("spotify"));
        song.setPopularity(track.getPopularity());
        song.setAcousticness(audioFeatures.getAcousticness());
        song.setDanceability(audioFeatures.getDanceability());
        song.setEnergy(audioFeatures.getEnergy());
        song.setInstrumentalness(audioFeatures.getInstrumentalness());
        song.setKey(audioFeatures.getKey());
        song.setLiveness(audioFeatures.getLiveness());
        song.setLoudness(audioFeatures.getLoudness());
        song.setMode(audioFeatures.getMode().getType());
        song.setSpeechiness(audioFeatures.getSpeechiness());
        song.setTempo(audioFeatures.getTempo());
        song.setTimeSignature(audioFeatures.getTimeSignature());
        song.setValence(audioFeatures.getValence());

        return song;

    }

    public PlaylistOwn convertSpotifyPlaylistToPlaylistOwn(String playlistId) {
        try {
            //retrieve the spotify playlist object
            // ! NOTE: the list of songs returned by the PlaylistRequest object is (by default) limited to 100 - trying to import a playlist of more than 100 songs will work, but only 100 songs from the playlist will be imported (likely in an order different than what the spotify playlist sorts them as). For best practice, only import playlists under 100 tracks in length
            GetPlaylistRequest getPlaylistRequest = spotifyAuthenticationService.getSpotifyApi()
                    .getPlaylist(playlistId)
                    .build();
            Playlist playlist = getPlaylistRequest.execute();

            //init new PlaylistOwn object
            PlaylistOwn playlistOwn = new PlaylistOwn();

            //get an array of trackIds from the spotify playlist
            Track[] tracks = Arrays.stream(playlist.getTracks().getItems()).map(PlaylistTrack::getTrack).toArray(Track[]::new);
            String[] trackIds = Arrays.stream(tracks).map(Track::getId).toArray(String[]::new);

            //partition trackIds in groups of 50, because the getSeveralTracksRequest limits the trackIds parameter to size 50
            TreeMap<Integer, String[]> partitionedTracksIds = new TreeMap<>();
            Set<Song> songs = new HashSet<>();
            int partitions = (int) Math.ceil((double) trackIds.length / 50);

            for (int i = 0; i < partitions; i++) {
                partitionedTracksIds.put(i, Arrays.copyOfRange(trackIds, i * 50, (i + 1) * 50));
            }

            for (Map.Entry<Integer, String[]> entry : partitionedTracksIds.entrySet()) {
                Set<Song> partition = convertSeveralSpotifyTracksToSongs(entry.getValue());
                if (partition != null) {
                    songs.addAll(partition);
                }
            }

            //set necessary attributes for playlistOwn
            playlistOwn.setName(playlist.getName());
            playlistOwn.setSongs(songs);

            return playlistOwn;

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        } catch (Exception exception) {
            System.out.println("Error: " + exception);
            return null;
        }
    }
}
