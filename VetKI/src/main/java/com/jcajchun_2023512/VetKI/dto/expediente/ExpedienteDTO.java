package com.jcajchun_2023512.VetKI.dto.expediente;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcajchun_2023512.VetKI.entity.ExpedienteClinico;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para un expediente clínico.
 * Incluye datos básicos de la cita (ID, mascota, veterinario, fecha) sin referencias
 * circulares para evitar problemas de serialización con Jackson.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpedienteDTO {

    private Long id;

    // Datos mínimos de la cita asociada para evitar ciclos de serialización
    private Long citaId;
    private String mascotaNombre;
    private String mascotaEspecie;
    private Long mascotaId;
    private String veterinarioNombre;
    private Long veterinarioId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaHoraCita;

    private String motivoCita;
    private String diagnostico;
    private String tratamiento;
    private BigDecimal pesoKg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaRegistro;

    /**
     * Método estático de mapeo desde la entidad al DTO,
     * evitando referencias bidireccionales (Jackson circular reference).
     */
    public static ExpedienteDTO fromEntity(ExpedienteClinico expediente) {
        if (expediente == null) {
            return null;
        }

        ExpedienteDTOBuilder builder = ExpedienteDTO.builder()
                .id(expediente.getId())
                .diagnostico(expediente.getDiagnostico())
                .tratamiento(expediente.getTratamiento())
                .pesoKg(expediente.getPesoKg())
                .fechaRegistro(expediente.getFechaRegistro());

        if (expediente.getCita() != null) {
            builder.citaId(expediente.getCita().getId())
                    .fechaHoraCita(expediente.getCita().getFechaHora())
                    .motivoCita(expediente.getCita().getMotivo());

            if (expediente.getCita().getMascota() != null) {
                builder.mascotaId(expediente.getCita().getMascota().getId())
                        .mascotaNombre(expediente.getCita().getMascota().getNombre())
                        .mascotaEspecie(expediente.getCita().getMascota().getEspecie() != null
                                ? expediente.getCita().getMascota().getEspecie().name()
                                : null);
            }

            if (expediente.getCita().getVeterinario() != null) {
                builder.veterinarioId(expediente.getCita().getVeterinario().getId())
                        .veterinarioNombre(expediente.getCita().getVeterinario().getNombre());
            }
        }

        return builder.build();
    }
}
