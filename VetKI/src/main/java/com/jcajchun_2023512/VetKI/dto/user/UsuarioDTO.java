package com.jcajchun_2023512.VetKI.dto.user;

import com.jcajchun_2023512.VetKI.entity.enums.Rol;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private Rol rol;

    public static UsuarioDTO fromEntity(com.jcajchun_2023512.VetKI.entity.Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol())
                .build();
    }
}
