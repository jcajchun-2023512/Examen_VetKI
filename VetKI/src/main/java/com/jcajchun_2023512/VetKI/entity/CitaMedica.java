package com.jcajchun_2023512.VetKI.entity;

import com.jcajchun_2023512.VetKI.entity.enums.EstadoCita;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que modela una cita médica para una mascota atendida por un veterinario.
 */
@Entity
@Table(name = "citas_medicas", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cita_vet_fecha_hora", columnNames = {"veterinario_id", "fecha_hora"})
    },
    indexes = {
        @Index(name = "idx_cita_vet", columnList = "veterinario_id"),
        @Index(name = "idx_cita_mascota", columnList = "mascota_id"),
        @Index(name = "idx_cita_fecha_hora", columnList = "fecha_hora"),
        @Index(name = "idx_cita_estado", columnList = "estado")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Usuario veterinario;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado;

    @OneToOne(mappedBy = "cita", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ExpedienteClinico expediente;
}
