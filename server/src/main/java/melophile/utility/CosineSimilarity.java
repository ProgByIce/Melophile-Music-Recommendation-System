package melophile.utility;

import melophile.model.Song;
import melophile.model.SpotifySongFeature;
import melophile.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Vector;

@Service
public class CosineSimilarity {

    @Autowired
    SongService songService;

    //computes the similarity between two songs by using cosine similarity. returns a value in the range [0.0, 1.0] - 0 being most dissimilar, 1 being most similar
    public double calculate(Song song1, Song song2) {

        //similarity(song1,song2) = cosine of (angle between song1 and song2's vector) = dot product between song1 and song2's vector / (magnitude of song1's vector) * (magnitude of song2's vector)
        //dot product(song1,song2) = song1[feature1]*song2[feature1] + song1[feature2]*song2[feature2] + song1[feature3]*song2[feature3] + ... + song1[featureN]*song2[featureN]
        //magnitude of song's vector = square root of (song[feature1]^2 + song[feature2]^2 + song[feature3]^2 + .. + song[featureN]^2

        //if comparing one and the same song, then we return 1 (implying maximum similarity, ie identical songs)
        if (song1.hasSameSpotifyId(song2)) {
            return 1.0d;
        }

        //Get a vector of each song's feature values
        Vector<Float> featureVector1 = songService.convertToFeatureVector(song1);
        Vector<Float> featureVector2 = songService.convertToFeatureVector(song2);

        double dotProduct = 0;
        double magnitudeSong1 = 0;
        double magnitudeSong2 = 0;
        double cosineSimilarity;

        /*
        perform some validation
        - ensuring that both feature sets do not consist of null values.
        - ensuring that both feature sets are of the same length
        - if a null feature is encountered, either exclude the feature from the similarity score, or throw an error
        - ex. some songs dont have a defined key (value: -1)
         */

        if ((featureVector1 != null && featureVector2 != null) && (featureVector1.size() == featureVector2.size())) {
            for (int i = 0; i < featureVector1.size(); i++) {
                dotProduct += featureVector1.get(i) * featureVector2.get(i);
                magnitudeSong1 += Math.pow(featureVector1.get(i), 2);
                magnitudeSong2 += Math.pow(featureVector2.get(i), 2);
            }
        } else {
            return -1;
        }

        magnitudeSong1 = Math.sqrt(magnitudeSong1);
        magnitudeSong2 = Math.sqrt(magnitudeSong2);

        cosineSimilarity = dotProduct / (magnitudeSong1 * magnitudeSong2);

        return cosineSimilarity;
    }

    //compute the similarity between a centroid and a song, only considering a subset of song features
    public double calculate(Centroid centroid, Song song, List<SpotifySongFeature> chosenFeatures) {

        double dotProduct = 0;
        double magnitudeCentroid = 0;
        double magnitudeSong = 0;
        double cosineSimilarity;

        for (SpotifySongFeature feature : chosenFeatures) {

            float songFeature = 0;

            //if the feature is not normalized, we must perform normalization on it for the song object (which holds features in their non-normalized form, by default)
            if (!feature.isNormalized()) {
                songFeature = songService.normalizeValue(song.getFeature(feature.getName()), feature.getMinimum(), feature.getMaximum());
            } else{
                songFeature = song.getFeature(feature.getName());
            }

            dotProduct += centroid.getCoordinates().get(feature) * songFeature;
            magnitudeCentroid += Math.pow(centroid.getCoordinates().get(feature), 2);
            magnitudeSong += Math.pow(songFeature, 2);
        }

        magnitudeCentroid = Math.sqrt(magnitudeCentroid);
        magnitudeSong = Math.sqrt(magnitudeSong);

        cosineSimilarity = dotProduct / (magnitudeCentroid * magnitudeSong);

        return cosineSimilarity;
    }
}
