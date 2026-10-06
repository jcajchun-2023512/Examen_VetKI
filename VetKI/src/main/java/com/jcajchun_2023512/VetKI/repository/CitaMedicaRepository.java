package com.jcajchun_2023512.VetKI.repository;

import com.jcajchun_2023512.VetKI.entity.CitaMedica;
import com.jcajchun_2023512.VetKI.entity.enums.EstadoCita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaMedicaRepository extends JpaRepository<CitaMedica, Long> {

    // Regla 2: Disponibilidad de veterinario
    boolean existsByVeterinarioIdAndFechaHoraAndEstado(Long veterinarioId, LocalDateTime fechaHora, EstadoCita estado);

    // Regla 3: Límite diario de citas por cliente
    long countByMascotaClienteIdAndEstadoAndFechaHoraBetween(Long clienteId, EstadoCita estado, LocalDateTime inicio, LocalDateTime fin);

    // Agenda para VET y ADMIN
    @Query("SELECT c FROM CitaMedica c " +
           "WHERE (:vetId IS NULL OR c.veterinario.id = :vetId) " +
           "AND (cast(:desde as timestamp) IS NULL OR c.fechaHora >= :desde) " +
           "AND (cast(:hasta as timestamp) IS NULL OR c.fechaHora <= :hasta) " +
           "ORDER BY c.fechaHora ASC")
    List<CitaMedica> findAgenda(
            @Param("vetId") Long vetId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta
    );

    // Historial paginado para CLIENTE
    @Query(value = "SELECT c FROM CitaMedica c " +
           "WHERE c.mascota.cliente.id = :clienteId " +
           "AND (:estado IS NULL OR c.estado = :estado) " +
           "AND (:mascotaId IS NULL OR c.mascota.id = :mascotaId) " +
           "AND (cast(:desde as timestamp) IS NULL OR c.fechaHora >= :desde) " +
           "AND (cast(:hasta as timestamp) IS NULL OR c.fechaHora <= :hasta) " +
           "ORDER BY c.fechaHora DESC",
           countQuery = "SELECT COUNT(c) FROM CitaMedica c " +
           "WHERE c.mascota.cliente.id = :clienteId " +
           "AND (:estado IS NULL OR c.estado = :estado) " +
           "AND (:mascotaId IS NULL OR c.mascota.id = :mascotaId) " +
           "AND (cast(:desde as timestamp) IS NULL OR c.fechaHora >= :desde) " +
           "AND (cast(:hasta as timestamp) IS NULL OR c.fechaHora <= :hasta)")
    Page<CitaMedica> findMisCitas(
            @Param("clienteId") Long clienteId,
            @Param("estado") EstadoCita estado,
            @Param("mascotaId") Long mascotaId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );

    // Reportes: Citas en rango
    long countByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT c.estado, COUNT(c) FROM CitaMedica c WHERE c.fechaHora BETWEEN :desde AND :hasta GROUP BY c.estado")
    List<Object[]> countByEstadoInRange(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT c.veterinario.id, c.veterinario.nombre, COUNT(c) FROM CitaMedica c WHERE c.fechaHora BETWEEN :desde AND :hasta GROUP BY c.veterinario.id, c.veterinario.nombre")
    List<Object[]> countByVeterinarioInRange(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    // Reporte resumen: Citas pendientes del día
    long countByEstadoAndFechaHoraBetween(EstadoCita estado, LocalDateTime desde, LocalDateTime hasta);
}
