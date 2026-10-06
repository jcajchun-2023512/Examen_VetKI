package com.jcajchun_2023512.VetKI.service;

import com.jcajchun_2023512.VetKI.dto.cita.CitaDTO;
import com.jcajchun_2023512.VetKI.dto.cita.CitaRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CitaMedicaService {

    /**
     * Agenda una nueva cita médica validando:
     * 1. Disponibilidad del veterinario (sin traslapes en ventana de 30 minutos).
     * 2. Límite de 2 citas en estado PENDIENTE por cliente para el mismo día.
     * 3. Pertenencia de la mascota para clientes.
     */
    CitaDTO agendarCita(CitaRequest request);

    /**
     * Consulta la agenda de citas médicas filtrada opcionalmente por veterinario,
     * fecha específica o rango temporal.
     */
    List<CitaDTO> getAgenda(Long vetId, LocalDate fecha, LocalDateTime desde, LocalDateTime hasta);

    /**
     * Cancela una cita médica programada validando que falten más de 2 horas para su inicio.
     */
    CitaDTO cancelarCita(Long id);

    /**
     * Obtiene el listado de citas de las mascotas asociadas al cliente autenticado.
     */
    List<CitaDTO> getMisCitas();

    /**
     * Obtiene una cita médica por su ID.
     */
    CitaDTO getCitaPorId(Long id);
}
