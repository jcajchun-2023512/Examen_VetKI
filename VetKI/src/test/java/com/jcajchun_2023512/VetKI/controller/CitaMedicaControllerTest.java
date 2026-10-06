package com.jcajchun_2023512.VetKI.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcajchun_2023512.VetKI.dto.cita.CitaDTO;
import com.jcajchun_2023512.VetKI.dto.cita.CitaRequest;
import com.jcajchun_2023512.VetKI.dto.mascota.MascotaDTO;
import com.jcajchun_2023512.VetKI.dto.user.UsuarioDTO;
import com.jcajchun_2023512.VetKI.entity.enums.Especie;
import com.jcajchun_2023512.VetKI.entity.enums.EstadoCita;
import com.jcajchun_2023512.VetKI.entity.enums.Rol;
import com.jcajchun_2023512.VetKI.security.JwtAuthenticationEntryPoint;
import com.jcajchun_2023512.VetKI.security.JwtService;
import com.jcajchun_2023512.VetKI.service.CitaMedicaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CitaMedicaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CitaMedicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CitaMedicaService citaMedicaService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("POST /api/v1/citas agenda una cita y retorna 201")
    @WithMockUser(roles = "CLIENTE")
    void agendarCita_Retorna201() throws Exception {
        LocalDateTime fecha = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0);

        CitaRequest request = CitaRequest.builder()
                .mascotaId(1L)
                .veterinarioId(2L)
                .fechaHora(fecha)
                .motivo("Chequeo general")
                .build();

        CitaDTO dto = CitaDTO.builder()
                .id(15L)
                .mascota(MascotaDTO.builder().id(1L).nombre("Toby").especie(Especie.PERRO).build())
                .veterinario(UsuarioDTO.builder().id(2L).nombre("Vet Carlos").rol(Rol.VET).build())
                .fechaHora(fecha)
                .motivo("Chequeo general")
                .estado(EstadoCita.PENDIENTE)
                .build();

        when(citaMedicaService.agendarCita(any(CitaRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/citas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15L))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.motivo").value("Chequeo general"));
    }

    @Test
    @DisplayName("GET /api/v1/citas/agenda retorna 200 y listado de citas")
    @WithMockUser(roles = "VET")
    void getAgenda_Retorna200() throws Exception {
        CitaDTO dto = CitaDTO.builder()
                .id(20L)
                .estado(EstadoCita.PENDIENTE)
                .motivo("Vacunación")
                .build();

        when(citaMedicaService.getAgenda(any(), any(), any(), any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/citas/agenda"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(20L));
    }

    @Test
    @DisplayName("PATCH /api/v1/citas/{id}/cancelar cancela cita y retorna 200")
    @WithMockUser(roles = "CLIENTE")
    void cancelarCita_Retorna200() throws Exception {
        CitaDTO dto = CitaDTO.builder()
                .id(30L)
                .estado(EstadoCita.CANCELADA)
                .motivo("Revisión cancelada")
                .build();

        when(citaMedicaService.cancelarCita(eq(30L))).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/citas/30/cancelar")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(30L))
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }
}
