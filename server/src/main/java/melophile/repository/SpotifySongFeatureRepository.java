package melophile.repository;

import melophile.model.SpotifySongFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpotifySongFeatureRepository extends JpaRepository<SpotifySongFeature, Integer> {
    SpotifySongFeature findById(int id);
}
