package it.universita.projectwork.dashboard.repo;

import it.universita.projectwork.dashboard.model.DatoProduttivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface DatoProduttivoRepo extends JpaRepository<DatoProduttivo, Long> {
    List<DatoProduttivo> findByTimestampBetweenOrderByTimestampAsc(Instant from, Instant to);

    List<DatoProduttivo> findTop50ByOrderByTimestampDesc();
}
