package melophile.commandline;

import melophile.controller.CommandLineController;
import melophile.model.PlaylistOwn;
import melophile.model.Song;
import melophile.model.SpotifySongFeature;
import melophile.utility.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class EnhancePlaylistCommand {

    @Autowired
    DisplayCommand displayCommand;
    @Autowired
    CommandLineController commandLineController;
    @Autowired
    KMeans kMeans;
    @Autowired
    EuclidianDistance euclidianDistance;
    @Autowired
    SilhouetteAnalysis silhouetteAnalysis;
    @Autowired
    CosineSimilarity cosineSimilarity;

    //variable to store the ratio of old songs to new songs in the enhanced playlist (i.e., for every x old songs in the original playlist, add one new songs to the enhanced playlist)
    private int oldSongsPerNewSong = 3;

    //variables to store the minimum and maximum values for K, used in K-Means algorithm
    private int minK = 2;
    private int maxK = 10;

    private static Scanner sc = new Scanner(System.in);

    public void enhancePlaylist() {

        String input = "";

        while (true) {

            try {

                System.out.println("\n< ENHANCE A PLAYLIST >");
                System.out.println("Please choose a playlist to enhance, by entering the corresponding id...");
                List<Integer> playlistIds = displayCommand.displayPlaylistOptions();

                input = sc.next();

                if (input.equals("exit")) {
                    return;
                }

                int option = Integer.parseInt(input);

                //if the entered id corresponds to an existing playlist id, then enhance that playlist
                if (playlistIds.contains(option)) {

                    //get user input for the song features they would like to cluster by
                    List<SpotifySongFeature> chosenFeatures = getFeaturesToClusterBy();

                    //check if user chose "exit" option during the feature selection process, and if so, stop enhancement process for the current playlist
                    if (chosenFeatures.isEmpty() || chosenFeatures == null) {
                        continue;
                    }

                    //log start time
                    long startTime = System.currentTimeMillis();

                    //store the playlist object
                    PlaylistOwn original = commandLineController.getPlaylistOwn(option);

                    //create the enhanced playlist based on the original playlist
                    PlaylistOwn enhanced = createEnhancePlaylist(original, chosenFeatures);

                    //save the enhanced playlist in the database
                    commandLineController.savePlaylistOwnInDatabase(enhanced);

                    //log end time, and calculate execution time as the difference between endtime and starttime
                    long endTime = System.currentTimeMillis();
                    System.out.println("Elapsed time for entire playlist enhancement process in milliseconds: " + (endTime - startTime) + " (or " + ((endTime - startTime) / 1000) + " seconds)");

                    return;

                } else {

                    //otherwise, prompt the user for a valid playlist id
                    System.out.println("Invalid range - Please follow the input instructions precisely");

                }
            } catch (NumberFormatException exception) {
                System.out.println("Invalid input - Please follow the input instructions precisely");
            } catch (Exception exception) {
                System.out.println("Error: " + exception);
            }
        }
    }

    private List<SpotifySongFeature> getFeaturesToClusterBy() {

        String input = "";

        while (true) {

            try {
                //display options to user and get user input
                System.out.println("\n< CONFIGURE CLUSTERING >");
                System.out.println("The algorithm requires you to choose the two, most important features by which to cluster songs");
                System.out.println("Please choose two song features (by id), seperated by a comma...");
                List<Integer> featureIds = displayCommand.displaySongFeatureOptions();

                input = sc.nextLine();

                if (input.equals("exit")) {
                    return null;
                }

                //get the input for the first and second features (which the user should have entered seperated by a string)
                String input1 = StringUtils.substringBefore(input, ",");
                String input2 = StringUtils.substringAfter(input, ",");

                //remove any potential white spaces that may exist in the input string for each feature
                input1 = input1.replaceAll("\\s", "");
                input2 = input2.replaceAll("\\s", "");

                //convert the feature input from String to int
                int feature1 = Integer.parseInt(input1);
                int feature2 = Integer.parseInt(input2);

                //if the entered feature ids correspond to existing feature ids AND they are different from each other, then return the list of those features
                if (featureIds.contains(feature1) && featureIds.contains(feature2) && feature1 != feature2) {

                    //retrieve the spotify song feature corresponding to each entered feature id
                    SpotifySongFeature spotifySongFeature1 = commandLineController.getSpotifySongFeature(feature1);
                    SpotifySongFeature spotifySongFeature2 = commandLineController.getSpotifySongFeature(feature2);

                    //add the retrieved song features to a list, and return that list
                    List<SpotifySongFeature> chosenFeatures = new ArrayList<>();
                    chosenFeatures.add(spotifySongFeature1);
                    chosenFeatures.add(spotifySongFeature2);

                    return chosenFeatures;

                } else {

                    //otherwise, prompt the user for a valid feature id
                    System.out.println("Invalid range - Please follow the input instructions precisely");

                }

            } catch (NumberFormatException exception) {
                System.out.println("Invalid input - Please follow the input instructions precisely");
            } catch (Exception exception) {
                System.out.println("Error: " + exception);
            }
        }
    }

    private PlaylistOwn createEnhancePlaylist(PlaylistOwn original, List<SpotifySongFeature> chosenFeatures) {

        try {
            //store the original playlist's songs in a list
            List<Song> originalSongs = new ArrayList<>(original.getSongs());
            List<Integer> originalSongIds = originalSongs.stream().map(Song::getId).toList();

            //get the optimal clustering scenario for the playlist
            Map<Centroid, List<Song>> optimalClustering = getOptimalClustering(originalSongs, chosenFeatures, euclidianDistance);

            //iterate through each entry of clusters, and determine how many new songs to add per cluster
            Map<Centroid, Integer> songsToAddPerCluster = new HashMap<>();

            for (Map.Entry<Centroid, List<Song>> entry : optimalClustering.entrySet()) {
                int clusterSize = entry.getValue().size();
                int songsToAdd = clusterSize / oldSongsPerNewSong + 1;
                songsToAddPerCluster.put(entry.getKey(), songsToAdd);
            }

            //iterate through each centroid, and find the n most similar songs to the centroid, to add to the enhanced playlist
            List<Song> allSongs = commandLineController.getAllSongs();
            List<Song> enhancedSongs = new ArrayList<>();
            PlaylistOwn enhanced = new PlaylistOwn();

            for (Map.Entry<Centroid, Integer> entry : songsToAddPerCluster.entrySet()) {

                //initialize a treemap to store songs and their corresponding similarity to the current centroid
                //use a tree map with reverse order so that the map will automatically be sorted in descending order by key (ie, similarity score)
                TreeMap<Double, Song> candidateSongs = new TreeMap<>(Comparator.reverseOrder());

                //fill candidateSongs
                for (Song song : allSongs) {

                    //if we encounter a song that exists in the original playlist, ignore it, as it shouldn't be a candidate for the enhanced playlist
                    if (originalSongIds.contains(song.getId())) {
                        //do nothing
                    } else {
                        candidateSongs.put(cosineSimilarity.calculate(entry.getKey(), song, chosenFeatures), song);
                    }
                }

                //add the top n songs from candidateSongs to enhancedSongs
                for (int i = 0; i < entry.getValue(); i++) {
                    enhancedSongs.add(candidateSongs.pollFirstEntry().getValue());
                }
            }

            //add the list of enhanced songs to the enhanced playlist
            enhanced.setSongs(new HashSet<>(enhancedSongs));

            // give the playlist an informative name
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            enhanced.setName("Enhanced (from \"" + original.getName() + "\") " + formatter.format(date));

            return enhanced;

        } catch (Exception exception) {
            System.out.println("Unable to create the enhanced playlist. Error: " + exception);
            return null;
        }
    }

    //perform rounds of K-means clustering with different K values, and return the optimal clustering scenario (ie, k-means clustering, where k produces highest silhouette score)
    private Map<Centroid, List<Song>> getOptimalClustering(List<Song> songs, List<SpotifySongFeature> chosenFeatures, Distance distance) {

        //initialize a TreeMap to keep track of the silhouette score for each k-value in a k-means iteration
        TreeMap<Double, Map<Centroid, List<Song>>> averageSilhouetteForK = new TreeMap<>(Comparator.reverseOrder());

        //perform k means clustering and silhouette analysis, for every k in the range [minK, maxK]. Store the results in averageSilhouetteForK
        for (int i = minK; i <= maxK; i++) {

            Map<Centroid, List<Song>> fit = kMeans.performClustering(songs, chosenFeatures, i, distance);
            Double averageSilhouette = silhouetteAnalysis.performAnalysis(fit, chosenFeatures, distance);

            /*
            IMPORTANT NOTE: only add an entry in averageSilhouetteForK if the map does not contain averageSilhouette
            WHY? This is done because k-means may produce less than k clusters, if one or more clusters receive no song
            assignments during the algorithm. Example: performing k-means with k=a vs k=b, where a>b, may result in the
            same clustering formations if k=a algorithm produces b valid clusters (ie. full clusters). Such a cluster
            formation would, therefore, produce the same silhouette score. Trying to add cluster formation k=a to
            the map, averageSilhouetteForK, will overwrite cluster formation entry k=b. We DO NOT want that to occur!
             */
            if(!averageSilhouetteForK.containsKey(averageSilhouette)){
                averageSilhouetteForK.put(averageSilhouette, fit);
            }

            //Display informative message to the user about clustering and silhouette analysis
            System.out.println("Performed K-Means clustering with K = " + i + ". " +
                    "Identified " + fit.keySet().size() + " non-empty clusters. " +
                    "Computed average silhouette score: " + averageSilhouette);
        }

        //display information about optimal cluster formation
        System.out.println("Silhouette Analysis determined optimal value for k as " + averageSilhouetteForK.firstEntry().getValue().size()
                + ", with an average silhouette of " + averageSilhouetteForK.firstEntry().getKey());

        //return the optimal k-means fit, which is determined by the k value with the highest silhouette score
        //Note: averageSilhouetteForK will automatically order itself in descending order, based on the key (ie. silhouette score). Thus, to find the k value corresponding to the largest silhouette score, we retrieve the first entry's value in averageSilhouetteForK
        return averageSilhouetteForK.firstEntry().getValue();

    }
}
