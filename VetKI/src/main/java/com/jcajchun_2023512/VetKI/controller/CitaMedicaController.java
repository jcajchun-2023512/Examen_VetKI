package com.jcajchun_2023512.VetKI.controller;

import com.jcajchun_2023512.VetKI.dto.cita.CitaDTO;
import com.jcajchun_2023512.VetKI.dto.cita.CitaRequest;
import com.jcajchun_2023512.VetKI.service.CitaMedicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaMedicaController {

    private final CitaMedicaService citaMedicaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<CitaDTO> agendarCita(@Valid @RequestBody CitaRequest request) {
        CitaDTO nuevaCita = citaMedicaService.agendarCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaCita);
    }

    @GetMapping("/agenda")
    @PreAuthorize("hasAnyRole('VET', 'ADMIN')")
    public ResponseEntity<List<CitaDTO>> getAgenda(
            @RequestParam(required = false) Long vetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta
    ) {
        List<CitaDTO> agenda = citaMedicaService.getAgenda(vetId, fecha, desde, hasta);
        return ResponseEntity.ok(agenda);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<CitaDTO> cancelarCita(@PathVariable Long id) {
        CitaDTO citaCancelada = citaMedicaService.cancelarCita(id);
        return ResponseEntity.ok(citaCancelada);
    }

    @GetMapping("/mis-citas")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<CitaDTO>> getMisCitas() {
        List<CitaDTO> citas = citaMedicaService.getMisCitas();
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'VET', 'ADMIN')")
    public ResponseEntity<CitaDTO> getCitaPorId(@PathVariable Long id) {
        CitaDTO cita = citaMedicaService.getCitaPorId(id);
        return ResponseEntity.ok(cita);
    }
}
