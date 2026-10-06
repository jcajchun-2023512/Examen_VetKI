package com.jcajchun_2023512.VetKI.controller;

import com.jcajchun_2023512.VetKI.dto.expediente.ExpedienteDTO;
import com.jcajchun_2023512.VetKI.dto.expediente.ExpedienteRequest;
import com.jcajchun_2023512.VetKI.service.ExpedienteClinicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de expedientes clínicos veterinarios.
 *
 * POST /api/v1/expedientes          - Registrar expediente (VET, ADMIN) → 201 CREATED
 * GET  /api/v1/expedientes/mascota/{mascotaId} - Historial de mascota (VET, CLIENTE, ADMIN) → 200 OK
 * GET  /api/v1/expedientes/{id}     - Consultar expediente por ID (VET, CLIENTE, ADMIN) → 200 OK
 */
@RestController
@RequestMapping("/api/v1/expedientes")
@RequiredArgsConstructor
public class ExpedienteClinicoController {

    private final ExpedienteClinicoService expedienteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('VET', 'ADMIN')")
    public ResponseEntity<ExpedienteDTO> registrarExpediente(
            @Valid @RequestBody ExpedienteRequest request) {
        ExpedienteDTO expediente = expedienteService.registrarExpediente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(expediente);
    }

    @GetMapping("/mascota/{mascotaId}")
    @PreAuthorize("hasAnyRole('VET', 'CLIENTE', 'ADMIN')")
    public ResponseEntity<List<ExpedienteDTO>> getExpedientesPorMascota(
            @PathVariable Long mascotaId) {
        List<ExpedienteDTO> expedientes = expedienteService.getExpedientesPorMascota(mascotaId);
        return ResponseEntity.ok(expedientes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VET', 'CLIENTE', 'ADMIN')")
    public ResponseEntity<ExpedienteDTO> getExpedientePorId(@PathVariable Long id) {
        ExpedienteDTO expediente = expedienteService.getExpedientePorId(id);
        return ResponseEntity.ok(expediente);
    }
}
