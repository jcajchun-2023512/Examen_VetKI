package com.jcajchun_2023512.VetKI.service;

import com.jcajchun_2023512.VetKI.dto.expediente.ExpedienteDTO;
import com.jcajchun_2023512.VetKI.dto.expediente.ExpedienteRequest;

import java.util.List;

/**
 * Contrato de servicio para la gestión de expedientes clínicos.
 */
public interface ExpedienteClinicoService {

    /**
     * Registra el diagnóstico y tratamiento de una cita médica.
     * Cambia el estado de la cita de PENDIENTE a COMPLETADA.
     * Solo usuarios con rol VET o ADMIN pueden ejecutar esta operación.
     *
     * @param request datos del expediente (citaId, diagnóstico, tratamiento, pesoKg)
     * @return ExpedienteDTO con los datos del expediente persistido
     */
    ExpedienteDTO registrarExpediente(ExpedienteRequest request);

    /**
     * Consulta el historial completo de expedientes clínicos de una mascota,
     * ordenado del más reciente al más antiguo.
     * Accesible por VET, ADMIN y el CLIENTE propietario de la mascota.
     *
     * @param mascotaId ID de la mascota
     * @return Lista de ExpedienteDTO ordenada por fechaRegistro DESC
     */
    List<ExpedienteDTO> getExpedientesPorMascota(Long mascotaId);

    /**
     * Consulta un expediente clínico específico por su ID.
     *
     * @param id ID del expediente
     * @return ExpedienteDTO correspondiente
     */
    ExpedienteDTO getExpedientePorId(Long id);
}
