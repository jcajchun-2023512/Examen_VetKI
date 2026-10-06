package com.jcajchun_2023512.VetKI.service;

import com.jcajchun_2023512.VetKI.dto.cita.CitaDTO;
import com.jcajchun_2023512.VetKI.dto.cita.CitaRequest;
import com.jcajchun_2023512.VetKI.entity.CitaMedica;
import com.jcajchun_2023512.VetKI.entity.Mascota;
import com.jcajchun_2023512.VetKI.entity.Usuario;
import com.jcajchun_2023512.VetKI.entity.enums.Especie;
import com.jcajchun_2023512.VetKI.entity.enums.EstadoCita;
import com.jcajchun_2023512.VetKI.entity.enums.Rol;
import com.jcajchun_2023512.VetKI.exception.BusinessRuleException;
import com.jcajchun_2023512.VetKI.exception.ResourceNotFoundException;
import com.jcajchun_2023512.VetKI.repository.CitaMedicaRepository;
import com.jcajchun_2023512.VetKI.repository.MascotaRepository;
import com.jcajchun_2023512.VetKI.repository.UsuarioRepository;
import com.jcajchun_2023512.VetKI.service.impl.CitaMedicaServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitaMedicaServiceTest {

    @Mock
    private CitaMedicaRepository citaMedicaRepository;

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CitaMedicaServiceImpl citaMedicaService;

    private Usuario clienteUser;
    private Usuario vetUser;
    private Mascota mascota;

    @BeforeEach
    void setUp() {
        clienteUser = Usuario.builder()
                .id(1L)
                .nombre("Cliente Uno")
                .email("cliente@vetki.com")
                .rol(Rol.CLIENTE)
                .build();

        vetUser = Usuario.builder()
                .id(2L)
                .nombre("Dr. Veterinario")
                .email("vet@vetki.com")
                .rol(Rol.VET)
                .build();

        mascota = Mascota.builder()
                .id(10L)
                .nombre("Bobby")
                .especie(Especie.PERRO)
                .edad(4)
                .cliente(clienteUser)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateUser(Usuario usuario) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("Agendar cita exitosamente cumpliendo todas las reglas")
    void agendarCita_Exitoso() {
        authenticateUser(clienteUser);

        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0);

        CitaRequest request = CitaRequest.builder()
                .mascotaId(10L)
                .veterinarioId(2L)
                .fechaHora(fechaFutura)
                .motivo("Vacunación anual")
                .build();

        when(mascotaRepository.findById(10L)).thenReturn(Optional.of(mascota));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(vetUser));
        when(citaMedicaRepository.existsByVeterinarioIdAndEstadoNotAndFechaHoraAfterAndFechaHoraBefore(
                eq(2L), eq(EstadoCita.CANCELADA), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(citaMedicaRepository.countByMascotaClienteIdAndEstadoAndFechaHoraBetween(
                eq(1L), eq(EstadoCita.PENDIENTE), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        CitaMedica guardada = CitaMedica.builder()
                .id(100L)
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(fechaFutura)
                .motivo("Vacunación anual")
                .estado(EstadoCita.PENDIENTE)
                .build();

        when(citaMedicaRepository.save(any(CitaMedica.class))).thenReturn(guardada);

        CitaDTO resultado = citaMedicaService.agendarCita(request);

        assertNotNull(resultado);
        assertEquals(100L, resultado.getId());
        assertEquals(EstadoCita.PENDIENTE, resultado.getEstado());
        assertEquals("Bobby", resultado.getMascota().getNombre());
        verify(citaMedicaRepository, times(1)).save(any(CitaMedica.class));
    }

    @Test
    @DisplayName("Regla 1: Rechazar agendamiento por traslape con otra cita del veterinario en ventana de 30 min")
    void agendarCita_Regla1_TraslapeVeterinario() {
        authenticateUser(clienteUser);

        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0);

        CitaRequest request = CitaRequest.builder()
                .mascotaId(10L)
                .veterinarioId(2L)
                .fechaHora(fechaFutura)
                .motivo("Consulta general")
                .build();

        when(mascotaRepository.findById(10L)).thenReturn(Optional.of(mascota));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(vetUser));
        when(citaMedicaRepository.existsByVeterinarioIdAndEstadoNotAndFechaHoraAfterAndFechaHoraBefore(
                eq(2L), eq(EstadoCita.CANCELADA), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> citaMedicaService.agendarCita(request));
        assertTrue(ex.getMessage().contains("traslapa"));
        verify(citaMedicaRepository, never()).save(any(CitaMedica.class));
    }

    @Test
    @DisplayName("Regla 2: Rechazar agendamiento si el cliente ya tiene 2 citas PENDIENTES para el mismo día")
    void agendarCita_Regla2_LimiteDiarioCliente() {
        authenticateUser(clienteUser);

        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0);

        CitaRequest request = CitaRequest.builder()
                .mascotaId(10L)
                .veterinarioId(2L)
                .fechaHora(fechaFutura)
                .motivo("Tercera cita del día")
                .build();

        when(mascotaRepository.findById(10L)).thenReturn(Optional.of(mascota));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(vetUser));
        when(citaMedicaRepository.existsByVeterinarioIdAndEstadoNotAndFechaHoraAfterAndFechaHoraBefore(
                eq(2L), eq(EstadoCita.CANCELADA), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);
        when(citaMedicaRepository.countByMascotaClienteIdAndEstadoAndFechaHoraBetween(
                eq(1L), eq(EstadoCita.PENDIENTE), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(2L); // Ya tiene 2 citas pendientes

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> citaMedicaService.agendarCita(request));
        assertTrue(ex.getMessage().contains("2 citas"));
        verify(citaMedicaRepository, never()).save(any(CitaMedica.class));
    }

    @Test
    @DisplayName("Validación: CLIENTE no puede agendar cita para mascota que no le pertenece")
    void agendarCita_MascotaAjena_Rechazada() {
        authenticateUser(clienteUser);

        Usuario otroCliente = Usuario.builder().id(99L).nombre("Otro").rol(Rol.CLIENTE).build();
        Mascota mascotaAjena = Mascota.builder()
                .id(55L)
                .nombre("Michi")
                .especie(Especie.GATO)
                .cliente(otroCliente)
                .build();

        CitaRequest request = CitaRequest.builder()
                .mascotaId(55L)
                .veterinarioId(2L)
                .fechaHora(LocalDateTime.now().plusDays(1))
                .motivo("Control")
                .build();

        when(mascotaRepository.findById(55L)).thenReturn(Optional.of(mascotaAjena));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> citaMedicaService.agendarCita(request));
        assertTrue(ex.getMessage().contains("no le pertenece"));
    }

    @Test
    @DisplayName("Validación: Usuario asignado debe poseer el rol VET")
    void agendarCita_UsuarioNoEsVet_Rechazada() {
        authenticateUser(clienteUser);

        Usuario usuarioNoVet = Usuario.builder().id(88L).nombre("No Vet").rol(Rol.CLIENTE).build();

        CitaRequest request = CitaRequest.builder()
                .mascotaId(10L)
                .veterinarioId(88L)
                .fechaHora(LocalDateTime.now().plusDays(1))
                .motivo("Control")
                .build();

        when(mascotaRepository.findById(10L)).thenReturn(Optional.of(mascota));
        when(usuarioRepository.findById(88L)).thenReturn(Optional.of(usuarioNoVet));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> citaMedicaService.agendarCita(request));
        assertTrue(ex.getMessage().contains("rol de VETERINARIO"));
    }

    @Test
    @DisplayName("Cancelar cita exitosamente con más de 2 horas de anticipación")
    void cancelarCita_Exitoso() {
        authenticateUser(clienteUser);

        // Cita programada dentro de 5 horas
        LocalDateTime fechaCita = LocalDateTime.now().plusHours(5);

        CitaMedica cita = CitaMedica.builder()
                .id(200L)
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(fechaCita)
                .motivo("Revisión")
                .estado(EstadoCita.PENDIENTE)
                .build();

        when(citaMedicaRepository.findById(200L)).thenReturn(Optional.of(cita));
        when(citaMedicaRepository.save(any(CitaMedica.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CitaDTO resultado = citaMedicaService.cancelarCita(200L);

        assertNotNull(resultado);
        assertEquals(EstadoCita.CANCELADA, resultado.getEstado());
        verify(citaMedicaRepository, times(1)).save(cita);
    }

    @Test
    @DisplayName("Regla 3: Rechazar cancelación si faltan 2 horas o menos")
    void cancelarCita_Regla3_MenosDe2Horas() {
        authenticateUser(clienteUser);

        // Cita programada dentro de 1 hora (menos de 2 horas)
        LocalDateTime fechaCita = LocalDateTime.now().plusHours(1);

        CitaMedica cita = CitaMedica.builder()
                .id(201L)
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(fechaCita)
                .motivo("Revisión")
                .estado(EstadoCita.PENDIENTE)
                .build();

        when(citaMedicaRepository.findById(201L)).thenReturn(Optional.of(cita));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> citaMedicaService.cancelarCita(201L));
        assertTrue(ex.getMessage().contains("2 horas de anticipación"));
        verify(citaMedicaRepository, never()).save(any(CitaMedica.class));
    }

    @Test
    @DisplayName("Cancelar cita rechazada si ya se encuentra CANCELADA")
    void cancelarCita_YaCancelada_Rechazada() {
        authenticateUser(clienteUser);

        CitaMedica cita = CitaMedica.builder()
                .id(202L)
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(LocalDateTime.now().plusHours(4))
                .motivo("Revisión")
                .estado(EstadoCita.CANCELADA)
                .build();

        when(citaMedicaRepository.findById(202L)).thenReturn(Optional.of(cita));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> citaMedicaService.cancelarCita(202L));
        assertTrue(ex.getMessage().contains("CANCELADA"));
    }

    @Test
    @DisplayName("Cancelar cita rechazada si ya se encuentra COMPLETADA")
    void cancelarCita_YaCompletada_Rechazada() {
        authenticateUser(clienteUser);

        CitaMedica cita = CitaMedica.builder()
                .id(203L)
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(LocalDateTime.now().plusHours(4))
                .motivo("Revisión")
                .estado(EstadoCita.COMPLETADA)
                .build();

        when(citaMedicaRepository.findById(203L)).thenReturn(Optional.of(cita));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> citaMedicaService.cancelarCita(203L));
        assertTrue(ex.getMessage().contains("COMPLETADA"));
    }
}
