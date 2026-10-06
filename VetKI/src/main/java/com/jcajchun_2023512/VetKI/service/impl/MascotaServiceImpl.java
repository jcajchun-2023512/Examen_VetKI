package com.jcajchun_2023512.VetKI.service.impl;

import com.jcajchun_2023512.VetKI.dto.mascota.MascotaDTO;
import com.jcajchun_2023512.VetKI.dto.mascota.MascotaRequest;
import com.jcajchun_2023512.VetKI.entity.Mascota;
import com.jcajchun_2023512.VetKI.entity.Usuario;
import com.jcajchun_2023512.VetKI.entity.enums.Rol;
import com.jcajchun_2023512.VetKI.exception.BusinessRuleException;
import com.jcajchun_2023512.VetKI.exception.ResourceNotFoundException;
import com.jcajchun_2023512.VetKI.repository.MascotaRepository;
import com.jcajchun_2023512.VetKI.repository.UsuarioRepository;
import com.jcajchun_2023512.VetKI.security.SecurityUtils;
import com.jcajchun_2023512.VetKI.service.MascotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MascotaServiceImpl implements MascotaService {

    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MascotaDTO> getMisMascotas() {
        Long clienteId = SecurityUtils.getAuthenticatedUserId();
        log.info("Consultando mascotas pertenecientes al cliente con ID: {}", clienteId);

        return mascotaRepository.findByClienteIdOrderByIdAsc(clienteId)
                .stream()
                .map(MascotaDTO::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public MascotaDTO registrarMascota(MascotaRequest request) {
        Usuario usuarioAutenticado = SecurityUtils.getAuthenticatedUser();
        Usuario duenoMascota;

        if (usuarioAutenticado.getRol() == Rol.CLIENTE) {
            // El cliente autenticado siempre es el dueño de sus propias mascotas
            duenoMascota = usuarioAutenticado;
        } else if (usuarioAutenticado.getRol() == Rol.ADMIN) {
            // Un administrador puede registrar mascotas para un cliente específico o para sí mismo
            if (request.getClienteId() != null) {
                duenoMascota = usuarioRepository.findById(request.getClienteId())
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + request.getClienteId()));
            } else {
                duenoMascota = usuarioAutenticado;
            }
        } else {
            throw new BusinessRuleException("El rol actual no tiene autorización para registrar mascotas");
        }

        Mascota mascota = Mascota.builder()
                .nombre(request.getNombre().trim())
                .especie(request.getEspecie())
                .raza(request.getRaza() != null ? request.getRaza().trim() : null)
                .edad(request.getEdad())
                .cliente(duenoMascota)
                .build();

        Mascota mascotaGuardada = mascotaRepository.save(mascota);
        log.info("Mascota '{}' registrada exitosamente con ID: {} para el cliente ID: {}",
                mascotaGuardada.getNombre(), mascotaGuardada.getId(), duenoMascota.getId());

        return MascotaDTO.fromEntity(mascotaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public MascotaDTO getMascotaPorId(Long id) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada con ID: " + id));

        Usuario usuarioAutenticado = SecurityUtils.getAuthenticatedUser();
        // Si un cliente intenta consultar, solo puede si la mascota le pertenece
        if (usuarioAutenticado.getRol() == Rol.CLIENTE && !mascota.getCliente().getId().equals(usuarioAutenticado.getId())) {
            throw new BusinessRuleException("No tiene permisos para consultar información de mascotas de otros clientes");
        }

        return MascotaDTO.fromEntity(mascota);
    }
}
