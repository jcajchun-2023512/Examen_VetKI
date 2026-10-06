package com.jcajchun_2023512.VetKI.service.impl;

import com.jcajchun_2023512.VetKI.dto.cita.CitaDTO;
import com.jcajchun_2023512.VetKI.dto.cita.CitaRequest;
import com.jcajchun_2023512.VetKI.entity.CitaMedica;
import com.jcajchun_2023512.VetKI.entity.Mascota;
import com.jcajchun_2023512.VetKI.entity.Usuario;
import com.jcajchun_2023512.VetKI.entity.enums.EstadoCita;
import com.jcajchun_2023512.VetKI.entity.enums.Rol;
import com.jcajchun_2023512.VetKI.exception.BusinessRuleException;
import com.jcajchun_2023512.VetKI.exception.ResourceNotFoundException;
import com.jcajchun_2023512.VetKI.repository.CitaMedicaRepository;
import com.jcajchun_2023512.VetKI.repository.MascotaRepository;
import com.jcajchun_2023512.VetKI.repository.UsuarioRepository;
import com.jcajchun_2023512.VetKI.security.SecurityUtils;
import com.jcajchun_2023512.VetKI.service.CitaMedicaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CitaMedicaServiceImpl implements CitaMedicaService {

    private final CitaMedicaRepository citaMedicaRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public CitaDTO agendarCita(CitaRequest request) {
        Usuario usuarioAutenticado = SecurityUtils.getAuthenticatedUser();

        // 1. Validar existencia de la mascota
        Mascota mascota = mascotaRepository.findById(request.getMascotaId())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + request.getMascotaId()));

        // Validar pertenencia de la mascota si el solicitante es CLIENTE
        if (usuarioAutenticado.getRol() == Rol.CLIENTE && !mascota.getCliente().getId().equals(usuarioAutenticado.getId())) {
            throw new BusinessRuleException("No puede agendar citas para una mascota que no le pertenece");
        }

        // 2. Validar existencia y rol del veterinario
        Usuario veterinario = usuarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario no encontrado con ID: " + request.getVeterinarioId()));

        if (veterinario.getRol() != Rol.VET) {
            throw new BusinessRuleException("El usuario asignado con ID " + request.getVeterinarioId() + " no posee el rol de VETERINARIO");
        }

        // 3. Validar que la fecha y hora sea en el futuro
        if (request.getFechaHora().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("La fecha y hora de la cita debe ser posterior a la fecha y hora actual");
        }

        // 4. Regla 1: Disponibilidad del Veterinario (30 minutos sin traslape)
        LocalDateTime inicioSlot = request.getFechaHora().minusMinutes(30);
        LocalDateTime finSlot = request.getFechaHora().plusMinutes(30);

        boolean hayTraslape = citaMedicaRepository
                .existsByVeterinarioIdAndEstadoNotAndFechaHoraAfterAndFechaHoraBefore(
                        veterinario.getId(),
                        EstadoCita.CANCELADA,
                        inicioSlot,
                        finSlot
                );

        if (hayTraslape) {
            throw new BusinessRuleException("El médico veterinario ya cuenta con una cita programada que se traslapa con el horario seleccionado. Cada cita tiene una duración de 30 minutos.");
        }

        // 5. Regla 2: Límite diario de 2 citas PENDIENTES por cliente para el mismo día
        Long clienteId = mascota.getCliente().getId();
        LocalDateTime inicioDia = request.getFechaHora().toLocalDate().atStartOfDay();
        LocalDateTime finDia = request.getFechaHora().toLocalDate().atTime(23, 59, 59, 999999999);

        long citasPendientesCliente = citaMedicaRepository
                .countByMascotaClienteIdAndEstadoAndFechaHoraBetween(
                        clienteId,
                        EstadoCita.PENDIENTE,
                        inicioDia,
                        finDia
                );

        if (citasPendientesCliente >= 2) {
            throw new BusinessRuleException("El cliente ya cuenta con 2 citas en estado PENDIENTE para la fecha "
                    + request.getFechaHora().toLocalDate() + ". No es posible programar más de 2 citas pendientes para el mismo día.");
        }

        // 6. Construir y guardar la cita médica
        CitaMedica nuevaCita = CitaMedica.builder()
                .mascota(mascota)
                .veterinario(veterinario)
                .fechaHora(request.getFechaHora())
                .motivo(request.getMotivo().trim())
                .estado(EstadoCita.PENDIENTE)
                .build();

        CitaMedica citaGuardada = citaMedicaRepository.save(nuevaCita);
        log.info("Cita médica agendada exitosamente con ID: {} para la mascota '{}' con el veterinario '{}' en {}",
                citaGuardada.getId(), mascota.getNombre(), veterinario.getNombre(), citaGuardada.getFechaHora());

        return CitaDTO.fromEntity(citaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaDTO> getAgenda(Long vetId, LocalDate fecha, LocalDateTime desde, LocalDateTime hasta) {
        Usuario usuarioAutenticado = SecurityUtils.getAuthenticatedUser();
        Long effectiveVetId = vetId;

        // Si es veterinario y no se especificó un vetId, se consulta su propia agenda
        if (usuarioAutenticado.getRol() == Rol.VET && effectiveVetId == null) {
            effectiveVetId = usuarioAutenticado.getId();
        }

        // Si se provee una fecha específica, calcular el rango de inicio y fin del día
        if (fecha != null) {
            desde = fecha.atStartOfDay();
            hasta = fecha.atTime(23, 59, 59, 999999999);
        }

        log.info("Consultando agenda para vetId: {}, desde: {}, hasta: {}", effectiveVetId, desde, hasta);

        return citaMedicaRepository.findAgenda(effectiveVetId, desde, hasta)
                .stream()
                .map(CitaDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public CitaDTO cancelarCita(Long id) {
        Usuario usuarioAutenticado = SecurityUtils.getAuthenticatedUser();

        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita médica no encontrada con ID: " + id));

        // Validar permisos si el usuario es CLIENTE
        if (usuarioAutenticado.getRol() == Rol.CLIENTE && !cita.getMascota().getCliente().getId().equals(usuarioAutenticado.getId())) {
            throw new BusinessRuleException("No tiene permisos para cancelar citas de mascotas que no le pertenecen");
        }

        // Validar estado actual de la cita
        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new BusinessRuleException("La cita ya se encuentra en estado CANCELADA");
        }

        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            throw new BusinessRuleException("No se puede cancelar una cita médica que ya ha sido COMPLETADA");
        }

        // Regla 3: Cancelación con anticipación mayor a 2 horas
        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isAfter(cita.getFechaHora())) {
            throw new BusinessRuleException("No es posible cancelar una cita cuya fecha y hora programada ya ha transcurrido");
        }

        Duration tiempoRestante = Duration.between(ahora, cita.getFechaHora());
        if (tiempoRestante.toMinutes() <= 120) {
            throw new BusinessRuleException("Una cita médica solo puede ser cancelada con más de 2 horas de anticipación a la hora programada");
        }

        cita.setEstado(EstadoCita.CANCELADA);
        CitaMedica citaActualizada = citaMedicaRepository.save(cita);
        log.info("Cita médica ID: {} cancelada exitosamente por el usuario ID: {}", id, usuarioAutenticado.getId());

        return CitaDTO.fromEntity(citaActualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaDTO> getMisCitas() {
        Long clienteId = SecurityUtils.getAuthenticatedUserId();
        log.info("Consultando historial de citas para el cliente autenticado ID: {}", clienteId);

        return citaMedicaRepository.findByMascotaClienteIdOrderByFechaHoraDesc(clienteId)
                .stream()
                .map(CitaDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CitaDTO getCitaPorId(Long id) {
        Usuario usuarioAutenticado = SecurityUtils.getAuthenticatedUser();
        CitaMedica cita = citaMedicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita médica no encontrada con ID: " + id));

        if (usuarioAutenticado.getRol() == Rol.CLIENTE && !cita.getMascota().getCliente().getId().equals(usuarioAutenticado.getId())) {
            throw new BusinessRuleException("No tiene permisos para consultar información de citas de otros clientes");
        }

        return CitaDTO.fromEntity(cita);
    }
}
