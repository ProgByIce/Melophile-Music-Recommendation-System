package melophile.repository;

import melophile.model.PlaylistOwn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistOwnRepository extends JpaRepository<PlaylistOwn, Integer> {
    PlaylistOwn findById(int id);
}
