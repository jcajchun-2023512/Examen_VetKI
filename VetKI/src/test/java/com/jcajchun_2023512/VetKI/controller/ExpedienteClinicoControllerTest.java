package com.jcajchun_2023512.VetKI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcajchun_2023512.VetKI.dto.expediente.ExpedienteDTO;
import com.jcajchun_2023512.VetKI.dto.expediente.ExpedienteRequest;
import com.jcajchun_2023512.VetKI.security.JwtAuthenticationEntryPoint;
import com.jcajchun_2023512.VetKI.security.JwtService;
import com.jcajchun_2023512.VetKI.service.ExpedienteClinicoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpedienteClinicoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpedienteClinicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpedienteClinicoService expedienteService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("POST /api/v1/expedientes registra expediente y retorna 201")
    @WithMockUser(roles = "VET")
    void registrarExpediente_Retorna201() throws Exception {
        ExpedienteRequest request = ExpedienteRequest.builder()
                .citaId(100L)
                .diagnostico("Infección bacteriana leve")
                .tratamiento("Amoxicilina 250mg - 7 días")
                .pesoKg(new BigDecimal("8.75"))
                .build();

        ExpedienteDTO response = ExpedienteDTO.builder()
                .id(1L)
                .citaId(100L)
                .mascotaNombre("Bobby")
                .mascotaEspecie("PERRO")
                .veterinarioNombre("Dr. Veterinario")
                .diagnostico("Infección bacteriana leve")
                .tratamiento("Amoxicilina 250mg - 7 días")
                .pesoKg(new BigDecimal("8.75"))
                .fechaRegistro(LocalDateTime.now())
                .build();

        when(expedienteService.registrarExpediente(any(ExpedienteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/expedientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.diagnostico").value("Infección bacteriana leve"))
                .andExpect(jsonPath("$.mascotaNombre").value("Bobby"))
                .andExpect(jsonPath("$.citaId").value(100L));
    }

    @Test
    @DisplayName("GET /api/v1/expedientes/mascota/{mascotaId} retorna historial de mascota")
    @WithMockUser(roles = "VET")
    void getExpedientesPorMascota_Retorna200() throws Exception {
        ExpedienteDTO dto = ExpedienteDTO.builder()
                .id(5L)
                .citaId(50L)
                .mascotaId(10L)
                .mascotaNombre("Max")
                .diagnostico("Parasitosis intestinal")
                .tratamiento("Desparasitación oral")
                .pesoKg(new BigDecimal("15.20"))
                .fechaRegistro(LocalDateTime.now().minusDays(10))
                .build();

        when(expedienteService.getExpedientesPorMascota(10L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/expedientes/mascota/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5L))
                .andExpect(jsonPath("$[0].mascotaNombre").value("Max"))
                .andExpect(jsonPath("$[0].diagnostico").value("Parasitosis intestinal"));
    }

    @Test
    @DisplayName("GET /api/v1/expedientes/{id} retorna expediente por ID")
    @WithMockUser(roles = "CLIENTE")
    void getExpedientePorId_Retorna200() throws Exception {
        ExpedienteDTO dto = ExpedienteDTO.builder()
                .id(7L)
                .citaId(70L)
                .mascotaNombre("Luna")
                .diagnostico("Otitis externa")
                .tratamiento("Gotas auditivas - 10 días")
                .pesoKg(new BigDecimal("4.30"))
                .fechaRegistro(LocalDateTime.now().minusDays(5))
                .build();

        when(expedienteService.getExpedientePorId(eq(7L))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/expedientes/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.diagnostico").value("Otitis externa"));
    }

    @Test
    @DisplayName("POST /api/v1/expedientes retorna 400 si request es inválido")
    @WithMockUser(roles = "VET")
    void registrarExpediente_RequestInvalido_Retorna400() throws Exception {
        // citaId null, diagnostico vacío = validación debe fallar
        String requestInvalido = """
                {
                  "diagnostico": "",
                  "tratamiento": "Tratamiento",
                  "pesoKg": 5.0
                }
                """;

        mockMvc.perform(post("/api/v1/expedientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestInvalido))
                .andExpect(status().isBadRequest());
    }
}
