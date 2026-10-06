package com.jcajchun_2023512.VetKI.dto.cita;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcajchun_2023512.VetKI.dto.mascota.MascotaDTO;
import com.jcajchun_2023512.VetKI.dto.user.UsuarioDTO;
import com.jcajchun_2023512.VetKI.entity.CitaMedica;
import com.jcajchun_2023512.VetKI.entity.enums.EstadoCita;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaDTO {

    private Long id;
    private MascotaDTO mascota;
    private UsuarioDTO veterinario;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaHora;

    private String motivo;
    private EstadoCita estado;

    public static CitaDTO fromEntity(CitaMedica cita) {
        if (cita == null) {
            return null;
        }
        return CitaDTO.builder()
                .id(cita.getId())
                .mascota(MascotaDTO.fromEntity(cita.getMascota()))
                .veterinario(UsuarioDTO.fromEntity(cita.getVeterinario()))
                .fechaHora(cita.getFechaHora())
                .motivo(cita.getMotivo())
                .estado(cita.getEstado())
                .build();
    }
}
