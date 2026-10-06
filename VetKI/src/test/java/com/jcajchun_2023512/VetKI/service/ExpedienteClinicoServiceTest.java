package com.jcajchun_2023512.VetKI.service;

import com.jcajchun_2023512.VetKI.dto.expediente.ExpedienteDTO;
import com.jcajchun_2023512.VetKI.dto.expediente.ExpedienteRequest;
import com.jcajchun_2023512.VetKI.entity.CitaMedica;
import com.jcajchun_2023512.VetKI.entity.ExpedienteClinico;
import com.jcajchun_2023512.VetKI.entity.Mascota;
import com.jcajchun_2023512.VetKI.entity.Usuario;
import com.jcajchun_2023512.VetKI.entity.enums.Especie;
import com.jcajchun_2023512.VetKI.entity.enums.EstadoCita;
import com.jcajchun_2023512.VetKI.entity.enums.Rol;
import com.jcajchun_2023512.VetKI.exception.BusinessRuleException;
import com.jcajchun_2023512.VetKI.exception.ResourceNotFoundException;
import com.jcajchun_2023512.VetKI.repository.CitaMedicaRepository;
import com.jcajchun_2023512.VetKI.repository.ExpedienteClinicoRepository;
import com.jcajchun_2023512.VetKI.repository.MascotaRepository;
import com.jcajchun_2023512.VetKI.service.impl.ExpedienteClinicoServiceImpl;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpedienteClinicoServiceTest {

    @Mock
    private ExpedienteClinicoRepository expedienteRepository;
    @Mock
    private CitaMedicaRepository citaMedicaRepository;
    @Mock
    private MascotaRepository mascotaRepository;

    @InjectMocks
    private ExpedienteClinicoServiceImpl expedienteService;

    private Usuario vetUser;
    private Usuario clienteUser;
    private Mascota mascota;
    private CitaMedica citaPendiente;

    @BeforeEach
    void setUp() {
        clienteUser = Usuario.builder()
                .id(1L).nombre("Cliente Test").email("cliente@vetki.com").rol(Rol.CLIENTE).build();

        vetUser = Usuario.builder()
                .id(2L).nombre("Dr. Vet Test").email("vet@vetki.com").rol(Rol.VET).build();

        mascota = Mascota.builder()
                .id(10L).nombre("Bobby").especie(Especie.PERRO).edad(3).cliente(clienteUser).build();

        citaPendiente = CitaMedica.builder()
                .id(100L)
                .mascota(mascota)
                .veterinario(vetUser)
                .fechaHora(LocalDateTime.now().plusDays(1))
                .motivo("Vacunación")
                .estado(EstadoCita.PENDIENTE)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(Usuario usuario) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                usuario, null, usuario.getAuthorities()));
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    @DisplayName("registrarExpediente: VET asignado registra exitosamente y cita pasa a COMPLETADA")
    void registrarExpediente_VetAsignado_Exitoso() {
        authenticate(vetUser);

        ExpedienteRequest request = ExpedienteRequest.builder()
                .citaId(100L)
                .diagnostico("Gripe canina leve")
                .tratamiento("Antibiótico 5 días")
                .pesoKg(new BigDecimal("12.50"))
                .build();

        when(citaMedicaRepository.findById(100L)).thenReturn(Optional.of(citaPendiente));
        when(expedienteRepository.existsByCitaId(100L)).thenReturn(false);

        ExpedienteClinico guardado = ExpedienteClinico.builder()
                .id(50L)
                .cita(citaPendiente)
                .diagnostico("Gripe canina leve")
                .tratamiento("Antibiótico 5 días")
                .pesoKg(new BigDecimal("12.50"))
                .fechaRegistro(LocalDateTime.now())
                .build();

        when(expedienteRepository.save(any(ExpedienteClinico.class))).thenReturn(guardado);
        when(citaMedicaRepository.save(any(CitaMedica.class))).thenReturn(citaPendiente);

        ExpedienteDTO resultado = expedienteService.registrarExpediente(request);

        assertNotNull(resultado);
        assertEquals(50L, resultado.getId());
        assertEquals("Gripe canina leve", resultado.getDiagnostico());
        assertEquals("Bobby", resultado.getMascotaNombre());
        assertEquals(EstadoCita.COMPLETADA, citaPendiente.getEstado());
        verify(citaMedicaRepository, times(1)).save(citaPendiente);
        verify(expedienteRepository, times(1)).save(any(ExpedienteClinico.class));
    }

    @Test
    @DisplayName("registrarExpediente: Rechazado si la cita ya está COMPLETADA")
    void registrarExpediente_CitaCompletada_Rechazado() {
        authenticate(vetUser);
        citaPendiente.setEstado(EstadoCita.COMPLETADA);

        when(citaMedicaRepository.findById(100L)).thenReturn(Optional.of(citaPendiente));

        ExpedienteRequest request = ExpedienteRequest.builder()
                .citaId(100L).diagnostico("X").tratamiento("Y").pesoKg(BigDecimal.TEN).build();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> expedienteService.registrarExpediente(request));
        assertTrue(ex.getMessage().contains("COMPLETADA"));
        verify(expedienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarExpediente: Rechazado si la cita está CANCELADA")
    void registrarExpediente_CitaCancelada_Rechazado() {
        authenticate(vetUser);
        citaPendiente.setEstado(EstadoCita.CANCELADA);

        when(citaMedicaRepository.findById(100L)).thenReturn(Optional.of(citaPendiente));

        ExpedienteRequest request = ExpedienteRequest.builder()
                .citaId(100L).diagnostico("X").tratamiento("Y").pesoKg(BigDecimal.TEN).build();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> expedienteService.registrarExpediente(request));
        assertTrue(ex.getMessage().contains("CANCELADA"));
    }

    @Test
    @DisplayName("registrarExpediente: Rechazado si ya existe un expediente para la cita")
    void registrarExpediente_ExpedienteYaExiste_Rechazado() {
        authenticate(vetUser);

        when(citaMedicaRepository.findById(100L)).thenReturn(Optional.of(citaPendiente));
        when(expedienteRepository.existsByCitaId(100L)).thenReturn(true); // ya existe

        ExpedienteRequest request = ExpedienteRequest.builder()
                .citaId(100L).diagnostico("X").tratamiento("Y").pesoKg(BigDecimal.TEN).build();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> expedienteService.registrarExpediente(request));
        assertTrue(ex.getMessage().contains("Ya existe"));
        verify(expedienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarExpediente: Rechazado si VET autenticado no es el veterinario asignado")
    void registrarExpediente_VetNoPropietario_Rechazado() {
        Usuario otroVet = Usuario.builder()
                .id(99L).nombre("Otro Vet").email("otro@vetki.com").rol(Rol.VET).build();
        authenticate(otroVet);

        when(citaMedicaRepository.findById(100L)).thenReturn(Optional.of(citaPendiente));
        when(expedienteRepository.existsByCitaId(100L)).thenReturn(false);

        ExpedienteRequest request = ExpedienteRequest.builder()
                .citaId(100L).diagnostico("X").tratamiento("Y").pesoKg(BigDecimal.TEN).build();

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> expedienteService.registrarExpediente(request));
        assertTrue(ex.getMessage().contains("no está asignado"));
        verify(expedienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("registrarExpediente: Cita no encontrada lanza ResourceNotFoundException")
    void registrarExpediente_CitaNoExiste_LanzaException() {
        authenticate(vetUser);
        when(citaMedicaRepository.findById(999L)).thenReturn(Optional.empty());

        ExpedienteRequest request = ExpedienteRequest.builder()
                .citaId(999L).diagnostico("X").tratamiento("Y").pesoKg(BigDecimal.TEN).build();

        assertThrows(ResourceNotFoundException.class,
                () -> expedienteService.registrarExpediente(request));
    }

    @Test
    @DisplayName("getExpedientesPorMascota: VET puede ver historial de cualquier mascota")
    void getExpedientesPorMascota_Vet_RetornaListado() {
        authenticate(vetUser);
        when(mascotaRepository.findById(10L)).thenReturn(Optional.of(mascota));

        ExpedienteClinico exp = ExpedienteClinico.builder()
                .id(1L).cita(citaPendiente).diagnostico("Dx1").tratamiento("Tx1")
                .pesoKg(new BigDecimal("10.0")).fechaRegistro(LocalDateTime.now()).build();

        when(expedienteRepository.findByCitaMascotaIdOrderByFechaRegistroDesc(10L))
                .thenReturn(List.of(exp));

        List<ExpedienteDTO> resultado = expedienteService.getExpedientesPorMascota(10L);

        assertEquals(1, resultado.size());
        assertEquals("Dx1", resultado.get(0).getDiagnostico());
    }

    @Test
    @DisplayName("getExpedientesPorMascota: CLIENTE no puede ver historial de mascota ajena")
    void getExpedientesPorMascota_ClienteAjeno_Rechazado() {
        authenticate(clienteUser);

        Usuario otroCliente = Usuario.builder()
                .id(77L).nombre("Otro").rol(Rol.CLIENTE).build();
        Mascota mascotaAjena = Mascota.builder()
                .id(88L).nombre("Luna").especie(Especie.GATO).cliente(otroCliente).build();

        when(mascotaRepository.findById(88L)).thenReturn(Optional.of(mascotaAjena));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> expedienteService.getExpedientesPorMascota(88L));
        assertTrue(ex.getMessage().contains("no le pertenecen"));
    }

    @Test
    @DisplayName("getExpedientesPorMascota: Mascota no encontrada lanza ResourceNotFoundException")
    void getExpedientesPorMascota_MascotaNoExiste_LanzaException() {
        authenticate(vetUser);
        when(mascotaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> expedienteService.getExpedientesPorMascota(999L));
    }
}
