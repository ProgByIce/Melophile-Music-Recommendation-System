package melophile.utility;

import melophile.model.Song;
import melophile.model.SpotifySongFeature;
import melophile.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KMeans {

    @Autowired
    SongService songService;
    private static final Random random = new Random();
    private int iterationLimit = 100;

    /*
    * Dataset: a list of song records
    * K: the number of clusters, must be provided in advance
    * Distance: the way that the distance is measured between two song records in an N-dimensional Cartesian Plane
    * K-Means terminates when the assignment stops changing for a few consecutive iterations. In addition to this
      termination condition, we can place an upper bound for the number of iterations, too. The maxIterations argument
      determines that upper bound
    * When K-Means terminates, each centroid should have a few assigned features, hence we’re using a
      Map<Centroid, List<Song>> as the return type. Basically, each map entry corresponds to a cluster
    */
    public Map<Centroid, List<Song>> performClustering(List<Song> songs, List<SpotifySongFeature> chosenFeatures, int k, Distance distance) {

        //log start time
        long startTime = System.nanoTime();

        //initialize maps to store the current clustering state, and the previous clustering state
        LinkedHashMap<Centroid, List<Song>> curClusterState = new LinkedHashMap<>();
        LinkedHashMap<Centroid, List<Song>> prevClusterState = new LinkedHashMap<>();

        //initialize a list of randomly positioned centroids
        List<Centroid> centroids = setRandomCentroids(songs, chosenFeatures, k);

        int i = 0;
        while (i < iterationLimit) {

            //for each iteration of the while loop, assign every song to its closest centroid
            for (Song song : songs) {
                Centroid closest = findClosestCentroid(song, chosenFeatures, centroids, distance);
                clusterAssignment(curClusterState, song, closest);
            }

            //sort the curClusterState and prevClusterState maps by converting them to TreeMap
            TreeMap<Centroid, List<Song>> sortedCurClusterState = new TreeMap<>(curClusterState);
            TreeMap<Centroid, List<Song>> sortedLastClusterState = new TreeMap<>(prevClusterState);

            // check if the current cluster state is the same as the last cluster state, by comparing their sorted maps
            boolean isSameAsPrevState = compareClusterStates(sortedCurClusterState, sortedLastClusterState);

            //remember the current cluster state by storing it in prevClusterState, so that it will be accessible for next iteration
            prevClusterState = curClusterState;

            //if the current cluster state is the same as the previous state, then the algorithm is finished with clustering
            if (isSameAsPrevState)
                break;

            //reposition centroids and continue on to next iteration
            centroids = reposition(curClusterState, chosenFeatures);
            curClusterState = new LinkedHashMap<>();

            i++;
        }

        //log end time, and calculate execution time as the difference between endtime and starttime
        long endTime = System.nanoTime();
        System.out.println("Elapsed time for k-means (with k = " + k + ") in nanoseconds: " + (endTime - startTime));

        return prevClusterState;
    }

    //generate a list of k centroids, randomly positioned in the bounds of each feature's [min value, max value]
    private List<Centroid> setRandomCentroids(List<Song> songs, List<SpotifySongFeature> chosenFeatures, int k) {

        //initialize maps to store the respective min and max value for each feature
        Map<SpotifySongFeature, Float> minFeatureValues = new HashMap<>();
        Map<SpotifySongFeature, Float> maxFeatureValues = new HashMap<>();

        //initialize an empty list of centroids
        List<Centroid> randomCentroids = new ArrayList<>();

        for (Song song : songs) {

            //convert the song to a map of chosen features
            Map<SpotifySongFeature, Float> featureMap = songService.convertToFeatureMap(song, chosenFeatures);

            //for each <feature,value> pair in the featureMap, determine whether the value is the min/max of all values
            for (Map.Entry<SpotifySongFeature, Float> entry : featureMap.entrySet()) {

                SpotifySongFeature feature = entry.getKey();
                Float value = entry.getValue();

                //compare the value in maxFeatureValues with the current value, and store the bigger of the two
                if (maxFeatureValues.get(feature) == null || maxFeatureValues.get(feature) < value)
                    maxFeatureValues.put(feature, value);

                //compare the value in minFeatureValues with the current value, and store the smaller of the two
                if (minFeatureValues.get(feature) == null || minFeatureValues.get(feature) > value)
                    minFeatureValues.put(feature, value);
            }
        }

        //create k number of centroids, randomly positioned in the range [min-max] for each feature
        int i = 0;
        while (i < k) {

            Map<SpotifySongFeature, Float> coordinates = new LinkedHashMap<>();

            for (SpotifySongFeature feature : chosenFeatures) {

                float min = minFeatureValues.get(feature);
                float max = maxFeatureValues.get(feature);
                float randomPosition = random.nextFloat() * (max - min) + min;
                coordinates.put(feature, randomPosition);

            }

            randomCentroids.add(new Centroid(coordinates));

            i++;
        }

        return randomCentroids;
    }

    //For a given song, find the closest centroid to that song
    private Centroid findClosestCentroid(Song song, List<SpotifySongFeature> chosenFeatures, List<Centroid> centroids, Distance distance) {

        double minDistance = Double.MAX_VALUE;
        Centroid closest = null;

        //iterate through all centroids to find the closest one
        for (Centroid centroid : centroids) {

            //compute the distance between the song and the current centroid
            double d = distance.compute(songService.convertToFeatureMap(song, chosenFeatures), centroid.getCoordinates());

            //if distance is less than minDistance, then update minDistance and assign this centroid as the closest
            if (d < minDistance) {
                minDistance = d;
                closest = centroid;
            }
        }

        return closest;
    }

    //assign a song to its nearest centroid cluster
    private void clusterAssignment(Map<Centroid, List<Song>> clusters, Song song, Centroid centroid) {

        //get the list of songs associated with the centroid
        List<Song> songsInCluster = clusters.get(centroid);

        //if that list is null, then initialize a new list and add song to it
        if (songsInCluster == null) {
            songsInCluster = new ArrayList<>();
            clusters.put(centroid, songsInCluster);
        }

        songsInCluster.add(song);
    }

    //return a list of repositioned centroids, now at the average location of all songs assigned to them
    private List<Centroid> reposition(Map<Centroid, List<Song>> clusters, List<SpotifySongFeature> chosenFeatures) {

        List<Centroid> repositionedCentroids = new ArrayList<>();

        for (Map.Entry<Centroid, List<Song>> entry : clusters.entrySet()) {
            repositionedCentroids.add(averageLocation(entry.getKey(), entry.getValue(), chosenFeatures));
        }

        return repositionedCentroids;
    }

    //compute the average location of all songs assigned to a specific centroid
    private Centroid averageLocation(Centroid centroid, List<Song> songs, List<SpotifySongFeature> chosenFeatures) {

        //check if no songs are assigned to the centroid - if so, no relocation is required
        boolean hasNoAssignedSongs = songs.isEmpty() || songs == null;
        if (hasNoAssignedSongs)
            return centroid;


        //declare an empty map, to store the sum of each song feature's values
        Map<SpotifySongFeature, Float> sumFeatureValues = new HashMap<>(centroid.getCoordinates());

        int counter = 0;
        for (Song song : songs) {

            //convert the song to a map of features
            Map<SpotifySongFeature, Float> featureMap = songService.convertToFeatureMap(song, chosenFeatures);

            //iterate through the feature map and sum the values for each feature
            for (Map.Entry<SpotifySongFeature, Float> entry : featureMap.entrySet()) {

                //on the first iteration, initialize sums
                if (counter == 0) {
                    sumFeatureValues.put(entry.getKey(), entry.getValue());
                } else {  //For consecutive iterations, increment the summed value by the current value
                    sumFeatureValues.put(entry.getKey(), sumFeatureValues.get(entry.getKey()) + entry.getValue());
                }
            }

            counter++;
        }

        //compute the average feature values
        Map<SpotifySongFeature, Float> avgFeatureValues = new HashMap<>();
        for (Map.Entry<SpotifySongFeature, Float> entry : sumFeatureValues.entrySet()) {
            SpotifySongFeature feature = entry.getKey();
            Float value = entry.getValue();
            avgFeatureValues.put(feature, value / songs.size());
        }

        return new Centroid(avgFeatureValues);
    }

    //compare two clusters to one another, to check for equality. Cluster are represented as Map<Centroid, List<Song>> objects
    //The method returns true if 1) the maps are of equal size, 2) each respective entry in map1 is equal to map2 (NOTE: the method for checking equality between entries is overridden here)
    private boolean compareClusterStates(Map<Centroid, List<Song>> clusterStateA, Map<Centroid, List<Song>> clusterStateB) {

        //if either one of the cluster maps are null or empty, return false
        if (clusterStateA == null || clusterStateA.isEmpty() || clusterStateB == null || clusterStateB.isEmpty()) {
            return false;
        }

        //if the maps are of differing size, return false
        if (clusterStateA.size() != clusterStateB.size()) {
            return false;
        }

        //compare respective entries of the two maps
        int countEntryA = 0;
        int countEntryB = 0;

        for (Map.Entry<Centroid, List<Song>> entryA : clusterStateA.entrySet()) {

            countEntryB = 0;    //reset the counter for entryB with every full iteration through entryA

            for (Map.Entry<Centroid, List<Song>> entryB : clusterStateB.entrySet()) {

                //only perform a comparison of respective entries
                if (countEntryA == countEntryB) {

                    //if respective centroids are different, return false
                    if (!entryA.getKey().equals(entryB.getKey())) {
                        return false;
                    }

                    //if respective song lists are different, return false
                    //case 1: song lists are of different size
                    if (entryA.getValue().size() != entryA.getValue().size()) {
                        return false;
                    } else {
                        for (int i = 0; i < entryA.getValue().size(); i++) {
                            //case two: respective songs in the list are different
                            if (!entryA.getValue().get(i).equals(entryB.getValue().get(i))) {
                                return false;
                            }
                        }
                    }

                }
                countEntryB++;
            }
            countEntryA++;
        }

        return true;
    }
}