package com.jcajchun_2023512.VetKI.service.impl;

import com.jcajchun_2023512.VetKI.dto.auth.AuthResponse;
import com.jcajchun_2023512.VetKI.dto.auth.LoginRequest;
import com.jcajchun_2023512.VetKI.dto.auth.RegisterRequest;
import com.jcajchun_2023512.VetKI.dto.user.UsuarioDTO;
import com.jcajchun_2023512.VetKI.entity.Usuario;
import com.jcajchun_2023512.VetKI.entity.enums.Rol;
import com.jcajchun_2023512.VetKI.exception.BadRequestException;
import com.jcajchun_2023512.VetKI.repository.UsuarioRepository;
import com.jcajchun_2023512.VetKI.security.JwtService;
import com.jcajchun_2023512.VetKI.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("El correo ya se encuentra registrado en el sistema: " + email);
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono() != null ? request.getTelefono().trim() : null)
                .rol(Rol.CLIENTE) // Rol predeterminado CLIENTE según requisitos técnicos
                .build();

        usuario = usuarioRepository.save(usuario);

        String jwtToken = jwtService.generateToken(usuario);

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .usuario(mapToDTO(usuario))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));

        String jwtToken = jwtService.generateToken(usuario);

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .usuario(mapToDTO(usuario))
                .build();
    }

    private UsuarioDTO mapToDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .rol(usuario.getRol())
                .build();
    }
}
