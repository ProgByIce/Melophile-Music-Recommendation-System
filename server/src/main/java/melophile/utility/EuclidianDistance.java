package melophile.utility;

import melophile.model.SpotifySongFeature;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EuclidianDistance implements Distance {

    //calculate the euclidian distance between two points in an N-dimensional cartesian plane, where each dimension corresponds to a song feature
    @Override
    public double compute(Map<SpotifySongFeature, Float> p, Map<SpotifySongFeature, Float> q){

        //eucludian distance between two points, p and q, is equal to the square root of the sum of squared differences between corresponding entries
        double sum = 0;

        for(Map.Entry<SpotifySongFeature, Float> entry : p.entrySet()){

            Float featureValue1 = p.get(entry.getKey());
            Float featureValue2 = q.get(entry.getKey());

            if (featureValue1 != null && featureValue2 != null){
                sum += Math.pow(featureValue1-featureValue2,2);
            }
        }

        return Math.sqrt(sum);
    }
}
