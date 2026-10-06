package com.jcajchun_2023512.VetKI.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe ser una dirección válida")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
