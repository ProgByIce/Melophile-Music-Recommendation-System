package melophile.utility;

import melophile.model.SpotifySongFeature;

import java.util.Map;

public class Centroid implements Comparable<Centroid> {

    private final Map<SpotifySongFeature, Float> coordinates;

    //constructors

    public Centroid(Map<SpotifySongFeature, Float> coordinates) {
        this.coordinates = coordinates;
    }

    //getters
    public Map<SpotifySongFeature, Float> getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString(){
        StringBuilder s = new StringBuilder();
        for (Map.Entry<SpotifySongFeature, Float> entry : this.getCoordinates().entrySet()) {
            s.append("| " + entry.getKey().getName() + " = " + entry.getValue() + " |");
        }
        return s.toString();
    }

    //special - use the below methods to define how centroid should be compared
    @Override
    public boolean equals(Object other){

        //if other is the same instance as this, then return tru
        if (other == this){
            return true;
        }

        //if the other object is not an instance of Centroid, then return false
        if (!(other instanceof Centroid)){
            return false;
        }

        // typecast other to type Centroid so that we can compare data members
        Centroid c = (Centroid) other;

        //two centroids are considered equal if they 1) have the same set of SpotifySongFeatures and...
        if (this.getCoordinates().keySet().equals((c.getCoordinates().keySet()))){

            //2) the respective values for each SpotifySongFeature are equal
            for (SpotifySongFeature feature: this.getCoordinates().keySet()) {

                float featureValue1 = this.getCoordinates().get(feature);
                float featureValue2 = c.getCoordinates().get(feature);

                //if a discrepency between feature values is encountered, then return false
                if (featureValue1 != featureValue2){
                    return false;
                }
            }

            //otherwise, return true
            return true;
        }

        //if none of the above conditions return a result, then by default, return false
        return false;
    }

    //compare Centroids by the value of the first SpotifySongFeature in their coordinate map
    @Override
    public int compareTo(Centroid o) {

        //implement comparisons in such a way that centroids are compared based on the sum of their feature values
        float sumFeatureValuesA = 0;
        float sumFeatureValuesB = 0;

        for (Float featureValue: this.getCoordinates().values()) {
            sumFeatureValuesA += featureValue;
        }

        for (Float featureValue: o.getCoordinates().values()) {
            sumFeatureValuesB += featureValue;
        }

        if(sumFeatureValuesA > sumFeatureValuesB){
            return 1;
        } else if(sumFeatureValuesA == sumFeatureValuesB){
            return 0;
        } else{
            return -1;
        }
    }
}
