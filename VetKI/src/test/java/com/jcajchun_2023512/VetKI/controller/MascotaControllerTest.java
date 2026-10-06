package com.jcajchun_2023512.VetKI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcajchun_2023512.VetKI.dto.mascota.MascotaDTO;
import com.jcajchun_2023512.VetKI.dto.mascota.MascotaRequest;
import com.jcajchun_2023512.VetKI.dto.user.UsuarioDTO;
import com.jcajchun_2023512.VetKI.entity.enums.Especie;
import com.jcajchun_2023512.VetKI.entity.enums.Rol;
import com.jcajchun_2023512.VetKI.security.JwtAuthenticationEntryPoint;
import com.jcajchun_2023512.VetKI.security.JwtService;
import com.jcajchun_2023512.VetKI.service.MascotaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MascotaController.class)
@AutoConfigureMockMvc(addFilters = false) // Probamos lógica del controlador
class MascotaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MascotaService mascotaService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /api/v1/mascotas/mis-mascotas retorna 200 y listado")
    @WithMockUser(roles = "CLIENTE")
    void getMisMascotas_Retorna200() throws Exception {
        MascotaDTO dto = MascotaDTO.builder()
                .id(1L)
                .nombre("Lucas")
                .especie(Especie.PERRO)
                .edad(2)
                .cliente(UsuarioDTO.builder().id(10L).nombre("Juan").rol(Rol.CLIENTE).build())
                .build();

        when(mascotaService.getMisMascotas()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/mascotas/mis-mascotas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Lucas"))
                .andExpect(jsonPath("$[0].especie").value("PERRO"));
    }

    @Test
    @DisplayName("POST /api/v1/mascotas crea una mascota y retorna 201")
    @WithMockUser(roles = "CLIENTE")
    void registrarMascota_Retorna201() throws Exception {
        MascotaRequest request = MascotaRequest.builder()
                .nombre("Michi")
                .especie(Especie.GATO)
                .raza("Persa")
                .edad(1)
                .build();

        MascotaDTO response = MascotaDTO.builder()
                .id(5L)
                .nombre("Michi")
                .especie(Especie.GATO)
                .raza("Persa")
                .edad(1)
                .cliente(UsuarioDTO.builder().id(10L).nombre("Juan").rol(Rol.CLIENTE).build())
                .build();

        when(mascotaService.registrarMascota(any(MascotaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/mascotas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.nombre").value("Michi"));
    }

    @Test
    @DisplayName("GET /api/v1/mascotas/{id} retorna 200 con la mascota")
    @WithMockUser(roles = "VET")
    void getMascotaPorId_Retorna200() throws Exception {
        MascotaDTO response = MascotaDTO.builder()
                .id(9L)
                .nombre("Rocky")
                .especie(Especie.PERRO)
                .edad(5)
                .build();

        when(mascotaService.getMascotaPorId(9L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/mascotas/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9L))
                .andExpect(jsonPath("$.nombre").value("Rocky"));
    }
}
