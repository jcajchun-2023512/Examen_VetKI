package com.jcajchun_2023512.VetKI.service.impl;

import com.jcajchun_2023512.VetKI.dto.expediente.ExpedienteDTO;
import com.jcajchun_2023512.VetKI.dto.expediente.ExpedienteRequest;
import com.jcajchun_2023512.VetKI.entity.CitaMedica;
import com.jcajchun_2023512.VetKI.entity.ExpedienteClinico;
import com.jcajchun_2023512.VetKI.entity.Mascota;
import com.jcajchun_2023512.VetKI.entity.Usuario;
import com.jcajchun_2023512.VetKI.entity.enums.EstadoCita;
import com.jcajchun_2023512.VetKI.entity.enums.Rol;
import com.jcajchun_2023512.VetKI.exception.BusinessRuleException;
import com.jcajchun_2023512.VetKI.exception.ResourceNotFoundException;
import com.jcajchun_2023512.VetKI.repository.CitaMedicaRepository;
import com.jcajchun_2023512.VetKI.repository.ExpedienteClinicoRepository;
import com.jcajchun_2023512.VetKI.repository.MascotaRepository;
import com.jcajchun_2023512.VetKI.security.SecurityUtils;
import com.jcajchun_2023512.VetKI.service.ExpedienteClinicoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpedienteClinicoServiceImpl implements ExpedienteClinicoService {

    private final ExpedienteClinicoRepository expedienteRepository;
    private final CitaMedicaRepository citaMedicaRepository;
    private final MascotaRepository mascotaRepository;

    @Override
    @Transactional
    public ExpedienteDTO registrarExpediente(ExpedienteRequest request) {
        Usuario usuarioAutenticado = SecurityUtils.getAuthenticatedUser();

        // 1. Verificar que el rol sea VET o ADMIN (doble capa de seguridad)
        if (usuarioAutenticado.getRol() != Rol.VET && usuarioAutenticado.getRol() != Rol.ADMIN) {
            throw new BusinessRuleException("Solo los veterinarios y administradores pueden registrar expedientes clínicos");
        }

        // 2. Verificar que la cita existe y está en estado PENDIENTE
        CitaMedica cita = citaMedicaRepository.findById(request.getCitaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cita médica no encontrada con ID: " + request.getCitaId()));

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new BusinessRuleException(
                    "No se puede registrar un expediente para una cita con estado CANCELADA");
        }

        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new BusinessRuleException(
                    "Esta cita médica ya tiene un expediente clínico registrado (estado: COMPLETADA)");
        }

        // 3. Validar que no exista ya un expediente previo para esta cita (unicidad)
        if (expedienteRepository.existsByCitaId(request.getCitaId())) {
            throw new BusinessRuleException(
                    "Ya existe un expediente clínico registrado para la cita con ID: " + request.getCitaId());
        }

        // 4. Si el veterinario autenticado es VET, validar que es el veterinario asignado a la cita
        if (usuarioAutenticado.getRol() == Rol.VET
                && !cita.getVeterinario().getId().equals(usuarioAutenticado.getId())) {
            throw new BusinessRuleException(
                    "El veterinario autenticado no está asignado a esta cita médica");
        }

        // 5. Construir y persistir el expediente
        ExpedienteClinico expediente = ExpedienteClinico.builder()
                .cita(cita)
                .diagnostico(request.getDiagnostico().trim())
                .tratamiento(request.getTratamiento().trim())
                .pesoKg(request.getPesoKg())
                .fechaRegistro(LocalDateTime.now())
                .build();

        ExpedienteClinico expedienteGuardado = expedienteRepository.save(expediente);

        // 6. Cambiar el estado de la cita a COMPLETADA
        cita.setEstado(EstadoCita.COMPLETADA);
        citaMedicaRepository.save(cita);

        log.info("Expediente clínico ID: {} registrado exitosamente para la cita ID: {} por el usuario ID: {}. Cita marcada como COMPLETADA.",
                expedienteGuardado.getId(), cita.getId(), usuarioAutenticado.getId());

        return ExpedienteDTO.fromEntity(expedienteGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpedienteDTO> getExpedientesPorMascota(Long mascotaId) {
        Usuario usuarioAutenticado = SecurityUtils.getAuthenticatedUser();

        // Verificar que la mascota existe
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mascota no encontrada con ID: " + mascotaId));

        // Si el usuario es CLIENTE, solo puede ver el historial de sus propias mascotas
        if (usuarioAutenticado.getRol() == Rol.CLIENTE
                && !mascota.getCliente().getId().equals(usuarioAutenticado.getId())) {
            throw new BusinessRuleException(
                    "No tiene permisos para consultar el historial clínico de mascotas que no le pertenecen");
        }

        log.info("Consultando expedientes de la mascota ID: {} por el usuario ID: {}",
                mascotaId, usuarioAutenticado.getId());

        return expedienteRepository.findByCitaMascotaIdOrderByFechaRegistroDesc(mascotaId)
                .stream()
                .map(ExpedienteDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExpedienteDTO getExpedientePorId(Long id) {
        Usuario usuarioAutenticado = SecurityUtils.getAuthenticatedUser();

        ExpedienteClinico expediente = expedienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expediente clínico no encontrado con ID: " + id));

        // Si es CLIENTE, solo puede ver expedientes de sus propias mascotas
        if (usuarioAutenticado.getRol() == Rol.CLIENTE
                && !expediente.getCita().getMascota().getCliente().getId().equals(usuarioAutenticado.getId())) {
            throw new BusinessRuleException(
                    "No tiene permisos para consultar expedientes de mascotas que no le pertenecen");
        }

        return ExpedienteDTO.fromEntity(expediente);
    }
}
