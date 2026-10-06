package com.jcajchun_2023512.VetKI.service;

import com.jcajchun_2023512.VetKI.dto.mascota.MascotaDTO;
import com.jcajchun_2023512.VetKI.dto.mascota.MascotaRequest;
import com.jcajchun_2023512.VetKI.entity.Mascota;
import com.jcajchun_2023512.VetKI.entity.Usuario;
import com.jcajchun_2023512.VetKI.entity.enums.Especie;
import com.jcajchun_2023512.VetKI.entity.enums.Rol;
import com.jcajchun_2023512.VetKI.exception.BusinessRuleException;
import com.jcajchun_2023512.VetKI.exception.ResourceNotFoundException;
import com.jcajchun_2023512.VetKI.repository.MascotaRepository;
import com.jcajchun_2023512.VetKI.repository.UsuarioRepository;
import com.jcajchun_2023512.VetKI.service.impl.MascotaServiceImpl;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MascotaServiceImpl mascotaService;

    private Usuario clienteUser;
    private Usuario adminUser;

    @BeforeEach
    void setUp() {
        clienteUser = Usuario.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan@gmail.com")
                .rol(Rol.CLIENTE)
                .build();

        adminUser = Usuario.builder()
                .id(2L)
                .nombre("Administrador Principal")
                .email("admin@vetki.com")
                .rol(Rol.ADMIN)
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
    @DisplayName("getMisMascotas debe retornar las mascotas del cliente en sesión")
    void getMisMascotas_RetornaMascotasDelCliente() {
        authenticateUser(clienteUser);

        Mascota mascota1 = Mascota.builder()
                .id(10L)
                .nombre("Firulais")
                .especie(Especie.PERRO)
                .raza("Labrador")
                .edad(3)
                .cliente(clienteUser)
                .build();

        when(mascotaRepository.findByClienteIdOrderByIdAsc(1L)).thenReturn(List.of(mascota1));

        List<MascotaDTO> resultado = mascotaService.getMisMascotas();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Firulais", resultado.get(0).getNombre());
        assertEquals(Especie.PERRO, resultado.get(0).getEspecie());
        verify(mascotaRepository, times(1)).findByClienteIdOrderByIdAsc(1L);
    }

    @Test
    @DisplayName("registrarMascota por CLIENTE debe asociarla automáticamente al cliente autenticado")
    void registrarMascota_ComoCliente_AsociaAlUsuarioAutenticado() {
        authenticateUser(clienteUser);

        MascotaRequest request = MascotaRequest.builder()
                .nombre("Michi")
                .especie(Especie.GATO)
                .raza("Siamés")
                .edad(2)
                .build();

        Mascota mascotaGuardada = Mascota.builder()
                .id(11L)
                .nombre("Michi")
                .especie(Especie.GATO)
                .raza("Siamés")
                .edad(2)
                .cliente(clienteUser)
                .build();

        when(mascotaRepository.save(any(Mascota.class))).thenReturn(mascotaGuardada);

        MascotaDTO dto = mascotaService.registrarMascota(request);

        assertNotNull(dto);
        assertEquals(11L, dto.getId());
        assertEquals("Michi", dto.getNombre());
        assertEquals(clienteUser.getId(), dto.getCliente().getId());
        verify(mascotaRepository, times(1)).save(any(Mascota.class));
    }

    @Test
    @DisplayName("registrarMascota por ADMIN con clienteId debe asociarla al cliente especificado")
    void registrarMascota_ComoAdmin_AsociaAlClienteEspecificado() {
        authenticateUser(adminUser);

        MascotaRequest request = MascotaRequest.builder()
                .nombre("Piolín")
                .especie(Especie.AVE)
                .raza("Canario")
                .edad(1)
                .clienteId(1L)
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(clienteUser));

        Mascota mascotaGuardada = Mascota.builder()
                .id(12L)
                .nombre("Piolín")
                .especie(Especie.AVE)
                .raza("Canario")
                .edad(1)
                .cliente(clienteUser)
                .build();

        when(mascotaRepository.save(any(Mascota.class))).thenReturn(mascotaGuardada);

        MascotaDTO dto = mascotaService.registrarMascota(request);

        assertNotNull(dto);
        assertEquals(12L, dto.getId());
        assertEquals("Piolín", dto.getNombre());
        assertEquals(1L, dto.getCliente().getId());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(mascotaRepository, times(1)).save(any(Mascota.class));
    }

    @Test
    @DisplayName("getMascotaPorId debe lanzar ResourceNotFoundException si no existe")
    void getMascotaPorId_NoExiste_LanzaException() {
        authenticateUser(adminUser);
        when(mascotaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mascotaService.getMascotaPorId(999L));
    }

    @Test
    @DisplayName("getMascotaPorId debe lanzar BusinessRuleException si un CLIENTE intenta ver mascota ajena")
    void getMascotaPorId_ClienteAjeno_LanzaException() {
        authenticateUser(clienteUser);

        Usuario otroCliente = Usuario.builder().id(50L).nombre("Otro").rol(Rol.CLIENTE).build();
        Mascota mascotaAjena = Mascota.builder()
                .id(20L)
                .nombre("Toby")
                .especie(Especie.PERRO)
                .cliente(otroCliente)
                .build();

        when(mascotaRepository.findById(20L)).thenReturn(Optional.of(mascotaAjena));

        assertThrows(BusinessRuleException.class, () -> mascotaService.getMascotaPorId(20L));
    }
}
