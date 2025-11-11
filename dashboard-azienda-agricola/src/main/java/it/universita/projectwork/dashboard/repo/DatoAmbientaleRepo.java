package it.universita.projectwork.dashboard.repo;

import it.universita.projectwork.dashboard.model.DatoAmbientale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface DatoAmbientaleRepo extends JpaRepository<DatoAmbientale, Long> {
    List<DatoAmbientale> findByTimestampBetweenOrderByTimestampAsc(Instant from, Instant to);

    List<DatoAmbientale> findTop50ByOrderByTimestampDesc();
}
