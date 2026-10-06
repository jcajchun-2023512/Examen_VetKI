package com.jcajchun_2023512.VetKI.entity.enums;

/**
 * Estados del ciclo de vida de una cita médica:
 * - PENDIENTE: Estado inicial.
 * - COMPLETADA: Estado final asignado exclusivamente tras el registro de expediente clínico.
 * - CANCELADA: Estado final asignado tras cancelación por cliente, admin o veterinario.
 */
public enum EstadoCita {
    PENDIENTE,
    COMPLETADA,
    CANCELADA
}
