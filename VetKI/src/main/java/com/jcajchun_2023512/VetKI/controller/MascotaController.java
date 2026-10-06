package com.jcajchun_2023512.VetKI.controller;

import com.jcajchun_2023512.VetKI.dto.mascota.MascotaDTO;
import com.jcajchun_2023512.VetKI.dto.mascota.MascotaRequest;
import com.jcajchun_2023512.VetKI.service.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;

    @GetMapping("/mis-mascotas")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<MascotaDTO>> getMisMascotas() {
        List<MascotaDTO> mascotas = mascotaService.getMisMascotas();
        return ResponseEntity.ok(mascotas);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    public ResponseEntity<MascotaDTO> registrarMascota(@Valid @RequestBody MascotaRequest request) {
        MascotaDTO nuevaMascota = mascotaService.registrarMascota(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaMascota);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VET', 'ADMIN')")
    public ResponseEntity<MascotaDTO> getMascotaPorId(@PathVariable Long id) {
        MascotaDTO mascota = mascotaService.getMascotaPorId(id);
        return ResponseEntity.ok(mascota);
    }
}
