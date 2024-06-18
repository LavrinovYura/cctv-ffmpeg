package polytech.diploma.repositories;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import polytech.diploma.models.streams.LiveStream;


import java.util.Optional;

@Repository
public interface StreamRepository extends JpaRepository<LiveStream, Long> {
    Optional<LiveStream> findByRtspUrl(String rtspUrl);
    Page<LiveStream> findAllByGroupName(String group, Pageable pageable);
}
