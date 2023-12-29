package saleson.service.transfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import saleson.model.Adata;

@Repository
public interface AdataRepository extends JpaRepository<Adata, Long> {
}
