package melophile.utility;

import melophile.model.Song;
import melophile.model.SpotifySongFeature;
import melophile.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SilhouetteAnalysis {

    @Autowired
    SongService songService;

    //use the silhouette method to determine optimal value of k

    //silhouette coefficient for a data point i: S(i) = (b(i) - a(i)) / max(a(i),b(i)), where....
    // a(i) = distance from data point i to the centroid of the cluster it belongs to
    // b(i) = distance from data point i to the centroid of the nearest cluster that data point i does NOT belong to

    // silhouette coefficient falls in the range [1,1], where
    // 1 implies minimal within-cluster variation, and maximal between-cluster variation (ie. "ideal clustering")
    // 0 implies within-cluster variation and between-cluster variation is the same (ie. "somewhat suboptimal clustering")
    // -1 implies maximal within-cluster variation, and minimal between-cluster variation (ie. "suboptimal clustering")

    //average silhouette for a fit = mean(S(i))
    //
    //to determine optimal k, plot average silhouette against k value -> k value that produces largest average silhouette should be used

    /*
    Dataset: A map of k centroids and a list of songs belonging to each centroid
    returns: the computed silhouette coefficient for this particular k-means fit
     */
    public double performAnalysis(Map<Centroid, List<Song>> kMeansFit, List<SpotifySongFeature> chosenFeatures, Distance distance){

        //log start time
        long startTime = System.nanoTime();

        double avgSilhouette;
        double numSongs = 0;    //counter to keep track of the number of songs in the dataset
        double sumSilhoutteCoefficients = 0;    //counter to keep a sum of each song's silhouette coefficient

        for (Map.Entry<Centroid, List<Song>> entry : kMeansFit.entrySet()){

            //initialize a list of foreign centroids (by getting a list of all centroids in the fit and removing the current centroid form the list)
            ArrayList<Centroid> foreignCentroids = new ArrayList<>(kMeansFit.keySet().stream().toList());
            foreignCentroids.remove(entry.getKey());
            Centroid ownCentroid = entry.getKey();  //store cluster's own centroid

            for (Song song : entry.getValue()) {
                Centroid nextClosestCentroid = nextClosestCentroid(foreignCentroids, song, chosenFeatures, distance);   //store current song's next closest centroid
                sumSilhoutteCoefficients += silhouetteCoefficient(ownCentroid, nextClosestCentroid, song, chosenFeatures, distance);
                numSongs++;
            }
        }

        //compute the average silhouette for the whole dataset by taking the mean of all silhouette coefficients
        avgSilhouette = sumSilhoutteCoefficients / numSongs;

        //log end time, and calculate execution time as the difference between endtime and starttime
        long endTime = System.nanoTime();
        System.out.println("Elapsed time for silhouette analysis (of k-means with k = " + kMeansFit.size() + ") in nanoseconds: " + (endTime - startTime));

        return avgSilhouette;
    }

    private double silhouetteCoefficient(Centroid ownCentroid, Centroid nextClosestCentroid, Song song, List<SpotifySongFeature> chosenFeatures, Distance distance){

        double a=distance.compute(ownCentroid.getCoordinates(), songService.convertToFeatureMap(song, chosenFeatures));
        double b=distance.compute(nextClosestCentroid.getCoordinates(), songService.convertToFeatureMap(song, chosenFeatures));
        double silhouetteCoefficient = (b-a)/Math.max(a,b);

        return silhouetteCoefficient;
    }

    private Centroid nextClosestCentroid(List<Centroid> foreignCentroids, Song song, List<SpotifySongFeature> chosenFeatures, Distance distance){

        double minDistance = Double.MAX_VALUE;
        Centroid nextClosest = null;

        for (Centroid centroid: foreignCentroids) {
            double d = distance.compute(centroid.getCoordinates(), songService.convertToFeatureMap(song, chosenFeatures));

            if (d<minDistance){
                minDistance = d;
                nextClosest = centroid;
            }
        }

        return nextClosest;
    }
}
