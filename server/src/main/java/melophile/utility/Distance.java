package melophile.utility;

import melophile.model.SpotifySongFeature;

import java.util.Map;

public interface Distance {
    double compute(Map<SpotifySongFeature, Float> p, Map<SpotifySongFeature, Float> q);
}
