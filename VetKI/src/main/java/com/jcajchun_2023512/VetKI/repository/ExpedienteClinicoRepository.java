package com.jcajchun_2023512.VetKI.repository;

import com.jcajchun_2023512.VetKI.entity.ExpedienteClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpedienteClinicoRepository extends JpaRepository<ExpedienteClinico, Long> {

    boolean existsByCitaId(Long citaId);

    Optional<ExpedienteClinico> findByCitaId(Long citaId);

    List<ExpedienteClinico> findByCitaMascotaIdOrderByFechaRegistroDesc(Long mascotaId);
}
