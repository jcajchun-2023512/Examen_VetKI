package com.jcajchun_2023512.VetKI.dto.expediente;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO de entrada para registrar un expediente clínico derivado de una cita médica.
 * El veterinario provee diagnóstico, tratamiento y peso del animal atendido.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpedienteRequest {

    @NotNull(message = "El ID de la cita médica es obligatorio")
    private Long citaId;

    @NotBlank(message = "El diagnóstico es obligatorio")
    private String diagnostico;

    @NotBlank(message = "El tratamiento es obligatorio")
    private String tratamiento;

    @NotNull(message = "El peso en kilogramos es obligatorio")
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor a 0")
    private BigDecimal pesoKg;
}
