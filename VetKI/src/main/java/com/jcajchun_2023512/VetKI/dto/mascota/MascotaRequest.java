package com.jcajchun_2023512.VetKI.dto.mascota;

import com.jcajchun_2023512.VetKI.entity.enums.Especie;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MascotaRequest {

    @NotBlank(message = "El nombre de la mascota es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotNull(message = "La especie es obligatoria (PERRO, GATO, AVE, OTRO)")
    private Especie especie;

    @Size(max = 100, message = "La raza no puede exceder 100 caracteres")
    private String raza;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad debe ser mayor o igual a 0")
    private Integer edad;

    /**
     * Opcional: Solo utilizado por usuarios con rol ADMIN para asociar
     * la mascota a un cliente específico. Para clientes autenticados,
     * se asocia automáticamente a su propio usuario.
     */
    private Long clienteId;
}
