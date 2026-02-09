package melophile.service;

import melophile.controller.CommandLineController;
import melophile.model.Song;
import melophile.model.SpotifySongFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SongService {

    @Autowired
    CommandLineController commandLineController;

    //convert a song to a Map of <Feature Name, Feature Value> pairs (for ALL song features), where each feature value is normalized
    public Map<String, Float> convertToFeatureMap(Song song) {

        Map<String, Float> featureMap = new HashMap();
        List<SpotifySongFeature> spotifySongFeatures = commandLineController.getAllSpotifySongFeatures();

        for (SpotifySongFeature feature : spotifySongFeatures) {

            //if the feature is NOT normalized...
            if (!feature.isNormalized()) {

                //normalization MUST be performed
                featureMap.put(feature.getName(), normalizeValue(song.getFeature(feature.getName()), feature.getMinimum(), feature.getMaximum()));

            } else { //if feature IS normalized

                //can insert the feature as it is into featureMap
                featureMap.put(feature.getName(), song.getFeature(feature.getName()));

            }
        }

        return featureMap;
    }

    public Map<SpotifySongFeature, Float> convertToFeatureMap(Song song, List<SpotifySongFeature> features) {

        Map<SpotifySongFeature, Float> featureMap = new HashMap();

        for (SpotifySongFeature feature : features) {

            //if the feature is NOT normalized...
            if (!feature.isNormalized()) {

                //normalization MUST be performed
                featureMap.put(feature, normalizeValue(song.getFeature(feature.getName()), feature.getMinimum(), feature.getMaximum()));

            } else { //if feature IS normalized

                //can insert the feature as it is into featureMap
                featureMap.put(feature, song.getFeature(feature.getName()));

            }
        }

        return featureMap;
    }

    //normalize a song feature value to fall in the range [0.0,1.0] using min-max normalization
    public static float normalizeValue(float val, float curMin, float curMax) {

        final float NEW_MIN = 0;
        final float NEW_MAX = 1;

        float normalizedVal = ((val - curMin) / (curMax - curMin)) * (NEW_MAX - NEW_MIN) + NEW_MIN;
        return normalizedVal;
    }

    //convert a song to a vector containing all of its normalized feature values
    public Vector<Float> convertToFeatureVector(Song song) {

        //Map<String, Float> featureMap = convertToFeatureMap(song);
        List<SpotifySongFeature> spotifySongFeatures = commandLineController.getAllSpotifySongFeatures();
        Vector<Float> featureVector = new Vector<>();

        for (SpotifySongFeature feature : spotifySongFeatures) {
            featureVector.add(song.getFeature(feature.getName()));
        }

        return featureVector;
    }

    public String getFeaturesAsString(Song song) {

        StringBuilder s = new StringBuilder();
        Map<String, Float> featureMap = convertToFeatureMap(song);

        s.append("[");
        for (Map.Entry<String, Float> entry : featureMap.entrySet()) {
            s.append("| " + entry.getKey() + " = " + entry.getValue() + " |");
        }
        s.append("]");

        return s.toString();
    }
}
