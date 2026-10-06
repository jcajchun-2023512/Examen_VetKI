package com.jcajchun_2023512.VetKI.dto.auth;

import com.jcajchun_2023512.VetKI.dto.user.UsuarioDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UsuarioDTO usuario;
}
