package com.jcajchun_2023512.VetKI.repository;

import com.jcajchun_2023512.VetKI.entity.Mascota;
import com.jcajchun_2023512.VetKI.entity.enums.Especie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    List<Mascota> findByClienteIdOrderByIdAsc(Long clienteId);

    Optional<Mascota> findByIdAndClienteId(Long id, Long clienteId);

    boolean existsByIdAndClienteId(Long id, Long clienteId);

    long countByEspecie(Especie especie);

    @Query("SELECT m.especie, COUNT(m) FROM Mascota m GROUP BY m.especie")
    List<Object[]> countGroupedByEspecie();
}
