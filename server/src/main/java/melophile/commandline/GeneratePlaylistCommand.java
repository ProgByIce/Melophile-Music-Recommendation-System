package melophile.commandline;

import melophile.controller.CommandLineController;
import melophile.model.PlaylistOwn;
import melophile.model.Song;
import melophile.model.SpotifySongFeature;
import melophile.service.ImportSpotifyDataService;
import melophile.service.SongService;
import melophile.utility.CosineSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class GeneratePlaylistCommand {

    @Autowired
    DisplayCommand displayCommand;
    @Autowired
    CommandLineController commandLineController;
    @Autowired
    SongService songService;
    @Autowired
    CosineSimilarity cosineSimilarity;
    @Autowired
    ImportSpotifyDataService importSpotifyDataService;

    private static Scanner sc = new Scanner(System.in);
    private static String[] options = {"Based on Custom Features", "Based on Spotify Track", "Return to Previous Menu"};

    public void generatePlaylist() {

        int option = 1;

        while (option != options.length) {

            System.out.println("\n< GENERATE PLAYLIST >");
            displayCommand.displayOptions(options);

            try {
                option = Integer.parseInt(sc.next());

                switch (option) {
                    case 1: {
                        generateFromCustomFeatures();
                        break;
                    }
                    case 2: {
                        generateFromSong();
                        break;
                    }
                    case 3: {
                        break;
                    }
                    default: {
                        System.out.println("Invalid option - Please enter an integer value between 1 and " + options.length);
                        break;
                    }
                }
            } catch (InvalidDataAccessApiUsageException | NullPointerException exception) {
                System.out.println("Invalid Spotify URL - Please enter a valid Spotify object's URL");
            } catch (NumberFormatException exception) {
                System.out.println("Invalid input - Please enter an integer value between 1 and " + options.length);
            } catch (Exception exception) {
                System.out.println("Error: " + exception);
            }
        }
    }

    public void generateFromCustomFeatures() {

        System.out.println("\n< GENERATE BASED ON CUSTOM FEATURES >");
        System.out.println("----------------------------------------");
        System.out.println("In order to generate a playlist, you must provide the following parameters... ");
        System.out.println("(Enter \"exit\" to return to the previous menu)");

        List<SpotifySongFeature> spotifySongFeatures = commandLineController.getAllSpotifySongFeatures();

        String input = "";

        //get user input for the number of songs in the playlist
        int numSongs = 0;
        while (true) {
            try {
                System.out.print("\t> Number of songs in the playlist (int), range [20-100]: ");
                input = sc.next();

                if (input.equals("exit")) {
                    return;
                }

                numSongs = Integer.valueOf(input);

                if (numSongs >= 20 && numSongs <= 100) {
                    break;
                } else {
                    System.out.println("\t\t! Invalid range");
                }

            } catch (Exception exception) {
                System.out.println("\t\t! Invalid input");
            }
        }

        //get user input for the spotify song features of the playlist
        Song targetSong = new Song();
        for (SpotifySongFeature feature : spotifySongFeatures) {

            boolean flag = true;

            while (flag) {

                //prompt the user for input for each spotify song feature
                System.out.print("\t> " + feature.getName() + " (" + feature.getDataType() + "), range [" +
                        feature.getMinimum() + "-" + feature.getMaximum() + "]: ");

                try {
                    input = sc.next();

                    if (input.equals("exit")) {
                        return;
                    }

                    //handle input validation differently based on what feature is being processed
                    if (feature.isNormalized()) {   //if the feature is NORMALIZED...

                        //and the data type of the feature is INT...
                        if (feature.getDataType().equals("int")) {

                            int value = Integer.parseInt(input);

                            if (value < feature.getMinimum() || value > feature.getMaximum()) {
                                System.out.println("\t\t! Input is out of range");
                            } else {

                                //break out of the while loop, to get to the next iteration of the feature set
                                flag = false;

                                //Set the value for "Mode", since it is the only normalized feature of type int
                                targetSong.setMode(value);
                                break;
                            }

                        } else { //or the data type of the feature as FLOAT...

                            float value = Float.parseFloat(input);

                            if (value < feature.getMinimum() || value > feature.getMaximum()) {
                                System.out.println("\t\t! Input is out of range");
                            } else {

                                //break out of the while loop, to get to the next iteration of the feature set
                                flag = false;

                                //store the respective feature in (Song object) targetSong
                                switch (feature.getName()) {
                                    case "acousticness":
                                        targetSong.setAcousticness(value);
                                        break;
                                    case "danceability":
                                        targetSong.setDanceability(value);
                                        break;
                                    case "energy":
                                        targetSong.setEnergy(value);
                                        break;
                                    case "instrumentalness":
                                        targetSong.setInstrumentalness(value);
                                        break;
                                    case "liveness":
                                        targetSong.setLiveness(value);
                                        break;
                                    case "speechiness":
                                        targetSong.setSpeechiness(value);
                                        break;
                                    case "valence":
                                        targetSong.setValence(value);
                                        break;
                                }
                            }

                        }
                    } else {    //if the feature is NOT NORMALIZED...

                        //and the data type of the feature is INT...
                        if (feature.getDataType().equals("int")) {

                            int value = Integer.parseInt(input);

                            if (value < feature.getMinimum() || value > feature.getMaximum()) {
                                System.out.println("\t\t! Input is out of range");
                            } else {

                                //break out of the while loop, to get to the next iteration of the feature set
                                flag = false;

                                //store the respective feature in (Song object) targetSong
                                switch (feature.getName()) {
                                    case "popularity":
                                        targetSong.setPopularity(value);
                                        break;
                                    case "key":
                                        targetSong.setKey(value);
                                        break;
                                    case "time signature":
                                        targetSong.setTimeSignature(value);
                                        break;
                                }
                            }
                        } else {    //or the data type of the feature is FLOAT...

                            float value = Float.parseFloat(input);

                            if (value < feature.getMinimum() || value > feature.getMaximum()) {
                                System.out.println("\t\t! Input is out of range");
                            } else {

                                //break out of the while loop, to get to the next iteration of the feature set
                                flag = false;

                                //store the respective feature in (Song object) targetSong
                                switch (feature.getName()) {
                                    case "loudness":
                                        targetSong.setLoudness(value);
                                        break;
                                    case "tempo":
                                        targetSong.setTempo(value);
                                        break;
                                }
                            }
                        }
                    }
                } catch (Exception exception) {
                    System.out.println("\t\t! Invalid input");
                }
            }
        }

        //create the playlist consisting of similar songs
        try {
            targetSong.setName("Custom Features");
            targetSong.setArtist("User");
            createSimilarityPlaylist(targetSong, numSongs);
        } catch (Exception exception) {
            System.out.println("Error: " + exception);
        }
    }

    public void generateFromSong() {

        System.out.println("\n< GENERATE BASED ON SPOTIFY TRACK >");
        System.out.println("----------------------------------------");
        System.out.println("In order to generate a playlist, you must provide the following parameters... ");
        System.out.println("(Enter \"exit\" to return to the previous menu)");

        String input = "";

        //get user input for the number of songs in the playlist
        int numSongs = 0;
        while (true) {
            try {
                System.out.print("\t> Number of songs in the playlist (int), range [20-100]: ");
                input = sc.next();

                if (input.equals("exit")) {
                    return;
                }

                numSongs = Integer.valueOf(input);

                if (numSongs >= 20 && numSongs <= 100) {
                    break;
                } else {
                    System.out.println("\t\t! Invalid range");
                }

            } catch (Exception exception) {
                System.out.println("\t\t! Invalid input");
            }
        }

        //get user input for the spotify track, on which to base the similarity playlist
        Song targetSong;
        while (true) {
            try {

                System.out.print("\t> Please enter the Spotify URL of the target track: ");
                input = sc.next();

                if (input.equals("exit")) {
                    return;
                }

                String trackId = importSpotifyDataService.extractIdFromUrl(input);
                targetSong = importSpotifyDataService.convertSpotifyTrackToSong(trackId);
                break;

            } catch (InvalidDataAccessApiUsageException | NullPointerException exception) {
                System.out.println("\t\t! Invalid Spotify URL - Please enter a valid Spotify object's URL");
            } catch (Exception exception) {
                System.out.println("\t\t! Error: " + exception);
            }
        }

        //create the playlist consisting of similar songs
        try {
            createSimilarityPlaylist(targetSong, numSongs);
        } catch (Exception exception) {
            System.out.println("Error: " + exception);
        }
    }

    //create a playlist of n songs, most similar to the given target song, and save it in the database
    private void createSimilarityPlaylist(Song targetSong, int numSongs) {

        System.out.println("Generating a playlist of " + numSongs + " songs, most similar to the target song with features...");
        System.out.println("Features: " + songService.getFeaturesAsString(targetSong));

        //log start time
        long startTime = System.currentTimeMillis();

        //initialize a treemap to store songs and their corresponding similarity to the target song
        //use a tree map with reverse order so that the map will automatically be sorted in descending order by key (ie, similarity score)
        TreeMap<Double, Song> candidateSongs = new TreeMap<>(Comparator.reverseOrder());
        List<Song> allSongs = commandLineController.getAllSongs();

        //iterate through all songs, computing similarity score
        for (Song song : allSongs) {
            candidateSongs.put(cosineSimilarity.calculate(targetSong, song), song);
        }

        //declare an empty list to store the songs with the highest similarity scores
        List<Song> nMostSimilarSongs = new ArrayList<>();

        //add the top numSongs number of songs from candidateSongs to nMostSimilarSongs
        for (int i = 0; i < numSongs; i++) {
            nMostSimilarSongs.add(candidateSongs.pollFirstEntry().getValue());
        }

        //create a playlist object, which stores the list of similar songs
        PlaylistOwn generated = new PlaylistOwn();
        generated.setSongs(new HashSet<>(nMostSimilarSongs));

        //generate an informative name for the playlist
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        generated.setName("Generated (from \"" + targetSong.getName() + "\" - " + targetSong.getArtist() + ") " + formatter.format(date));

        //save the playlist in the database
        commandLineController.savePlaylistOwnInDatabase(generated);

        //log end time, and calculate execution time as the difference between endtime and starttime
        long endTime = System.currentTimeMillis();
        System.out.println("Elapsed time in milliseconds: " + (endTime - startTime) + " (or " + ((endTime - startTime) / 1000) + " seconds)");
    }
}
