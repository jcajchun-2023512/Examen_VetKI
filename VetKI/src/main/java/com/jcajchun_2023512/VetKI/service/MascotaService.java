package com.jcajchun_2023512.VetKI.service;

import com.jcajchun_2023512.VetKI.dto.mascota.MascotaDTO;
import com.jcajchun_2023512.VetKI.dto.mascota.MascotaRequest;

import java.util.List;

public interface MascotaService {

    /**
     * Obtiene el listado de mascotas pertenecientes al cliente autenticado en el token JWT.
     */
    List<MascotaDTO> getMisMascotas();

    /**
     * Registra una nueva mascota. Si el usuario es CLIENTE, se asocia automáticamente a él.
     * Si es ADMIN, puede asociarla a cualquier cliente mediante clienteId en el payload.
     */
    MascotaDTO registrarMascota(MascotaRequest request);

    /**
     * Consulta la información detallada de una mascota por su ID.
     */
    MascotaDTO getMascotaPorId(Long id);
}
