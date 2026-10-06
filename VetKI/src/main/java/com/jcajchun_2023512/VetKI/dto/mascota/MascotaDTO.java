package com.jcajchun_2023512.VetKI.dto.mascota;

import com.jcajchun_2023512.VetKI.dto.user.UsuarioDTO;
import com.jcajchun_2023512.VetKI.entity.Mascota;
import com.jcajchun_2023512.VetKI.entity.enums.Especie;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MascotaDTO {

    private Long id;
    private String nombre;
    private Especie especie;
    private String raza;
    private Integer edad;
    private UsuarioDTO cliente;

    public static MascotaDTO fromEntity(Mascota mascota) {
        if (mascota == null) {
            return null;
        }
        return MascotaDTO.builder()
                .id(mascota.getId())
                .nombre(mascota.getNombre())
                .especie(mascota.getEspecie())
                .raza(mascota.getRaza())
                .edad(mascota.getEdad())
                .cliente(UsuarioDTO.fromEntity(mascota.getCliente()))
                .build();
    }
}
